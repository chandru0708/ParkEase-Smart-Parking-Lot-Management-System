package module2_allocation;

import common.*;
import module1_setup.Floor;
import module1_setup.ParkingLot;

import java.util.*;

/**
 * Service managing vehicle registration and intelligent slot allocation.
 * Demonstrates Strategy / Nearest-Slot Search and Business Rule Enforcement.
 */
public class VehicleAllocationService {

    private final ParkingLot parkingLot;
    private final Set<String> activeVehicleRegistrations;

    public VehicleAllocationService(ParkingLot parkingLot) {
        if (parkingLot == null) {
            throw new IllegalArgumentException("Parking lot cannot be null.");
        }
        this.parkingLot = parkingLot;
        this.activeVehicleRegistrations = new HashSet<>();
    }

    public ParkingLot getParkingLot() {
        return parkingLot;
    }

    public boolean isVehicleParked(String registrationNumber) {
        if (registrationNumber == null) return false;
        return activeVehicleRegistrations.contains(registrationNumber.trim().toUpperCase());
    }

    /**
     * Finds and allocates the nearest available suitable slot for the given vehicle.
     * Rules:
     * - Vehicle with active parking session cannot enter again.
     * - BIKE: prefers BIKE slot; if none free, allows CAR slot.
     * - CAR: only CAR slots.
     * - TRUCK: only TRUCK slots.
     * - Nearest slot priority: lowest floor number first, then lowest slot number.
     * - Skips closed floors, occupied slots, and maintenance slots.
     */
    public Slot allocateSlot(Vehicle vehicle) {
        if (vehicle == null) {
            throw new IllegalArgumentException("Vehicle cannot be null.");
        }

        String regNumber = vehicle.getRegistrationNumber();
        if (isVehicleParked(regNumber)) {
            throw new IllegalStateException("Vehicle " + regNumber + " is already parked with an active ticket.");
        }

        Slot selectedSlot = findBestAvailableSlot(vehicle.getVehicleType());

        if (selectedSlot == null) {
            throw new IllegalStateException("PARKING FULL - No suitable parking slot available.");
        }

        selectedSlot.occupy(vehicle);
        activeVehicleRegistrations.add(regNumber);
        return selectedSlot;
    }

    /**
     * Releases active vehicle registration on exit.
     */
    public void markVehicleExited(String registrationNumber) {
        if (registrationNumber != null) {
            activeVehicleRegistrations.remove(registrationNumber.trim().toUpperCase());
        }
    }

    /**
     * Manually registers active vehicle if restoring state.
     */
    public void markVehicleEntered(String registrationNumber) {
        if (registrationNumber != null) {
            activeVehicleRegistrations.add(registrationNumber.trim().toUpperCase());
        }
    }

    /**
     * Finds the nearest suitable slot.
     */
    public Slot findBestAvailableSlot(Enums.VehicleType vehicleType) {
        if (vehicleType == Enums.VehicleType.BIKE) {
            // First search for dedicated BIKE slots across all open floors
            Slot bikeSlot = searchAvailableSlot(Enums.SlotType.BIKE);
            if (bikeSlot != null) {
                return bikeSlot;
            }
            // If no BIKE slot is free, fallback to CAR slot
            return searchAvailableSlot(Enums.SlotType.CAR);
        } else if (vehicleType == Enums.VehicleType.CAR) {
            // Only CAR slots
            return searchAvailableSlot(Enums.SlotType.CAR);
        } else if (vehicleType == Enums.VehicleType.TRUCK) {
            // Only TRUCK slots
            return searchAvailableSlot(Enums.SlotType.TRUCK);
        }
        return null;
    }

    private Slot searchAvailableSlot(Enums.SlotType targetType) {
        for (Floor floor : parkingLot.getAllFloors()) {
            // Closed floors must NOT receive new vehicles
            if (!floor.isOpen()) {
                continue;
            }
            for (Slot slot : floor.getAllSlots()) {
                // Must be FREE and match requested slot type
                if (slot.getStatus() == Enums.SlotStatus.FREE && slot.getSlotType() == targetType) {
                    return slot;
                }
            }
        }
        return null;
    }

    /**
     * Returns all currently available slots across open floors.
     */
    public List<Slot> getAvailableSlots() {
        List<Slot> available = new ArrayList<>();
        for (Floor floor : parkingLot.getAllFloors()) {
            if (!floor.isOpen()) continue;
            for (Slot slot : floor.getAllSlots()) {
                if (slot.getStatus() == Enums.SlotStatus.FREE) {
                    available.add(slot);
                }
            }
        }
        return available;
    }

    /**
     * Returns all currently parked vehicles across the lot.
     */
    public List<Vehicle> getCurrentlyParkedVehicles() {
        List<Vehicle> parked = new ArrayList<>();
        for (Floor floor : parkingLot.getAllFloors()) {
            for (Slot slot : floor.getAllSlots()) {
                if (slot.getStatus() == Enums.SlotStatus.OCCUPIED && slot.getCurrentVehicle() != null) {
                    parked.add(slot.getCurrentVehicle());
                }
            }
        }
        return parked;
    }
}
