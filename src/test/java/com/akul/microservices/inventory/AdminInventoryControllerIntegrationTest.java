package com.akul.microservices.inventory;

import com.akul.microservices.inventory.aplication.dto.InventoryItemDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * AdminInventoryControllerIntegrationTest.java.
 *
 * @author Andrii Kulynych
 * @since 3/16/2026
 */
@ActiveProfiles("test")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        properties = {
                "spring.flyway.enabled=false",
                "spring.cloud.discovery.enabled=false",
                "eureka.client.enabled=false"
        }
)
@Testcontainers
class AdminInventoryControllerIntegrationTest {

    @Autowired
    private TestRestTemplate restTemplate;

    private InventoryItemDto testItem;

    @BeforeEach
    void setup() {
        testItem = new InventoryItemDto("SKU1", "Test Product", 50);
    }

    // ==== Testcontainers PostgreSQL ====
    @Container
    static PostgreSQLContainer<?> postgresContainer = new PostgreSQLContainer<>("postgres:14")
            .withDatabaseName("testdb")
            .withUsername("test")
            .withPassword("test");

    @DynamicPropertySource
    static void registerPgProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgresContainer::getJdbcUrl);
        registry.add("spring.datasource.username", postgresContainer::getUsername);
        registry.add("spring.datasource.password", postgresContainer::getPassword);
    }

    @Test
    void fullCrudFlow() {
        // --------- CREATE ---------
        ResponseEntity<InventoryItemDto> createResp = restTemplate.postForEntity(
                "/api/v1/admin/inventory",
                testItem,
                InventoryItemDto.class
        );
        assertThat(createResp.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(createResp.getBody().getSkuCode()).isEqualTo(testItem.getSkuCode());

        // --------- GET ONE ---------
        ResponseEntity<InventoryItemDto> getResp = restTemplate.getForEntity(
                "/api/v1/admin/inventory/" + testItem.getSkuCode(),
                InventoryItemDto.class
        );
        assertThat(getResp.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(getResp.getBody().getName()).isEqualTo(testItem.getName());

        // --------- UPDATE ---------
        testItem.setName("Updated Product");
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<InventoryItemDto> updateReq = new HttpEntity<>(testItem, headers);
        ResponseEntity<InventoryItemDto> updateResp = restTemplate.exchange(
                "/api/v1/admin/inventory/" + testItem.getSkuCode(),
                HttpMethod.PUT,
                updateReq,
                InventoryItemDto.class
        );
        assertThat(updateResp.getBody().getName()).isEqualTo("Updated Product");

        // --------- GET ALL ---------
        ResponseEntity<InventoryItemDto[]> allResp = restTemplate.getForEntity(
                "/api/v1/admin/inventory",
                InventoryItemDto[].class
        );
        List<InventoryItemDto> allItems = List.of(allResp.getBody());
        assertThat(allItems).extracting("skuCode").contains(testItem.getSkuCode());

        // --------- DELETE ---------
        restTemplate.delete("/api/v1/admin/inventory/" + testItem.getSkuCode());
        ResponseEntity<InventoryItemDto> deletedResp = restTemplate.getForEntity(
                "/api/v1/admin/inventory/" + testItem.getSkuCode(),
                InventoryItemDto.class
        );
        assertThat(deletedResp.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }
}
