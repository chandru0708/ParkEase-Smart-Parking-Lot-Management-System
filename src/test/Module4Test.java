package test;

import common.*;
import module4_payment.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Independent Test Runner for Module 4: Fee Calculation, Monthly Passes, and Payments.
 */
public class Module4Test {

    public static void main(String[] args) {
        System.out.println(">>> Running Module 4 Tests: Fee Calculation & Payment <<<");
        int passed = 0;
        int total = 0;

        FeeCalculator feeCalculator = new StandardFeeCalculator();
        LocalDateTime baseTime = LocalDateTime.of(2026, 10, 30, 8, 0, 0);

        // Test 1: First 15 minutes are FREE
        total++;
        try {
            Ticket bikeTicket = new Ticket("T-1", "KA01BK1111", Enums.VehicleType.BIKE, 0, 1, Enums.SlotType.BIKE, baseTime);
            double fee12Min = feeCalculator.calculateFee(bikeTicket, baseTime.plusMinutes(12));
            double fee15Min = feeCalculator.calculateFee(bikeTicket, baseTime.plusMinutes(15));
            assert fee12Min == 0.0;
            assert fee15Min == 0.0;
            passed++;
            System.out.println("  [PASS] Test 1: First 15 minutes are free (fee = 0.0).");
        } catch (Throwable t) {
            System.err.println("  [FAIL] Test 1: " + t.getMessage());
        }

        // Test 2: After 15 minutes, rounds up to next full hour (16 mins = 1 hour)
        total++;
        try {
            Ticket bikeTicket = new Ticket("T-1", "KA01BK1111", Enums.VehicleType.BIKE, 0, 1, Enums.SlotType.BIKE, baseTime);
            double fee16Min = feeCalculator.calculateFee(bikeTicket, baseTime.plusMinutes(16));
            double fee60Min = feeCalculator.calculateFee(bikeTicket, baseTime.plusMinutes(60));
            assert fee16Min == 10.0; // 1 hour at Rs. 10/hr
            assert fee60Min == 10.0;
            passed++;
            System.out.println("  [PASS] Test 2: 16 mins rounds up to 1 hr (Rs. 10).");
        } catch (Throwable t) {
            System.err.println("  [FAIL] Test 2: " + t.getMessage());
        }

        // Test 3: Multiple hours calculation (Car: 3 hours = Rs. 60, 5 hours = Rs. 100)
        total++;
        try {
            Ticket carTicket = new Ticket("T-2", "KA01CR2222", Enums.VehicleType.CAR, 0, 2, Enums.SlotType.CAR, baseTime);
            double fee3Hr = feeCalculator.calculateFee(carTicket, baseTime.plusHours(3));
            double fee5Hr = feeCalculator.calculateFee(carTicket, baseTime.plusHours(5));
            assert fee3Hr == 60.0;
            assert fee5Hr == 100.0;
            passed++;
            System.out.println("  [PASS] Test 3: Car multi-hour rates (3h = Rs. 60, 5h = Rs. 100).");
        } catch (Throwable t) {
            System.err.println("  [FAIL] Test 3: " + t.getMessage());
        }

        // Test 4: Daily cap of 8 hours of hourly rate
        // Bike: 10 hrs -> 8 hrs * 10 = Rs. 80
        // Car: 10 hrs -> 8 hrs * 20 = Rs. 160
        // Truck: 10 hrs -> 8 hrs * 40 = Rs. 320
        total++;
        try {
            Ticket bikeTicket = new Ticket("T-1", "KA01BK1111", Enums.VehicleType.BIKE, 0, 1, Enums.SlotType.BIKE, baseTime);
            Ticket carTicket = new Ticket("T-2", "KA01CR2222", Enums.VehicleType.CAR, 0, 2, Enums.SlotType.CAR, baseTime);
            Ticket truckTicket = new Ticket("T-3", "KA01TR3333", Enums.VehicleType.TRUCK, 0, 3, Enums.SlotType.TRUCK, baseTime);

            assert feeCalculator.calculateFee(bikeTicket, baseTime.plusHours(10)) == 80.0;
            assert feeCalculator.calculateFee(carTicket, baseTime.plusHours(10)) == 160.0;
            assert feeCalculator.calculateFee(truckTicket, baseTime.plusHours(10)) == 320.0;
            passed++;
            System.out.println("  [PASS] Test 4: Daily 8-hour cap verified for Bike (80), Car (160), Truck (320).");
        } catch (Throwable t) {
            System.err.println("  [FAIL] Test 4: " + t.getMessage());
        }

        // Test 5: Bike parked in Car slot must be charged at Bike rate
        total++;
        try {
            Ticket bikeInCarSlot = new Ticket("T-4", "KA01BK4444", Enums.VehicleType.BIKE, 0, 5, Enums.SlotType.CAR, baseTime);
            double fee = feeCalculator.calculateFee(bikeInCarSlot, baseTime.plusHours(2));
            assert fee == 20.0; // 2 hrs * 10 (Bike rate, NOT Car rate of 20/hr)
            passed++;
            System.out.println("  [PASS] Test 5: Bike parked in Car slot charged at Bike rate (Rs. 20 for 2h).");
        } catch (Throwable t) {
            System.err.println("  [FAIL] Test 5: " + t.getMessage());
        }

        // Test 6: Monthly Pass validation
        total++;
        try {
            MonthlyPassService passService = new MonthlyPassService();
            LocalDate today = LocalDate.of(2026, 10, 30);
            MonthlyPass pass = passService.issuePass("KA01BK1111", Enums.VehicleType.BIKE, today.minusDays(5), 1);
            assert passService.hasValidPass("KA01BK1111", Enums.VehicleType.BIKE, today);

            // Expired pass check
            assert !passService.hasValidPass("KA01BK1111", Enums.VehicleType.BIKE, today.plusMonths(2));
            passed++;
            System.out.println("  [PASS] Test 6: Monthly pass active and expired validation.");
        } catch (Throwable t) {
            System.err.println("  [FAIL] Test 6: " + t.getMessage());
        }

        // Test 7: Payment methods (Cash, UPI, Card) and Receipts
        total++;
        try {
            PaymentMethod cash = new CashPayment(100.0);
            boolean cashSuccess = cash.processPayment(80.0, true);
            assert cashSuccess;
            assert ((CashPayment) cash).getChangeAmount() == 20.0;

            PaymentMethod upi = new UPIPayment("user@okaxis");
            boolean upiSuccess = upi.processPayment(50.0, true);
            boolean upiFail = upi.processPayment(50.0, false);
            assert upiSuccess && !upiFail;

            PaymentMethod card = new CardPayment("1234567890123456", "John");
            assert ((CardPayment) card).getMaskedCardNumber().endsWith("3456");

            Receipt receipt = new Receipt("RCP-1", "PAY-1", "T-1", "KA01BK1111", 80.0,
                    cash.getMethodType(), LocalDateTime.now(), Enums.PaymentStatus.PAID);
            assert receipt.formatReceipt().contains("PARKEASE PAYMENT RECEIPT");
            passed++;
            System.out.println("  [PASS] Test 7: Polymorphic payment methods and receipt generation.");
        } catch (Throwable t) {
            System.err.println("  [FAIL] Test 7: " + t.getMessage());
        }

        System.out.printf("Module 4 Results: %d/%d passed.\n\n", passed, total);
        if (passed != total) {
            System.exit(1);
        }
    }
}
