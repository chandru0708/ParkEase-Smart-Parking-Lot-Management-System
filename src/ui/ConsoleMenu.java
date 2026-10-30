package ui;

import common.*;
import module1_setup.Admin;
import module1_setup.Floor;
import module4_payment.*;
import service.ParkingLotService;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Scanner;

/**
 * Terminal User Interface presenting interactive menus, input validation, and clear feedback.
 * Demonstrates Separation of Concerns (UI separated from business logic).
 */
public class ConsoleMenu {

    private final ParkingLotService service;
    private final Scanner scanner;
    private final DateTimeFormatter timeFormatter;

    public ConsoleMenu(ParkingLotService service) {
        this.service = service;
        this.scanner = new Scanner(System.in);
        this.timeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    }

    public void start() {
        boolean running = true;
        while (running) {
            printMainMenu();
            String choice = prompt("Enter your choice: ");
            switch (choice.trim()) {
                case "1" -> handleAdminPanel();
                case "2" -> handleVehicleEntry();
                case "3" -> handleParkingSlotStatus();
                case "4" -> handleTicketManagement();
                case "5" -> handleVehicleExit();
                case "6" -> handleFeeCalculation();
                case "7" -> handlePayment();
                case "8" -> handleMonthlyPassManagement();
                case "9" -> handleParkingReports();
                case "10" -> handleMonitoringAndAlerts();
                case "11" -> handleViewAllParkedVehicles();
                case "12" -> handleViewPaymentHistory();
                case "0" -> {
                    System.out.println("\nThank you for using ParkEase Parking Lot Management System. Goodbye!");
                    running = false;
                }
                default -> printError("Invalid choice! Please select an option between 0 and 12.");
            }
        }
    }

    private void printMainMenu() {
        System.out.println("\n============================================");
        System.out.println("       PARKING LOT MANAGEMENT SYSTEM        ");
        System.out.println("============================================");
        System.out.println(" 1. ADMIN PANEL");
        System.out.println(" 2. VEHICLE ENTRY");
        System.out.println(" 3. PARKING SLOT STATUS");
        System.out.println(" 4. TICKET MANAGEMENT");
        System.out.println(" 5. VEHICLE EXIT");
        System.out.println(" 6. FEE CALCULATION");
        System.out.println(" 7. PAYMENT");
        System.out.println(" 8. MONTHLY PASS MANAGEMENT");
        System.out.println(" 9. PARKING REPORTS");
        System.out.println(" 10. MONITORING AND ALERTS");
        System.out.println(" 11. VIEW ALL PARKED VEHICLES");
        System.out.println(" 12. VIEW PAYMENT HISTORY");
        System.out.println(" 0. EXIT APPLICATION");
        System.out.println("============================================");
    }

