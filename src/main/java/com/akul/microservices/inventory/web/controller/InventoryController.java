package com.akul.microservices.inventory.web.controller;

import com.akul.microservices.inventory.aplication.dto.CheckStockResponse;
import com.akul.microservices.inventory.aplication.dto.ReserveInventoryRequest;
import com.akul.microservices.inventory.aplication.dto.ReserveInventoryResponse;
import com.akul.microservices.inventory.aplication.service.InventoryService;
import com.akul.microservices.inventory.domain.model.InventoryReservation;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

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

    // ============================
    // Reserve stock
    // ============================
    @PostMapping("/reserve")
    public ResponseEntity<List<ReserveInventoryResponse>> reserve(@RequestBody ReserveInventoryRequest request) {
        List<ReserveInventoryResponse> responses = inventoryService.reserveOrder(request);
        return ResponseEntity.ok(responses);
    }

    // ============================
    // Confirm reservation
    // ============================
    @PostMapping("/confirm")
    public ResponseEntity<String> confirm(@RequestParam String orderId) {
        inventoryService.confirmReservation(orderId);
        return ResponseEntity.ok("Reservation confirmed for order: " + orderId);
    }

    // ============================
    // Cancel reservation
    // ============================
    @PostMapping("/cancel")
    public ResponseEntity<String> cancel(@RequestParam String orderId) {
        inventoryService.cancelReservation(orderId);
        return ResponseEntity.ok("Reservation cancelled for order: " + orderId);
    }

    // ============================
    // Check stock availability
    // ============================
    @GetMapping("/check")
    public ResponseEntity<CheckStockResponse> checkStock(@RequestParam String skuCode,
                                                         @RequestParam int quantity) {
        boolean available = inventoryService.isProductInStock(skuCode, quantity);
        CheckStockResponse response = new CheckStockResponse(skuCode, quantity, available);
        return ResponseEntity.ok(response);
    }
}
