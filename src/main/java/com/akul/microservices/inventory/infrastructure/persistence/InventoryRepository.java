package com.akul.microservices.inventory.infrastructure.persistence;

import com.akul.microservices.inventory.domain.model.Inventory;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * InventoryRepository.java.
 *
 * @author Andrii Kulynych
 * @since 2/14/2026
 */
@Repository
public interface InventoryRepository extends JpaRepository<Inventory, String> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT i FROM Inventory i WHERE i.skuCode = :sku")
    Optional<Inventory> findByIdForUpdate(@Param("sku") String sku);
}
