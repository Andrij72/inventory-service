package com.akul.microservices.inventory.domain.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.Instant;

/**
 * InventoryEvent.java.
 *
 * @author Andrii Kulynych
 * @since 2/12/2026
 */

@Entity
@Table(name = "inventory_event")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class InventoryEvent {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "sku_code", nullable = false)
    private String skuCode;

    @Column(name = "event_type", nullable = false)
    private String eventType;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "payload", columnDefinition = "jsonb", nullable = false)
    private String payload;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private EventStatus status;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "processed_at")
    private Instant processedAt;

    public enum EventStatus {
        PENDING,
        PROCESSED,
        FAILED
    }

    public static InventoryEvent create(String skuCode, String eventType, String payload) {
        InventoryEvent event = new InventoryEvent();
        event.skuCode = skuCode;
        event.eventType = eventType;
        event.payload = payload;
        event.status = EventStatus.PENDING;
        event.createdAt = Instant.now();
        return event;
    }

    public void markProcessed() {
        this.status = EventStatus.PROCESSED;
        this.processedAt = Instant.now();
    }

    public void markFailed() {
        this.status = EventStatus.FAILED;
    }
}
