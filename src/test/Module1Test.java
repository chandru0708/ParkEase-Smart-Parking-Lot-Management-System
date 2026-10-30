package test;

import common.*;
import module1_setup.*;

/**
 * Independent Test Runner for Module 1: Parking Lot Setup.
 */
public class Module1Test {

    public static void main(String[] args) {
        System.out.println(">>> Running Module 1 Tests: Parking Lot Setup <<<");
        int passed = 0;
        int total = 0;

        Admin admin = new Admin("ADM-1", "admin", "admin123", "Super Admin");
        ParkingSetupService setupService = new ParkingSetupService(null);

        // Test 1: Create Parking Lot
        total++;
        try {
            ParkingLot lot = setupService.createParkingLot(admin, "ParkEase Hub", "MG Road, Bengaluru");
            assert lot != null && "ParkEase Hub".equals(lot.getName());
            passed++;
            System.out.println("  [PASS] Test 1: Create parking lot successfully.");
        } catch (Throwable t) {
            System.err.println("  [FAIL] Test 1: " + t.getMessage());
        }

        // Test 2: Add Floors and Slots
        total++;
        try {
            Floor f0 = setupService.addFloor(admin, 0);
            Floor f1 = setupService.addFloor(admin, 1);
            setupService.addSlot(admin, 0, 1, Enums.SlotType.BIKE);
            setupService.addSlot(admin, 0, 2, Enums.SlotType.CAR);
            setupService.addSlot(admin, 0, 3, Enums.SlotType.TRUCK);
            assert f0.getTotalSlotsCount() == 3;
            assert f1.getTotalSlotsCount() == 0;
            passed++;
            System.out.println("  [PASS] Test 2: Add floors and slots.");
        } catch (Throwable t) {
            System.err.println("  [FAIL] Test 2: " + t.getMessage());
        }

        // Test 3: Prevent duplicate floor numbers
        total++;
        try {
            boolean thrown = false;
            try {
                setupService.addFloor(admin, 0); // Duplicate floor 0
            } catch (IllegalArgumentException e) {
                thrown = true;
            }
            assert thrown;
            passed++;
            System.out.println("  [PASS] Test 3: Prevent duplicate floor numbers.");
        } catch (Throwable t) {
            System.err.println("  [FAIL] Test 3: " + t.getMessage());
        }

        // Test 4: Prevent duplicate slot numbers on the same floor
        total++;
        try {
            boolean thrown = false;
            try {
                setupService.addSlot(admin, 0, 1, Enums.SlotType.CAR); // Slot 1 already exists on floor 0
            } catch (IllegalArgumentException e) {
                thrown = true;
            }
            assert thrown;
            passed++;
            System.out.println("  [PASS] Test 4: Prevent duplicate slot numbers on the same floor.");
        } catch (Throwable t) {
            System.err.println("  [FAIL] Test 4: " + t.getMessage());
        }

        // Test 5: Slot type cannot be changed while OCCUPIED
        total++;
        try {
            Slot carSlot = setupService.getParkingLot().findSlot(0, 2);
            Car sampleCar = new Car("KA01AB1234", "John Doe");
            carSlot.occupy(sampleCar);
            boolean thrown = false;
            try {
                setupService.changeSlotType(admin, 0, 2, Enums.SlotType.TRUCK);
            } catch (IllegalStateException e) {
                thrown = true;
            }
            assert thrown;
            // Vacate for subsequent tests
            carSlot.vacate();
            passed++;
            System.out.println("  [PASS] Test 5: Occupied slot cannot change type.");
        } catch (Throwable t) {
            System.err.println("  [FAIL] Test 5: " + t.getMessage());
        }

        // Test 6: Only FREE slot can be marked UNDER_MAINTENANCE
        total++;
        try {
            Slot truckSlot = setupService.getParkingLot().findSlot(0, 3);
            Truck sampleTruck = new Truck("KA01TR9999", "Heavy Transport");
            truckSlot.occupy(sampleTruck);
            boolean thrown = false;
            try {
                setupService.markSlotUnderMaintenance(admin, 0, 3);
            } catch (IllegalStateException e) {
                thrown = true;
            }
            assert thrown;
            truckSlot.vacate(); // Vacate
            setupService.markSlotUnderMaintenance(admin, 0, 3);
            assert truckSlot.getStatus() == Enums.SlotStatus.UNDER_MAINTENANCE;
            setupService.reopenSlot(admin, 0, 3);
            assert truckSlot.getStatus() == Enums.SlotStatus.FREE;
            passed++;
            System.out.println("  [PASS] Test 6: Maintenance transition rules enforced.");
        } catch (Throwable t) {
            System.err.println("  [FAIL] Test 6: " + t.getMessage());
        }

        // Test 7: Floor close and reopen
        total++;
        try {
            Floor f0 = setupService.getParkingLot().getFloor(0);
            assert f0.isOpen();
            setupService.closeFloor(admin, 0);
            assert !f0.isOpen();
            setupService.reopenFloor(admin, 0);
            assert f0.isOpen();
            passed++;
            System.out.println("  [PASS] Test 7: Floor close and reopen works.");
        } catch (Throwable t) {
            System.err.println("  [FAIL] Test 7: " + t.getMessage());
        }

        // Test 8: Unauthorized admin rejected
        total++;
        try {
            boolean thrown = false;
            try {
                setupService.addFloor(null, 99);
            } catch (SecurityException e) {
                thrown = true;
            }
            assert thrown;
            passed++;
            System.out.println("  [PASS] Test 8: Unauthorized admin access rejected.");
        } catch (Throwable t) {
            System.err.println("  [FAIL] Test 8: " + t.getMessage());
        }

        System.out.printf("Module 1 Results: %d/%d passed.\n\n", passed, total);
        if (passed != total) {
            System.exit(1);
        }
    }
}
