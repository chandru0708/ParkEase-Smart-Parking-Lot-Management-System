package common;

import java.util.Objects;

/**
 * Base abstract class representing a parking slot on a floor.
 * Demonstrates Encapsulation and Polymorphism.
 */
public abstract class Slot {
    private final int floorNumber;
    private final int slotNumber;
    private Enums.SlotType slotType;
    private Enums.SlotStatus status;
    private Vehicle currentVehicle;

    public Slot(int floorNumber, int slotNumber, Enums.SlotType slotType) {
        if (floorNumber < 0) {
            throw new IllegalArgumentException("Floor number cannot be negative.");
        }
        if (slotNumber <= 0) {
            throw new IllegalArgumentException("Slot number must be greater than zero.");
        }
        if (slotType == null) {
            throw new IllegalArgumentException("Slot type cannot be null.");
        }
        this.floorNumber = floorNumber;
        this.slotNumber = slotNumber;
        this.slotType = slotType;
        this.status = Enums.SlotStatus.FREE;
        this.currentVehicle = null;
    }

    public int getFloorNumber() {
        return floorNumber;
    }

    public int getSlotNumber() {
        return slotNumber;
    }

    public Enums.SlotType getSlotType() {
        return slotType;
    }

    public Enums.SlotStatus getStatus() {
        return status;
    }

    public Vehicle getCurrentVehicle() {
        return currentVehicle;
    }

    public boolean isAvailable() {
        return status == Enums.SlotStatus.FREE;
    }

    public boolean isOccupied() {
        return status == Enums.SlotStatus.OCCUPIED;
    }

    public boolean isUnderMaintenance() {
        return status == Enums.SlotStatus.UNDER_MAINTENANCE;
    }

    /**
     * Polymorphic method to determine whether this slot can physically accommodate the vehicle type.
     */
    public abstract boolean canAccommodate(Enums.VehicleType vehicleType);

    /**
     * Allocates this slot to a vehicle.
     */
    public void occupy(Vehicle vehicle) {
        if (vehicle == null) {
            throw new IllegalArgumentException("Vehicle cannot be null.");
        }
        if (status == Enums.SlotStatus.OCCUPIED) {
            throw new IllegalStateException("Slot " + slotNumber + " is already occupied.");
        }
        if (status == Enums.SlotStatus.UNDER_MAINTENANCE) {
            throw new IllegalStateException("Slot " + slotNumber + " is under maintenance.");
        }
        if (!canAccommodate(vehicle.getVehicleType())) {
            throw new IllegalArgumentException(String.format("Slot type %s cannot accommodate vehicle type %s.",
                    slotType, vehicle.getVehicleType()));
        }
        this.currentVehicle = vehicle;
        this.status = Enums.SlotStatus.OCCUPIED;
    }

    /**
     * Vacates the vehicle from this slot and restores status to FREE.
     */
    public Vehicle vacate() {
        if (status != Enums.SlotStatus.OCCUPIED) {
            throw new IllegalStateException("Slot " + slotNumber + " is not currently occupied.");
        }
        Vehicle vacated = this.currentVehicle;
        this.currentVehicle = null;
        this.status = Enums.SlotStatus.FREE;
        return vacated;
    }

    /**
     * Marks slot as under maintenance. Allowed only if slot is currently FREE.
     */
    public void markUnderMaintenance() {
        if (status == Enums.SlotStatus.OCCUPIED) {
            throw new IllegalStateException("Cannot mark an occupied slot under maintenance.");
        }
        this.status = Enums.SlotStatus.UNDER_MAINTENANCE;
    }

    /**
     * Reopens slot for parking after maintenance.
     */
    public void reopenFromMaintenance() {
        if (status != Enums.SlotStatus.UNDER_MAINTENANCE) {
            throw new IllegalStateException("Slot is not currently under maintenance.");
        }
        this.status = Enums.SlotStatus.FREE;
    }

    /**
     * Changes slot type. Rule: Type cannot be changed while slot is OCCUPIED.
     */
    public void changeSlotType(Enums.SlotType newType) {
        if (newType == null) {
            throw new IllegalArgumentException("New slot type cannot be null.");
        }
        if (status == Enums.SlotStatus.OCCUPIED) {
            throw new IllegalStateException("A slot's type cannot be changed while it is Occupied.");
        }
        this.slotType = newType;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Slot slot)) return false;
        return floorNumber == slot.floorNumber && slotNumber == slot.slotNumber;
    }

    @Override
    public int hashCode() {
        return Objects.hash(floorNumber, slotNumber);
    }

    @Override
    public String toString() {
        return String.format("Slot[F%d-S%d | Type: %s | Status: %s | Vehicle: %s]",
                floorNumber, slotNumber, slotType, status,
                currentVehicle != null ? currentVehicle.getRegistrationNumber() : "None");
    }
}
