package com.akul.microservices.inventory.infrastructure.messaging;

import com.akul.microservices.inventory.domain.model.Inventory;
import com.akul.microservices.inventory.infrastructure.messaging.events.OrderItemEvent;
import com.akul.microservices.inventory.infrastructure.messaging.events.OrderPlacedEvent;
import com.akul.microservices.inventory.infrastructure.persistance.InventoryRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

/**
 * InventoryKafkaLisener.java.
 *
 * @author Andrii Kulynych
 * @since 2/23/2026
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class InventoryKafkaListener {

    private final InventoryRepository inventoryRepository;
    private final KafkaTemplate<String, String> kafkaTemplate;

    @KafkaListener(topics = "order-placed", groupId = "inventory-group")
    @Transactional
    public void handleOrderPlaced(OrderPlacedEvent event) {

        for (OrderItemEvent item : event.items()) {

            Inventory inventory = inventoryRepository
                    .findById(item.skuCode())
                    .orElseThrow();

            if (!inventory.canReserve(item.quantity())) {
                kafkaTemplate.send("inventory-rejected", event.orderNumber());
                return;
            }

            inventory.reserve(item.quantity());
            inventoryRepository.save(inventory);
        }

        kafkaTemplate.send("inventory-confirmed", event.orderNumber());
    }
}

