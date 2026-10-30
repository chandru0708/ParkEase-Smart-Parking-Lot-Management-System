package service;

import common.*;
import module1_setup.*;
import module2_allocation.*;
import module3_ticket.*;
import module4_payment.*;
import module5_reports.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * High-Level Unified Facade Service orchestrating all 5 modules using the shared ParkingDataStore.
 * Demonstrates Facade Pattern, Dependency Inversion, and High Cohesion / Low Coupling.
 */
public class ParkingLotService {

    private final ParkingDataStore dataStore;
    private final ParkingSetupService setupService;
    private final VehicleAllocationService allocationService;
    private final TicketService ticketService;
    private final FeeCalculator feeCalculator;
    private final MonthlyPassService monthlyPassService;
    private final ReportService reportService;
    private final AtomicInteger paymentCounter;
    private final AtomicInteger receiptCounter;

    public ParkingLotService(ParkingDataStore dataStore) {
        if (dataStore == null) {
            throw new IllegalArgumentException("DataStore cannot be null.");
        }
        this.dataStore = dataStore;
        this.setupService = new ParkingSetupService(dataStore.getParkingLot());
        this.allocationService = new VehicleAllocationService(dataStore.getParkingLot());
        this.ticketService = new TicketService(dataStore.getTickets(), dataStore.getParkingLot(), allocationService);
        this.feeCalculator = new StandardFeeCalculator();
        this.monthlyPassService = new MonthlyPassService(dataStore.getMonthlyPasses());
        this.reportService = new ReportService(dataStore.getParkingLot(), dataStore.getTickets(), dataStore.getPayments());
        this.paymentCounter = new AtomicInteger(7000);
        this.receiptCounter = new AtomicInteger(9000);
    }

    public ParkingDataStore getDataStore() {
        return dataStore;
    }

    public ParkingSetupService getSetupService() {
        return setupService;
    }

    public VehicleAllocationService getAllocationService() {
        return allocationService;
    }

    public TicketService getTicketService() {
        return ticketService;
    }

    public MonthlyPassService getMonthlyPassService() {
        return monthlyPassService;
    }

    public ReportService getReportService() {
        return reportService;
    }

    public FeeCalculator getFeeCalculator() {
        return feeCalculator;
    }

    // ==========================================
    // VEHICLE ENTRY & ALLOCATION
    // ==========================================

    /**
     * Registers and parks a vehicle, creating an active ticket.
     */
    public Ticket parkVehicle(String registrationNumber, Enums.VehicleType vehicleType, String ownerName, LocalDateTime entryTime) {
        if (entryTime == null) entryTime = LocalDateTime.now();

        // 1. Instantiate concrete Vehicle subclass polymorphically
        Vehicle vehicle;
        switch (vehicleType) {
            case BIKE -> vehicle = new Bike(registrationNumber, ownerName);
            case CAR -> vehicle = new Car(registrationNumber, ownerName);
            case TRUCK -> vehicle = new Truck(registrationNumber, ownerName);
            default -> throw new IllegalArgumentException("Unsupported vehicle type: " + vehicleType);
        }

        // 2. Persist vehicle to registry
        dataStore.getVehicles().put(vehicle.getRegistrationNumber(), vehicle);

        // 3. Allocate nearest available slot
        Slot allocatedSlot = allocationService.allocateSlot(vehicle);

        // 4. Generate parking ticket
        Ticket ticket = ticketService.generateTicket(vehicle, allocatedSlot, entryTime);
        return ticket;
    }

    // ==========================================
    // TICKET & FEE MANAGEMENT
    // ==========================================

    /**
     * Resolves a ticket by Ticket ID or Vehicle Registration Number.
     */
    public Ticket findTicket(String identifier) {
        if (identifier == null || identifier.trim().isEmpty()) return null;
        String clean = identifier.trim().toUpperCase();
        Ticket t = ticketService.getTicketById(clean);
        if (t != null) return t;
        return ticketService.findActiveTicketByRegistration(clean);
    }

