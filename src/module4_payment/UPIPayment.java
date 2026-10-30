package module4_payment;

import common.Enums;

/**
 * UPI payment processor.
 * Demonstrates Polymorphism.
 */
public class UPIPayment implements PaymentMethod {

    private final String upiId;

    public UPIPayment(String upiId) {
        if (upiId == null || !upiId.contains("@")) {
            throw new IllegalArgumentException("Invalid UPI ID. Must contain '@'.");
        }
        this.upiId = upiId.trim();
    }

    public String getUpiId() {
        return upiId;
    }

    @Override
    public Enums.PaymentMethodType getMethodType() {
        return Enums.PaymentMethodType.UPI;
    }

    @Override
    public boolean processPayment(double amount, boolean simulateSuccess) {
        return simulateSuccess;
    }

    @Override
    public String getDetails() {
        return "UPI [VPA: " + upiId + "]";
    }
}
