CREATE TABLE IF NOT EXISTS catalog (
    id UUID PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    description TEXT,
    image_url VARCHAR(500),
    price DOUBLE PRECISION NOT NULL,
    stock INTEGER NOT NULL,
    origin_location VARCHAR(255) NOT NULL,
    travel_date DATE NOT NULL,
    user_id UUID NOT NULL,
    CONSTRAINT fk_catalog_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

-- Create index on user_id for faster queries
CREATE INDEX IF NOT EXISTS idx_catalog_user_id ON catalog(user_id);

-- Create index on travel_date for filtering
CREATE INDEX IF NOT EXISTS idx_catalog_travel_date ON catalog(travel_date);
