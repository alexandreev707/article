-- Product variants (for products with size/color options)
INSERT INTO product_variants (id, product_id, name, sku, price, stock, active) VALUES
    -- Wireless Headphones - colors
    ('40000001-0000-0000-0000-000000000001', '20000001-0000-0000-0000-000000000001', 'Black', 'WH-BLK-001', 129.99, 20, true),
    ('40000001-0000-0000-0000-000000000002', '20000001-0000-0000-0000-000000000001', 'White', 'WH-WHT-001', 129.99, 15, true),
    ('40000001-0000-0000-0000-000000000003', '20000001-0000-0000-0000-000000000001', 'Silver', 'WH-SLV-001', 129.99, 15, true),
    -- Smartphone - storage
    ('40000001-0000-0000-0000-000000000004', '20000001-0000-0000-0000-000000000002', '128GB', 'SP-128-001', 599.99, 15, true),
    ('40000001-0000-0000-0000-000000000005', '20000001-0000-0000-0000-000000000002', '256GB', 'SP-256-001', 699.99, 10, true),
    -- Cotton T-Shirt - sizes
    ('40000001-0000-0000-0000-000000000006', '20000001-0000-0000-0000-000000000004', 'S', 'TS-S-001', 19.99, 25, true),
    ('40000001-0000-0000-0000-000000000007', '20000001-0000-0000-0000-000000000004', 'M', 'TS-M-001', 19.99, 35, true),
    ('40000001-0000-0000-0000-000000000008', '20000001-0000-0000-0000-000000000004', 'L', 'TS-L-001', 19.99, 40, true),
    -- Denim Jeans - sizes
    ('40000001-0000-0000-0000-000000000009', '20000001-0000-0000-0000-000000000005', '30', 'DJ-30-001', 49.99, 20, true),
    ('40000001-0000-0000-0000-000000000010', '20000001-0000-0000-0000-000000000005', '32', 'DJ-32-001', 49.99, 30, true),
    ('40000001-0000-0000-0000-000000000011', '20000001-0000-0000-0000-000000000005', '34', 'DJ-34-001', 49.99, 25, true);

INSERT INTO variant_attributes (variant_id, attribute_key, attribute_value) VALUES
    ('40000001-0000-0000-0000-000000000001', 'color', 'Black'),
    ('40000001-0000-0000-0000-000000000002', 'color', 'White'),
    ('40000001-0000-0000-0000-000000000003', 'color', 'Silver'),
    ('40000001-0000-0000-0000-000000000006', 'size', 'S'),
    ('40000001-0000-0000-0000-000000000007', 'size', 'M'),
    ('40000001-0000-0000-0000-000000000008', 'size', 'L');

-- Carts for customer
INSERT INTO carts (id, user_id, created_at, updated_at) VALUES
    ('50000001-0000-0000-0000-000000000001', '00000001-0000-0000-0000-000000000001', NOW(), NOW());

-- Cart items (customer has headphones, t-shirt, and building blocks in cart)
INSERT INTO cart_items (id, cart_id, product_id, quantity, added_at) VALUES
    (gen_random_uuid(), '50000001-0000-0000-0000-000000000001', '20000001-0000-0000-0000-000000000001', 2, NOW()),
    (gen_random_uuid(), '50000001-0000-0000-0000-000000000001', '20000001-0000-0000-0000-000000000004', 1, NOW()),
    (gen_random_uuid(), '50000001-0000-0000-0000-000000000001', '20000001-0000-0000-0000-000000000012', 1, NOW());

-- Wishlists (customer favorites)
INSERT INTO wishlists (id, user_id, product_id, added_at) VALUES
    (gen_random_uuid(), '00000001-0000-0000-0000-000000000001', '20000001-0000-0000-0000-000000000002', NOW()),
    (gen_random_uuid(), '00000001-0000-0000-0000-000000000001', '20000001-0000-0000-0000-000000000003', NOW()),
    (gen_random_uuid(), '00000001-0000-0000-0000-000000000001', '20000001-0000-0000-0000-000000000009', NOW());

