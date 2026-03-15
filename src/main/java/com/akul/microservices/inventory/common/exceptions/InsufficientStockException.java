package com.akul.microservices.inventory.common.exceptions;

/**
 * InsufficientStockException.java.
 *
 * @author Andrii Kulynych
 * @since 2/12/2026
 */
public class InsufficientStockException extends RuntimeException {

    public InsufficientStockException(final String skuCode,
                                      int quantity,
                                      int availableQuantity) {
        super("Insufficient stock for '%s' requested='%d' available='%d'"
                .formatted(skuCode, quantity, availableQuantity));
    }
}

