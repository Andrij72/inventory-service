package com.akul.microservices.inventory.common.exceptions;

/**
 * InsufficientStockException.java.
 *
 * @author Andrii Kulynych
 * @since 2/12/2026
 */
public class InsufficientStockException extends RuntimeException {

    public InsufficientStockException(final String sku) {
        super("Inventory item with SKU '%s' not found!".formatted(sku));
    }
}

