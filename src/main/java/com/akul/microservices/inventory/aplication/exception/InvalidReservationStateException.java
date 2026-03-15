package com.akul.microservices.inventory.aplication.exception;

/**
 * InvalidReservationStateException.java.
 *
 * @author Andrii Kulynych
 * @since 3/13/2026
 */
public class InvalidReservationStateException extends RuntimeException {

    public InvalidReservationStateException(String orderId) {
        super("Reservation for order '%s' is not in a valid state".formatted(orderId));
    }
}
