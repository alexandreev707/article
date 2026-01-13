-- V1__initial_schema.sql (с проверками IF NOT EXISTS)

-- Categories
CREATE TABLE IF NOT EXISTS categories (
                                          id BIGSERIAL PRIMARY KEY,
                                          parent_id BIGINT REFERENCES categories(id),
                                          name VARCHAR(255) NOT NULL,
                                          slug VARCHAR(50),
                                          attributes JSONB,
                                          icon_url VARCHAR(500),
                                          created_at TIMESTAMPTZ DEFAULT NOW()
);

-- Уникальность slug (если нет)
DO $$
    BEGIN
        IF NOT EXISTS (
            SELECT 1 FROM pg_constraint WHERE conname = 'categories_slug_key'
        ) THEN
            ALTER TABLE categories ADD CONSTRAINT categories_slug_key UNIQUE (slug);
        END IF;
    END $$;

-- Products
CREATE TABLE IF NOT EXISTS products (
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

-- Индексы Products (idempotent)
CREATE INDEX IF NOT EXISTS idx_seller_wallet ON products(seller_wallet);
CREATE INDEX IF NOT EXISTS idx_category_id ON products(category_id);
CREATE INDEX IF NOT EXISTS idx_products_status ON products(status);

-- Orders
CREATE TABLE IF NOT EXISTS orders (
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

-- Индексы Orders (idempotent + уникальные имена)
CREATE INDEX IF NOT EXISTS idx_orders_buyer ON orders(buyer_wallet);
CREATE INDEX IF NOT EXISTS idx_orders_seller ON orders(seller_wallet);
CREATE INDEX IF NOT EXISTS idx_orders_status ON orders(status);

-- Initial categories (только если пусто)
INSERT INTO categories (name, slug, icon_url)
SELECT 'Electronics', 'electronics', '/icons/electronics.svg'
WHERE NOT EXISTS (SELECT 1 FROM categories WHERE slug = 'electronics');

INSERT INTO categories (name, slug, icon_url)
SELECT 'Fashion', 'fashion', '/icons/fashion.svg'
WHERE NOT EXISTS (SELECT 1 FROM categories WHERE slug = 'fashion');

INSERT INTO categories (name, slug, icon_url)
SELECT 'Beauty', 'beauty', '/icons/beauty.svg'
WHERE NOT EXISTS (SELECT 1 FROM categories WHERE slug = 'beauty');

INSERT INTO categories (name, slug, icon_url)
SELECT 'Home', 'home', '/icons/home.svg'
WHERE NOT EXISTS (SELECT 1 FROM categories WHERE slug = 'home');
