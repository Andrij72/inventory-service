package com.akul.microservices.inventory;

import com.akul.microservices.inventory.aplication.service.InventoryOutboxSagaHandler;
import com.akul.microservices.inventory.domain.model.Inventory;
import com.akul.microservices.inventory.domain.model.InventoryReservation;
import com.akul.microservices.inventory.infrastructure.outbox.InventoryEventType;
import com.akul.microservices.inventory.infrastructure.outbox.InventoryOutbox;
import com.akul.microservices.inventory.infrastructure.outbox.InventoryOutboxRepository;
import com.akul.microservices.inventory.infrastructure.persistence.InventoryRepository;
import com.akul.microservices.inventory.infrastructure.persistence.InventoryReservationRepository;
import com.akul.microservices.order.event.OrderItem;
import com.akul.microservices.order.event.OrderPlacedEvent;
import com.akul.microservices.order.event.OrderStatus;
import org.awaitility.Awaitility;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
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
                "spring.kafka.consumer.properties.spring.json.trusted.packages=com.akul.microservices.**",
                "spring.task.scheduling.enabled=false"
        })
@Testcontainers
class InventorySagaOutboxIntegrationTest {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:14")
            .withDatabaseName("testdb")
            .withUsername("test")
            .withPassword("test");

    @Container
    static KafkaContainer kafka = new KafkaContainer(DockerImageName.parse("confluentinc/cp-kafka:7.6.1"));
    @Autowired
    private InventoryReservationRepository inventoryReservationRepository;

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
    private InventoryReservationRepository reservationRepository;

    @Autowired
    private InventoryOutboxSagaHandler inventoryOutboxSagaHandler;

    @Autowired
    private InventoryOutboxRepository inventoryOutboxRepository;

    @BeforeEach
    void setup() {
        reservationRepository.deleteAll();
        inventoryOutboxRepository.deleteAll();
        inventoryRepository.deleteAll();
        Inventory testInventory = Inventory.createNew("SKU1", "Test Product", 10);
        inventoryRepository.save(testInventory);
        inventoryRepository.flush();
    }

    @Test
    void shouldReserveInventory_andCreateReservation_andPublishEvent() {
        OrderPlacedEvent orderEvent = OrderPlacedEvent.newBuilder()
                .setOrderNbr("ORDER-1")
                .setEmail("test@example.com")
                .setFirstName("A")
                .setLastName("K")
                .setStatus(OrderStatus.PENDING)
                .setCreatedAt(Instant.now())
                .setItems(List.of(new OrderItem("SKU1", "100", 10, "Test Product")))
                .build();
        inventoryOutboxSagaHandler.reserveOrderFromEvent(orderEvent);
        Awaitility.await()
                .atMost(Duration.ofSeconds(5))
                .untilAsserted(() -> {
                    var reservations = inventoryReservationRepository.findByOrderId("ORDER-1");
                    assertThat(reservations).hasSize(1);
                    assertThat(reservations.get(0).getStatus().name()).isEqualTo("PENDING");

                    var outboxes = inventoryOutboxRepository.findAll();
                    assertThat(outboxes)
                            .anyMatch(o -> o.getEventType() == InventoryEventType.INVENTORY_CONFIRMED);

                    var inventory = inventoryRepository.findById("SKU1").orElseThrow();
                    assertThat(inventory.getAvailableQuantity()).isEqualTo(0); // 10 - 10
                });
    }


    @Test
    void shouldRejectInventory_whenNotEnoughStock() {
        OrderPlacedEvent orderEvent = OrderPlacedEvent.newBuilder()
                .setOrderNbr("ORDER-2")
                .setEmail("test@example.com")
                .setFirstName("A")
                .setLastName("K")
                .setStatus(OrderStatus.PENDING)
                .setCreatedAt(Instant.now())
                .setItems(List.of(new OrderItem("SKU1", "100", 20, "Test Product")))
                .build();
        inventoryOutboxSagaHandler.reserveOrderFromEvent(orderEvent);
        Awaitility.await()
                .atMost(5, TimeUnit.SECONDS)
                .untilAsserted(() -> {
                    List<InventoryOutbox> outboxes = inventoryOutboxRepository.findAll();
                    assertThat(outboxes)
                            .anyMatch(o -> o.getEventType() == InventoryEventType.INVENTORY_REJECTED);
                });
        Inventory inventory = inventoryRepository.findById("SKU1").orElseThrow();
        assertThat(inventory.getAvailableQuantity()).isEqualTo(10);
    }


    @Test
    void shouldExpireReservation_afterTTL_andHandleMultiSkuOrder() {

        Inventory sku1 = Inventory.createNew("SKU2", "Product 1", 5);
        Inventory sku2 = Inventory.createNew("SKU3", "Product 2", 3);
        inventoryRepository.save(sku1);
        inventoryRepository.save(sku2);
        inventoryRepository.flush();
        OrderPlacedEvent orderEvent = OrderPlacedEvent.newBuilder()
                .setOrderNbr("ORDER-MULTI")
                .setEmail("test@example.com")
                .setFirstName("A")
                .setLastName("K")
                .setStatus(OrderStatus.PENDING)
                .setCreatedAt(Instant.now())
                .setItems(List.of(
                        new OrderItem("SKU2", "100", 5, "Product 1"),
                        new OrderItem("SKU3", "101", 3, "Product 2")
                ))
                .build();
        inventoryOutboxSagaHandler.reserveOrderFromEvent(orderEvent);
        inventoryOutboxSagaHandler.releaseExpiredReservations();
        List<InventoryReservation> reservations = reservationRepository.findByOrderId("ORDER-MULTI");
        assertThat(reservations).hasSize(2);
        for (InventoryReservation r : reservations) {
            assertThat(r.getStatus())
                    .isEqualTo(InventoryReservation.ReservationStatus.EXPIRED);
        }
        Inventory inv1 = inventoryRepository.findById("SKU2").orElseThrow();
        Inventory inv2 = inventoryRepository.findById("SKU3").orElseThrow();
        assertThat(inv1.getAvailableQuantity()).isEqualTo(5);
        assertThat(inv2.getAvailableQuantity()).isEqualTo(3);
        List<InventoryOutbox> outboxes = inventoryOutboxRepository.findAll();
        assertThat(outboxes)
                .anyMatch(o -> o.getEventType() == InventoryEventType.INVENTORY_EXPIRED
                               && o.getSkuCode().equals("SKU2"))
                .anyMatch(o -> o.getEventType() == InventoryEventType.INVENTORY_EXPIRED
                               && o.getSkuCode().equals("SKU3"));
    }
}
