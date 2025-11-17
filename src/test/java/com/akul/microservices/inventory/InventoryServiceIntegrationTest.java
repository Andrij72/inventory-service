package com.akul.microservices.inventory;

import com.akul.microservices.inventory.model.Inventory;
import com.akul.microservices.inventory.repository.InventoryRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ActiveProfiles("test")
@Testcontainers
@SpringBootTest
@AutoConfigureMockMvc
class InventoryServiceIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private InventoryRepository inventoryRepository;

    @ServiceConnection
    static MySQLContainer<?> mysql = new MySQLContainer<>("mysql:8.3.0")
            .withDatabaseName("testdb")
            .withUsername("test")
            .withPassword("test");

    @BeforeEach
    void setup() {
        inventoryRepository.deleteAll();
        inventoryRepository.save(new Inventory(null, "iphone_15", 20));
        inventoryRepository.save(new Inventory(null, "ps5", 5));
    }

    @Test
    void shouldReturnTrueWhenEnoughQuantityAvailable() throws Exception {
        mockMvc.perform(get("/api/v1/inventory")
                        .param("sku", "iphone_15")
                        .param("quantity", "10"))
                .andExpect(status().isOk())
                .andExpect(content().string("true"));
    }

    @Test
    void shouldReturnFalseWhenNotEnoughQuantity() throws Exception {
        mockMvc.perform(get("/api/v1/inventory")
                        .param("sku", "ps5")
                        .param("quantity", "10"))
                .andExpect(status().isOk())
                .andExpect(content().string("false"));
    }
}
