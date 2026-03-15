package com.akul.microservices.inventory.aplication.exception;

/**
 * InvalidQuantityException.java.
 *
 * @author Andrii Kulynych
 * @since 3/13/2026
 */
public class InvalidQuantityException extends RuntimeException {
    public InvalidQuantityException(String message) {
        super(message);
    }
}
