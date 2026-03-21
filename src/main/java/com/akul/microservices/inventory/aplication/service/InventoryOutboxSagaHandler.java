package com.akul.microservices.inventory.aplication.service;

import com.akul.microservices.inventory.common.exceptions.InventoryNotFoundException;
import com.akul.microservices.inventory.domain.model.Inventory;
import com.akul.microservices.inventory.domain.model.InventoryReservation;
import com.akul.microservices.inventory.event.InventoryEvent;
import com.akul.microservices.inventory.infrastructure.mapper.InventoryEventTypeMapper;
import com.akul.microservices.inventory.infrastructure.outbox.InventoryEventType;
import com.akul.microservices.inventory.infrastructure.outbox.InventoryOutbox;
import com.akul.microservices.inventory.infrastructure.outbox.InventoryOutboxRepository;
import com.akul.microservices.inventory.infrastructure.persistance.InventoryRepository;
import com.akul.microservices.inventory.infrastructure.persistance.InventoryReservationRepository;
import com.akul.microservices.order.event.OrderItem;
import com.akul.microservices.order.event.OrderPlacedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class InventoryOutboxSagaHandler {

    private final InventoryOutboxRepository outboxRepository;
    private final InventoryReservationRepository reservationRepository;
    private final InventoryRepository inventoryRepository;

    // =========================
    // TTL CLEANUP
    // =========================
    @Scheduled(fixedRateString = "${inventory.ttl.check.millis:60000}")
    public void releaseExpiredReservations() {

        var expired = reservationRepository
                .findByStatusAndExpiresAtBefore(
                        InventoryReservation.ReservationStatus.PENDING,
                        Instant.now()
                );

        for (InventoryReservation reservation : expired) {

            reservation.expire();
            reservationRepository.save(reservation);

            Inventory inventory = inventoryRepository.findById(reservation.getSkuCode())
                    .orElseThrow(() -> new InventoryNotFoundException(reservation.getSkuCode()));

            inventory.release(reservation.getQuantity());
            inventoryRepository.save(inventory);

            publishEvent(
                    reservation.getOrderId(),
                    reservation.getSkuCode(),
                    InventoryEventType.INVENTORY_EXPIRED,
                    Map.of("quantity", reservation.getQuantity())
            );
        }
    }

    // =========================
    // OUTBOX
    // =========================
    private void publishEvent(String orderNbr,
                              String skuCode,
                              InventoryEventType eventType,
                              Map<String, Object> payloadData) {

        InventoryEvent event = InventoryEvent.newBuilder()
                .setEventId(UUID.randomUUID().toString())
                .setOrderNbr(orderNbr)
                .setEventType(InventoryEventTypeMapper.toAvro(eventType))
                .setCreatedAt(Instant.now())
                .build();

        byte[] payload;
        try {
            payload = event.toByteBuffer().array();
        } catch (Exception e) {
            throw new RuntimeException("Failed to serialize Avro event", e);
        }

        InventoryOutbox outbox = InventoryOutbox.create(
                orderNbr,
                skuCode,
                eventType,
                payload
        );

        outboxRepository.save(outbox);

        log.info("Outbox saved: orderNbr={}, skuCode={}, type={}, payload={}",
                orderNbr, skuCode, eventType, payloadData);
    }

    // =========================
    // FROM ORDER EVENT
    // =========================
    @Transactional
    public void reserveOrderFromEvent(OrderPlacedEvent orderEvent) {


        for (OrderItem item : orderEvent.getItems()) {
            boolean exists = outboxRepository.existsByAggregateIdAndSkuCodeAndEventType(
                    orderEvent.getOrderNbr(), item.getSku(), InventoryEventType.INVENTORY_CONFIRMED
            );
            if (exists) return;
            Inventory inventory = inventoryRepository.findByIdForUpdate(item.getSku())
                    .orElseThrow(() -> new IllegalStateException(
                            "Inventory not found for SKU " + item.getSku()));

            if (!inventory.canReserve(item.getQuantity())) {

                publishEvent(
                        orderEvent.getOrderNbr(),
                        item.getSku(),
                        InventoryEventType.INVENTORY_REJECTED,
                        Map.of(
                                "quantity", item.getQuantity()
                        )
                );

                continue;
            }

            inventory.reserve(item.getQuantity());
            inventoryRepository.save(inventory);

            publishEvent(
                    orderEvent.getOrderNbr(),
                    item.getSku(),
                    InventoryEventType.INVENTORY_CONFIRMED,
                    Map.of(
                            "quantity", item.getQuantity()
                    )
            );
        }
    }
}