    // ==========================================================
    // 1. ADMIN PANEL
    // ==========================================================
    private void handleAdminPanel() {
        Admin admin = authenticateAdmin();
        if (admin == null) {
            printError("Admin authentication failed. Returning to main menu.");
            return;
        }

        boolean inAdmin = true;
        while (inAdmin) {
            System.out.println("\n--------------------------------------------");
            System.out.println("                ADMIN PANEL                 ");
            System.out.println("--------------------------------------------");
            System.out.println(" 1. Create parking lot");
            System.out.println(" 2. Add floor");
            System.out.println(" 3. Add parking slots");
            System.out.println(" 4. Change slot type");
            System.out.println(" 5. Mark slot under maintenance");
            System.out.println(" 6. Reopen slot");
            System.out.println(" 7. Close floor");
            System.out.println(" 8. Reopen floor");
            System.out.println(" 9. Display parking configuration");
            System.out.println(" 0. Back");
            System.out.println("--------------------------------------------");

            String choice = prompt("Enter your choice: ");
            switch (choice.trim()) {
                case "1" -> {
                    String name = prompt("Enter Parking Lot Name: ");
                    String address = prompt("Enter Address: ");
                    try {
                        service.getSetupService().createParkingLot(admin, name, address);
                        printSuccess("Parking lot '" + name + "' created successfully!");
                    } catch (Exception e) {
                        printError("Failed to create parking lot: " + e.getMessage());
                    }
                }
                case "2" -> {
                    int floorNo = promptInt("Enter Floor Number (e.g. 0, 1, 2): ");
                    try {
                        service.adminAddFloor(admin, floorNo);
                        printSuccess("Floor " + floorNo + " added successfully!");
                    } catch (Exception e) {
                        printError("Failed to add floor: " + e.getMessage());
                    }
                }
                case "3" -> {
                    int floorNo = promptInt("Enter Floor Number: ");
                    int slotNo = promptInt("Enter Slot Number: ");
                    Enums.SlotType type = promptSlotType();
                    if (type != null) {
                        try {
                            service.adminAddSlot(admin, floorNo, slotNo, type);
                            printSuccess("Slot #" + slotNo + " (" + type + ") added to Floor " + floorNo + "!");
                        } catch (Exception e) {
                            printError("Failed to add slot: " + e.getMessage());
                        }
                    }
                }
                case "4" -> {
                    int floorNo = promptInt("Enter Floor Number: ");
                    int slotNo = promptInt("Enter Slot Number: ");
                    Enums.SlotType newType = promptSlotType();
                    if (newType != null) {
                        try {
                            service.adminChangeSlotType(admin, floorNo, slotNo, newType);
                            printSuccess("Slot #" + slotNo + " on Floor " + floorNo + " changed to " + newType + "!");
                        } catch (Exception e) {
                            printError("Failed to change slot type: " + e.getMessage());
                        }
                    }
                }
                case "5" -> {
                    int floorNo = promptInt("Enter Floor Number: ");
                    int slotNo = promptInt("Enter Slot Number: ");
                    try {
                        service.adminMarkSlotMaintenance(admin, floorNo, slotNo);
                        printSuccess("Slot #" + slotNo + " on Floor " + floorNo + " marked UNDER MAINTENANCE.");
                    } catch (Exception e) {
                        printError("Operation failed: " + e.getMessage());
                    }
                }
                case "6" -> {
                    int floorNo = promptInt("Enter Floor Number: ");
                    int slotNo = promptInt("Enter Slot Number: ");
                    try {
                        service.adminReopenSlot(admin, floorNo, slotNo);
                        printSuccess("Slot #" + slotNo + " on Floor " + floorNo + " is reopened and FREE.");
                    } catch (Exception e) {
                        printError("Operation failed: " + e.getMessage());
                    }
                }
                case "7" -> {
                    int floorNo = promptInt("Enter Floor Number to CLOSE: ");
                    try {
                        service.adminCloseFloor(admin, floorNo);
                        printSuccess("Floor " + floorNo + " is now CLOSED. No new entries permitted.");
                    } catch (Exception e) {
                        printError("Operation failed: " + e.getMessage());
                    }
                }
                case "8" -> {
                    int floorNo = promptInt("Enter Floor Number to REOPEN: ");
                    try {
                        service.adminReopenFloor(admin, floorNo);
                        printSuccess("Floor " + floorNo + " is now REOPENED for parking.");
                    } catch (Exception e) {
                        printError("Operation failed: " + e.getMessage());
                    }
                }
                case "9" -> System.out.println("\n" + service.getConfigurationSummary());
                case "0" -> inAdmin = false;
                default -> printError("Invalid option.");
            }
        }
    }

    private Admin authenticateAdmin() {
        System.out.println("\n[ADMIN LOGIN]");
        String username = prompt("Enter username (default: 'admin'): ");
        String password = prompt("Enter password (default: 'admin123'): ");
        Admin current = service.getDataStore().getActiveAdmin();
        if (current != null && current.getUsername().equalsIgnoreCase(username) && current.authenticate(password)) {
            printSuccess("Welcome, " + current.getName() + "!");
            return current;
        }
        return null;
    }

