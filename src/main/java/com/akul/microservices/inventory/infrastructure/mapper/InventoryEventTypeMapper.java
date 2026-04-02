package com.akul.microservices.inventory.infrastructure.mapper;

import com.akul.microservices.inventory.infrastructure.outbox.InventoryEventType;

public final class InventoryEventTypeMapper {

    private InventoryEventTypeMapper() {}

    public static com.akul.microservices.inventory.event.InventoryEventType toAvro(InventoryEventType type) {
        try {
            return com.akul.microservices.inventory.event.InventoryEventType.valueOf(type.name());
        } catch (IllegalArgumentException ex) {
            return com.akul.microservices.inventory.event.InventoryEventType.INVENTORY_REJECTED;
        }
    }
}