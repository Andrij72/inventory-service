package com.akul.microservices.inventory.aplication.dto;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

/**
 * ReserveInventoryRequest.java.
 *
 * @author Andrii Kulynych
 * @since 2/15/2026
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReserveInventoryRequest {
    private String orderId;
    private List<OrderItemDto> items;
}