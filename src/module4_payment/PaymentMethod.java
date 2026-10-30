package module4_payment;

import common.Enums;

/**
 * Interface representing a payment method.
 * Demonstrates Abstraction and Strategy / Polymorphic Payment Processing.
 */
public interface PaymentMethod {

    Enums.PaymentMethodType getMethodType();

    /**
     * Executes payment processing.
     * @param amount the fee amount to process
     * @param simulateSuccess simulation flag for academic console execution
     * @return true if successful, false otherwise
     */
    boolean processPayment(double amount, boolean simulateSuccess);

    /**
     * Informational summary of the payment method details.
     */
    String getDetails();
}
