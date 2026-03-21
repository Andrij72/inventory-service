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

    private final InventoryRepository inventoryRepository;
    private final KafkaTemplate<String, InventoryEvent> kafkaTemplate;

//    @KafkaListener(
//            topics = "order-created",
//            groupId = "inventory-group",
//            containerFactory = "kafkaListenerContainerFactory"
//    )
//    @Transactional
//    public void handleOrderPlaced(OrderPlacedEvent orderEvent) {
//        log.info("Received OrderPlacedEvent: {}", orderEvent.getOrderNbr());
//
//        List<Inventory> inventories = orderEvent.getItems().stream()
//                .map(item -> inventoryRepository
//                        .findByIdForUpdate(item.getSku())
//                        .orElseThrow(() -> new IllegalStateException(
//                                "Inventory not found for SKU " + item.getSku())))
//                .toList();
//
//        boolean canReserveAll = true;
//        for (int i = 0; i < inventories.size(); i++) {
//            if (!inventories.get(i).canReserve(orderEvent.getItems().get(i).getQuantity())) {
//                canReserveAll = false;
//                break;
//            }
//        }
//
//        InventoryEvent event = InventoryEvent.newBuilder()
//                .setEventId(orderEvent.getOrderNbr())
//                .setOrderNbr(orderEvent.getOrderNbr())
//                .setEventType(canReserveAll ?
//                        InventoryEventType.INVENTORY_CONFIRMED :
//                        InventoryEventType.INVENTORY_REJECTED)
//                .setCreatedAt(Instant.now())
//                .build();
//
//        kafkaTemplate.send(
//                canReserveAll ? "inventory-confirmed" : "inventory-rejected",
//                orderEvent.getOrderNbr(),
//                event
//        );
//
//        log.info("Sent {} for order {}",
//                event.getEventType(), orderEvent.getOrderNbr());
//
//        if (canReserveAll) {
//            for (int i = 0; i < inventories.size(); i++) {
//                Inventory inventory = inventories.get(i);
//                inventory.reserve(orderEvent.getItems().get(i).getQuantity());
//                inventoryRepository.save(inventory);
//            }
//        }
//
//
//    }


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
