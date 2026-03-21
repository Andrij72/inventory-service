package com.akul.microservices.inventory.aplication.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * InventoryItemDto.java.
 *
 * @author Andrii Kulynych
 * @since 3/16/2026
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InventoryItemDto {
    private String skuCode;
    private String name;
    private int availableQuantity;
}
