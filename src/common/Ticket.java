package common;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Represents a parking ticket issued to a vehicle upon entry.
 * Demonstrates Encapsulation and Domain State Logic.
 */
public class Ticket {
    private final String ticketId;
    private final String vehicleRegistrationNumber;
    private final Enums.VehicleType vehicleType;
    private final int floorNumber;
    private final int slotNumber;
    private final Enums.SlotType allocatedSlotType;
    private final LocalDateTime entryTime;

    private LocalDateTime paymentTime;
    private LocalDateTime exitTime;
    private Enums.TicketStatus status;
    private double paidAmount;
    private double penaltyAmount;
    private boolean isLost;

    public Ticket(String ticketId, String vehicleRegistrationNumber, Enums.VehicleType vehicleType,
                  int floorNumber, int slotNumber, Enums.SlotType allocatedSlotType, LocalDateTime entryTime) {
        if (ticketId == null || ticketId.trim().isEmpty()) {
            throw new IllegalArgumentException("Ticket ID cannot be null or empty.");
        }
        if (vehicleRegistrationNumber == null || vehicleRegistrationNumber.trim().isEmpty()) {
            throw new IllegalArgumentException("Vehicle registration number cannot be null or empty.");
        }
        if (vehicleType == null) {
            throw new IllegalArgumentException("Vehicle type cannot be null.");
        }
        if (entryTime == null) {
            throw new IllegalArgumentException("Entry time cannot be null.");
        }
        this.ticketId = ticketId.trim();
        this.vehicleRegistrationNumber = vehicleRegistrationNumber.trim().toUpperCase();
        this.vehicleType = vehicleType;
        this.floorNumber = floorNumber;
        this.slotNumber = slotNumber;
        this.allocatedSlotType = allocatedSlotType;
        this.entryTime = entryTime;
        this.status = Enums.TicketStatus.ACTIVE;
        this.paidAmount = 0.0;
        this.penaltyAmount = 0.0;
        this.isLost = false;
    }

    public String getTicketId() {
        return ticketId;
    }

    public String getVehicleRegistrationNumber() {
        return vehicleRegistrationNumber;
    }

    public Enums.VehicleType getVehicleType() {
        return vehicleType;
    }

    public int getFloorNumber() {
        return floorNumber;
    }

    public int getSlotNumber() {
        return slotNumber;
    }

    public Enums.SlotType getAllocatedSlotType() {
        return allocatedSlotType;
    }

    public LocalDateTime getEntryTime() {
        return entryTime;
    }

    public LocalDateTime getPaymentTime() {
        return paymentTime;
    }

    public LocalDateTime getExitTime() {
        return exitTime;
    }

    public Enums.TicketStatus getStatus() {
        return status;
    }

    public double getPaidAmount() {
        return paidAmount;
    }

    public double getPenaltyAmount() {
        return penaltyAmount;
    }

    public boolean isLost() {
        return isLost;
    }

    /**
     * Marks the ticket as lost, adds Rs. 500 penalty, and updates status to LOST if not already paid/closed.
     */
    public void markLost() {
        this.isLost = true;
        this.penaltyAmount = 500.0;
        if (this.status == Enums.TicketStatus.ACTIVE) {
            this.status = Enums.TicketStatus.LOST;
        }
    }

    /**
     * Records payment details and updates ticket status to PAID.
     */
    public void markPaid(LocalDateTime paymentTime, double amount) {
        if (paymentTime == null) {
            throw new IllegalArgumentException("Payment time cannot be null.");
        }
        this.paymentTime = paymentTime;
        this.paidAmount += amount;
        this.status = Enums.TicketStatus.PAID;
    }

    /**
     * Marks ticket as CLOSED upon exit.
     */
    public void markClosed(LocalDateTime exitTime) {
        if (exitTime == null) {
            throw new IllegalArgumentException("Exit time cannot be null.");
        }
        this.exitTime = exitTime;
        this.status = Enums.TicketStatus.CLOSED;
    }

    /**
     * Reverts status back to ACTIVE if 15-minute payment window has expired and more fees are due.
     */
    public void revertToActive() {
        this.status = Enums.TicketStatus.ACTIVE;
    }

    /**
     * Checks if exit is permitted within the 15-minute grace period after payment.
     */
    public boolean isWithinPaymentGracePeriod(LocalDateTime currentTime) {
        if (status != Enums.TicketStatus.PAID || paymentTime == null) {
            return false;
        }
        Duration diff = Duration.between(paymentTime, currentTime);
        return !diff.isNegative() && diff.toMinutes() <= 15;
    }

    /**
     * Checks whether vehicle has been parked for more than 24 hours continuously.
     */
    public boolean isParkedOver24Hours(LocalDateTime currentTime) {
        if (status == Enums.TicketStatus.CLOSED) {
            return false;
        }
        Duration duration = Duration.between(entryTime, currentTime);
        return duration.toHours() >= 24;
    }

    /**
     * Calculates duration in minutes from entry to given time.
     */
    public long getDurationMinutes(LocalDateTime toTime) {
        if (toTime == null) return 0;
        Duration duration = Duration.between(entryTime, toTime);
        return Math.max(0, duration.toMinutes());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Ticket ticket)) return false;
        return Objects.equals(ticketId, ticket.ticketId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(ticketId);
    }

    @Override
    public String toString() {
        return String.format("Ticket[ID=%s | Reg=%s | Type=%s | Slot=F%d-S%d (%s) | Status=%s | Entry=%s | Paid=%s]",
                ticketId, vehicleRegistrationNumber, vehicleType, floorNumber, slotNumber, allocatedSlotType,
                status, entryTime, paymentTime != null ? paymentTime.toString() : "N/A");
    }
}
