package com.akul.microservices.inventory.controller;

import com.akul.microservices.inventory.service.InventoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * InventoryController.java
 *
 * @author Andrii Kulynch
 * @version 1.0
 * @since 8/27/2025
 */
@RestController
@RequestMapping("/api/v1/inventory")
@RequiredArgsConstructor
public class InventoryController {
    private final InventoryService inventoryService;

    @GetMapping
    public boolean isProductAvailable(@RequestParam("skuCode") String skuCode, @RequestParam("quantity") Integer quantity) {
        return inventoryService.isProductInStock(skuCode, quantity);
    }
}
