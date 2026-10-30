package module4_payment;

import common.Enums;
import common.Ticket;

import java.time.LocalDateTime;

/**
 * Strategy interface for calculating parking fees.
 * Demonstrates Abstraction and Open/Closed Principle.
 */
public interface FeeCalculator {

    /**
     * Calculates the parking fee for a ticket at a given calculation timestamp.
     *
     * @param ticket the parking ticket
     * @param calculationTime the time of fee calculation
     * @return the calculated fee in Rupees
     */
    double calculateFee(Ticket ticket, LocalDateTime calculationTime);
}