    // ==========================================================
    // 2. VEHICLE ENTRY
    // ==========================================================
    private void handleVehicleEntry() {
        System.out.println("\n--- VEHICLE ENTRY & ALLOCATION ---");
        String regNumber = prompt("Enter Vehicle Registration Number (e.g., KA01AB1234): ");
        if (regNumber.trim().isEmpty()) {
            printError("Registration number cannot be empty.");
            return;
        }

        System.out.println("Select Vehicle Type:");
        System.out.println("1. BIKE (Rs. 10/hr)");
        System.out.println("2. CAR  (Rs. 20/hr)");
        System.out.println("3. TRUCK (Rs. 40/hr)");
        String typeChoice = prompt("Enter type (1/2/3): ");
        Enums.VehicleType vType;
        switch (typeChoice.trim()) {
            case "1" -> vType = Enums.VehicleType.BIKE;
            case "2" -> vType = Enums.VehicleType.CAR;
            case "3" -> vType = Enums.VehicleType.TRUCK;
            default -> {
                printError("Invalid vehicle type selected.");
                return;
            }
        }

        String ownerName = prompt("Enter Owner Name: ");
        if (ownerName.trim().isEmpty()) ownerName = "Guest Owner";

        try {
            LocalDateTime entryTime = LocalDateTime.now();
            Ticket ticket = service.parkVehicle(regNumber, vType, ownerName, entryTime);
            printSuccess("Vehicle parked successfully!");
            System.out.println("====================================================");
            System.out.println("                 PARKEASE ENTRY TICKET              ");
            System.out.println("====================================================");
            System.out.printf(" Ticket ID      : %s\n", ticket.getTicketId());
            System.out.printf(" Vehicle Reg    : %s\n", ticket.getVehicleRegistrationNumber());
            System.out.printf(" Vehicle Type   : %s\n", ticket.getVehicleType());
            System.out.printf(" Allocated Slot : Floor %d, Slot %d (%s Slot)\n",
                    ticket.getFloorNumber(), ticket.getSlotNumber(), ticket.getAllocatedSlotType());
            System.out.printf(" Entry Time     : %s\n", ticket.getEntryTime().format(timeFormatter));
            System.out.println(" Rates          : First 15 mins free | 8-hr daily cap");
            System.out.println("====================================================");
        } catch (Exception e) {
            printError(e.getMessage());
        }
    }

    // ==========================================================
    // 3. PARKING SLOT STATUS
    // ==========================================================
    private void handleParkingSlotStatus() {
        System.out.println("\n" + service.getReportService().getFloorWiseSlotReport());
        System.out.println(service.getReportService().getSlotTypeWiseReport());
    }

    // ==========================================================
    // 4. TICKET MANAGEMENT
    // ==========================================================
    private void handleTicketManagement() {
        System.out.println("\n--- TICKET MANAGEMENT ---");
        System.out.println("1. Search Ticket by ID or Registration Number");
        System.out.println("2. Report Lost Ticket");
        System.out.println("3. View All Active Tickets");
        System.out.println("4. View Vehicles Parked > 24 Hours");
        System.out.println("0. Back");
        String opt = prompt("Enter choice: ");

        switch (opt.trim()) {
            case "1" -> {
                String id = prompt("Enter Ticket ID or Vehicle Registration Number: ");
                Ticket t = service.findTicket(id);
                if (t != null) {
                    displayTicketDetails(t);
                } else {
                    printError("No active ticket found for: " + id);
                }
            }
            case "2" -> {
                String reg = prompt("Enter Vehicle Registration Number for Lost Ticket: ");
                try {
                    Ticket lostT = service.reportLostTicket(reg);
                    printSuccess("Ticket reported as LOST! Rs. 500 flat penalty applied.");
                    displayTicketDetails(lostT);
                } catch (Exception e) {
                    printError(e.getMessage());
                }
            }
            case "3" -> {
                List<Ticket> active = service.getTicketService().getAllActiveTickets();
                if (active.isEmpty()) {
                    System.out.println("No active tickets currently.");
                } else {
                    System.out.println("\n--- ACTIVE TICKETS ---");
                    for (Ticket t : active) {
                        System.out.printf("ID: %-10s | Reg: %-12s | Type: %-5s | Slot: F%d-S%d | Status: %-6s | Entry: %s\n",
                                t.getTicketId(), t.getVehicleRegistrationNumber(), t.getVehicleType(),
                                t.getFloorNumber(), t.getSlotNumber(), t.getStatus(), t.getEntryTime().format(timeFormatter));
                    }
                }
            }
            case "4" -> {
                List<Ticket> overdue = service.getTicketService().getTicketsParkedOver24Hours(LocalDateTime.now());
                if (overdue.isEmpty()) {
                    printSuccess("No vehicles currently parked for over 24 hours.");
                } else {
                    System.out.println("\n[ALERT: VEHICLES PARKED OVER 24 HOURS]");
                    for (Ticket t : overdue) {
                        System.out.printf("Ticket: %s | Reg: %s | Duration: %d hours | Floor: %d, Slot: %d\n",
                                t.getTicketId(), t.getVehicleRegistrationNumber(),
                                t.getDurationMinutes(LocalDateTime.now()) / 60,
                                t.getFloorNumber(), t.getSlotNumber());
                    }
                }
            }
            case "0" -> {}
            default -> printError("Invalid choice.");
        }
    }

