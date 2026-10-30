package module4_payment;

import common.Enums;

/**
 * Cash payment processor.
 * Demonstrates Polymorphism and Strategy implementation.
 */
public class CashPayment implements PaymentMethod {

    private double tenderedAmount;
    private double changeAmount;

    public CashPayment(double tenderedAmount) {
        this.tenderedAmount = tenderedAmount;
        this.changeAmount = 0.0;
    }

    public double getTenderedAmount() {
        return tenderedAmount;
    }

    public double getChangeAmount() {
        return changeAmount;
    }

    @Override
    public Enums.PaymentMethodType getMethodType() {
        return Enums.PaymentMethodType.CASH;
    }

    @Override
    public boolean processPayment(double amount, boolean simulateSuccess) {
        if (!simulateSuccess) {
            return false;
        }
        if (tenderedAmount < amount) {
            return false;
        }
        this.changeAmount = tenderedAmount - amount;
        return true;
    }

    @Override
    public String getDetails() {
        return String.format("Cash [Tendered: Rs. %.2f, Change: Rs. %.2f]", tenderedAmount, changeAmount);
    }
}
