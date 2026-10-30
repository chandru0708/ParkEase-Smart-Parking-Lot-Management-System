package common;

/**
 * Concrete Vehicle class for Car.
 * Hourly rate: Rs. 20, Monthly pass fee: Rs. 1500.
 */
public class Car extends Vehicle {

    public Car(String registrationNumber, String ownerName) {
        super(registrationNumber, Enums.VehicleType.CAR, ownerName);
    }

    @Override
    public double getHourlyRate() {
        return Enums.VehicleType.CAR.getHourlyRate();
    }

    @Override
    public double getMonthlyPassFee() {
        return Enums.VehicleType.CAR.getMonthlyPassFee();
    }
}
