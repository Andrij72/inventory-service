package com.akul.microservices.inventory;

import com.akul.microservices.inventory.model.Inventory;
import com.akul.microservices.inventory.repository.InventoryRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@Import(TestcontainersConfiguration.class)
//@ActiveProfiles("test")
@AutoConfigureMockMvc
class TestInventoryServiceApplication {

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private InventoryRepository inventoryRepository;

	@BeforeEach
	void setup() {
		inventoryRepository.deleteAll();
		inventoryRepository.save(new Inventory(null,"iphone_15", 20));
		inventoryRepository.save(new Inventory(null,"ps5", 5));
	}

	@Test
	void shouldReturnTrueWhenEnoughQuantityAvailable() throws Exception {
		mockMvc.perform(get("/api/v1/inventory")
						.param("skuCode", "iphone_15")
						.param("quantity", "10"))
				.andExpect(status().isOk())
				.andExpect(content().string("true"));
	}

	@Test
	void shouldReturnFalseWhenNotEnoughQuantity() throws Exception {
		mockMvc.perform(get("/api/v1/inventory")
						.param("skuCode", "ps5")
						.param("quantity", "10"))
				.andExpect(status().isOk())
				.andExpect(content().string("false"));
	}
}
