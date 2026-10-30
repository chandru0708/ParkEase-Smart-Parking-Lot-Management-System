package module1_setup;

import common.Enums;
import common.Slot;

import java.util.*;

/**
 * Represents a floor in the parking lot.
 * Demonstrates Encapsulation and Aggregate Root pattern for Slots.
 */
public class Floor {
    private final int floorNumber;
    private boolean isOpen;
    private final Map<Integer, Slot> slots;

    public Floor(int floorNumber) {
        if (floorNumber < 0) {
            throw new IllegalArgumentException("Floor number cannot be negative.");
        }
        this.floorNumber = floorNumber;
        this.isOpen = true;
        this.slots = new TreeMap<>(); // sorted by slot number
    }

    public int getFloorNumber() {
        return floorNumber;
    }

    public boolean isOpen() {
        return isOpen;
    }

    public void closeFloor() {
        this.isOpen = false;
    }

    public void reopenFloor() {
        this.isOpen = true;
    }

    /**
     * Adds a slot to this floor. Enforces uniqueness of slot number on the floor.
     */
    public void addSlot(Slot slot) {
        if (slot == null) {
            throw new IllegalArgumentException("Slot cannot be null.");
        }
        if (slot.getFloorNumber() != this.floorNumber) {
            throw new IllegalArgumentException(String.format("Slot floor number (%d) does not match floor (%d).",
                    slot.getFloorNumber(), this.floorNumber));
        }
        if (slots.containsKey(slot.getSlotNumber())) {
            throw new IllegalArgumentException(String.format("Slot %d already exists on floor %d.",
                    slot.getSlotNumber(), this.floorNumber));
        }
        slots.put(slot.getSlotNumber(), slot);
    }

    public Slot getSlot(int slotNumber) {
        return slots.get(slotNumber);
    }

    public List<Slot> getAllSlots() {
        return new ArrayList<>(slots.values());
    }

    public int getTotalSlotsCount() {
        return slots.size();
    }

    public int getFreeSlotsCount() {
        int count = 0;
        for (Slot s : slots.values()) {
            if (s.getStatus() == Enums.SlotStatus.FREE) count++;
        }
        return count;
    }

    public int getOccupiedSlotsCount() {
        int count = 0;
        for (Slot s : slots.values()) {
            if (s.getStatus() == Enums.SlotStatus.OCCUPIED) count++;
        }
        return count;
    }

    public int getUnderMaintenanceSlotsCount() {
        int count = 0;
        for (Slot s : slots.values()) {
            if (s.getStatus() == Enums.SlotStatus.UNDER_MAINTENANCE) count++;
        }
        return count;
    }

    /**
     * Calculates occupancy percentage.
     * Formula: (Occupied Slots / (Free Slots + Occupied Slots)) * 100
     * Maintenance slots are strictly excluded from the denominator.
     */
    public double getOccupancyPercentage() {
        int free = getFreeSlotsCount();
        int occupied = getOccupiedSlotsCount();
        int validSlots = free + occupied;
        if (validSlots == 0) {
            return 0.0;
        }
        return ((double) occupied / validSlots) * 100.0;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Floor floor)) return false;
        return floorNumber == floor.floorNumber;
    }

    @Override
    public int hashCode() {
        return Objects.hash(floorNumber);
    }

    @Override
    public String toString() {
        return String.format("Floor %d [Status: %s | Total: %d, Free: %d, Occupied: %d, Maintenance: %d, Occupancy: %.1f%%]",
                floorNumber, isOpen ? "OPEN" : "CLOSED", getTotalSlotsCount(),
                getFreeSlotsCount(), getOccupiedSlotsCount(), getUnderMaintenanceSlotsCount(),
                getOccupancyPercentage());
    }
}
