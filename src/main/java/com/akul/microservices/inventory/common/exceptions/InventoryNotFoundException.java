package com.akul.microservices.inventory.common.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * InventoryNotfoundException.java.
 *
 * @author Andrii Kulynych
 * @since 3/16/2026
 */
@ResponseStatus(HttpStatus.NOT_FOUND)
public class InventoryNotFoundException extends RuntimeException {

    public InventoryNotFoundException(String message) {
        super(message);
    }
}