-- Users
CREATE TABLE users (
    id UUID PRIMARY KEY,
    email VARCHAR(255) NOT NULL UNIQUE,
    username VARCHAR(255) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    full_name VARCHAR(255),
    phone_number VARCHAR(50),
    avatar_url VARCHAR(500),
    blocked BOOLEAN DEFAULT FALSE,
    email_verified BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP
);

CREATE TABLE user_roles (
    user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    role VARCHAR(50) NOT NULL,
    PRIMARY KEY (user_id, role)
);

-- Categories
CREATE TABLE categories (
    id UUID PRIMARY KEY,
    name VARCHAR(255) NOT NULL UNIQUE,
    slug VARCHAR(255) NOT NULL UNIQUE,
    description TEXT,
    icon_url VARCHAR(500),
    banner_url VARCHAR(500),
    parent_id UUID REFERENCES categories(id),
    display_order INT DEFAULT 0,
    active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL
);

-- Delivery options
CREATE TABLE delivery_options (
    id UUID PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    type VARCHAR(50) NOT NULL,
    description TEXT,
    price DECIMAL(19, 2) NOT NULL DEFAULT 0,
    free_shipping_threshold DECIMAL(19, 2),
    estimated_days_min INT,
    estimated_days_max INT,
    pickup_address TEXT,
    latitude DOUBLE PRECISION,
    longitude DOUBLE PRECISION,
    active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL
);

-- Products
CREATE TABLE products (
    id UUID PRIMARY KEY,
    seller_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    category_id UUID NOT NULL REFERENCES categories(id) ON DELETE CASCADE,
    title VARCHAR(500) NOT NULL,
    slug VARCHAR(500) NOT NULL,
    description TEXT DEFAULT '',
    price DECIMAL(19, 2) NOT NULL,
    discount_price DECIMAL(19, 2),
    discount_percentage INT,
    rating DECIMAL(3, 2) DEFAULT 0,
    review_count INT DEFAULT 0,
    view_count INT DEFAULT 0,
    stock INT NOT NULL DEFAULT 0,
    sku VARCHAR(100) UNIQUE,
    brand VARCHAR(255),
    weight_grams INT,
    active BOOLEAN DEFAULT TRUE,
    is_new BOOLEAN DEFAULT FALSE,
    is_featured BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP
);

CREATE INDEX idx_seller_id ON products(seller_id);
CREATE INDEX idx_category_id ON products(category_id);
CREATE INDEX idx_active_created ON products(active, created_at);

CREATE TABLE product_images (
    product_id UUID NOT NULL REFERENCES products(id) ON DELETE CASCADE,
    image_order INT NOT NULL,
    image_url VARCHAR(500),
    PRIMARY KEY (product_id, image_order)
);

CREATE TABLE product_attributes (
    product_id UUID NOT NULL REFERENCES products(id) ON DELETE CASCADE,
    attribute_key VARCHAR(255) NOT NULL,
    attribute_value VARCHAR(1000),
    PRIMARY KEY (product_id, attribute_key)
);

-- Product variants
CREATE TABLE product_variants (
    id UUID PRIMARY KEY,
    product_id UUID NOT NULL REFERENCES products(id) ON DELETE CASCADE,
    name VARCHAR(255) NOT NULL,
    sku VARCHAR(100) UNIQUE,
    price DECIMAL(19, 2),
    stock INT NOT NULL DEFAULT 0,
    image_url VARCHAR(500),
    active BOOLEAN DEFAULT TRUE
);

CREATE TABLE variant_attributes (
    variant_id UUID NOT NULL REFERENCES product_variants(id) ON DELETE CASCADE,
    attribute_key VARCHAR(255) NOT NULL,
    attribute_value VARCHAR(255),
    PRIMARY KEY (variant_id, attribute_key)
);

-- Carts
CREATE TABLE carts (
    id UUID PRIMARY KEY,
    user_id UUID NOT NULL UNIQUE REFERENCES users(id) ON DELETE CASCADE,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP
);

CREATE TABLE cart_items (
    id UUID PRIMARY KEY,
    cart_id UUID NOT NULL REFERENCES carts(id) ON DELETE CASCADE,
    product_id UUID NOT NULL REFERENCES products(id) ON DELETE CASCADE,
    variant_id UUID REFERENCES product_variants(id) ON DELETE SET NULL,
    quantity INT NOT NULL DEFAULT 1,
    added_at TIMESTAMP NOT NULL,
    UNIQUE (cart_id, product_id)
);

-- Wishlists
CREATE TABLE wishlists (
    id UUID PRIMARY KEY,
    user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    product_id UUID NOT NULL REFERENCES products(id) ON DELETE CASCADE,
    added_at TIMESTAMP NOT NULL,
    UNIQUE (user_id, product_id)
);

