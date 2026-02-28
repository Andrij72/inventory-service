package com.akul.microservices.inventory.common.exceptions;

/**
 * InventoryNotFoundException.java.
 *
 * @author Andrii Kulynych
 * @since 2/15/2026
 */
public class InventoryNotFoundException extends RuntimeException {
    public InventoryNotFoundException(String skuCode) {
        super("Inventory not found for SKU: " + skuCode);
    }
}
