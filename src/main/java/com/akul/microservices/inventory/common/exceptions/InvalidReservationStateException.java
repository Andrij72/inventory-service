package com.akul.microservices.inventory.common.exceptions;

/**
 * InvalidReservationStateException.java.
 *
 * @author Andrii Kulynych
 * @since 2/14/2026
 */
public class InvalidReservationStateException extends RuntimeException {
    public InvalidReservationStateException(String orderId) {
        super("Reservation for order '%s' is not in a valid state".formatted(orderId));
    }
}
