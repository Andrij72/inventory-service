package com.akul.microservices.inventory.aplication.mapper;

import com.akul.microservices.inventory.aplication.dto.ReserveInventoryResponse;
import com.akul.microservices.inventory.domain.model.InventoryReservation;

/**
 * InventoryMapper.java.
 *
 * @author Andrii Kulynych
 * @since 3/14/2026
 */
public class InventoryMapper {
    public static ReserveInventoryResponse toDto(InventoryReservation reservation) {

        return ReserveInventoryResponse.builder()
                .orderId(reservation.getOrderId())
                .skuCode(reservation.getSkuCode())
                .quantity(reservation.getQuantity())
                .status(reservation.getStatus().name())
                .build();
    }
}