-- Orders (customer's past orders)
INSERT INTO orders (id, order_number, buyer_id, subtotal, shipping_cost, discount_amount, tax_amount, total_price,
    status, payment_method, payment_status, delivery_option_id, address_line, city, region, zip_code, country,
    recipient_name, recipient_phone, created_at, updated_at, delivered_at) VALUES
    ('90000001-0000-0000-0000-000000000001', 'ORD-2024-001', '00000001-0000-0000-0000-000000000001',
     129.99, 9.99, 0, 0, 139.98, 'DELIVERED', 'CARD', 'PAID', '30000001-0000-0000-0000-000000000002',
     '456 Oak Ave', 'Los Angeles', 'CA', '90001', 'USA', 'John Doe', '+1234567890', NOW() - INTERVAL '30 days', NOW(), NOW() - INTERVAL '25 days'),
    ('90000001-0000-0000-0000-000000000002', 'ORD-2024-002', '00000001-0000-0000-0000-000000000001',
     64.97, 0, 0, 0, 64.97, 'SHIPPED', 'CARD', 'PAID', '30000001-0000-0000-0000-000000000001',
     '456 Oak Ave', 'Los Angeles', 'CA', '90001', 'USA', 'John Doe', '+1234567890', NOW() - INTERVAL '5 days', NOW(), NULL);

-- Order items
INSERT INTO order_items (id, order_id, product_id, seller_id, quantity, unit_price, total_price, status) VALUES
    (gen_random_uuid(), '90000001-0000-0000-0000-000000000001', '20000001-0000-0000-0000-000000000001', '00000001-0000-0000-0000-000000000002', 1, 129.99, 129.99, 'DELIVERED'),
    (gen_random_uuid(), '90000001-0000-0000-0000-000000000002', '20000001-0000-0000-0000-000000000004', '00000001-0000-0000-0000-000000000002', 2, 19.99, 39.98, 'SHIPPED'),
    (gen_random_uuid(), '90000001-0000-0000-0000-000000000002', '20000001-0000-0000-0000-000000000008', '00000001-0000-0000-0000-000000000002', 1, 24.99, 24.99, 'SHIPPED');

-- Chat conversations (customer <-> seller about products)
INSERT INTO chat_conversations (id, user1_id, user2_id, product_id, last_message_at, created_at) VALUES
    ('70000001-0000-0000-0000-000000000001', '00000001-0000-0000-0000-000000000001', '00000001-0000-0000-0000-000000000002',
     '20000001-0000-0000-0000-000000000002', NOW(), NOW() - INTERVAL '2 days');

-- Chat messages
INSERT INTO chat_messages (id, conversation_id, sender_id, text, message_type, "read", timestamp) VALUES
    (gen_random_uuid(), '70000001-0000-0000-0000-000000000001', '00000001-0000-0000-0000-000000000001',
     'Hi! Is the smartphone still available?', 'TEXT', true, NOW() - INTERVAL '2 days'),
    (gen_random_uuid(), '70000001-0000-0000-0000-000000000001', '00000001-0000-0000-0000-000000000002',
     'Yes, we have it in stock. Which storage option do you need?', 'TEXT', true, NOW() - INTERVAL '2 days' + INTERVAL '1 hour'),
    (gen_random_uuid(), '70000001-0000-0000-0000-000000000001', '00000001-0000-0000-0000-000000000001',
     '256GB would be perfect. Can you ship today?', 'TEXT', true, NOW() - INTERVAL '1 day'),
    (gen_random_uuid(), '70000001-0000-0000-0000-000000000001', '00000001-0000-0000-0000-000000000002',
     'Sure! Order before 2 PM and we ship same day.', 'TEXT', false, NOW());

-- Promotions
INSERT INTO promotions (id, title, description, code, discount_type, discount_value, min_order_amount, max_discount_amount,
    start_date, end_date, usage_limit, usage_count, active, created_at) VALUES
    ('80000001-0000-0000-0000-000000000001', 'Welcome Sale', '10% off your first order', 'WELCOME10',
     'PERCENTAGE', 10, 50, 25, NOW() - INTERVAL '30 days', NOW() + INTERVAL '60 days', 1000, 42, true, NOW()),
    ('80000001-0000-0000-0000-000000000002', 'Electronics Deal', 'Fixed $50 off electronics over $200', 'TECH50',
     'FIXED_AMOUNT', 50, 200, 50, NOW() - INTERVAL '7 days', NOW() + INTERVAL '23 days', NULL, 8, true, NOW()),
    ('80000001-0000-0000-0000-000000000003', 'Free Shipping', 'Free shipping on orders over $100', 'FREESHIP100',
     'FIXED_AMOUNT', 9.99, 100, 9.99, NOW(), NOW() + INTERVAL '90 days', NULL, 0, true, NOW());

-- Promotion categories (link promotions to categories)
INSERT INTO promotion_categories (promotion_id, category_id) VALUES
    ('80000001-0000-0000-0000-000000000002', '10000001-0000-0000-0000-000000000001'),
    ('80000001-0000-0000-0000-000000000003', '10000001-0000-0000-0000-000000000001'),
    ('80000001-0000-0000-0000-000000000003', '10000001-0000-0000-0000-000000000002'),
    ('80000001-0000-0000-0000-000000000003', '10000001-0000-0000-0000-000000000003');
