package com.akul.microservices.inventory.infrastructure.messaging;

import com.akul.microservices.inventory.infrastructure.outbox.InventoryEventType;
import org.springframework.stereotype.Component;

/**
 * InventoryTopicResolver.java.
 *
 * @author Andrii Kulynych
 * @since 3/3/2026
 */
@Component
public class InventoryTopicResolver {

    public String resolveTopic(InventoryEventType eventType) {

        return switch (eventType) {
            case INVENTORY_CONFIRMED -> "inventory-confirmed";
            case INVENTORY_EXPIRED -> "inventory-expired";
            case INVENTORY_REJECTED -> "inventory-rejected";
            case INVENTORY_CANCELLED -> "inventory-cancelled";
        };
    }
}
