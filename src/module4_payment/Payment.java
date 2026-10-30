package module4_payment;

import common.Enums;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Represents an immutable Payment transaction record.
 * Demonstrates Encapsulation and Transactional Integrity.
 */
public class Payment {
    private final String paymentId;
    private final String ticketId;
    private final double amount;
    private final LocalDateTime paymentTimestamp;
    private final Enums.PaymentMethodType paymentMethod;
    private final Enums.PaymentStatus paymentStatus;
    private final String paymentDetails;

    public Payment(String paymentId, String ticketId, double amount, LocalDateTime paymentTimestamp,
                   Enums.PaymentMethodType paymentMethod, Enums.PaymentStatus paymentStatus, String paymentDetails) {
        if (paymentId == null || paymentId.trim().isEmpty()) {
            throw new IllegalArgumentException("Payment ID cannot be null or empty.");
        }
        if (ticketId == null || ticketId.trim().isEmpty()) {
            throw new IllegalArgumentException("Ticket ID cannot be null or empty.");
        }
        this.paymentId = paymentId.trim();
        this.ticketId = ticketId.trim();
        this.amount = amount;
        this.paymentTimestamp = paymentTimestamp != null ? paymentTimestamp : LocalDateTime.now();
        this.paymentMethod = paymentMethod;
        this.paymentStatus = paymentStatus;
        this.paymentDetails = paymentDetails != null ? paymentDetails : "";
    }

    public String getPaymentId() {
        return paymentId;
    }

    public String getTicketId() {
        return ticketId;
    }

    public double getAmount() {
        return amount;
    }

    public LocalDateTime getPaymentTimestamp() {
        return paymentTimestamp;
    }

    public Enums.PaymentMethodType getPaymentMethod() {
        return paymentMethod;
    }

    public Enums.PaymentStatus getPaymentStatus() {
        return paymentStatus;
    }

    public String getPaymentDetails() {
        return paymentDetails;
    }

    public boolean isSuccessful() {
        return paymentStatus == Enums.PaymentStatus.PAID;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Payment payment)) return false;
        return Objects.equals(paymentId, payment.paymentId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(paymentId);
    }

    @Override
    public String toString() {
        return String.format("Payment[ID=%s | Ticket=%s | Amount=Rs. %.2f | Method=%s | Status=%s | Time=%s]",
                paymentId, ticketId, amount, paymentMethod, paymentStatus, paymentTimestamp);
    }
}
