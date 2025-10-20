package com.akul.microservices.inventory.repository;

import com.akul.microservices.inventory.model.Inventory;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * InventoryRepository.java
 *
 * @author Andrii Kulynch
 * @version 1.0
 * @since 8/27/2025
 */
public interface InventoryRepository extends JpaRepository<Inventory, Long> {
    boolean existsBySkuCodeAndQuantityIsGreaterThanEqual(String skuCode, Integer quantity);
}
