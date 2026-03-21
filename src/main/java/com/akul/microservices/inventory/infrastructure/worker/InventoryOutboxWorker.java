package com.akul.microservices.inventory.infrastructure.worker;


import com.akul.microservices.inventory.event.InventoryEvent;
import com.akul.microservices.inventory.infrastructure.messaging.InventoryTopicResolver;
import com.akul.microservices.inventory.infrastructure.outbox.InventoryOutbox;
import com.akul.microservices.inventory.infrastructure.outbox.InventoryOutboxRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.nio.ByteBuffer;

import java.util.List;

/**
 * InventoryOutboxWorker.java.
 *
 * @author Andrii Kulynych
 * @since 2/28/2026
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class InventoryOutboxWorker {

    private static final int BATCH_SIZE = 100;
    private final InventoryOutboxRepository repository;
    private final KafkaTemplate<String, InventoryEvent> kafkaTemplate;
    private final InventoryTopicResolver topicResolver;

    @Scheduled(fixedDelayString = "${outbox.worker.delay:5000}")
    public void process() {
        List<InventoryOutbox> events =
                repository.findBatchForProcessing(BATCH_SIZE);
        for (InventoryOutbox event : events) {
            processSingle(event);
        }
    }

    public void processSingle(InventoryOutbox event) {
        String topic = topicResolver.resolveTopic(event.getEventType());
        try {
            log.info("Publishing inventory event: id={}, sku={}, type={}, topic={}",
                    event.getId(), event.getSkuCode(), event.getEventType(), topic);
            ByteBuffer buffer = ByteBuffer.wrap(event.getPayload());
            InventoryEvent avroEvent = InventoryEvent.fromByteBuffer(buffer);
            var result = kafkaTemplate.send(topic, event.getSkuCode(), avroEvent).get();
            event.markProcessed();
            repository.save(event);
            log.info("Kafka ACK received: topic={}, partition={}, offset={}, sku={}",
                    result.getRecordMetadata().topic(),
                    result.getRecordMetadata().partition(),
                    result.getRecordMetadata().offset(),
                    event.getSkuCode());
        } catch (Exception ex) {
            log.error("Inventory outbox publish failed: id={}, sku={}, type={}",
                    event.getId(), event.getSkuCode(), event.getEventType(), ex);
            event.markFailed();
            repository.save(event);
        }
    }
}