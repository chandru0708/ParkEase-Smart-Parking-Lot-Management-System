package module3_ticket;

import common.*;
import module1_setup.ParkingLot;
import module2_allocation.VehicleAllocationService;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Service managing parking ticket generation, lost ticket workflows, and exit approvals.
 * Demonstrates Single Responsibility and State Management.
 */
public class TicketService {

    private final Map<String, Ticket> ticketStore;
    private final ParkingLot parkingLot;
    private final VehicleAllocationService allocationService;
    private final AtomicInteger ticketSequence;

    public TicketService(ParkingLot parkingLot, VehicleAllocationService allocationService) {
        this.ticketStore = new LinkedHashMap<>();
        this.parkingLot = parkingLot;
        this.allocationService = allocationService;
        this.ticketSequence = new AtomicInteger(1000);
    }

    public TicketService(Map<String, Ticket> ticketStore, ParkingLot parkingLot, VehicleAllocationService allocationService) {
        this.ticketStore = ticketStore;
        this.parkingLot = parkingLot;
        this.allocationService = allocationService;
        this.ticketSequence = new AtomicInteger(1000 + ticketStore.size());
    }

    public Map<String, Ticket> getTicketStore() {
        return ticketStore;
    }

    /**
     * Generates a new parking ticket for an allocated vehicle and slot.
     */
    public Ticket generateTicket(Vehicle vehicle, Slot slot, LocalDateTime entryTime) {
        if (vehicle == null) throw new IllegalArgumentException("Vehicle cannot be null.");
        if (slot == null) throw new IllegalArgumentException("Slot cannot be null.");
        if (entryTime == null) entryTime = LocalDateTime.now();

        String regNumber = vehicle.getRegistrationNumber();
        Ticket existingActive = findActiveTicketByRegistration(regNumber);
        if (existingActive != null) {
            throw new IllegalStateException("Active ticket already exists for vehicle: " + regNumber + " (ID: " + existingActive.getTicketId() + ")");
        }

        String ticketId = "TKT-" + ticketSequence.incrementAndGet();
        Ticket ticket = new Ticket(ticketId, regNumber, vehicle.getVehicleType(),
                slot.getFloorNumber(), slot.getSlotNumber(), slot.getSlotType(), entryTime);

        ticketStore.put(ticketId, ticket);
        return ticket;
    }

    /**
     * Looks up ticket by Ticket ID.
     */
    public Ticket getTicketById(String ticketId) {
        if (ticketId == null) return null;
        return ticketStore.get(ticketId.trim().toUpperCase());
    }

    /**
     * Finds active or paid ticket for a vehicle registration number.
     */
    public Ticket findActiveTicketByRegistration(String regNumber) {
        if (regNumber == null) return null;
        String normalized = regNumber.trim().toUpperCase();
        for (Ticket t : ticketStore.values()) {
            if (t.getVehicleRegistrationNumber().equalsIgnoreCase(normalized) &&
                    (t.getStatus() == Enums.TicketStatus.ACTIVE ||
                     t.getStatus() == Enums.TicketStatus.PAID ||
                     t.getStatus() == Enums.TicketStatus.LOST)) {
                return t;
            }
        }
        return null;
    }

    /**
     * Returns list of all currently active or paid tickets.
     */
    public List<Ticket> getAllActiveTickets() {
        List<Ticket> activeTickets = new ArrayList<>();
        for (Ticket t : ticketStore.values()) {
            if (t.getStatus() == Enums.TicketStatus.ACTIVE ||
                t.getStatus() == Enums.TicketStatus.PAID ||
                t.getStatus() == Enums.TicketStatus.LOST) {
                activeTickets.add(t);
            }
        }
        return activeTickets;
    }

    /**
     * Identifies all vehicles parked continuously for more than 24 hours.
     */
    public List<Ticket> getTicketsParkedOver24Hours(LocalDateTime currentTime) {
        if (currentTime == null) currentTime = LocalDateTime.now();
        List<Ticket> overdue = new ArrayList<>();
        for (Ticket t : ticketStore.values()) {
            if (t.isParkedOver24Hours(currentTime)) {
                overdue.add(t);
            }
        }
        return overdue;
    }

    /**
     * Handles lost ticket workflow by applying Rs. 500 flat penalty.
     */
    public Ticket reportLostTicket(String registrationNumber) {
        Ticket ticket = findActiveTicketByRegistration(registrationNumber);
        if (ticket == null) {
            throw new IllegalArgumentException("No active parking record found for registration: " + registrationNumber);
        }
        ticket.markLost();
        return ticket;
    }

    /**
     * Validates whether a vehicle is permitted to exit.
     * Enforces the 15-minute grace period after payment.
     */
    public boolean canExit(Ticket ticket, LocalDateTime exitTime) {
        if (ticket == null) {
            throw new IllegalArgumentException("Ticket cannot be null.");
        }
        if (exitTime == null) {
            exitTime = LocalDateTime.now();
        }

        if (ticket.getStatus() == Enums.TicketStatus.CLOSED) {
            throw new IllegalStateException("Ticket " + ticket.getTicketId() + " is already closed.");
        }

        if (ticket.getStatus() != Enums.TicketStatus.PAID) {
            return false;
        }

        // Check 15-minute window
        if (!ticket.isWithinPaymentGracePeriod(exitTime)) {
            // Revert back to active so additional payment can be settled
            ticket.revertToActive();
            return false;
        }

        return true;
    }

    /**
     * Finalizes vehicle exit: releases slot, updates ticket to CLOSED, and records exit time.
     */
    public boolean processExit(Ticket ticket, LocalDateTime exitTime) {
        if (ticket == null) {
            throw new IllegalArgumentException("Ticket cannot be null.");
        }
        if (exitTime == null) {
            exitTime = LocalDateTime.now();
        }

        if (!canExit(ticket, exitTime)) {
            long minutesSincePayment = ticket.getPaymentTime() != null ?
                    Duration.between(ticket.getPaymentTime(), exitTime).toMinutes() : -1;
            if (minutesSincePayment > 15) {
                throw new IllegalStateException(String.format(
                        "Exit rejected: 15-minute post-payment grace period expired (%d minutes elapsed). Additional fee payment required.",
                        minutesSincePayment));
            } else {
                throw new IllegalStateException("Exit rejected: Ticket has not been PAID.");
            }
        }

        // Release slot
        Slot slot = parkingLot.findSlot(ticket.getFloorNumber(), ticket.getSlotNumber());
        if (slot != null && slot.isOccupied()) {
            slot.vacate();
        }

        // Update allocation service tracking
        if (allocationService != null) {
            allocationService.markVehicleExited(ticket.getVehicleRegistrationNumber());
        }

        // Close ticket
        ticket.markClosed(exitTime);
        return true;
    }
}
