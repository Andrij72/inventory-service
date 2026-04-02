CREATE TABLE IF NOT EXISTS inventory (
                                         sku_code VARCHAR(255) PRIMARY KEY,
                                         name VARCHAR(255) NOT NULL,
                                         available_quantity INT NOT NULL DEFAULT 0,
                                         reserved_quantity INT NOT NULL DEFAULT 0,
                                         version BIGINT NOT NULL DEFAULT 0
);

CREATE TABLE IF NOT EXISTS inventory_reservation (
                                                     id BIGSERIAL PRIMARY KEY,
                                                     order_id VARCHAR(255) NOT NULL,
                                                     sku_code VARCHAR(255) NOT NULL,
                                                     quantity INT NOT NULL,
                                                     status VARCHAR(50) NOT NULL,
                                                     created_at TIMESTAMP NOT NULL DEFAULT NOW(),
                                                     expires_at TIMESTAMP,
                                                     updated_at TIMESTAMP,
                                                     version    BIGINT NOT NULL DEFAULT 0,

                                                     CONSTRAINT fk_reservation_inventory
                                                         FOREIGN KEY (sku_code)
                                                             REFERENCES inventory(sku_code)
                                                             ON DELETE CASCADE
);

CREATE TABLE inventory_outbox
(
    id            BIGSERIAL PRIMARY KEY,
    aggregate_id  VARCHAR(100)             NOT NULL,
    sku_code      VARCHAR(100)             NOT NULL,
    event_type    VARCHAR(50)              NOT NULL,
    payload       bytea                   NOT NULL,
    status        VARCHAR(20)              NOT NULL,
    retry_count   INT                               DEFAULT 0,
    version       BIGINT                            DEFAULT 0,
    next_retry_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    created_at    TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    processed_at  TIMESTAMP WITH TIME ZONE
);

CREATE INDEX IF NOT EXISTS idx_reservation_order_id
    ON inventory_reservation(order_id);

CREATE UNIQUE INDEX IF NOT EXISTS uq_reservation_order_sku
    ON inventory_reservation(order_id, sku_code);

CREATE INDEX idx_inventory_outbox_status_retry
    ON inventory_outbox (status, next_retry_at);

CREATE INDEX idx_inventory_outbox_created_at
    ON inventory_outbox (created_at);

CREATE INDEX idx_inventory_outbox_aggregate
    ON inventory_outbox (aggregate_id);

CREATE INDEX idx_inventory_outbox_ordering
    ON inventory_outbox (aggregate_id, created_at);
