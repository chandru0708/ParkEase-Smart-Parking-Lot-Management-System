package module4_payment;

import common.Ticket;

import java.time.Duration;
import java.time.LocalDateTime;

/**
 * Standard implementation of fee calculation rules.
 * Business Rules:
 * - Hourly rates: Bike = Rs. 10, Car = Rs. 20, Truck = Rs. 40.
 * - First 15 minutes are FREE.
 * - After 15 minutes, duration is rounded UP to the next full hour.
 * - Maximum charge per day is 8 hours of the hourly rate (applied per 24-hour block).
 * - A Bike parked in a Car slot is charged at the Bike rate (based on vehicle type).
 * - Adds lost ticket penalty if applicable.
 * - Deducts any previously paid amount for top-ups.
 */
public class StandardFeeCalculator implements FeeCalculator {

    @Override
    public double calculateFee(Ticket ticket, LocalDateTime calculationTime) {
        if (ticket == null) {
            throw new IllegalArgumentException("Ticket cannot be null.");
        }
        if (calculationTime == null) {
            calculationTime = LocalDateTime.now();
        }

        Duration duration = Duration.between(ticket.getEntryTime(), calculationTime);
        long totalMinutes = Math.max(0, duration.toMinutes());

        double hourlyRate = ticket.getVehicleType().getHourlyRate();
        double parkingFee = 0.0;

        if (totalMinutes > 15) {
            long minutesPerDay = 24 * 60;
            long fullDays = totalMinutes / minutesPerDay;
            long remainingMinutes = totalMinutes % minutesPerDay;

            // Charge for full 24-hour days (each capped at 8 hours)
            parkingFee += fullDays * (8 * hourlyRate);

            // Charge for remaining time within the current day
            if (remainingMinutes > 0) {
                // If it's day 0 and remainingMinutes <= 15, it was handled above, but here totalMinutes > 15.
                int hours = (int) Math.ceil(remainingMinutes / 60.0);
                int chargeableHours = Math.min(8, hours);
                parkingFee += chargeableHours * hourlyRate;
            }
        }

        // Add lost ticket penalty if flagged
        double penalty = ticket.isLost() ? ticket.getPenaltyAmount() : 0.0;
        double totalGross = parkingFee + penalty;

        // Deduct previously paid amount (e.g. if 15-minute exit grace window expired)
        double netFee = totalGross - ticket.getPaidAmount();
        return Math.max(0.0, netFee);
    }
}
