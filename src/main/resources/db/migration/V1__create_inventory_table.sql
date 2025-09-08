CREATE TABLE IF NOT EXISTS t_inventory

(
    id       BIGINT NOT NULL AUTO_INCREMENT,
    sku_code VARCHAR(255) DEFAULT NULL,
    quantity INT          DEFAULT 0,
    PRIMARY KEY (id)
);