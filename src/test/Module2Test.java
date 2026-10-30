package test;

import common.*;
import module1_setup.*;
import module2_allocation.*;

/**
 * Independent Test Runner for Module 2: Vehicle Entry and Slot Allocation.
 */
public class Module2Test {

    public static void main(String[] args) {
        System.out.println(">>> Running Module 2 Tests: Vehicle Allocation <<<");
        int passed = 0;
        int total = 0;

        Admin admin = new Admin("ADM-1", "admin", "admin123", "Super Admin");
        ParkingSetupService setupService = new ParkingSetupService(null);
        ParkingLot lot = setupService.createParkingLot(admin, "Test Lot", "Test Address");

        // Floor 0: Bike (Slot 1), Car (Slot 2)
        setupService.addFloor(admin, 0);
        setupService.addSlot(admin, 0, 1, Enums.SlotType.BIKE);
        setupService.addSlot(admin, 0, 2, Enums.SlotType.CAR);

        // Floor 1: Bike (Slot 1), Truck (Slot 2)
        setupService.addFloor(admin, 1);
        setupService.addSlot(admin, 1, 1, Enums.SlotType.BIKE);
        setupService.addSlot(admin, 1, 2, Enums.SlotType.TRUCK);

        VehicleAllocationService allocService = new VehicleAllocationService(lot);

        // Test 1: Bike receives nearest Bike slot (Floor 0, Slot 1)
        total++;
        try {
            Bike bike1 = new Bike("KA01BK1111", "Alice");
            Slot slot = allocService.allocateSlot(bike1);
            assert slot.getFloorNumber() == 0 && slot.getSlotNumber() == 1 && slot.getSlotType() == Enums.SlotType.BIKE;
            assert slot.isOccupied();
            passed++;
            System.out.println("  [PASS] Test 1: Bike receives nearest Bike slot (F0-S1).");
        } catch (Throwable t) {
            System.err.println("  [FAIL] Test 1: " + t.getMessage());
        }

        // Test 2: Second Bike receives next nearest Bike slot (Floor 1, Slot 1)
        total++;
        try {
            Bike bike2 = new Bike("KA01BK2222", "Bob");
            Slot slot = allocService.allocateSlot(bike2);
            assert slot.getFloorNumber() == 1 && slot.getSlotNumber() == 1 && slot.getSlotType() == Enums.SlotType.BIKE;
            passed++;
            System.out.println("  [PASS] Test 2: Second Bike receives next Bike slot (F1-S1).");
        } catch (Throwable t) {
            System.err.println("  [FAIL] Test 2: " + t.getMessage());
        }

        // Test 3: Bike receives Car slot when all Bike slots are full (Floor 0, Slot 2)
        total++;
        try {
            Bike bike3 = new Bike("KA01BK3333", "Charlie");
            Slot slot = allocService.allocateSlot(bike3);
            assert slot.getFloorNumber() == 0 && slot.getSlotNumber() == 2 && slot.getSlotType() == Enums.SlotType.CAR;
            passed++;
            System.out.println("  [PASS] Test 3: Bike receives Car slot when Bike slots are exhausted.");
        } catch (Throwable t) {
            System.err.println("  [FAIL] Test 3: " + t.getMessage());
        }

        // Test 4: Car cannot receive Bike slot or full Car slot -> Rejected with PARKING FULL
        total++;
        try {
            Car car1 = new Car("KA01CR5555", "David");
            boolean thrown = false;
            try {
                allocService.allocateSlot(car1);
            } catch (IllegalStateException e) {
                if (e.getMessage().contains("PARKING FULL")) {
                    thrown = true;
                }
            }
            assert thrown;
            passed++;
            System.out.println("  [PASS] Test 4: Car cannot take Bike slot; rejects with PARKING FULL.");
        } catch (Throwable t) {
            System.err.println("  [FAIL] Test 4: " + t.getMessage());
        }

        // Test 5: Truck receives Truck slot (Floor 1, Slot 2)
        total++;
        try {
            Truck truck1 = new Truck("KA01TR7777", "Eve Logistics");
            Slot slot = allocService.allocateSlot(truck1);
            assert slot.getFloorNumber() == 1 && slot.getSlotNumber() == 2 && slot.getSlotType() == Enums.SlotType.TRUCK;
            passed++;
            System.out.println("  [PASS] Test 5: Truck receives Truck slot (F1-S2).");
        } catch (Throwable t) {
            System.err.println("  [FAIL] Test 5: " + t.getMessage());
        }

        // Test 6: Duplicate vehicle entry rejected
        total++;
        try {
            boolean thrown = false;
            try {
                Bike duplicateBike = new Bike("KA01BK1111", "Alice");
                allocService.allocateSlot(duplicateBike);
            } catch (IllegalStateException e) {
                if (e.getMessage().contains("already parked")) {
                    thrown = true;
                }
            }
            assert thrown;
            passed++;
            System.out.println("  [PASS] Test 6: Duplicate vehicle entry rejected.");
        } catch (Throwable t) {
            System.err.println("  [FAIL] Test 6: " + t.getMessage());
        }

        // Test 7: Closed floor is ignored during allocation
        total++;
        try {
            // Setup fresh floor 2 with a free Car slot
            setupService.addFloor(admin, 2);
            setupService.addSlot(admin, 2, 1, Enums.SlotType.CAR);
            setupService.closeFloor(admin, 2); // Close floor 2

            Car car2 = new Car("KA01CR8888", "Frank");
            boolean thrown = false;
            try {
                allocService.allocateSlot(car2);
            } catch (IllegalStateException e) {
                if (e.getMessage().contains("PARKING FULL")) {
                    thrown = true;
                }
            }
            assert thrown;

            // Now reopen floor 2 -> Allocation should succeed!
            setupService.reopenFloor(admin, 2);
            Slot slot = allocService.allocateSlot(car2);
            assert slot.getFloorNumber() == 2 && slot.getSlotNumber() == 1;
            passed++;
            System.out.println("  [PASS] Test 7: Closed floor ignored, reopened floor accepted.");
        } catch (Throwable t) {
            System.err.println("  [FAIL] Test 7: " + t.getMessage());
        }

        System.out.printf("Module 2 Results: %d/%d passed.\n\n", passed, total);
        if (passed != total) {
            System.exit(1);
        }
    }
}
