package com.akul.microservices.inventory.aplication.service;

import com.akul.microservices.inventory.aplication.dto.InventoryItemDto;
import com.akul.microservices.inventory.common.exceptions.InventoryNotFoundException;
import com.akul.microservices.inventory.domain.model.Inventory;
import com.akul.microservices.inventory.infrastructure.persistence.InventoryRepository;
import lombok.AllArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

/**
 * AdminInventoryService.java.
 *
 * @author Andrii Kulynych
 * @since 3/16/2026
 */
@Service
@AllArgsConstructor
public class AdminInventoryService {

    private static final Logger log = LoggerFactory.getLogger(AdminInventoryService.class);

    private final InventoryRepository inventoryRepository;

    public List<InventoryItemDto> getAllItems() {
        return inventoryRepository.findAll().stream()
                .map(i->new InventoryItemDto(i.getSkuCode(),i.getName(), i.getAvailableQuantity()))
                .collect(Collectors.toUnmodifiableList());

    }

    public  InventoryItemDto getItem(String skuCode){
        Inventory inventory = inventoryRepository.findById(skuCode)
                .orElseThrow(() -> new InventoryNotFoundException(skuCode));

        return new InventoryItemDto(inventory.getSkuCode(), inventory.getName(), inventory.getAvailableQuantity());
    }

    public InventoryItemDto createInventoryItem(InventoryItemDto dto) {
        Inventory inventory = Inventory.createNew(dto.getSkuCode(), dto.getName(), dto.getAvailableQuantity());
        Inventory saved = inventoryRepository.save(inventory);
        log.info("Inventory item created: sku={}", inventory.getSkuCode());

        return new InventoryItemDto(
                saved.getSkuCode(),
                saved.getName(),
                saved.getAvailableQuantity()
        );
    }

    public InventoryItemDto updateItem(String skuCode, InventoryItemDto dto) {
        Inventory inventory = inventoryRepository.findById(skuCode)
                .orElseThrow(() -> new InventoryNotFoundException(skuCode));
        inventory.updateName(dto.getName());
        inventory.updateStock(dto.getAvailableQuantity());
        inventoryRepository.save(inventory);
        log.info("Inventory item updated: sku={}", inventory.getSkuCode());

        return new InventoryItemDto(
                inventory.getSkuCode(),
                inventory.getName(),
                inventory.getAvailableQuantity());
    }

    public void deleteItem(String skuCode) {
       Inventory inventory = inventoryRepository.findById(skuCode)
                .orElseThrow(() -> new InventoryNotFoundException(skuCode));
        inventoryRepository.delete(inventory);
        log.info("Deleted Item with sku: {}", inventory.getSkuCode());
    }

    public boolean isProductInStock(String skuCode, int quantity) {
        Inventory inventory = inventoryRepository.findById(skuCode)
                .orElseThrow(() -> new InventoryNotFoundException(skuCode));
        return inventory.getAvailableQuantity() >= quantity;
    }
}
