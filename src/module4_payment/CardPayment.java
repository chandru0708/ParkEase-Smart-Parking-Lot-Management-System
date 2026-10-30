package module4_payment;

import common.Enums;

/**
 * Card payment processor (Credit / Debit).
 * Demonstrates Polymorphism.
 */
public class CardPayment implements PaymentMethod {

    private final String maskedCardNumber;
    private final String cardHolderName;

    public CardPayment(String cardNumber, String cardHolderName) {
        if (cardNumber == null || cardNumber.replaceAll("\\s+", "").length() < 4) {
            throw new IllegalArgumentException("Card number must be valid.");
        }
        String clean = cardNumber.replaceAll("\\s+", "");
        this.maskedCardNumber = "XXXX-XXXX-XXXX-" + clean.substring(clean.length() - 4);
        this.cardHolderName = cardHolderName != null ? cardHolderName.trim() : "Guest";
    }

    public String getMaskedCardNumber() {
        return maskedCardNumber;
    }

    public String getCardHolderName() {
        return cardHolderName;
    }

    @Override
    public Enums.PaymentMethodType getMethodType() {
        return Enums.PaymentMethodType.CARD;
    }

    @Override
    public boolean processPayment(double amount, boolean simulateSuccess) {
        return simulateSuccess;
    }

    @Override
    public String getDetails() {
        return String.format("Card [%s | Holder: %s]", maskedCardNumber, cardHolderName);
    }
}