    private void displayTicketDetails(Ticket t) {
        System.out.println("====================================================");
        System.out.println("                   TICKET DETAILS                   ");
        System.out.println("====================================================");
        System.out.printf(" Ticket ID      : %s\n", t.getTicketId());
        System.out.printf(" Vehicle Reg    : %s\n", t.getVehicleRegistrationNumber());
        System.out.printf(" Vehicle Type   : %s\n", t.getVehicleType());
        System.out.printf(" Slot Location  : Floor %d, Slot %d (%s Slot)\n",
                t.getFloorNumber(), t.getSlotNumber(), t.getAllocatedSlotType());
        System.out.printf(" Status         : %s\n", t.getStatus());
        System.out.printf(" Entry Time     : %s\n", t.getEntryTime().format(timeFormatter));
        System.out.printf(" Payment Time   : %s\n", t.getPaymentTime() != null ? t.getPaymentTime().format(timeFormatter) : "Not Paid");
        System.out.printf(" Amount Paid    : Rs. %.2f\n", t.getPaidAmount());
        System.out.printf(" Penalty (Lost) : Rs. %.2f\n", t.getPenaltyAmount());
        System.out.printf(" Current Fee Due: Rs. %.2f\n", service.calculateFeeDue(t, LocalDateTime.now()));
        System.out.println("====================================================");
    }

    // ==========================================================
    // 5. VEHICLE EXIT
    // ==========================================================
    private void handleVehicleExit() {
        System.out.println("\n--- VEHICLE EXIT GATE ---");
        String id = prompt("Enter Ticket ID or Vehicle Registration Number: ");
        Ticket ticket = service.findTicket(id);
        if (ticket == null) {
            printError("No active ticket found for '" + id + "'.");
            return;
        }

        LocalDateTime now = LocalDateTime.now();
        try {
            boolean exited = service.processExit(ticket, now);
            if (exited) {
                printSuccess(String.format("Vehicle %s has successfully exited! Slot Floor %d, Slot %d is now FREE.",
                        ticket.getVehicleRegistrationNumber(), ticket.getFloorNumber(), ticket.getSlotNumber()));
            }
        } catch (IllegalStateException e) {
            printError(e.getMessage());
            double feeDue = service.calculateFeeDue(ticket, now);
            if (ticket.getStatus() != Enums.TicketStatus.PAID || feeDue > 0) {
                System.out.printf("Outstanding amount to settle: Rs. %.2f. Please proceed to Menu Option 7 (PAYMENT).\n", feeDue);
            }
        }
    }

    // ==========================================================
    // 6. FEE CALCULATION
    // ==========================================================
    private void handleFeeCalculation() {
        System.out.println("\n--- PARKING FEE ESTIMATOR / INQUIRY ---");
        String id = prompt("Enter Ticket ID or Vehicle Registration Number: ");
        Ticket ticket = service.findTicket(id);
        if (ticket == null) {
            printError("No active ticket found for '" + id + "'.");
            return;
        }

        LocalDateTime now = LocalDateTime.now();
        double fee = service.calculateFeeDue(ticket, now);
        long mins = ticket.getDurationMinutes(now);

        System.out.println("====================================================");
        System.out.println("               FEE CALCULATION BREAKDOWN            ");
        System.out.println("====================================================");
        System.out.printf(" Vehicle Reg    : %s (%s)\n", ticket.getVehicleRegistrationNumber(), ticket.getVehicleType());
        System.out.printf(" Parked At      : Floor %d, Slot %d (%s Slot)\n",
                ticket.getFloorNumber(), ticket.getSlotNumber(), ticket.getAllocatedSlotType());
        System.out.printf(" Duration       : %d minutes (%d hrs %d mins)\n", mins, mins / 60, mins % 60);
        System.out.printf(" Hourly Rate    : Rs. %.2f/hr\n", ticket.getVehicleType().getHourlyRate());
        System.out.printf(" Penalty        : Rs. %.2f\n", ticket.getPenaltyAmount());
        System.out.printf(" Total Fee Due  : Rs. %.2f\n", fee);
        if (mins <= 15) {
            System.out.println(" * Note: First 15 minutes are FREE! *");
        }
        System.out.println("====================================================");
    }

