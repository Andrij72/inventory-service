CREATE TABLE IF NOT EXISTS t_inventory
(
    id       BIGINT AUTO_INCREMENT PRIMARY KEY,
    sku_code VARCHAR(255) NOT NULL,
    quantity INT          NOT NULL
);
