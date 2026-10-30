package test;

import common.*;
import module1_setup.*;
import module4_payment.*;
import module5_reports.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

/**
 * Independent Test Runner for Module 5: Reports and Monitoring.
 */
public class Module5Test {

    public static void main(String[] args) {
        System.out.println(">>> Running Module 5 Tests: Reports & Monitoring <<<");
        int passed = 0;
        int total = 0;

        Admin admin = new Admin("ADM-1", "admin", "admin123", "Super Admin");
        ParkingSetupService setupService = new ParkingSetupService(null);
        ParkingLot lot = setupService.createParkingLot(admin, "Test Lot", "City Center");

        // Create Floor 0 with 10 slots
        Floor f0 = setupService.addFloor(admin, 0);
        for (int i = 1; i <= 8; i++) {
            setupService.addSlot(admin, 0, i, Enums.SlotType.CAR);
        }
        setupService.addSlot(admin, 0, 9, Enums.SlotType.BIKE);
        setupService.addSlot(admin, 0, 10, Enums.SlotType.TRUCK);

        Map<String, Ticket> ticketStore = new HashMap<>();
        Map<String, Payment> paymentStore = new HashMap<>();
        ReportService reportService = new ReportService(lot, ticketStore, paymentStore);

        // Test 1: Occupancy calculation excludes Maintenance slots
        // Total = 10 slots. Mark slot 10 under maintenance.
        // Now valid slots = 9. Occupy 8 slots -> Occupancy = 8/9 = 88.89%
        total++;
        try {
            setupService.markSlotUnderMaintenance(admin, 0, 10);
            assert f0.getUnderMaintenanceSlotsCount() == 1;

            // Occupy 8 slots (slots 1 to 8)
            for (int i = 1; i <= 8; i++) {
                Car car = new Car("KA01C" + i, "Owner " + i);
                f0.getSlot(i).occupy(car);
            }

            double occ = f0.getOccupancyPercentage();
            // Free = 1 (slot 9), Occupied = 8 (slots 1-8). Total valid = 9.
            // 8 / 9 * 100 = 88.8888...
            assert Math.abs(occ - 88.888) < 0.1;
            passed++;
            System.out.println("  [PASS] Test 1: Maintenance slots excluded from occupancy denominator.");
        } catch (Throwable t) {
            System.err.println("  [FAIL] Test 1: " + t.getMessage());
        }

        // Test 2: Occupancy alert triggers when > 90%
        // Now occupy slot 9 as well. Free = 0, Occupied = 9. Occupancy = 9/9 * 100 = 100%.
        total++;
        try {
            Bike bike = new Bike("KA01B99", "Bike Owner");
            f0.getSlot(9).occupy(bike);
            double occ = f0.getOccupancyPercentage();
            assert occ == 100.0;

            List<String> alerts = reportService.checkFloorOccupancyAlerts();
            assert !alerts.isEmpty();
            assert alerts.get(0).contains("[ADMIN ALERT]");
            passed++;
            System.out.println("  [PASS] Test 2: Floor occupancy alert triggers when > 90%.");
        } catch (Throwable t) {
            System.err.println("  [FAIL] Test 2: " + t.getMessage());
        }

        // Test 3: Revenue report displays only selected day's successful payments
        total++;
        try {
            LocalDate today = LocalDate.of(2026, 10, 30);
            LocalDate yesterday = today.minusDays(1);

            Payment p1 = new Payment("P1", "T1", 100.0, today.atTime(10, 0), Enums.PaymentMethodType.UPI, Enums.PaymentStatus.PAID, "ok");
            Payment p2 = new Payment("P2", "T2", 50.0, today.atTime(11, 0), Enums.PaymentMethodType.CASH, Enums.PaymentStatus.PAID, "ok");
            Payment p3 = new Payment("P3", "T3", 200.0, today.atTime(12, 0), Enums.PaymentMethodType.CARD, Enums.PaymentStatus.FAILED, "failed");
            Payment p4 = new Payment("P4", "T4", 300.0, yesterday.atTime(9, 0), Enums.PaymentMethodType.UPI, Enums.PaymentStatus.PAID, "yesterday");

            paymentStore.put("P1", p1);
            paymentStore.put("P2", p2);
            paymentStore.put("P3", p3);
            paymentStore.put("P4", p4);

            double todayRev = reportService.getTotalRevenueForDate(today);
            assert todayRev == 150.0; // 100 + 50 (ignoring failed 200 and yesterday's 300)
            passed++;
            System.out.println("  [PASS] Test 3: Daily revenue isolates successful payments for specified date (Rs. 150.0).");
        } catch (Throwable t) {
            System.err.println("  [FAIL] Test 3: " + t.getMessage());
        }

        // Test 4: 24-hour stay alert
        total++;
        try {
            LocalDateTime entry = LocalDateTime.now().minusHours(26);
            Ticket longTicket = new Ticket("T-OLD", "KA01ZZ9999", Enums.VehicleType.CAR, 0, 1, Enums.SlotType.CAR, entry);
            ticketStore.put("T-OLD", longTicket);

            List<String> stayAlerts = reportService.check24HourParkingAlerts(LocalDateTime.now());
            assert !stayAlerts.isEmpty();
            assert stayAlerts.get(0).contains("[LONG STAY ALERT]");
            passed++;
            System.out.println("  [PASS] Test 4: Long-duration (24+ hr) parking alert verified.");
        } catch (Throwable t) {
            System.err.println("  [FAIL] Test 4: " + t.getMessage());
        }

        System.out.printf("Module 5 Results: %d/%d passed.\n\n", passed, total);
        if (passed != total) {
            System.exit(1);
        }
    }
}
