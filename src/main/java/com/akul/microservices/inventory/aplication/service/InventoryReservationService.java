package com.akul.microservices.inventory.aplication.service;

import com.akul.microservices.inventory.aplication.dto.OrderItemDto;
import com.akul.microservices.inventory.aplication.dto.ReserveInventoryRequest;
import com.akul.microservices.inventory.aplication.dto.ReserveInventoryResponse;
import com.akul.microservices.inventory.common.exceptions.InsufficientStockException;
import com.akul.microservices.inventory.common.exceptions.InvalidReservationStateException;
import com.akul.microservices.inventory.common.exceptions.InventoryNotFoundException;
import com.akul.microservices.inventory.common.exceptions.ReservationNotFoundException;
import com.akul.microservices.inventory.domain.model.Inventory;
import com.akul.microservices.inventory.domain.model.InventoryReservation;
import com.akul.microservices.inventory.infrastructure.persistance.InventoryRepository;
import com.akul.microservices.inventory.infrastructure.persistance.InventoryReservationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.akul.microservices.inventory.aplication.mapper.InventoryMapper;

import java.util.ArrayList;
import java.util.List;

/**
 * InventoryReservationService.java.
 *
 * @author Andrii Kulynych
 * @since 3/21/2026
 */
@Service
@RequiredArgsConstructor
public class InventoryReservationService {

    private final InventoryRepository inventoryRepository;
    private final InventoryReservationRepository reservationRepository;

    @Value("${inventory.reservation.minutes:10}")
    private long reservationMinutes;

    @Transactional
    public List<ReserveInventoryResponse> reserve(ReserveInventoryRequest request) {
        List<InventoryReservation> reservations = new ArrayList<>();
        for (OrderItemDto item : request.getItems()) {
            var existing = reservationRepository.findByOrderIdAndSkuCode(request.getOrderId(), item.getSkuCode());
            if (existing.isPresent()) {
                reservations.add(existing.get());
                continue;
            }

            Inventory inventory = inventoryRepository.findById(item.getSkuCode())
                    .orElseThrow(() -> new InventoryNotFoundException(item.getSkuCode()));
            if (!inventory.canReserve(item.getQuantity())) {
                throw new InsufficientStockException(item.getSkuCode(), item.getQuantity(), inventory.getAvailableQuantity());
            }
            inventory.reserve(item.getQuantity());
            InventoryReservation reservation = InventoryReservation.create(
                    request.getOrderId(),
                    item.getSkuCode(),
                    item.getQuantity(),
                    reservationMinutes
            );
            reservationRepository.save(reservation);
            inventoryRepository.save(inventory);

            reservations.add(reservation);
        }
        List<ReserveInventoryResponse> responses = reservations.stream()
                .map(InventoryMapper::toDto)
                .toList();
        
        return responses;
    }

    @Transactional
    public void confirm(String orderId) {
        var reservations = reservationRepository.findByOrderId(orderId);
        if (reservations.isEmpty()) {
            throw new ReservationNotFoundException(orderId);
        }
        for (InventoryReservation r : reservations) {
            if (!r.isPending()) {
                throw new InvalidReservationStateException(orderId);
            }
            r.confirm();
            Inventory inventory = inventoryRepository.findById(r.getSkuCode())
                    .orElseThrow(() -> new InventoryNotFoundException(r.getSkuCode()));
            inventory.confirm(r.getQuantity());

            reservationRepository.save(r);
            inventoryRepository.save(inventory);
        }
    }

    @Transactional
    public void cancel(String orderId) {
        var reservations = reservationRepository.findByOrderId(orderId);
        if (reservations.isEmpty()) {
            throw new ReservationNotFoundException(orderId);
        }
        for (InventoryReservation r : reservations) {
            if (!r.isPending()) {
                throw new InvalidReservationStateException(orderId);
            }
            r.cancel();
            Inventory inventory = inventoryRepository.findById(r.getSkuCode())
                    .orElseThrow(() -> new InventoryNotFoundException(r.getSkuCode()));
            inventory.release(r.getQuantity());
            reservationRepository.save(r);
            inventoryRepository.save(inventory);
        }
    }

    public boolean isProductInStock(String skuCode, int quantity) {

        Inventory inventory = inventoryRepository.findById(skuCode)
                .orElseThrow(() -> new InventoryNotFoundException(skuCode));

        return inventory.getAvailableQuantity() >= quantity;
    }
}
