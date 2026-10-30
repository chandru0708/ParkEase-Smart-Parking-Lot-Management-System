package common;

/**
 * Concrete Vehicle class for Bike.
 * Hourly rate: Rs. 10, Monthly pass fee: Rs. 500.
 */
public class Bike extends Vehicle {

    public Bike(String registrationNumber, String ownerName) {
        super(registrationNumber, Enums.VehicleType.BIKE, ownerName);
    }

    @Override
    public double getHourlyRate() {
        return Enums.VehicleType.BIKE.getHourlyRate();
    }

    @Override
    public double getMonthlyPassFee() {
        return Enums.VehicleType.BIKE.getMonthlyPassFee();
    }
}
