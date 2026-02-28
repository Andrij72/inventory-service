package com.akul.microservices.inventory.infrastructure.outbox;

/**
 * InventoryEventType.java.
 *
 * @author Andrii Kulynych
 * @since 2/27/2026
 */
public enum InventoryEventType {
        RESERVED,
        CONFIRMED,
        CANCELLED,
        EXPIRED
    }
