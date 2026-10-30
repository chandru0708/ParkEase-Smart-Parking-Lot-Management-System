package common;

/**
 * Concrete Vehicle class for Truck.
 * Hourly rate: Rs. 40, Monthly pass fee: Rs. 3000.
 */
public class Truck extends Vehicle {

    public Truck(String registrationNumber, String ownerName) {
        super(registrationNumber, Enums.VehicleType.TRUCK, ownerName);
    }

    @Override
    public double getHourlyRate() {
        return Enums.VehicleType.TRUCK.getHourlyRate();
    }

    @Override
    public double getMonthlyPassFee() {
        return Enums.VehicleType.TRUCK.getMonthlyPassFee();
    }
}
