package com.akul.microservices.inventory.aplication.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;


/**
 * ReserveInventoryResponse.java.
 *
 * @author Andrii Kulynych
 * @since 2/15/2026
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReserveInventoryResponse {
        private String orderId;
        private String skuCode;
        private int quantity;
        private String status;
    }
