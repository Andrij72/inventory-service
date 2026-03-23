package com.akul.microservices.inventory.infrastructure.persistence;

import com.akul.microservices.inventory.domain.model.InventoryReservation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

/**
 * InventoryReservationRepository.java.
 *
 * @author Andrii Kulynych
 * @since 2/14/2026
 */
@Repository
public interface InventoryReservationRepository extends JpaRepository<InventoryReservation, Long> {

    List<InventoryReservation> findByOrderId(String orderId);

    boolean existsByOrderId(String orderId);

    Optional<InventoryReservation> findByOrderIdAndSkuCode(
            String orderId,
            String skuCode);


    List<InventoryReservation> findByStatusAndExpiresAtBefore(
            InventoryReservation.ReservationStatus status, Instant now
    );
}
