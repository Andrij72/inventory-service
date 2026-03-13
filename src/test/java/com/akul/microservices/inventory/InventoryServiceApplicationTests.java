package com.akul.microservices.inventory;


import io.restassured.RestAssured;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.List;
import java.util.Map;

import static io.restassured.RestAssured.given;

@Disabled
@ActiveProfiles("test")
@Testcontainers
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class InventoryServiceApplicationTests {

    @ServiceConnection
    static MySQLContainer<?> mysql = new MySQLContainer<>("mysql:8.3.0")
            .withDatabaseName("inventorydb")
            .withUsername("test")
            .withPassword("test");

    @LocalServerPort
    private Integer port;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @BeforeEach
    void setUp() {
        RestAssured.baseURI = "http://localhost";
        RestAssured.port = port;

        jdbcTemplate.execute("TRUNCATE TABLE t_inventory");


        jdbcTemplate.update("INSERT INTO t_inventory (sku_code, quantity) VALUES (?, ?)", "iphone_14", 10);
        jdbcTemplate.update("INSERT INTO t_inventory (sku_code, quantity) VALUES (?, ?)", "samsung_a90", 0);
        List<Map<String,Object>> tables = jdbcTemplate.queryForList("SHOW TABLES;");
        tables.forEach(System.out::println);
    }

    static {
        mysql.start();
    }

    @Test
    void contextLoads() {
    }

    @Test
    void shouldReturnTrueWhenProductInStock() {
        given()
                .queryParam("sku", "iphone_14")
                .queryParam("quantity", 5)
                .when()
                .get("/api/v1/inventory")
                .then()
                .statusCode(200)
                .body(Matchers.equalTo("true"));
    }

    @Test
    void shouldReturnFalseWhenProductOutOfStock() {
        given()
                .queryParam("sku", "samsung_a90")
                .queryParam("quantity", 15)
                .when()
                .get("/api/v1/inventory")
                .then()
                .statusCode(200)
                .body(Matchers.equalTo("false"));
    }

    @Test
    void shouldReturnFalseWhenProductDoesNotExist() {
        given()
                .queryParam("sku", "non_existing")
                .queryParam("quantity", 5)
                .when()
                .get("/api/v1/inventory")
                .then()
                .statusCode(200)
                .body(Matchers.equalTo("false"));
    }
}
