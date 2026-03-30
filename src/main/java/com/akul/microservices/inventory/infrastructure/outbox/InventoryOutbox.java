package com.akul.microservices.inventory.infrastructure.outbox;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Instant;

/**
 * InventoryOutbox.java.
 *
 * @author Andrii Kulynych
 * @since 2/27/2026
 */
@Entity
@Table(name = "inventory_outbox")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class InventoryOutbox {

    private static final int MAX_RETRY = 5;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String aggregateId; // orderId
    private String skuCode;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private InventoryEventType eventType;

    @Column(columnDefinition = "bytea")
    private byte[] payload;

    @Enumerated(EnumType.STRING)
    private Status status;

    private int retryCount;
    private Instant createdAt;
    private Instant processedAt;
    private Instant nextRetryAt;

    @Version
    private Long version;

    private InventoryOutbox(String aggregateId, String skuCode,
                            InventoryEventType eventType, byte[] payload) {

        this.aggregateId = aggregateId;
        this.skuCode = skuCode;
        this.eventType = eventType;
        this.payload = payload;

        this.status = Status.PENDING;
        this.retryCount = 0;
        this.createdAt = Instant.now();
        this.nextRetryAt = Instant.now();
    }

    public static InventoryOutbox create(String aggregateId, String skuCode,
                                         InventoryEventType eventType, byte[] payload) {
        return new InventoryOutbox(aggregateId, skuCode, eventType, payload);
    }

    // =========================
    // State transitions
    // =========================
    public void markProcessing() {
        validatePersisted();
        validateMutable();
        this.status = Status.PROCESSING;
    }

    public void markProcessed() {
        validatePersisted();
        validateMutable();
        this.status = Status.PROCESSED;
        this.processedAt = Instant.now();
    }

    public void markFailed() {
        validatePersisted();
        validateMutable();
        this.retryCount++;
        if (retryCount >= MAX_RETRY) {
            this.status = Status.FAILED;
            return;
        }
        this.status = Status.PENDING;
        long backoff = Math.min(30L * (1L << retryCount), 300);
        this.nextRetryAt = Instant.now().plusSeconds(backoff);
    }

    private void validatePersisted() {
        if (id == null) throw new IllegalStateException("Entity is not persisted yet");
    }

    private void validateMutable() {
        if (status == Status.PROCESSED || status == Status.FAILED)
            throw new IllegalStateException("Terminal state reached");
    }

    public enum Status {
        PENDING,
        PROCESSING,
        PROCESSED,
        FAILED
    }
}
