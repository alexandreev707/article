CREATE TABLE sync_log (
    id BIGSERIAL PRIMARY KEY,
    order_id BIGINT REFERENCES orders(id),
    action VARCHAR(50) NOT NULL,
    source VARCHAR(20) NOT NULL,
    details JSONB,
    created_at TIMESTAMPTZ DEFAULT NOW()
);

CREATE INDEX idx_sync_log_order ON sync_log(order_id);
CREATE INDEX idx_sync_log_action ON sync_log(action);
