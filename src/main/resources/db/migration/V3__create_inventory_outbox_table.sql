CREATE TABLE inventory_outbox
(
    id            BIGSERIAL PRIMARY KEY,

    sku_code      VARCHAR(100)             NOT NULL,
    event_type    VARCHAR(50)              NOT NULL,

    payload       JSONB                    NOT NULL,

    status        VARCHAR(20)              NOT NULL,

    retry_count   INT                               DEFAULT 0,

    version       BIGINT                            DEFAULT 0,

    next_retry_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),

    created_at    TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    processed_at  TIMESTAMP WITH TIME ZONE
);

CREATE INDEX idx_inventory_outbox_status_retry
    ON inventory_outbox (status, next_retry_at);

CREATE INDEX idx_inventory_outbox_created_at
    ON inventory_outbox (created_at);
