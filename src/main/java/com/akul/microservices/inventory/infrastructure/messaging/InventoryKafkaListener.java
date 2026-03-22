package com.akul.microservices.inventory.infrastructure.messaging;

import com.akul.microservices.inventory.aplication.service.InventoryOutboxSagaHandler;
import com.akul.microservices.inventory.event.InventoryEvent;
import com.akul.microservices.inventory.infrastructure.persistance.InventoryRepository;
import com.akul.microservices.order.event.OrderPlacedEvent;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class InventoryKafkaListener {

    private final InventoryOutboxSagaHandler inventoryOutboxSagaHandler;

    @KafkaListener(
            topics = "order-created",
            groupId = "inventory-group",
            containerFactory = "kafkaListenerContainerFactory"
    )
    @Transactional
    public void handleOrderPlaced(OrderPlacedEvent orderEvent) {
        log.info("Received OrderPlacedEvent: {}", orderEvent.getOrderNbr());

        inventoryOutboxSagaHandler.reserveOrderFromEvent(orderEvent);
    }
}
