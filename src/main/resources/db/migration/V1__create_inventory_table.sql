CREATE TABLE IF NOT EXISTS inventory (
                                         sku_code VARCHAR(255) PRIMARY KEY,
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
                                                     updated_at TIMESTAMP,

                                                     CONSTRAINT fk_reservation_inventory
                                                         FOREIGN KEY (sku_code)
                                                             REFERENCES inventory(sku_code)
                                                             ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS inventory_event (
                                               id BIGSERIAL PRIMARY KEY,
                                               sku_code VARCHAR(255) NOT NULL,
                                               event_type VARCHAR(50) NOT NULL,
                                               payload JSONB NOT NULL,
                                               status VARCHAR(50) NOT NULL,
                                               processed BOOLEAN NOT NULL DEFAULT FALSE,
                                               created_at TIMESTAMP NOT NULL DEFAULT NOW(),
                                               processed_at TIMESTAMP,

                                               CONSTRAINT fk_event_inventory
                                                   FOREIGN KEY (sku_code)
                                                       REFERENCES inventory(sku_code)
                                                       ON DELETE CASCADE
);



CREATE INDEX IF NOT EXISTS idx_reservation_order_id
    ON inventory_reservation(order_id);


CREATE UNIQUE INDEX IF NOT EXISTS uq_reservation_order_sku
    ON inventory_reservation(order_id, sku_code);


CREATE INDEX IF NOT EXISTS idx_event_processed
    ON inventory_event(processed);

CREATE INDEX IF NOT EXISTS idx_event_sku
    ON inventory_event(sku_code);


CREATE INDEX IF NOT EXISTS idx_event_payload
    ON inventory_event
        USING GIN (payload);

ALTER TABLE inventory_event ADD CONSTRAINT chk_event_status
    CHECK (status IN ('PENDING','PROCESSED','FAILED'));