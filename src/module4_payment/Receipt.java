package module4_payment;

import common.Enums;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Objects;

/**
 * Represents a printed/generated transaction receipt for payment attempts.
 * Demonstrates Value Object immutability and presentation separation.
 */
public class Receipt {
    private final String receiptId;
    private final String paymentId;
    private final String ticketId;
    private final String vehicleRegistrationNumber;
    private final double amount;
    private final Enums.PaymentMethodType paymentMethod;
    private final LocalDateTime paymentDate;
    private final Enums.PaymentStatus paymentStatus;

    public Receipt(String receiptId, String paymentId, String ticketId, String vehicleRegistrationNumber,
                   double amount, Enums.PaymentMethodType paymentMethod, LocalDateTime paymentDate,
                   Enums.PaymentStatus paymentStatus) {
        this.receiptId = receiptId;
        this.paymentId = paymentId;
        this.ticketId = ticketId;
        this.vehicleRegistrationNumber = vehicleRegistrationNumber;
        this.amount = amount;
        this.paymentMethod = paymentMethod;
        this.paymentDate = paymentDate != null ? paymentDate : LocalDateTime.now();
        this.paymentStatus = paymentStatus;
    }

    public String getReceiptId() {
        return receiptId;
    }

    public String getPaymentId() {
        return paymentId;
    }

    public String getTicketId() {
        return ticketId;
    }

    public String getVehicleRegistrationNumber() {
        return vehicleRegistrationNumber;
    }

    public double getAmount() {
        return amount;
    }

    public Enums.PaymentMethodType getPaymentMethod() {
        return paymentMethod;
    }

    public LocalDateTime getPaymentDate() {
        return paymentDate;
    }

    public Enums.PaymentStatus getPaymentStatus() {
        return paymentStatus;
    }

    public String formatReceipt() {
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        StringBuilder sb = new StringBuilder();
        sb.append("====================================================\n");
        sb.append("                 PARKEASE PAYMENT RECEIPT            \n");
        sb.append("====================================================\n");
        sb.append(String.format(" Receipt ID     : %s\n", receiptId));
        sb.append(String.format(" Payment ID     : %s\n", paymentId));
        sb.append(String.format(" Ticket ID      : %s\n", ticketId));
        sb.append(String.format(" Vehicle Reg No : %s\n", vehicleRegistrationNumber));
        sb.append(String.format(" Date & Time    : %s\n", paymentDate.format(dtf)));
        sb.append(String.format(" Payment Method : %s\n", paymentMethod));
        sb.append(String.format(" Amount Paid    : Rs. %.2f\n", amount));
        sb.append(String.format(" Status         : %s\n", paymentStatus == Enums.PaymentStatus.PAID ? "[SUCCESSFUL - PAID]" : "[FAILED - TRANSACTION DECLINED]"));
        sb.append("====================================================\n");
        if (paymentStatus == Enums.PaymentStatus.PAID) {
            sb.append("  * Please exit parking within 15 minutes of payment *\n");
        } else {
            sb.append("  * Exit NOT allowed until payment succeeds. Please retry *\n");
        }
        sb.append("====================================================\n");
        return sb.toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Receipt receipt)) return false;
        return Objects.equals(receiptId, receipt.receiptId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(receiptId);
    }

    @Override
    public String toString() {
        return String.format("Receipt[%s | PayID=%s | Reg=%s | Rs. %.2f | %s]",
                receiptId, paymentId, vehicleRegistrationNumber, amount, paymentStatus);
    }
}
