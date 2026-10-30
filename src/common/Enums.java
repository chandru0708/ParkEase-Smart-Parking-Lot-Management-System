package common;

/**
 * Common enums used throughout the Parking Lot Management System.
 */
public class Enums {

    public enum VehicleType {
        BIKE("Bike", 10.0, 500.0),
        CAR("Car", 20.0, 1500.0),
        TRUCK("Truck", 40.0, 3000.0);

        private final String displayName;
        private final double hourlyRate;
        private final double monthlyPassFee;

        VehicleType(String displayName, double hourlyRate, double monthlyPassFee) {
            this.displayName = displayName;
            this.hourlyRate = hourlyRate;
            this.monthlyPassFee = monthlyPassFee;
        }

        public String getDisplayName() {
            return displayName;
        }

        public double getHourlyRate() {
            return hourlyRate;
        }

        public double getMonthlyPassFee() {
            return monthlyPassFee;
        }
    }

    public enum SlotType {
        BIKE,
        CAR,
        TRUCK
    }

    public enum SlotStatus {
        FREE,
        OCCUPIED,
        UNDER_MAINTENANCE
    }

    public enum TicketStatus {
        ACTIVE,
        PAID,
        CLOSED,
        LOST
    }

    public enum PaymentStatus {
        PAID,
        FAILED
    }

    public enum PaymentMethodType {
        CASH,
        UPI,
        CARD
    }
}
