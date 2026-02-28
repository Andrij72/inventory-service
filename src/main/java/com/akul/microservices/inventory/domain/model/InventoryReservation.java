package com.akul.microservices.inventory.domain.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import jakarta.persistence.Version;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;

/**
 * InventoryReservation.java.
 * InventoryReservation Aggregate Root
 *
 * @author Andrii Kulynych
 * @since 2/12/2026
 */

@Entity
@Table(name = "inventory_reservation",
        uniqueConstraints = @UniqueConstraint(
                columnNames = {"order_id", "sku_code"}
        ))
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PROTECTED)
@Builder
public class InventoryReservation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "order_id", nullable = false)
    private String orderId;

    @Column(name = "sku_code", nullable = false)
    private String skuCode;

    @Column(name = "quantity", nullable = false)
    private int quantity;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private ReservationStatus status;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "updated_at")
    private Instant updatedAt;

    @Column(name = "expires_at")
    private Instant expiresAt;

    @Version
    private Long version;

    private static final Clock DEFAULT_CLOCK = Clock.systemUTC();

    public enum ReservationStatus {
        PENDING,
        CONFIRMED,
        CANCELLED,
        EXPIRED
    }

    // =========================
    // Factory
    // =========================

    public static InventoryReservation create(
            String orderId,
            String skuCode,
            int quantity,
            long reservationMinutes
    ) {
        validateQuantity(quantity);

        Instant now = Instant.now(DEFAULT_CLOCK);

        InventoryReservation reservation = new InventoryReservation();
        reservation.orderId = orderId;
        reservation.skuCode = skuCode;
        reservation.quantity = quantity;
        reservation.status = ReservationStatus.PENDING;
        reservation.createdAt = now;
        reservation.expiresAt = now.plus(Duration.ofMinutes(reservationMinutes));

        return reservation;
    }

    // =========================
    // Domain behavior
    // =========================

    public void confirm() {
        validateActiveState();
        validateNotExpired();

        this.status = ReservationStatus.CONFIRMED;
        this.updatedAt = Instant.now(DEFAULT_CLOCK);
    }

    public void cancel() {
        validateActiveState();
        validateNotExpired();

        this.status = ReservationStatus.CANCELLED;
        this.updatedAt = Instant.now(DEFAULT_CLOCK);
    }

    public void expire() {
        if (status != ReservationStatus.PENDING) return;

        if (expiresAt != null &&
            Instant.now(DEFAULT_CLOCK).isAfter(expiresAt)) {

            this.status = ReservationStatus.EXPIRED;
            this.updatedAt = Instant.now(DEFAULT_CLOCK);
        }
    }

    // =========================
    // Validation
    // =========================

    private static void validateQuantity(int quantity) {
        if (quantity <= 0) {
            throw new IllegalArgumentException("Quantity must be greater than zero");
        }
    }

    private void validateActiveState() {
        if (status == ReservationStatus.CANCELLED ||
            status == ReservationStatus.EXPIRED) {
            throw new IllegalStateException(
                    "Cannot modify reservation from terminal state"
            );
        }
    }

    private void validateNotExpired() {
        if (expiresAt != null &&
            Instant.now(DEFAULT_CLOCK).isAfter(expiresAt)) {
            throw new IllegalStateException("Reservation is expired");
        }
    }
}