    // ==========================================================
    // 7. PAYMENT
    // ==========================================================
    private void handlePayment() {
        System.out.println("\n--- PROCESS PAYMENT ---");
        String id = prompt("Enter Ticket ID or Vehicle Registration Number: ");
        Ticket ticket = service.findTicket(id);
        if (ticket == null) {
            printError("No active ticket found for '" + id + "'.");
            return;
        }

        LocalDateTime now = LocalDateTime.now();
        double feeDue = service.calculateFeeDue(ticket, now);

        System.out.printf("Vehicle: %s | Total Amount Due: Rs. %.2f\n", ticket.getVehicleRegistrationNumber(), feeDue);

        if (feeDue == 0.0) {
            printSuccess("No parking fee due (Free window or active Monthly Pass). Ticket marked as PAID.");
            ticket.markPaid(now, 0.0);
            return;
        }

        System.out.println("\nSelect Payment Method:");
        System.out.println("1. CASH");
        System.out.println("2. UPI");
        System.out.println("3. CARD");
        String methodChoice = prompt("Enter method (1/2/3): ");

        PaymentMethod method;
        switch (methodChoice.trim()) {
            case "1" -> {
                double tendered = promptDouble("Enter Cash Tendered by customer (Rs.): ");
                if (tendered < feeDue) {
                    printError(String.format("Tendered amount (Rs. %.2f) is less than required fee (Rs. %.2f). Payment aborted.",
                            tendered, feeDue));
                    return;
                }
                method = new CashPayment(tendered);
            }
            case "2" -> {
                String upiId = prompt("Enter UPI ID (e.g., user@okhdfcbank): ");
                if (!upiId.contains("@")) {
                    printError("Invalid UPI ID.");
                    return;
                }
                method = new UPIPayment(upiId);
            }
            case "3" -> {
                String cardNum = prompt("Enter 16-digit Card Number: ");
                String holder = prompt("Enter Cardholder Name: ");
                try {
                    method = new CardPayment(cardNum, holder);
                } catch (Exception e) {
                    printError("Invalid Card details: " + e.getMessage());
                    return;
                }
            }
            default -> {
                printError("Invalid payment method.");
                return;
            }
        }

        // Academic Console Simulation: allow testing success or failure
        System.out.println("\n[Console Simulation] Select Transaction Outcome:");
        System.out.println("1. Simulate Payment SUCCESS");
        System.out.println("2. Simulate Payment FAILURE / DECLINE");
        String outcome = prompt("Select (1/2, default 1): ");
        boolean simulateSuccess = !"2".equals(outcome.trim());

        Receipt receipt = service.processPayment(ticket, method, simulateSuccess, now);

        System.out.println("\n" + receipt.formatReceipt());

        if (receipt.getPaymentStatus() == Enums.PaymentStatus.PAID) {
            printSuccess("Payment successful! You may now exit within 15 minutes (Menu Option 5).");
        } else {
            printError("Payment FAILED! Exit is blocked until payment succeeds.");
        }
    }

