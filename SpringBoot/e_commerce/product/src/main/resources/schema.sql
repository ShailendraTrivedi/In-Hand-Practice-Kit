CREATE TABLE IF NOT EXISTS products (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    price DOUBLE PRECISION NOT NULL,
    product_type VARCHAR(50) NOT NULL,
    weight DOUBLE PRECISION,
    shipping_address VARCHAR(500),
    download_link VARCHAR(500),
    file_size_mb BIGINT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

