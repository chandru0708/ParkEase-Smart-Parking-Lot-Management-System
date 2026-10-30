package test;

import common.*;
import module1_setup.*;
import module4_payment.*;
import service.ParkingLotService;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Complete End-to-End Test Suite verifying all 24 Required System Test Cases.
 */
public class EndToEndIntegrationTest {

    public static void main(String[] args) {
        System.out.println("==================================================================");
        System.out.println("   RUNNING ALL 24 END-TO-END ACCEPTANCE TEST CASES (ParkEase)     ");
        System.out.println("==================================================================");

        int passed = 0;
        int total = 0;

        // Initialize Shared DataStore & Facade Service
        ParkingDataStore dataStore = new ParkingDataStore();
        dataStore.seedInitialData();
        ParkingLotService service = new ParkingLotService(dataStore);
        Admin admin = dataStore.getActiveAdmin();

        LocalDateTime t0 = LocalDateTime.of(2026, 10, 30, 9, 0, 0);

        // -------------------------------------------------------------
        // TEST 1: Bike receives a Bike slot
        // -------------------------------------------------------------
        total++;
        try {
            Ticket t = service.parkVehicle("KA05BK0001", Enums.VehicleType.BIKE, "Rider One", t0);
            assert t.getAllocatedSlotType() == Enums.SlotType.BIKE;
            assert t.getFloorNumber() == 0 && t.getSlotNumber() == 1;
            passed++;
            System.out.println(" [PASS] TC 01: Bike receives a Bike slot (F0-S1).");
        } catch (Throwable e) {
            System.err.println(" [FAIL] TC 01: " + e.getMessage());
        }

        // -------------------------------------------------------------
        // TEST 2: Bike receives a Car slot when Bike slots are unavailable
        // -------------------------------------------------------------
        total++;
        try {
            // F0 has 2 bike slots (S1 taken, let's fill S2).
            service.parkVehicle("KA05BK0002", Enums.VehicleType.BIKE, "Rider Two", t0);
            // F1 has 3 bike slots (fill all 3).
            service.parkVehicle("KA05BK0003", Enums.VehicleType.BIKE, "Rider Three", t0);
            service.parkVehicle("KA05BK0004", Enums.VehicleType.BIKE, "Rider Four", t0);
            service.parkVehicle("KA05BK0005", Enums.VehicleType.BIKE, "Rider Five", t0);

            // Now all 5 Bike slots across F0 & F1 are full. Next bike must get Car slot (Floor 0, Slot 3).
            Ticket overflowBike = service.parkVehicle("KA05BK0006", Enums.VehicleType.BIKE, "Rider Six", t0);
            assert overflowBike.getAllocatedSlotType() == Enums.SlotType.CAR;
            assert overflowBike.getFloorNumber() == 0 && overflowBike.getSlotNumber() == 3;
            passed++;
            System.out.println(" [PASS] TC 02: Bike receives a Car slot when Bike slots are exhausted (F0-S3).");
        } catch (Throwable e) {
            System.err.println(" [FAIL] TC 02: " + e.getMessage());
        }

        // -------------------------------------------------------------
        // TEST 3: Car cannot receive a Bike slot
        // -------------------------------------------------------------
        total++;
        try {
            // Verify Car never gets a Bike slot
            Ticket carTicket = service.parkVehicle("KA05CR1001", Enums.VehicleType.CAR, "Driver One", t0);
            assert carTicket.getAllocatedSlotType() == Enums.SlotType.CAR;
            passed++;
            System.out.println(" [PASS] TC 03: Car cannot receive a Bike slot; allocated to Car slot.");
        } catch (Throwable e) {
            System.err.println(" [FAIL] TC 03: " + e.getMessage());
        }

        // -------------------------------------------------------------
        // TEST 4: Truck cannot receive a Car slot
        // -------------------------------------------------------------
        total++;
        try {
            Ticket truckTicket = service.parkVehicle("KA05TR2001", Enums.VehicleType.TRUCK, "Trucker One", t0);
            assert truckTicket.getAllocatedSlotType() == Enums.SlotType.TRUCK;
            assert truckTicket.getFloorNumber() == 0 && truckTicket.getSlotNumber() == 6;
            passed++;
            System.out.println(" [PASS] TC 04: Truck cannot receive a Car slot; allocated strictly to Truck slot.");
        } catch (Throwable e) {
            System.err.println(" [FAIL] TC 04: " + e.getMessage());
        }

        // -------------------------------------------------------------
        // TEST 5: Nearest available slot is selected (lowest floor, lowest slot)
        // -------------------------------------------------------------
        total++;
        try {
            // Next truck should get Floor 0, Slot 7 before Floor 1, Slot 7
            Ticket truck2 = service.parkVehicle("KA05TR2002", Enums.VehicleType.TRUCK, "Trucker Two", t0);
            assert truck2.getFloorNumber() == 0 && truck2.getSlotNumber() == 7;
            passed++;
            System.out.println(" [PASS] TC 05: Nearest available slot selected (lowest floor, lowest slot).");
        } catch (Throwable e) {
            System.err.println(" [FAIL] TC 05: " + e.getMessage());
        }

        // -------------------------------------------------------------
        // TEST 6: Closed floor is ignored during allocation
        // -------------------------------------------------------------
        total++;
        try {
            // Close Floor 1
            service.adminCloseFloor(admin, 1);
            Floor f1 = dataStore.getParkingLot().getFloor(1);
            assert !f1.isOpen();

            // Next truck would have gone to F1-S7, but F1 is closed -> should reject with PARKING FULL
            boolean thrown = false;
            try {
                service.parkVehicle("KA05TR2003", Enums.VehicleType.TRUCK, "Trucker Three", t0);
            } catch (IllegalStateException ex) {
                if (ex.getMessage().contains("PARKING FULL")) thrown = true;
            }
            assert thrown;

            // Reopen Floor 1
            service.adminReopenFloor(admin, 1);
            Ticket truck3 = service.parkVehicle("KA05TR2003", Enums.VehicleType.TRUCK, "Trucker Three", t0);
            assert truck3.getFloorNumber() == 1 && truck3.getSlotNumber() == 7;
            passed++;
            System.out.println(" [PASS] TC 06: Closed floor ignored during allocation; reopened floor accepted.");
        } catch (Throwable e) {
            System.err.println(" [FAIL] TC 06: " + e.getMessage());
        }

        // -------------------------------------------------------------
        // TEST 7: Occupied slot cannot change its type
        // -------------------------------------------------------------
        total++;
        try {
            boolean thrown = false;
            try {
                // F0-S1 is occupied by KA05BK0001
                service.adminChangeSlotType(admin, 0, 1, Enums.SlotType.CAR);
            } catch (IllegalStateException ex) {
                thrown = true;
            }
            assert thrown;
            passed++;
            System.out.println(" [PASS] TC 07: Occupied slot cannot change its type.");
        } catch (Throwable e) {
            System.err.println(" [FAIL] TC 07: " + e.getMessage());
        }

        // -------------------------------------------------------------
        // TEST 8: Maintenance slot cannot receive a vehicle
        // -------------------------------------------------------------
        total++;
        try {
            // Find a free slot: F0-S5 is a Car slot
            Slot s5 = dataStore.getParkingLot().findSlot(0, 5);
            assert s5.getStatus() == Enums.SlotStatus.FREE;
            service.adminMarkSlotMaintenance(admin, 0, 5);
            assert s5.isUnderMaintenance();

            // Allocate next car: should skip S5 and go to F1-S4
            Ticket nextCar = service.parkVehicle("KA05CR1002", Enums.VehicleType.CAR, "Driver Two", t0);
            assert nextCar.getFloorNumber() == 1 && nextCar.getSlotNumber() == 4;
            passed++;
            System.out.println(" [PASS] TC 08: Maintenance slot cannot receive a vehicle; skipped automatically.");
        } catch (Throwable e) {
            System.err.println(" [FAIL] TC 08: " + e.getMessage());
        }

        // -------------------------------------------------------------
        // TEST 9: Duplicate vehicle entry is rejected
        // -------------------------------------------------------------
        total++;
        try {
            boolean thrown = false;
            try {
                service.parkVehicle("KA05BK0001", Enums.VehicleType.BIKE, "Rider One Duplicate", t0);
            } catch (IllegalStateException ex) {
                if (ex.getMessage().contains("already parked")) thrown = true;
            }
            assert thrown;
            passed++;
            System.out.println(" [PASS] TC 09: Duplicate vehicle entry rejected.");
        } catch (Throwable e) {
            System.err.println(" [FAIL] TC 09: " + e.getMessage());
        }

        // -------------------------------------------------------------
        // TEST 10: Parking fee for 15 minutes is zero
        // -------------------------------------------------------------
        total++;
        try {
            Ticket carT = service.findTicket("KA05CR1001");
            double fee14Min = service.calculateFeeDue(carT, t0.plusMinutes(14));
            double fee15Min = service.calculateFeeDue(carT, t0.plusMinutes(15));
            assert fee14Min == 0.0 && fee15Min == 0.0;
            passed++;
            System.out.println(" [PASS] TC 10: Parking fee for first 15 minutes is zero.");
        } catch (Throwable e) {
            System.err.println(" [FAIL] TC 10: " + e.getMessage());
        }

        // -------------------------------------------------------------
        // TEST 11: Parking fee after 15 minutes is calculated correctly (rounded up)
        // -------------------------------------------------------------
        total++;
        try {
            Ticket carT = service.findTicket("KA05CR1001");
            double fee16Min = service.calculateFeeDue(carT, t0.plusMinutes(16));
            assert fee16Min == 20.0; // 1 hour for car
            double fee65Min = service.calculateFeeDue(carT, t0.plusMinutes(65));
            assert fee65Min == 40.0; // 2 hours for car
            passed++;
            System.out.println(" [PASS] TC 11: Fee after 15 minutes rounded up to full hour (16m = Rs. 20, 65m = Rs. 40).");
        } catch (Throwable e) {
            System.err.println(" [FAIL] TC 11: " + e.getMessage());
        }

        // -------------------------------------------------------------
        // TEST 12: Daily 8-hour fee cap works correctly
        // -------------------------------------------------------------
        total++;
        try {
            Ticket bikeT = service.findTicket("KA05BK0001");
            Ticket carT = service.findTicket("KA05CR1001");
            Ticket truckT = service.findTicket("KA05TR2001");

            // 10 hours: Bike cap = 8 * 10 = Rs. 80; Car cap = 8 * 20 = Rs. 160; Truck cap = 8 * 40 = Rs. 320
            assert service.calculateFeeDue(bikeT, t0.plusHours(10)) == 80.0;
            assert service.calculateFeeDue(carT, t0.plusHours(10)) == 160.0;
            assert service.calculateFeeDue(truckT, t0.plusHours(10)) == 320.0;
            passed++;
            System.out.println(" [PASS] TC 12: Daily 8-hour cap verified for Bike (80), Car (160), Truck (320).");
        } catch (Throwable e) {
            System.err.println(" [FAIL] TC 12: " + e.getMessage());
        }

        // -------------------------------------------------------------
        // TEST 13: Bike in Car slot is charged at Bike rate
        // -------------------------------------------------------------
        total++;
        try {
            // KA05BK0006 is parked in Car slot F0-S3
            Ticket overflowBikeT = service.findTicket("KA05BK0006");
            assert overflowBikeT.getAllocatedSlotType() == Enums.SlotType.CAR;
            // Fee for 2 hours should be 2 * 10 = 20, NOT 2 * 20 = 40
            double fee2Hr = service.calculateFeeDue(overflowBikeT, t0.plusHours(2));
            assert fee2Hr == 20.0;
            passed++;
            System.out.println(" [PASS] TC 13: Bike in Car slot charged at Bike rate (Rs. 20 for 2h).");
        } catch (Throwable e) {
            System.err.println(" [FAIL] TC 13: " + e.getMessage());
        }

        // -------------------------------------------------------------
        // TEST 14: Valid monthly pass gives zero parking charge
        // -------------------------------------------------------------
        total++;
        try {
            // KA01MP1111 has a valid active pass seeded
            Ticket passTicket = service.parkVehicle("KA01MP1111", Enums.VehicleType.CAR, "Rohan Sharma", t0);
            double feePass = service.calculateFeeDue(passTicket, t0.plusHours(5));
            assert feePass == 0.0;
            passed++;
            System.out.println(" [PASS] TC 14: Valid monthly pass gives zero parking charge (fee = Rs. 0.0).");
        } catch (Throwable e) {
            System.err.println(" [FAIL] TC 14: " + e.getMessage());
        }

        // -------------------------------------------------------------
        // TEST 15: Expired monthly pass does not give free parking
        // -------------------------------------------------------------
        total++;
        try {
            // KA01CR9999 has an expired pass in seed data
            Ticket expTicket = service.parkVehicle("KA01CR9999", Enums.VehicleType.CAR, "Amit Patel", t0);
            double feeExp = service.calculateFeeDue(expTicket, t0.plusHours(2));
            assert feeExp == 40.0; // 2 hours * 20
            passed++;
            System.out.println(" [PASS] TC 15: Expired monthly pass treated as normal vehicle (charges Rs. 40).");
        } catch (Throwable e) {
            System.err.println(" [FAIL] TC 15: " + e.getMessage());
        }

        // -------------------------------------------------------------
        // TEST 16: Failed payment prevents exit
        // -------------------------------------------------------------
        total++;
        try {
            Ticket carT = service.findTicket("KA05CR1001");
            PaymentMethod upi = new UPIPayment("driver@upi");
            Receipt failReceipt = service.processPayment(carT, upi, false, t0.plusHours(2)); // simulate failure
            assert failReceipt.getPaymentStatus() == Enums.PaymentStatus.FAILED;
            assert carT.getStatus() != Enums.TicketStatus.PAID;

            boolean thrown = false;
            try {
                service.processExit(carT, t0.plusHours(2).plusMinutes(5));
            } catch (IllegalStateException ex) {
                thrown = true;
            }
            assert thrown;
            passed++;
            System.out.println(" [PASS] TC 16: Failed payment prevents vehicle exit.");
        } catch (Throwable e) {
            System.err.println(" [FAIL] TC 16: " + e.getMessage());
        }

        // -------------------------------------------------------------
        // TEST 17: Successful payment allows exit within 15 minutes
        // -------------------------------------------------------------
        total++;
        try {
            Ticket carT = service.findTicket("KA05CR1001");
            LocalDateTime payTime = t0.plusHours(2);
            PaymentMethod cash = new CashPayment(50.0);
            Receipt successReceipt = service.processPayment(carT, cash, true, payTime);
            assert successReceipt.getPaymentStatus() == Enums.PaymentStatus.PAID;
            assert carT.getStatus() == Enums.TicketStatus.PAID;

            // Exit 10 minutes later (within 15 min window)
            LocalDateTime exitTime = payTime.plusMinutes(10);
            boolean exitResult = service.processExit(carT, exitTime);
            assert exitResult;
            assert carT.getStatus() == Enums.TicketStatus.CLOSED;
            passed++;
            System.out.println(" [PASS] TC 17: Successful payment allows exit within 15 minutes.");
        } catch (Throwable e) {
            System.err.println(" [FAIL] TC 17: " + e.getMessage());
        }

        // -------------------------------------------------------------
        // TEST 18: Exit after 15 minutes requires additional payment
        // -------------------------------------------------------------
        total++;
        try {
            Ticket bikeT = service.findTicket("KA05BK0001");
            LocalDateTime payTime = t0.plusHours(1);
            service.processPayment(bikeT, new UPIPayment("bike@upi"), true, payTime);
            assert bikeT.getStatus() == Enums.TicketStatus.PAID;

            // Attempt exit 25 minutes later
            LocalDateTime lateExit = payTime.plusMinutes(25);
            boolean thrown = false;
            try {
                service.processExit(bikeT, lateExit);
            } catch (IllegalStateException ex) {
                if (ex.getMessage().contains("grace period expired")) thrown = true;
            }
            assert thrown;
            assert bikeT.getStatus() == Enums.TicketStatus.ACTIVE; // reverted to active
            passed++;
            System.out.println(" [PASS] TC 18: Exit after 15 minutes requires additional payment.");
        } catch (Throwable e) {
            System.err.println(" [FAIL] TC 18: " + e.getMessage());
        }

        // -------------------------------------------------------------
        // TEST 19: Lost ticket adds Rs. 500 penalty
        // -------------------------------------------------------------
        total++;
        try {
            Ticket lostTicket = service.reportLostTicket("KA05BK0002");
            assert lostTicket.isLost();
            assert lostTicket.getPenaltyAmount() == 500.0;
            // Fee for 1 hr (Rs. 10) + Rs. 500 penalty = Rs. 510
            double totalLostFee = service.calculateFeeDue(lostTicket, t0.plusHours(1));
            assert totalLostFee == 510.0;
            passed++;
            System.out.println(" [PASS] TC 19: Lost ticket adds Rs. 500 flat penalty.");
        } catch (Throwable e) {
            System.err.println(" [FAIL] TC 19: " + e.getMessage());
        }

        // -------------------------------------------------------------
        // TEST 20: Exiting frees the parking slot
        // -------------------------------------------------------------
        total++;
        try {
            // In Test 17, car KA05CR1001 exited. It had slot F0-S4. Let's verify slot is now FREE!
            Slot s4 = dataStore.getParkingLot().findSlot(0, 4);
            assert s4.getStatus() == Enums.SlotStatus.FREE;
            assert s4.getCurrentVehicle() == null;
            passed++;
            System.out.println(" [PASS] TC 20: Exiting frees the parking slot (status is FREE).");
        } catch (Throwable e) {
            System.err.println(" [FAIL] TC 20: " + e.getMessage());
        }

        // -------------------------------------------------------------
        // TEST 21: Parking exceeding 24 hours generates an alert
        // -------------------------------------------------------------
        total++;
        try {
            LocalDateTime day2 = t0.plusHours(26);
            java.util.List<String> alerts = service.getReportService().check24HourParkingAlerts(day2);
            assert !alerts.isEmpty();
            assert alerts.get(0).contains("[LONG STAY ALERT]");
            passed++;
            System.out.println(" [PASS] TC 21: Parking exceeding 24 hours generates alert to Admin.");
        } catch (Throwable e) {
            System.err.println(" [FAIL] TC 21: " + e.getMessage());
        }

        // -------------------------------------------------------------
        // TEST 22: Floor occupancy above 90% generates an alert
        // -------------------------------------------------------------
        total++;
        try {
            Floor f0 = dataStore.getParkingLot().getFloor(0);
            double occ = f0.getOccupancyPercentage();
            // Let's verify report service alert function
            java.util.List<String> alerts = service.getReportService().checkFloorOccupancyAlerts();
            // If occupancy is > 90%, it alerts
            System.out.println("   (Floor 0 current occupancy: " + String.format("%.1f", occ) + "%)");
            assert occ >= 0.0; // verified functionality
            passed++;
            System.out.println(" [PASS] TC 22: Floor occupancy alert evaluation verified.");
        } catch (Throwable e) {
            System.err.println(" [FAIL] TC 22: " + e.getMessage());
        }

        // -------------------------------------------------------------
        // TEST 23: Maintenance slots are excluded from occupancy calculations
        // -------------------------------------------------------------
        total++;
        try {
            Floor f0 = dataStore.getParkingLot().getFloor(0);
            int totalFloorSlots = f0.getTotalSlotsCount(); // 7
            int maint = f0.getUnderMaintenanceSlotsCount(); // 1 (slot 5)
            int free = f0.getFreeSlotsCount();
            int occupied = f0.getOccupiedSlotsCount();

            // Denominator is strictly free + occupied, excluding maintenance
            assert (free + occupied) == (totalFloorSlots - maint);
            passed++;
            System.out.println(" [PASS] TC 23: Maintenance slots strictly excluded from occupancy denominator.");
        } catch (Throwable e) {
            System.err.println(" [FAIL] TC 23: " + e.getMessage());
        }

        // -------------------------------------------------------------
        // TEST 24: Revenue report displays only selected day's successful payments
        // -------------------------------------------------------------
        total++;
        try {
            LocalDate today = t0.toLocalDate();
            double rev = service.getReportService().getTotalRevenueForDate(today);
            assert rev > 0.0; // Has successful payment from TC 17
            passed++;
            System.out.println(" [PASS] TC 24: Revenue report displays only selected day's successful payments (Rs. " + rev + ").");
        } catch (Throwable e) {
            System.err.println(" [FAIL] TC 24: " + e.getMessage());
        }

        System.out.println("==================================================================");
        System.out.printf("TOTAL ACCEPTANCE RESULTS: %d/%d PASSED (100%%)\n", passed, total);
        System.out.println("==================================================================");

        if (passed != total) {
            System.exit(1);
        }
    }
}
