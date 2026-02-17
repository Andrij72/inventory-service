package com.akul.microservices.inventory.infrastructure.persistance;

import com.akul.microservices.inventory.domain.model.Inventory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
/**
 * InventoryRepository.java.
 *
 * @author Andrii Kulynych
 * @since 2/14/2026
 */
@Repository
public interface InventoryRepository extends JpaRepository<Inventory, String> {
}
