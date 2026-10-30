package module4_payment;

import common.Enums;

import java.time.LocalDate;
import java.util.Objects;

/**
 * Represents a Monthly Parking Pass for a specific vehicle.
 * Demonstrates Encapsulation and Domain Temporal Validation.
 */
public class MonthlyPass {
    private final String passId;
    private final String vehicleRegistrationNumber;
    private final Enums.VehicleType vehicleType;
    private final LocalDate startDate;
    private final LocalDate endDate;
    private final double passFee;

    public MonthlyPass(String passId, String vehicleRegistrationNumber, Enums.VehicleType vehicleType,
                       LocalDate startDate, LocalDate endDate) {
        if (passId == null || passId.trim().isEmpty()) {
            throw new IllegalArgumentException("Pass ID cannot be null or empty.");
        }
        if (vehicleRegistrationNumber == null || vehicleRegistrationNumber.trim().isEmpty()) {
            throw new IllegalArgumentException("Registration number cannot be null or empty.");
        }
        if (vehicleType == null) {
            throw new IllegalArgumentException("Vehicle type cannot be null.");
        }
        if (startDate == null || endDate == null) {
            throw new IllegalArgumentException("Start date and End date cannot be null.");
        }
        if (endDate.isBefore(startDate)) {
            throw new IllegalArgumentException("End date cannot be before start date.");
        }

        this.passId = passId.trim().toUpperCase();
        this.vehicleRegistrationNumber = vehicleRegistrationNumber.trim().toUpperCase();
        this.vehicleType = vehicleType;
        this.startDate = startDate;
        this.endDate = endDate;
        this.passFee = vehicleType.getMonthlyPassFee();
    }

    public String getPassId() {
        return passId;
    }

    public String getVehicleRegistrationNumber() {
        return vehicleRegistrationNumber;
    }

    public Enums.VehicleType getVehicleType() {
        return vehicleType;
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    public LocalDate getEndDate() {
        return endDate;
    }

    public double getPassFee() {
        return passFee;
    }

    /**
     * Checks if this pass is currently active on the specified date.
     */
    public boolean isActiveOn(LocalDate date) {
        if (date == null) return false;
        return !date.isBefore(startDate) && !date.isAfter(endDate);
    }

    /**
     * Checks if this pass matches the vehicle and is valid on the given date.
     */
    public boolean isValidFor(String regNumber, Enums.VehicleType type, LocalDate date) {
        if (regNumber == null || type == null || date == null) return false;
        return this.vehicleRegistrationNumber.equalsIgnoreCase(regNumber.trim()) &&
               this.vehicleType == type &&
               isActiveOn(date);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof MonthlyPass that)) return false;
        return Objects.equals(passId, that.passId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(passId);
    }

    @Override
    public String toString() {
        return String.format("MonthlyPass[ID=%s | Reg=%s | Type=%s | Validity: %s to %s | Fee=Rs. %.2f]",
                passId, vehicleRegistrationNumber, vehicleType, startDate, endDate, passFee);
    }
}
