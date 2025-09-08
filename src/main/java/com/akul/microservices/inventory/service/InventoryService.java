package com.akul.microservices.inventory.service;

import com.akul.microservices.inventory.repository.InventoryRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * InventoryService.java
 *
 * @author Andrii Kulynch
 * @version 1.0
 * @since 8/27/2025
 */
@Service
@Transactional
@RequiredArgsConstructor
public class InventoryService {
    private final InventoryRepository inventoryRepository;

    public boolean isProductInStock(String skuCode, Integer quantity) {

        return inventoryRepository.existsBySkuCodeAndQuantityIsGreaterThanEqual(skuCode, quantity);

    }

}