    /**
     * Calculates fee due for a ticket, incorporating Monthly Pass rules.
     */
    public double calculateFeeDue(Ticket ticket, LocalDateTime calculationTime) {
        if (ticket == null) {
            throw new IllegalArgumentException("Ticket cannot be null.");
        }
        if (calculationTime == null) calculationTime = LocalDateTime.now();

        // Check if vehicle has an active monthly pass on this date
        LocalDate calcDate = calculationTime.toLocalDate();
        boolean hasPass = monthlyPassService.hasValidPass(ticket.getVehicleRegistrationNumber(), ticket.getVehicleType(), calcDate);
        if (hasPass) {
            // Pass holder is not charged parking fee. Only penalty if ticket lost.
            if (ticket.isLost()) {
                double net = ticket.getPenaltyAmount() - ticket.getPaidAmount();
                return Math.max(0.0, net);
            }
            return 0.0;
        }

        return feeCalculator.calculateFee(ticket, calculationTime);
    }

    /**
     * Marks a ticket as lost, adding Rs. 500 penalty.
     */
    public Ticket reportLostTicket(String registrationNumber) {
        return ticketService.reportLostTicket(registrationNumber);
    }

    // ==========================================
    // PAYMENT & RECEIPT PROCESSING
    // ==========================================

    /**
     * Processes payment for a parking ticket using a polymorphic PaymentMethod.
     */
    public Receipt processPayment(Ticket ticket, PaymentMethod paymentMethod, boolean simulateSuccess, LocalDateTime paymentTime) {
        if (ticket == null) {
            throw new IllegalArgumentException("Ticket cannot be null.");
        }
        if (paymentMethod == null) {
            throw new IllegalArgumentException("Payment method cannot be null.");
        }
        if (paymentTime == null) paymentTime = LocalDateTime.now();

        double feeDue = calculateFeeDue(ticket, paymentTime);

        boolean success = paymentMethod.processPayment(feeDue, simulateSuccess);
        Enums.PaymentStatus status = success ? Enums.PaymentStatus.PAID : Enums.PaymentStatus.FAILED;

        String paymentId = "PAY-" + paymentCounter.incrementAndGet();
        Payment payment = new Payment(paymentId, ticket.getTicketId(), feeDue, paymentTime,
                paymentMethod.getMethodType(), status, paymentMethod.getDetails());

        dataStore.getPayments().put(paymentId, payment);

        if (success) {
            ticket.markPaid(paymentTime, feeDue);
        }

        String receiptId = "RCP-" + receiptCounter.incrementAndGet();
        Receipt receipt = new Receipt(receiptId, paymentId, ticket.getTicketId(),
                ticket.getVehicleRegistrationNumber(), feeDue, paymentMethod.getMethodType(), paymentTime, status);

        dataStore.getReceipts().add(receipt);
        return receipt;
    }

    /**
     * Processes vehicle exit. Enforces 15-minute grace period and slot release.
     */
    public boolean processExit(Ticket ticket, LocalDateTime exitTime) {
        return ticketService.processExit(ticket, exitTime);
    }

    // ==========================================
    // MONTHLY PASS MANAGEMENT
    // ==========================================

    public MonthlyPass purchaseMonthlyPass(String registrationNumber, Enums.VehicleType vehicleType, LocalDate startDate, int months) {
        return monthlyPassService.issuePass(registrationNumber, vehicleType, startDate, months);
    }

    // ==========================================
    // ADMINISTRATIVE CONFIGURATION
    // ==========================================

    public void adminChangeSlotType(Admin admin, int floorNumber, int slotNumber, Enums.SlotType newType) {
        setupService.changeSlotType(admin, floorNumber, slotNumber, newType);
    }

    public void adminMarkSlotMaintenance(Admin admin, int floorNumber, int slotNumber) {
        setupService.markSlotUnderMaintenance(admin, floorNumber, slotNumber);
    }

    public void adminReopenSlot(Admin admin, int floorNumber, int slotNumber) {
        setupService.reopenSlot(admin, floorNumber, slotNumber);
    }

    public void adminCloseFloor(Admin admin, int floorNumber) {
        setupService.closeFloor(admin, floorNumber);
    }

    public void adminReopenFloor(Admin admin, int floorNumber) {
        setupService.reopenFloor(admin, floorNumber);
    }

    public Floor adminAddFloor(Admin admin, int floorNumber) {
        return setupService.addFloor(admin, floorNumber);
    }

    public Slot adminAddSlot(Admin admin, int floorNumber, int slotNumber, Enums.SlotType slotType) {
        return setupService.addSlot(admin, floorNumber, slotNumber, slotType);
    }

    public String getConfigurationSummary() {
        return setupService.getConfigurationSummary();
    }
}
