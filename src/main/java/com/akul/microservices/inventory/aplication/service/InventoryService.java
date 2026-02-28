package com.akul.microservices.inventory.aplication.service;


import com.akul.microservices.inventory.common.exceptions.InvalidReservationStateException;
import com.akul.microservices.inventory.common.exceptions.InventoryNotFoundException;
import com.akul.microservices.inventory.common.util.JsonUtil;
import com.akul.microservices.inventory.domain.model.Inventory;
import com.akul.microservices.inventory.domain.model.InventoryEvent;
import com.akul.microservices.inventory.domain.model.InventoryReservation;
import com.akul.microservices.inventory.infrastructure.messaging.InventoryEventProducer;
import com.akul.microservices.inventory.infrastructure.persistance.InventoryEventRepository;
import com.akul.microservices.inventory.infrastructure.persistance.InventoryRepository;
import com.akul.microservices.inventory.infrastructure.persistance.InventoryReservationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;
import java.util.Optional;


/**
 * InventoryService.java.
 *
 * @author Andrii Kulynych
 * @since 2/15/2026
 */
@Service
@RequiredArgsConstructor
@Transactional
public class InventoryService {

    private final InventoryRepository inventoryRepository;
    private final InventoryReservationRepository reservationRepository;
    private final InventoryEventRepository eventRepository;
    private final InventoryEventProducer eventProducer;

    @Value("${inventory.reservation.minutes:10}")
    private long reservationMinutes;

    // ============================
    // Reserve stock (idempotent)
    // ============================
    public InventoryReservation reserveStock(
            String orderId,
            String skuCode,
            int quantity
    ) {

        Optional<InventoryReservation> existing =
                reservationRepository.findByOrderIdAndSkuCode(orderId, skuCode);

        if (existing.isPresent()) {
            return existing.get();
        }

        Inventory inventory = inventoryRepository.findById(skuCode)
                .orElseThrow(() -> new InventoryNotFoundException(skuCode));

        inventory.reserve(quantity);

        InventoryReservation reservation =
                InventoryReservation.create(
                        orderId,
                        skuCode,
                        quantity,
                        reservationMinutes
                );

        reservationRepository.save(reservation);

        InventoryEvent event = InventoryEvent.create(
                skuCode,
                "INVENTORY_RESERVED",
                JsonUtil.toJson(Map.of(
                        "orderId", orderId,
                        "skuCode", skuCode,
                        "quantity", quantity
                ))
        );

        eventRepository.save(event);

        return reservation;
    }

    // ============================
    // Confirm reservation (Saga)
    // ============================
    public void confirmReservation(String orderId) {
        InventoryReservation reservation = reservationRepository.findByOrderId(orderId)
                .orElseThrow(() -> new RuntimeException("Reservation not found"));

        if (reservation.getStatus() != InventoryReservation.ReservationStatus.PENDING) {
            throw new InvalidReservationStateException();
        }

        // mark as confirmed
        reservation.confirm();
        reservationRepository.save(reservation);

        // reduce reservedQuantity in inventory
        Inventory inventory = inventoryRepository.findById(reservation.getSkuCode())
                .orElseThrow(() -> new InventoryNotFoundException(reservation.getSkuCode()));
        inventory.confirm(reservation.getQuantity());
        inventoryRepository.save(inventory);

        // create event
        InventoryEvent event = InventoryEvent.create(
                reservation.getSkuCode(),
                "INVENTORY_CONFIRMED",
                JsonUtil.toJson(Map.of("orderId", orderId))
        );
        eventRepository.save(event);

        // eventProducer
        eventProducer.sendEvent(event);
    }

    // ============================
    // Cancel reservation (Saga compensation)
    // ============================
    public void cancelReservation(String orderId) {
        InventoryReservation reservation = reservationRepository.findByOrderId(orderId)
                .orElseThrow(() -> new RuntimeException("Reservation not found"));

        if (reservation.getStatus() != InventoryReservation.ReservationStatus.PENDING) {
            throw new InvalidReservationStateException();
        }

        // mark as cancelled
        reservation.cancel();
        reservationRepository.save(reservation);

        // release inventory
        Inventory inventory = inventoryRepository.findById(reservation.getSkuCode())
                .orElseThrow(() -> new InventoryNotFoundException(reservation.getSkuCode()));
        inventory.release(reservation.getQuantity());
        inventoryRepository.save(inventory);

        // create event
        InventoryEvent event = InventoryEvent.create(
                reservation.getSkuCode(),
                "INVENTORY_CANCELLED",
                JsonUtil.toJson(Map.of("orderId", orderId))
        );
        eventRepository.save(event);

        // eventProducer
        eventProducer.sendEvent(event);
    }

    // ============================
    // Check stock availability
    // ============================
    @Transactional(readOnly = true)
    public boolean isProductInStock(String skuCode, int quantity) {
        Inventory inventory = inventoryRepository.findById(skuCode)
                .orElseThrow(() -> new InventoryNotFoundException(skuCode));
        return inventory.getAvailableQuantity() >= quantity;
    }
}
