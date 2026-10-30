package common;

import java.util.Objects;

/**
 * Abstract base class representing a Vehicle in the parking system.
 * Demonstrates Abstraction and Encapsulation.
 */
public abstract class Vehicle {
    private final String registrationNumber;
    private final Enums.VehicleType vehicleType;
    private final String ownerName;

    public Vehicle(String registrationNumber, Enums.VehicleType vehicleType, String ownerName) {
        if (registrationNumber == null || registrationNumber.trim().isEmpty()) {
            throw new IllegalArgumentException("Registration number cannot be null or empty.");
        }
        if (vehicleType == null) {
            throw new IllegalArgumentException("Vehicle type cannot be null.");
        }
        if (ownerName == null || ownerName.trim().isEmpty()) {
            throw new IllegalArgumentException("Owner name cannot be null or empty.");
        }
        this.registrationNumber = registrationNumber.trim().toUpperCase();
        this.vehicleType = vehicleType;
        this.ownerName = ownerName.trim();
    }

    public String getRegistrationNumber() {
        return registrationNumber;
    }

    public Enums.VehicleType getVehicleType() {
        return vehicleType;
    }

    public String getOwnerName() {
        return ownerName;
    }

    /**
     * Polymorphic method to return vehicle-specific hourly parking rate.
     */
    public abstract double getHourlyRate();

    /**
     * Polymorphic method to return vehicle-specific monthly pass fee.
     */
    public abstract double getMonthlyPassFee();

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Vehicle vehicle)) return false;
        return Objects.equals(registrationNumber, vehicle.registrationNumber);
    }

    @Override
    public int hashCode() {
        return Objects.hash(registrationNumber);
    }

    @Override
    public String toString() {
        return String.format("%s [%s] Owner: %s", vehicleType, registrationNumber, ownerName);
    }
}
