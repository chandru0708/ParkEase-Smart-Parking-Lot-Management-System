package module1_setup;

import common.*;

/**
 * Service handling Parking Lot Setup and Administrative Configuration.
 * Demonstrates Single Responsibility Principle and Authorization Enforcement.
 */
public class ParkingSetupService {

    private ParkingLot parkingLot;

    public ParkingSetupService(ParkingLot parkingLot) {
        this.parkingLot = parkingLot;
    }

    public ParkingLot getParkingLot() {
        return parkingLot;
    }

    public void setParkingLot(ParkingLot parkingLot) {
        this.parkingLot = parkingLot;
    }

    /**
     * Validates that the executing caller is an authorized Admin.
     */
    private void validateAdmin(Admin admin) {
        if (admin == null) {
            throw new SecurityException("Unauthorized: Only an authenticated Admin can perform setup operations.");
        }
    }

    /**
     * Creates a new parking lot configuration.
     */
    public ParkingLot createParkingLot(Admin admin, String name, String address) {
        validateAdmin(admin);
        this.parkingLot = new ParkingLot(name, address);
        return this.parkingLot;
    }

    /**
     * Adds a new floor to the parking lot.
     */
    public Floor addFloor(Admin admin, int floorNumber) {
        validateAdmin(admin);
        if (parkingLot == null) {
            throw new IllegalStateException("Parking lot has not been initialized yet.");
        }
        Floor floor = new Floor(floorNumber);
        parkingLot.addFloor(floor);
        return floor;
    }

    /**
     * Adds a slot to a specified floor.
     */
    public Slot addSlot(Admin admin, int floorNumber, int slotNumber, Enums.SlotType slotType) {
        validateAdmin(admin);
        if (parkingLot == null) {
            throw new IllegalStateException("Parking lot has not been initialized yet.");
        }
        Floor floor = parkingLot.getFloor(floorNumber);
        if (floor == null) {
            throw new IllegalArgumentException("Floor " + floorNumber + " does not exist.");
        }
        if (slotType == null) {
            throw new IllegalArgumentException("Slot type cannot be null.");
        }

        Slot slot;
        switch (slotType) {
            case BIKE -> slot = new BikeSlot(floorNumber, slotNumber);
            case CAR -> slot = new CarSlot(floorNumber, slotNumber);
            case TRUCK -> slot = new TruckSlot(floorNumber, slotNumber);
            default -> throw new IllegalArgumentException("Unsupported slot type: " + slotType);
        }

        floor.addSlot(slot);
        return slot;
    }

    /**
     * Changes slot type if slot is not currently occupied.
     */
    public void changeSlotType(Admin admin, int floorNumber, int slotNumber, Enums.SlotType newType) {
        validateAdmin(admin);
        Slot slot = getSlotOrThrow(floorNumber, slotNumber);
        slot.changeSlotType(newType);
    }

    /**
     * Marks a slot under maintenance. Must be currently FREE.
     */
    public void markSlotUnderMaintenance(Admin admin, int floorNumber, int slotNumber) {
        validateAdmin(admin);
        Slot slot = getSlotOrThrow(floorNumber, slotNumber);
        slot.markUnderMaintenance();
    }

    /**
     * Reopens a slot from maintenance back to FREE.
     */
    public void reopenSlot(Admin admin, int floorNumber, int slotNumber) {
        validateAdmin(admin);
        Slot slot = getSlotOrThrow(floorNumber, slotNumber);
        slot.reopenFromMaintenance();
    }

    /**
     * Closes an entire floor.
     */
    public void closeFloor(Admin admin, int floorNumber) {
        validateAdmin(admin);
        Floor floor = getFloorOrThrow(floorNumber);
        floor.closeFloor();
    }

    /**
     * Reopens a closed floor.
     */
    public void reopenFloor(Admin admin, int floorNumber) {
        validateAdmin(admin);
        Floor floor = getFloorOrThrow(floorNumber);
        floor.reopenFloor();
    }

    /**
     * Returns a formatted text view of the parking lot configuration.
     */
    public String getConfigurationSummary() {
        if (parkingLot == null) {
            return "No Parking Lot configured yet.";
        }
        StringBuilder sb = new StringBuilder();
        sb.append("====================================================================\n");
        sb.append(String.format("PARKING LOT: %s | ADDRESS: %s\n", parkingLot.getName(), parkingLot.getAddress()));
        sb.append(String.format("Total Floors: %d | Total Slots: %d\n", parkingLot.getFloorCount(), parkingLot.getTotalSlotsCount()));
        sb.append("====================================================================\n");
        for (Floor floor : parkingLot.getAllFloors()) {
            sb.append(String.format("--- Floor %d [%s] (Slots: %d, Occupancy: %.1f%%) ---\n",
                    floor.getFloorNumber(), floor.isOpen() ? "OPEN" : "CLOSED",
                    floor.getTotalSlotsCount(), floor.getOccupancyPercentage()));
            for (Slot slot : floor.getAllSlots()) {
                sb.append(String.format("   Slot #%d: Type=%-5s | Status=%-17s | Vehicle=%s\n",
                        slot.getSlotNumber(), slot.getSlotType(), slot.getStatus(),
                        slot.getCurrentVehicle() != null ? slot.getCurrentVehicle().getRegistrationNumber() : "None"));
            }
        }
        sb.append("====================================================================\n");
        return sb.toString();
    }

    private Floor getFloorOrThrow(int floorNumber) {
        if (parkingLot == null) {
            throw new IllegalStateException("Parking lot is not initialized.");
        }
        Floor floor = parkingLot.getFloor(floorNumber);
        if (floor == null) {
            throw new IllegalArgumentException("Floor " + floorNumber + " not found.");
        }
        return floor;
    }

    private Slot getSlotOrThrow(int floorNumber, int slotNumber) {
        Floor floor = getFloorOrThrow(floorNumber);
        Slot slot = floor.getSlot(slotNumber);
        if (slot == null) {
            throw new IllegalArgumentException(String.format("Slot %d not found on floor %d.", slotNumber, floorNumber));
        }
        return slot;
    }
}
