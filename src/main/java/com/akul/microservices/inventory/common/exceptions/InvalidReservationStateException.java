package com.akul.microservices.inventory.common.exceptions;

/**
 * InvalidReservationStateException.java.
 *
 * @author Andrii Kulynych
 * @since 2/14/2026
 */
public class gitInvalidReservationStateException extends RuntimeException {
    public InvalidReservationStateException() {
        super("Cannot release more than reserved");
    }

    public InvalidReservationStateException(String massage) {
        super(massage);
    }
}
