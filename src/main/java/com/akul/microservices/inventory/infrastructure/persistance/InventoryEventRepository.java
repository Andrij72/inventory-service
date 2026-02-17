
package com.akul.microservices.inventory.infrastructure.persistance;


import com.akul.microservices.inventory.domain.model.InventoryEvent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
/**
 * InventoryEventRepository.java.
 *
 * @author Andrii Kulynych
 * @since 2/14/2026
 */

@Repository
public interface InventoryEventRepository extends JpaRepository<InventoryEvent, Long> {

      List<InventoryEvent> findByStatus(InventoryEvent.EventStatus  status);
}
