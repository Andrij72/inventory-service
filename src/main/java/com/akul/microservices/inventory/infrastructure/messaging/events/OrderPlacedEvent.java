package com.akul.microservices.inventory.infrastructure.messaging.events;

/**
 * OrderPlacedEvent.java.
 *
 * @author Andrii Kulynych
 * @since 2/23/2026
 */
import java.util.List;

public record OrderPlacedEvent(
        String orderNumber,
        List<OrderItemEvent> items
) {}
