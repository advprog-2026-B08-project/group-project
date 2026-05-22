CREATE TABLE IF NOT EXISTS stock_decrease_event (
    id UUID PRIMARY KEY,
    request_id UUID NOT NULL UNIQUE,
    catalog_id UUID NOT NULL,
    quantity INTEGER NOT NULL,
    created_at TIMESTAMP NOT NULL
);

CREATE INDEX IF NOT EXISTS idx_stock_decrease_event_catalog_id
    ON stock_decrease_event(catalog_id);
