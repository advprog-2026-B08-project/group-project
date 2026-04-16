CREATE TABLE IF NOT EXISTS orders (
    id UUID PRIMARY KEY,
    buyer_id UUID NOT NULL,
    jastiper_id UUID NOT NULL,
    product_id UUID NOT NULL,
    quantity INTEGER NOT NULL,
    shipping_address VARCHAR(500) NOT NULL,
    total_price NUMERIC(19, 2) NOT NULL,
    status VARCHAR(50) NOT NULL,
    created_at TIMESTAMP,
    updated_at TIMESTAMP,
    CONSTRAINT chk_orders_quantity CHECK (quantity > 0),
    CONSTRAINT chk_orders_total_price CHECK (total_price >= 0)
);

CREATE TABLE IF NOT EXISTS status_history (
    id UUID PRIMARY KEY,
    order_id UUID NOT NULL,
    status VARCHAR(50) NOT NULL,
    timestamp TIMESTAMP,
    CONSTRAINT fk_status_history_order FOREIGN KEY (order_id) REFERENCES orders(id) ON DELETE CASCADE
);

CREATE INDEX IF NOT EXISTS idx_orders_buyer_id ON orders(buyer_id);
CREATE INDEX IF NOT EXISTS idx_orders_jastiper_id ON orders(jastiper_id);
CREATE INDEX IF NOT EXISTS idx_orders_status ON orders(status);
CREATE INDEX IF NOT EXISTS idx_status_history_order_id ON status_history(order_id);
