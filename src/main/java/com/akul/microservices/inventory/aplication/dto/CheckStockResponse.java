package com.akul.microservices.inventory.aplication.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * CheckStockResponse.java.
 *
 * @author Andrii Kulynych
 * @since 2/16/2026
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CheckStockResponse {
    private String skuCode;
    private int requestedQuantity;
    private boolean available;
}
