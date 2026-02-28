package com.akul.microservices.inventory.infrastructure.messaging.events;

/**
 * OrderItemEvent.java.
 *
 * @author Andrii Kulynych
 * @since 2/23/2026
 */
public record OrderItemEvent(
        String skuCode,
        int quantity
) {}
