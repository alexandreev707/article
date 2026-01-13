-- Categories
CREATE TABLE categories (
    id BIGSERIAL PRIMARY KEY,
    parent_id BIGINT REFERENCES categories(id),
    name VARCHAR(255) NOT NULL,
    slug VARCHAR(50) UNIQUE NOT NULL,
    attributes JSONB,
    icon_url VARCHAR(500),
    created_at TIMESTAMPTZ DEFAULT NOW()
);

-- Products
CREATE TABLE products (
    id BIGSERIAL PRIMARY KEY,
    seller_wallet VARCHAR(66) NOT NULL,
    category_id BIGINT NOT NULL REFERENCES categories(id),
    title VARCHAR(255) NOT NULL,
    description TEXT,
    price_usd DECIMAL(10,2) NOT NULL,
    images JSONB,
    specs JSONB,
    shipping_profiles JSONB,
    solana_product_id VARCHAR(44),
    status VARCHAR(20) DEFAULT 'ACTIVE',
    created_at TIMESTAMPTZ DEFAULT NOW(),
    updated_at TIMESTAMPTZ DEFAULT NOW()
);

CREATE INDEX idx_seller_wallet ON products(seller_wallet);
CREATE INDEX idx_category_id ON products(category_id);
CREATE INDEX idx_status ON products(status);

-- Orders
CREATE TABLE orders (
    id BIGSERIAL PRIMARY KEY,
    product_id BIGINT NOT NULL REFERENCES products(id),
    buyer_wallet VARCHAR(66) NOT NULL,
    seller_wallet VARCHAR(66) NOT NULL,
    amount_usdc DECIMAL(18,6) NOT NULL,
    shipping_country VARCHAR(2),
    shipping_address JSONB,
    shipping_cost_usd DECIMAL(8,2),
    solana_escrow VARCHAR(44),
    solana_tx_id VARCHAR(88),
    status VARCHAR(20) DEFAULT 'PENDING_PAYMENT',
    tracking_number VARCHAR(50),
    created_at TIMESTAMPTZ DEFAULT NOW(),
    shipped_at TIMESTAMPTZ,
    delivered_at TIMESTAMPTZ,
    completed_at TIMESTAMPTZ
);

CREATE INDEX idx_buyer ON orders(buyer_wallet);
CREATE INDEX idx_seller ON orders(seller_wallet);
CREATE INDEX idx_status ON orders(status);

-- Initial categories
INSERT INTO categories (name, slug, icon_url) VALUES
('Electronics', 'electronics', '/icons/electronics.svg'),
('Fashion', 'fashion', '/icons/fashion.svg'),
('Beauty', 'beauty', '/icons/beauty.svg'),
('Home', 'home', '/icons/home.svg');
