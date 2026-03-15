package com.akul.microservices.inventory.aplication.service;

import com.akul.microservices.inventory.aplication.dto.OrderItemDto;
import com.akul.microservices.inventory.aplication.dto.ReserveInventoryRequest;
import com.akul.microservices.inventory.aplication.dto.ReserveInventoryResponse;
import com.akul.microservices.inventory.aplication.exception.InvalidReservationStateException;
import com.akul.microservices.inventory.aplication.exception.ReservationNotFoundException;
import com.akul.microservices.inventory.aplication.mapper.InventoryMapper;
import com.akul.microservices.inventory.common.exceptions.InsufficientStockException;
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
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;


/**
 * Inventory service responsible for stock reservation lifecycle.
 *
 * Supports:
 * - stock reservation
 * - reservation confirmation
 * - reservation cancellation
 * - automatic TTL expiration
 *
 * Publishes domain events used in Saga-based order processing.
 */
@Slf4j
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

    public List<ReserveInventoryResponse> reserveOrder(ReserveInventoryRequest request) {

        List<ReserveInventoryResponse> responses = new ArrayList<>();

        for (OrderItemDto item : request.getItems()) {

            var existing = reservationRepository
                    .findByOrderIdAndSkuCode(request.getOrderId(), item.getSkuCode());

            if (existing.isPresent()) {
                responses.add(InventoryMapper.toDto(existing.get()));
                continue;
            }

            Inventory inventory = inventoryRepository.findById(item.getSkuCode())
                    .orElseThrow(() -> new InventoryNotFoundException(item.getSkuCode()));

            if (!inventory.canReserve(item.getQuantity())) {
                throw new InsufficientStockException(
                        item.getSkuCode(),
                        item.getQuantity(),
                        inventory.getAvailableQuantity()
                );
            }

            inventory.reserve(item.getQuantity());
            inventoryRepository.save(inventory);

            InventoryReservation reservation = InventoryReservation.create(
                    request.getOrderId(),
                    item.getSkuCode(),
                    item.getQuantity(),
                    reservationMinutes
            );

            reservationRepository.save(reservation);

            publishEvent(
                    item.getSkuCode(),
                    "INVENTORY_RESERVED",
                    Map.of(
                            "orderId", request.getOrderId(),
                            "skuCode", item.getSkuCode(),
                            "quantity", item.getQuantity()
                    )
            );

            responses.add(InventoryMapper.toDto(reservation));
        }

        return responses;
    }

    public void confirmReservation(String orderId) {

        var reservations = reservationRepository.findByOrderId(orderId);

        if (reservations.isEmpty()) {
            throw new ReservationNotFoundException(orderId);
        }

        for (InventoryReservation reservation : reservations) {

            if (!reservation.isPending()) {
                throw new InvalidReservationStateException(orderId);
            }

            reservation.confirm();
            reservationRepository.save(reservation);

            Inventory inventory = inventoryRepository.findById(reservation.getSkuCode())
                    .orElseThrow(() -> new InventoryNotFoundException(reservation.getSkuCode()));

            inventory.confirm(reservation.getQuantity());
            inventoryRepository.save(inventory);

            publishEvent(
                    reservation.getSkuCode(),
                    "INVENTORY_CONFIRMED",
                    Map.of("orderId", orderId)
            );
        }
    }

    public void cancelReservation(String orderId) {

        var reservations = reservationRepository.findByOrderId(orderId);

        if (reservations.isEmpty()) {
            throw new ReservationNotFoundException(orderId);
        }

        for (InventoryReservation reservation : reservations) {

            if (!reservation.isPending()) {
                throw new InvalidReservationStateException(orderId);
            }

            reservation.cancel();
            reservationRepository.save(reservation);

            Inventory inventory = inventoryRepository.findById(reservation.getSkuCode())
                    .orElseThrow(() -> new InventoryNotFoundException(reservation.getSkuCode()));

            inventory.release(reservation.getQuantity());
            inventoryRepository.save(inventory);

            publishEvent(
                    reservation.getSkuCode(),
                    "INVENTORY_CANCELLED",
                    Map.of("orderId", orderId)
            );
        }
    }

    @Transactional(readOnly = true)
    public boolean isProductInStock(String skuCode, int quantity) {

        Inventory inventory = inventoryRepository.findById(skuCode)
                .orElseThrow(() -> new InventoryNotFoundException(skuCode));

        return inventory.getAvailableQuantity() >= quantity;
    }

    @Scheduled(fixedRateString = "${inventory.ttl.check.millis:60000}")
    @Transactional
    public void releaseExpiredReservations() {

        var expiredReservations = reservationRepository
                .findByStatusAndExpiresAtBefore(
                        InventoryReservation.ReservationStatus.PENDING,
                        Instant.now()
                );

        for (InventoryReservation reservation : expiredReservations) {

            reservation.expire();
            reservationRepository.save(reservation);

            Inventory inventory = inventoryRepository.findById(reservation.getSkuCode())
                    .orElseThrow(() -> new InventoryNotFoundException(reservation.getSkuCode()));

            inventory.release(reservation.getQuantity());
            inventoryRepository.save(inventory);

            publishEvent(
                    reservation.getSkuCode(),
                    "INVENTORY_EXPIRED",
                    Map.of(
                            "orderId", reservation.getOrderId(),
                            "skuCode", reservation.getSkuCode(),
                            "quantity", reservation.getQuantity()
                    )
            );
        }
    }

    private void publishEvent(String skuCode, String type, Map<String, Object> payload) {

        InventoryEvent event = InventoryEvent.create(
                skuCode,
                type,
                JsonUtil.toJson(payload)
        );

        eventRepository.save(event);
        eventProducer.sendEvent(event);
    }
}