    // ==========================================================
    // 8. MONTHLY PASS MANAGEMENT
    // ==========================================================
    private void handleMonthlyPassManagement() {
        System.out.println("\n--- MONTHLY PASS MANAGEMENT ---");
        System.out.println("1. Purchase New Monthly Pass");
        System.out.println("2. Verify Pass Status for Vehicle");
        System.out.println("3. List All Monthly Passes");
        System.out.println("0. Back");
        String opt = prompt("Enter choice: ");

        switch (opt.trim()) {
            case "1" -> {
                String reg = prompt("Enter Vehicle Registration Number: ");
                Enums.VehicleType type = promptVehicleType();
                if (type == null) return;
                int months = promptInt("Enter Duration in Months (e.g. 1): ");
                try {
                    MonthlyPass pass = service.purchaseMonthlyPass(reg, type, LocalDate.now(), months);
                    printSuccess("Monthly Pass Issued Successfully!");
                    System.out.println("====================================================");
                    System.out.printf(" Pass ID         : %s\n", pass.getPassId());
                    System.out.printf(" Vehicle Reg     : %s\n", pass.getVehicleRegistrationNumber());
                    System.out.printf(" Vehicle Type    : %s\n", pass.getVehicleType());
                    System.out.printf(" Validity Period : %s to %s\n", pass.getStartDate(), pass.getEndDate());
                    System.out.printf(" Total Fee Paid  : Rs. %.2f\n", pass.getPassFee() * months);
                    System.out.println("====================================================");
                } catch (Exception e) {
                    printError(e.getMessage());
                }
            }
            case "2" -> {
                String reg = prompt("Enter Vehicle Registration Number: ");
                Enums.VehicleType type = promptVehicleType();
                if (type == null) return;
                MonthlyPass pass = service.getMonthlyPassService().findValidPass(reg, type, LocalDate.now());
                if (pass != null) {
                    printSuccess(String.format("ACTIVE PASS FOUND: %s (Valid until %s)", pass.getPassId(), pass.getEndDate()));
                } else {
                    printError("No valid active pass found for " + reg + " on today's date.");
                }
            }
            case "3" -> {
                List<MonthlyPass> passes = service.getMonthlyPassService().getAllPasses();
                System.out.println("\n========================================================================================");
                System.out.printf("%-10s | %-14s | %-8s | %-12s | %-12s | %-8s\n",
                        "Pass ID", "Registration", "Type", "Start Date", "End Date", "Status");
                System.out.println("----------------------------------------------------------------------------------------");
                LocalDate today = LocalDate.now();
                for (MonthlyPass p : passes) {
                    boolean active = p.isActiveOn(today);
                    System.out.printf("%-10s | %-14s | %-8s | %-12s | %-12s | %-8s\n",
                            p.getPassId(), p.getVehicleRegistrationNumber(), p.getVehicleType(),
                            p.getStartDate(), p.getEndDate(), active ? "ACTIVE" : "EXPIRED");
                }
                System.out.println("========================================================================================");
            }
            case "0" -> {}
            default -> printError("Invalid choice.");
        }
    }

    // ==========================================================
    // 9. PARKING REPORTS
    // ==========================================================
    private void handleParkingReports() {
        System.out.println("\n--- PARKING AUDIT & ANALYTICS REPORTS ---");
        System.out.println("1. Floor-wise Slot Occupancy Report");
        System.out.println("2. Slot-Type Occupancy Report");
        System.out.println("3. Daily Revenue Audit");
        System.out.println("0. Back");
        String opt = prompt("Enter choice: ");

        switch (opt.trim()) {
            case "1" -> System.out.println("\n" + service.getReportService().getFloorWiseSlotReport());
            case "2" -> System.out.println("\n" + service.getReportService().getSlotTypeWiseReport());
            case "3" -> {
                String dateStr = prompt("Enter Date (YYYY-MM-DD, press Enter for Today): ");
                LocalDate date = dateStr.trim().isEmpty() ? LocalDate.now() : LocalDate.parse(dateStr.trim());
                System.out.println("\n" + service.getReportService().getDailyRevenueReport(date));
            }
            case "0" -> {}
            default -> printError("Invalid choice.");
        }
    }

