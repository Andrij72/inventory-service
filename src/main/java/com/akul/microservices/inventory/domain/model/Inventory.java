package com.akul.microservices.inventory.domain.model;

import com.akul.microservices.inventory.common.exceptions.InsufficientStockException;
import com.akul.microservices.inventory.common.exceptions.InvalidReservationStateException;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "inventory")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PROTECTED)
public class Inventory {

    @Id
    @Column(name = "sku_code")
    private String skuCode;

    @Column(name = "available_quantity", nullable = false)
    private int availableQuantity;

    @Column(name = "reserved_quantity", nullable = false)
    private int reservedQuantity;

    @Version
    private long version;

    public Inventory(String skuCode, int initialQuantity) {
        this.skuCode = skuCode;
        this.availableQuantity = initialQuantity;
        this.reservedQuantity = 0;
    }

    /** Reserve stock for an order. Throws if not enough available. */
    public void reserve(int quantity) {
        if (quantity <= 0) {
            throw new InvalidReservationStateException("Cannot reserve non-positive quantity");
        }
        if (quantity > availableQuantity) {
            throw new InsufficientStockException(skuCode);
        }
        availableQuantity -= quantity;
        reservedQuantity += quantity;
    }

    /** Release reserved stock back to available. */
    public void release(int quantity) {
        validateQuantity(quantity);
        if (quantity > reservedQuantity) {
            throw new InvalidReservationStateException(
                    "Cannot release " + quantity + " from reserved " + reservedQuantity
            );
        }
        reservedQuantity -= quantity;
        availableQuantity += quantity;
    }

    /** Confirm reserved stock (reduce reserved). */
    public void confirm(int quantity) {
        validateQuantity(quantity);
        if (quantity > reservedQuantity) {
            throw new InvalidReservationStateException(
                    "Cannot confirm " + quantity + " from reserved " + reservedQuantity
            );
        }
        reservedQuantity -= quantity;
    }

    /** Common validation for quantity */
    private void validateQuantity(int quantity) {
        if (quantity <= 0) {
            throw new InvalidReservationStateException("Quantity must be positive");
        }
    }

    /** Check if enough stock is available for reservation */
    public boolean canReserve(int quantity) {
        return quantity > 0 && quantity <= availableQuantity;
    }
}
