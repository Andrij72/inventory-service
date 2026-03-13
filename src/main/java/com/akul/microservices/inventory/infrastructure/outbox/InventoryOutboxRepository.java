package com.akul.microservices.inventory.infrastructure.outbox;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * InventoryOutboxRepository.java.
 *
 * @author Andrii Kulynych
 * @since 2/28/2026
 */
@Repository
public interface InventoryOutboxRepository extends JpaRepository<InventoryOutbox, Long> {

    @Query(value = """
            SELECT *
            FROM inventory_outbox
            WHERE status = 'PENDING'
            AND next_retry_at <= NOW()
            ORDER BY created_at
            FOR UPDATE SKIP LOCKED
            LIMIT :limit
            """, nativeQuery = true)
    List<InventoryOutbox> findBatchForProcessing(@Param("limit") int limit);
}
