package com.akul.microservices.inventory.infrastructure.persistance;

import com.akul.microservices.inventory.domain.model.InventoryReservation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

/**
 * InventoryReservationRepository.java.
 *
 * @author Andrii Kulynych
 * @since 2/14/2026
 */
@Repository
public interface InventoryReservationRepository extends JpaRepository<InventoryReservation, Long> {

    Optional<InventoryReservation> findByOrderId(String orderId);

    boolean existsByOrderId(String orderId);

    Optional<InventoryReservation> findByOrderIdAndSkuCode(
            String orderId,
            String skuCode
    );


}
