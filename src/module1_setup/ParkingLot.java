package module1_setup;

import common.Slot;

import java.util.*;

/**
 * Represents the entire Parking Lot facility.
 * Demonstrates the Aggregate Root pattern.
 */
public class ParkingLot {
    private final String name;
    private final String address;
    private final Map<Integer, Floor> floors;

    public ParkingLot(String name, String address) {
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Parking lot name cannot be null or empty.");
        }
        if (address == null || address.trim().isEmpty()) {
            throw new IllegalArgumentException("Address cannot be null or empty.");
        }
        this.name = name.trim();
        this.address = address.trim();
        this.floors = new TreeMap<>(); // sorted by floor number
    }

    public String getName() {
        return name;
    }

    public String getAddress() {
        return address;
    }

    /**
     * Adds a floor to the parking lot. Rejects duplicate floor numbers.
     */
    public void addFloor(Floor floor) {
        if (floor == null) {
            throw new IllegalArgumentException("Floor cannot be null.");
        }
        if (floors.containsKey(floor.getFloorNumber())) {
            throw new IllegalArgumentException("Floor " + floor.getFloorNumber() + " already exists in this parking lot.");
        }
        floors.put(floor.getFloorNumber(), floor);
    }

    public Floor getFloor(int floorNumber) {
        return floors.get(floorNumber);
    }

    public List<Floor> getAllFloors() {
        return new ArrayList<>(floors.values());
    }

    public int getFloorCount() {
        return floors.size();
    }

    /**
     * Finds a slot across all floors.
     */
    public Slot findSlot(int floorNumber, int slotNumber) {
        Floor floor = floors.get(floorNumber);
        if (floor != null) {
            return floor.getSlot(slotNumber);
        }
        return null;
    }

    public int getTotalSlotsCount() {
        int total = 0;
        for (Floor f : floors.values()) {
            total += f.getTotalSlotsCount();
        }
        return total;
    }

    public int getTotalFreeSlotsCount() {
        int free = 0;
        for (Floor f : floors.values()) {
            free += f.getFreeSlotsCount();
        }
        return free;
    }

    public int getTotalOccupiedSlotsCount() {
        int occupied = 0;
        for (Floor f : floors.values()) {
            occupied += f.getOccupiedSlotsCount();
        }
        return occupied;
    }

    public int getTotalUnderMaintenanceSlotsCount() {
        int maintenance = 0;
        for (Floor f : floors.values()) {
            maintenance += f.getUnderMaintenanceSlotsCount();
        }
        return maintenance;
    }

    public boolean hasAtLeastOneFloor() {
        return !floors.isEmpty();
    }

    @Override
    public String toString() {
        return String.format("ParkingLot[%s, Address: %s, Floors: %d, Slots: %d]",
                name, address, floors.size(), getTotalSlotsCount());
    }
}
