package com.akul.microservices.inventory.domain.model;

import com.akul.microservices.inventory.common.exceptions.InvalidQuantityException;
import com.akul.microservices.inventory.common.exceptions.InsufficientStockException;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
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

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "available_quantity", nullable = false)
    private int availableQuantity;

    @Column(name = "reserved_quantity", nullable = false)
    private int reservedQuantity;

    @Version
    private long version;

    protected Inventory(String skuCode, String name, int initialQuantity) {
        this.skuCode = skuCode;
        this.name = name;
        this.availableQuantity = initialQuantity;
        this.reservedQuantity = 0;
    }

    public static Inventory createNew(String skuCode, String name, int initialQuantity) {
        return new Inventory(skuCode, name, initialQuantity);
    }

    public void updateName(String newName) {
        if (newName == null || newName.isBlank()) {
            throw new IllegalArgumentException("Name cannot be empty");
        }
        this.name = newName;
    }

    public void updateStock(int newQuantity) {
        if (newQuantity < reservedQuantity) {
            throw new InvalidQuantityException(
                    "Cannot set availableQuantity less than reservedQuantity"
            );
        }
        this.availableQuantity = newQuantity;
    }

    /** Reserve stock for an order */
    public void reserve(int quantity) {
        ensurePositive(quantity);
        if (quantity > availableQuantity) {
            throw new InsufficientStockException(skuCode, quantity, availableQuantity);
        }
        availableQuantity -= quantity;
        reservedQuantity += quantity;
    }

    /** Release reserved stock back to available */
    public void release(int quantity) {
        ensurePositive(quantity);
        if (quantity > reservedQuantity) {
            throw new InvalidQuantityException(
                    "Cannot release %d from reserved %d".formatted(quantity, reservedQuantity)
            );
        }
        reservedQuantity -= quantity;
        availableQuantity += quantity;
    }

    /** Confirm reserved stock (reduce reserved) */
    public void confirm(int quantity) {
        ensurePositive(quantity);
        if (quantity > reservedQuantity) {
            throw new InvalidQuantityException(
                    "Cannot confirm %d from reserved %d".formatted(quantity, reservedQuantity)
            );
        }
        reservedQuantity -= quantity;
    }

    /** Check if enough stock is available */
    public boolean canReserve(int quantity) {
        return quantity > 0 && quantity <= availableQuantity;
    }

    /** Validate positive quantity */
    private void ensurePositive(int quantity) {
        if (quantity <= 0) {
            throw new InvalidQuantityException("Quantity must be positive");
        }
    }
}
