package com.akul.microservices.inventory.common.exceptions;

/**
 * ReservationNotFoundException.java.
 *
 * @author Andrii Kulynych
 * @since 3/13/2026
 */
public class ReservationNotFoundException extends RuntimeException {
    public ReservationNotFoundException(String orderId) {
        super("Reservation for order '%s' not found".formatted(orderId));
    }
}
