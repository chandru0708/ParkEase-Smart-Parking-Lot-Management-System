package test;

import common.*;
import module1_setup.*;
import module2_allocation.*;
import module3_ticket.*;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Independent Test Runner for Module 3: Ticket and Exit Management.
 */
public class Module3Test {

    public static void main(String[] args) {
        System.out.println(">>> Running Module 3 Tests: Ticket & Exit Management <<<");
        int passed = 0;
        int total = 0;

        Admin admin = new Admin("ADM-1", "admin", "admin123", "Super Admin");
        ParkingSetupService setupService = new ParkingSetupService(null);
        ParkingLot lot = setupService.createParkingLot(admin, "Test Lot", "Test Address");
        setupService.addFloor(admin, 0);
        setupService.addSlot(admin, 0, 1, Enums.SlotType.CAR);
        setupService.addSlot(admin, 0, 2, Enums.SlotType.BIKE);

        VehicleAllocationService allocService = new VehicleAllocationService(lot);
        TicketService ticketService = new TicketService(lot, allocService);

        LocalDateTime now = LocalDateTime.of(2026, 10, 30, 10, 0, 0);

        // Test 1: Ticket generation upon entry
        total++;
        Car car1 = new Car("KA01AA1111", "Alice");
        Slot slot1 = allocService.allocateSlot(car1);
        Ticket t1 = null;
        try {
            t1 = ticketService.generateTicket(car1, slot1, now);
            assert t1 != null && t1.getTicketId().startsWith("TKT-");
            assert t1.getStatus() == Enums.TicketStatus.ACTIVE;
            assert slot1.isOccupied();
            passed++;
            System.out.println("  [PASS] Test 1: Unique ticket generated with entry timestamp.");
        } catch (Throwable t) {
            System.err.println("  [FAIL] Test 1: " + t.getMessage());
        }

        // Test 2: Prevent duplicate active ticket
        total++;
        try {
            boolean thrown = false;
            try {
                ticketService.generateTicket(car1, slot1, now);
            } catch (IllegalStateException e) {
                thrown = true;
            }
            assert thrown;
            passed++;
            System.out.println("  [PASS] Test 2: Duplicate active ticket rejected.");
        } catch (Throwable t) {
            System.err.println("  [FAIL] Test 2: " + t.getMessage());
        }

        // Test 3: Unpaid ticket cannot exit
        total++;
        try {
            boolean canExit = ticketService.canExit(t1, now.plusMinutes(10));
            assert !canExit;
            boolean thrown = false;
            try {
                ticketService.processExit(t1, now.plusMinutes(10));
            } catch (IllegalStateException e) {
                thrown = true;
            }
            assert thrown;
            passed++;
            System.out.println("  [PASS] Test 3: Unpaid ticket cannot exit.");
        } catch (Throwable t) {
            System.err.println("  [FAIL] Test 3: " + t.getMessage());
        }

        // Test 4: Paid ticket exits within 15 minutes; frees slot & closes ticket
        total++;
        try {
            LocalDateTime payTime = now.plusMinutes(45);
            t1.markPaid(payTime, 40.0);
            LocalDateTime exitTime = payTime.plusMinutes(10); // within 15 mins
            assert ticketService.canExit(t1, exitTime);
            boolean exited = ticketService.processExit(t1, exitTime);
            assert exited;
            assert t1.getStatus() == Enums.TicketStatus.CLOSED;
            assert slot1.getStatus() == Enums.SlotStatus.FREE;
            assert !allocService.isVehicleParked(car1.getRegistrationNumber());
            passed++;
            System.out.println("  [PASS] Test 4: Paid ticket exits within 15 mins, frees slot and closes ticket.");
        } catch (Throwable t) {
            System.err.println("  [FAIL] Test 4: " + t.getMessage());
        }

        // Test 5: Exit after 15 minutes of payment is rejected
        total++;
        try {
            Bike bike1 = new Bike("KA01BK9999", "Bob");
            Slot slot2 = allocService.allocateSlot(bike1);
            Ticket t2 = ticketService.generateTicket(bike1, slot2, now);
            LocalDateTime payTime = now.plusHours(2);
            t2.markPaid(payTime, 20.0);

            // Attempt exit 20 minutes after payment (> 15 mins)
            LocalDateTime lateExit = payTime.plusMinutes(20);
            assert !ticketService.canExit(t2, lateExit);
            assert t2.getStatus() == Enums.TicketStatus.ACTIVE; // reverted to active
            boolean thrown = false;
            try {
                ticketService.processExit(t2, lateExit);
            } catch (IllegalStateException e) {
                thrown = true;
            }
            assert thrown;
            passed++;
            System.out.println("  [PASS] Test 5: Exit after 15 mins requires re-payment and rejects exit.");
        } catch (Throwable t) {
            System.err.println("  [FAIL] Test 5: " + t.getMessage());
        }

        // Test 6: Lost ticket handling adds Rs. 500 penalty
        total++;
        try {
            Ticket lostTicket = ticketService.reportLostTicket("KA01BK9999");
            assert lostTicket.isLost();
            assert lostTicket.getPenaltyAmount() == 500.0;
            assert lostTicket.getStatus() == Enums.TicketStatus.LOST;
            passed++;
            System.out.println("  [PASS] Test 6: Lost ticket records Rs. 500 penalty and LOST status.");
        } catch (Throwable t) {
            System.err.println("  [FAIL] Test 6: " + t.getMessage());
        }

        // Test 7: Over 24 hours alert
        total++;
        try {
            LocalDateTime dayLater = now.plusHours(25);
            List<Ticket> overdue = ticketService.getTicketsParkedOver24Hours(dayLater);
            assert !overdue.isEmpty();
            assert overdue.get(0).getVehicleRegistrationNumber().equals("KA01BK9999");
            passed++;
            System.out.println("  [PASS] Test 7: 24+ hour parked vehicle correctly flagged.");
        } catch (Throwable t) {
            System.err.println("  [FAIL] Test 7: " + t.getMessage());
        }

        System.out.printf("Module 3 Results: %d/%d passed.\n\n", passed, total);
        if (passed != total) {
            System.exit(1);
        }
    }
}
