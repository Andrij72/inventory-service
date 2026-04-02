package com.akul.microservices.inventory.web.controller;


import com.akul.microservices.inventory.aplication.dto.CheckStockResponse;
import com.akul.microservices.inventory.aplication.dto.InventoryItemDto;
import com.akul.microservices.inventory.aplication.service.AdminInventoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * AdminInventoryController.java.
 *
 * REST CRUD controller for Inventory management.
 *
 * @author Andrii Kulynych
 * @since 3/16/2026
 */
@RestController
@RequestMapping("/api/v1/admin/inventory")
@RequiredArgsConstructor
public class AdminInventoryController {

    private final AdminInventoryService adminInventoryService;

    // ============================ GET ALL ============================
    @GetMapping
    public ResponseEntity<List<InventoryItemDto>> getAll() {
        return ResponseEntity.ok(adminInventoryService.getAllItems());
    }

    // ============================ GET ONE ============================
    @GetMapping("/{skuCode}")
    public ResponseEntity<InventoryItemDto> getById(@PathVariable String skuCode) {
        InventoryItemDto found = adminInventoryService.getItem(skuCode);

        return ResponseEntity.ok(found);
    }

    // ============================ CREATE ============================
    @PostMapping
    public ResponseEntity<InventoryItemDto> create(@RequestBody InventoryItemDto dto) {
        InventoryItemDto created = adminInventoryService.createInventoryItem(dto);
        return ResponseEntity.ok(created);
    }

    // ============================ UPDATE ============================
    @PutMapping("/{skuCode}")
    public ResponseEntity<InventoryItemDto> update(
            @PathVariable String skuCode,
            @RequestBody InventoryItemDto dto) {

        InventoryItemDto updated = adminInventoryService.updateItem(skuCode, dto);
        return ResponseEntity.ok(updated);
    }

    // ============================ DELETE ============================
    @DeleteMapping("/{skuCode}")
    public ResponseEntity<Void> delete(@PathVariable String skuCode) {
        adminInventoryService.deleteItem(skuCode);
        return ResponseEntity.noContent().build();
    }

    //============================Check stock availability==================
    @GetMapping("/check")
    public ResponseEntity<CheckStockResponse> checkStock(@RequestParam String skuCode,
                                                         @RequestParam int quantity) {
        boolean available = adminInventoryService.isProductInStock(skuCode, quantity);
        CheckStockResponse response = new CheckStockResponse(skuCode, quantity, available);
        return ResponseEntity.ok(response);
    }
}
