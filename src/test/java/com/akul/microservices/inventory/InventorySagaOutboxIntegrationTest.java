package com.akul.microservices.inventory;

import com.akul.microservices.inventory.aplication.service.InventoryOutboxSagaHandler;
import com.akul.microservices.inventory.domain.model.Inventory;
import com.akul.microservices.inventory.infrastructure.outbox.InventoryOutbox;
import com.akul.microservices.inventory.infrastructure.outbox.InventoryOutboxRepository;
import com.akul.microservices.inventory.infrastructure.persistance.InventoryRepository;
import com.akul.microservices.order.event.OrderItem;
import com.akul.microservices.order.event.OrderPlacedEvent;
import com.akul.microservices.order.event.OrderStatus;
import org.awaitility.Awaitility;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.KafkaContainer;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;

@ActiveProfiles("test")
@SpringBootTest(
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        properties = {
                "spring.flyway.enabled=false",
                "spring.cloud.discovery.enabled=false",
                "eureka.client.enabled=false",
                "spring.kafka.producer.key-serializer=org.apache.kafka.common.serialization.StringSerializer",
                "spring.kafka.producer.value-serializer=org.springframework.kafka.support.serializer.JsonSerializer",
                "spring.kafka.consumer.key-deserializer=org.apache.kafka.common.serialization.StringDeserializer",
                "spring.kafka.consumer.value-deserializer=org.springframework.kafka.support.serializer.JsonDeserializer",
                "spring.kafka.consumer.properties.spring.json.trusted.packages=com.akul.microservices.**"
        }
)
@Testcontainers
@Import(KafkaTestConfig.class)
class InventorySagaOutboxIntegrationTest {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:14")
            .withDatabaseName("testdb")
            .withUsername("test")
            .withPassword("test");

    @Container
    static KafkaContainer kafka = new KafkaContainer(DockerImageName.parse("confluentinc/cp-kafka:7.6.1"));

    @DynamicPropertySource
    static void properties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
        registry.add("spring.kafka.bootstrap-servers", kafka::getBootstrapServers);
    }

    @Autowired
    private KafkaTemplate<String, OrderPlacedEvent> kafkaTemplate;

    @Autowired
    private InventoryRepository inventoryRepository;

    @Autowired
    private InventoryOutboxSagaHandler inventoryOutboxSagaHandler;


    @Autowired
    private InventoryOutboxRepository inventoryOutboxRepository;

    private Inventory testInventory;

    @BeforeEach
    void setup() {
        inventoryOutboxRepository.deleteAll();
        inventoryRepository.deleteAll();
        testInventory = Inventory.createNew("SKU1", "Test Product", 10);
        inventoryRepository.save(testInventory);
        inventoryRepository.flush();
    }

    @Test
    void shouldReserveInventory_andPublishConfirmedEvent_directly() {
        OrderPlacedEvent orderEvent = OrderPlacedEvent.newBuilder()
                .setOrderNbr("ORDER-1")
                .setEmail("test@example.com")
                .setFirstName("Andr")
                .setLastName("Kul")
                .setStatus(OrderStatus.PENDING)
                .setCreatedAt(Instant.now())
                .setItems(List.of(new OrderItem("SKU1", "100", 3, "Test Product")))
                .build();
        inventoryOutboxSagaHandler.reserveOrderFromEvent(orderEvent);
        Awaitility.await()
                .atMost(5, TimeUnit.SECONDS)
                .pollInterval(Duration.ofMillis(100))
                .untilAsserted(() -> {
                    List<InventoryOutbox> outboxes = inventoryOutboxRepository.findAll();
                    System.out.println(">>> Current Outbox Records:");
                    outboxes.forEach(System.out::println);

                    assertThat(outboxes)
                            .anySatisfy(outbox -> assertThat(outbox.getEventType().name())
                                    .isEqualTo("INVENTORY_CONFIRMED"));
                });

        Inventory updated = inventoryRepository.findById("SKU1").orElseThrow();
        System.out.println(">>> Current Inventory: " + updated);
        assertThat(updated.getAvailableQuantity()).isEqualTo(7);
    }
}
