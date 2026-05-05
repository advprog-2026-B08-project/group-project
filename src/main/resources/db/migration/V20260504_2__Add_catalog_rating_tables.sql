ALTER TABLE catalog
ADD COLUMN IF NOT EXISTS rating_count INTEGER NOT NULL DEFAULT 0;

ALTER TABLE catalog
ADD COLUMN IF NOT EXISTS rating_sum INTEGER NOT NULL DEFAULT 0;

ALTER TABLE catalog
ADD COLUMN IF NOT EXISTS rating_average DOUBLE PRECISION NOT NULL DEFAULT 0;

CREATE TABLE IF NOT EXISTS catalog_rating_event (
    id UUID PRIMARY KEY,
    order_id UUID NOT NULL UNIQUE,
    catalog_id UUID NOT NULL,
    buyer_id UUID NOT NULL,
    product_rating INTEGER NOT NULL,
    created_at TIMESTAMP NOT NULL
);

CREATE INDEX IF NOT EXISTS idx_catalog_rating_event_catalog_id
    ON catalog_rating_event(catalog_id);
