package com.akul.microservices.inventory.infrastructure.messaging;

import com.akul.microservices.inventory.domain.model.InventoryEvent;
import com.akul.microservices.inventory.infrastructure.persistance.InventoryEventRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * InventoryEventProducer.java
 * Producer for inventory events.
 * - sendEvent → immediate event sending
 * - Scheduled retry → re-send pending events
 *
 * @author Andrii Kulynych
 * @since 2/16/2026
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class InventoryEventProducer {

    private final InventoryEventRepository eventRepository;
    private final KafkaTemplate<String, String> kafkaTemplate;

    /**
     * Sends an event immediately to Kafka.
     * Marks event as PROCESSED if successful or FAILED if error occurs.
     *
     * @param event InventoryEvent to be sent
     */
    public void sendEvent(InventoryEvent event) {
        try {
            kafkaTemplate.send("inventory-events", event.getSkuCode(), event.getPayload());
            event.markProcessed();
            eventRepository.save(event);
            log.info("Event sent successfully, id={}", event.getId());
        } catch (Exception ex) {
            log.error("Failed to send event id={}", event.getId(), ex);
            event.markFailed();
            eventRepository.save(event);
        }
    }

    /**
     * Scheduled task to retry sending PENDING events.
     * Runs every 5 seconds and re-sends events not yet processed.
     */
    @Scheduled(fixedDelay = 5000)
    public void publishPendingEvents() {
        List<InventoryEvent> events = eventRepository.findByStatus(InventoryEvent.EventStatus.PENDING);
        for (InventoryEvent event : events) {
            try {
                kafkaTemplate.send("inventory-events", event.getSkuCode(), event.getPayload());
                event.markProcessed();
                eventRepository.save(event);
                log.info("Scheduled retry: Event sent successfully, id={}", event.getId());
            } catch (Exception ex) {
                log.error("Scheduled retry failed for event id={}", event.getId(), ex);
                event.markFailed();
                eventRepository.save(event);
            }
        }
    }
}