-- Orders
CREATE TABLE orders (
    id UUID PRIMARY KEY,
    order_number VARCHAR(50) NOT NULL UNIQUE,
    buyer_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    subtotal DECIMAL(19, 2) NOT NULL,
    shipping_cost DECIMAL(19, 2) DEFAULT 0,
    discount_amount DECIMAL(19, 2) DEFAULT 0,
    tax_amount DECIMAL(19, 2) DEFAULT 0,
    total_price DECIMAL(19, 2) NOT NULL,
    status VARCHAR(50) NOT NULL DEFAULT 'PENDING',
    payment_method VARCHAR(50),
    payment_status VARCHAR(50) DEFAULT 'PENDING',
    delivery_option_id UUID REFERENCES delivery_options(id) ON DELETE SET NULL,
    tracking_number VARCHAR(255),
    notes TEXT,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP,
    delivered_at TIMESTAMP,
    -- Embedded address
    address_line VARCHAR(500) NOT NULL,
    city VARCHAR(255) NOT NULL,
    region VARCHAR(255) NOT NULL,
    zip_code VARCHAR(20) NOT NULL,
    country VARCHAR(100) NOT NULL,
    recipient_name VARCHAR(255),
    recipient_phone VARCHAR(50)
);

CREATE INDEX idx_buyer_id ON orders(buyer_id);
CREATE INDEX idx_status ON orders(status);
CREATE INDEX idx_created_at ON orders(created_at);

CREATE TABLE order_items (
    id UUID PRIMARY KEY,
    order_id UUID NOT NULL REFERENCES orders(id) ON DELETE CASCADE,
    product_id UUID NOT NULL REFERENCES products(id) ON DELETE CASCADE,
    seller_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    variant_id UUID REFERENCES product_variants(id) ON DELETE SET NULL,
    quantity INT NOT NULL DEFAULT 1,
    unit_price DECIMAL(19, 2) NOT NULL,
    total_price DECIMAL(19, 2) NOT NULL,
    status VARCHAR(50) NOT NULL DEFAULT 'PENDING'
);

-- Reviews
CREATE TABLE reviews (
    id UUID PRIMARY KEY,
    product_id UUID NOT NULL REFERENCES products(id) ON DELETE CASCADE,
    author_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    order_id UUID REFERENCES orders(id) ON DELETE SET NULL,
    rating INT NOT NULL,
    comment TEXT DEFAULT '',
    likes_count INT DEFAULT 0,
    dislikes_count INT DEFAULT 0,
    verified_purchase BOOLEAN DEFAULT FALSE,
    seller_response TEXT,
    seller_response_at TIMESTAMP,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP,
    UNIQUE (product_id, author_id)
);

CREATE INDEX idx_product_id ON reviews(product_id);

CREATE TABLE review_images (
    review_id UUID NOT NULL REFERENCES reviews(id) ON DELETE CASCADE,
    image_order INT NOT NULL,
    image_url VARCHAR(500),
    PRIMARY KEY (review_id, image_order)
);

-- Chat
CREATE TABLE chat_conversations (
    id UUID PRIMARY KEY,
    user1_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    user2_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    product_id UUID REFERENCES products(id) ON DELETE SET NULL,
    last_message_at TIMESTAMP,
    created_at TIMESTAMP NOT NULL,
    UNIQUE (user1_id, user2_id)
);

CREATE TABLE chat_messages (
    id UUID PRIMARY KEY,
    conversation_id UUID NOT NULL REFERENCES chat_conversations(id) ON DELETE CASCADE,
    sender_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    text TEXT NOT NULL,
    message_type VARCHAR(50) DEFAULT 'TEXT',
    attachment_url VARCHAR(500),
    "read" BOOLEAN DEFAULT FALSE,
    timestamp TIMESTAMP NOT NULL
);

CREATE INDEX idx_conversation_id ON chat_messages(conversation_id);
CREATE INDEX idx_timestamp ON chat_messages(timestamp);

-- Promotions
CREATE TABLE promotions (
    id UUID PRIMARY KEY,
    title VARCHAR(255) NOT NULL,
    description TEXT,
    code VARCHAR(100) UNIQUE,
    discount_type VARCHAR(50) NOT NULL,
    discount_value DECIMAL(19, 2) NOT NULL,
    min_order_amount DECIMAL(19, 2),
    max_discount_amount DECIMAL(19, 2),
    start_date TIMESTAMP NOT NULL,
    end_date TIMESTAMP NOT NULL,
    usage_limit INT,
    usage_count INT DEFAULT 0,
    active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL
);

CREATE TABLE promotion_categories (
    promotion_id UUID NOT NULL REFERENCES promotions(id) ON DELETE CASCADE,
    category_id UUID NOT NULL REFERENCES categories(id) ON DELETE CASCADE,
    PRIMARY KEY (promotion_id, category_id)
);