    // ==========================================================
    // 10. MONITORING AND ALERTS
    // ==========================================================
    private void handleMonitoringAndAlerts() {
        System.out.println("\n====================================================");
        System.out.println("              SYSTEM MONITORING & ALERTS            ");
        System.out.println("====================================================");

        List<String> occAlerts = service.getReportService().checkFloorOccupancyAlerts();
        if (occAlerts.isEmpty()) {
            System.out.println(" [Floor Capacity] All floor occupancies are within normal limits (<= 90%).");
        } else {
            for (String alert : occAlerts) {
                System.out.println(" ! " + alert);
            }
        }

        List<String> stayAlerts = service.getReportService().check24HourParkingAlerts(LocalDateTime.now());
        if (stayAlerts.isEmpty()) {
            System.out.println(" [Continuous Stays] No vehicles parked for more than 24 hours.");
        } else {
            for (String alert : stayAlerts) {
                System.out.println(" ! " + alert);
            }
        }
        System.out.println("====================================================");
    }

    // ==========================================================
    // 11. VIEW ALL PARKED VEHICLES
    // ==========================================================
    private void handleViewAllParkedVehicles() {
        System.out.println("\n" + service.getReportService().getCurrentlyParkedVehiclesReport());
    }

    // ==========================================================
    // 12. VIEW PAYMENT HISTORY
    // ==========================================================
    private void handleViewPaymentHistory() {
        List<Receipt> receipts = service.getDataStore().getReceipts();
        System.out.println("\n========================================================================================");
        System.out.println("                               PAYMENT TRANSACTION AUDIT LOG                            ");
        System.out.println("========================================================================================");
        System.out.printf("%-10s | %-10s | %-10s | %-14s | %-10s | %-6s | %-8s\n",
                "Receipt ID", "Payment ID", "Ticket ID", "Vehicle Reg", "Amount", "Method", "Status");
        System.out.println("----------------------------------------------------------------------------------------");
        if (receipts.isEmpty()) {
            System.out.println("                       No payment transactions recorded yet.                     ");
        } else {
            for (Receipt r : receipts) {
                System.out.printf("%-10s | %-10s | %-10s | %-14s | Rs.%-7.2f | %-6s | %-8s\n",
                        r.getReceiptId(), r.getPaymentId(), r.getTicketId(), r.getVehicleRegistrationNumber(),
                        r.getAmount(), r.getPaymentMethod(), r.getPaymentStatus());
            }
        }
        System.out.println("========================================================================================");
    }

    // ==========================================================
    // HELPER INPUT UTILITIES
    // ==========================================================

    private String prompt(String message) {
        System.out.print(message);
        return scanner.nextLine();
    }

    private int promptInt(String message) {
        while (true) {
            System.out.print(message);
            String input = scanner.nextLine();
            try {
                return Integer.parseInt(input.trim());
            } catch (NumberFormatException e) {
                printError("Invalid number format. Please enter an integer.");
            }
        }
    }

    private double promptDouble(String message) {
        while (true) {
            System.out.print(message);
            String input = scanner.nextLine();
            try {
                return Double.parseDouble(input.trim());
            } catch (NumberFormatException e) {
                printError("Invalid amount format. Please enter a valid number.");
            }
        }
    }

    private Enums.VehicleType promptVehicleType() {
        System.out.println("Select Vehicle Type:");
        System.out.println("1. BIKE");
        System.out.println("2. CAR");
        System.out.println("3. TRUCK");
        String c = prompt("Enter choice (1/2/3): ");
        return switch (c.trim()) {
            case "1" -> Enums.VehicleType.BIKE;
            case "2" -> Enums.VehicleType.CAR;
            case "3" -> Enums.VehicleType.TRUCK;
            default -> {
                printError("Invalid choice.");
                yield null;
            }
        };
    }

    private Enums.SlotType promptSlotType() {
        System.out.println("Select Slot Type:");
        System.out.println("1. BIKE");
        System.out.println("2. CAR");
        System.out.println("3. TRUCK");
        String c = prompt("Enter choice (1/2/3): ");
        return switch (c.trim()) {
            case "1" -> Enums.SlotType.BIKE;
            case "2" -> Enums.SlotType.CAR;
            case "3" -> Enums.SlotType.TRUCK;
            default -> {
                printError("Invalid choice.");
                yield null;
            }
        };
    }

    private void printSuccess(String message) {
        System.out.println("\n[SUCCESS] " + message);
    }

    private void printError(String message) {
        System.out.println("\n[ERROR] " + message);
    }
}
