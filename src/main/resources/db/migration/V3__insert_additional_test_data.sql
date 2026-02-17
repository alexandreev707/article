-- Product variants (for products with size/color options)
INSERT INTO product_variants (id, product_id, name, sku, price, stock, active)
SELECT gen_random_uuid(), p.id, 'Black', 'WH-BLK-001', 129.99, 20, true
FROM products p WHERE p.title = 'Wireless Bluetooth Headphones';

INSERT INTO product_variants (id, product_id, name, sku, price, stock, active)
SELECT gen_random_uuid(), p.id, 'White', 'WH-WHT-001', 129.99, 15, true
FROM products p WHERE p.title = 'Wireless Bluetooth Headphones';

INSERT INTO product_variants (id, product_id, name, sku, price, stock, active)
SELECT gen_random_uuid(), p.id, 'Silver', 'WH-SLV-001', 129.99, 15, true
FROM products p WHERE p.title = 'Wireless Bluetooth Headphones';

INSERT INTO product_variants (id, product_id, name, sku, price, stock, active)
SELECT gen_random_uuid(), p.id, '128GB', 'SP-128-001', 599.99, 15, true
FROM products p WHERE p.title = 'Smartphone 128GB';

INSERT INTO product_variants (id, product_id, name, sku, price, stock, active)
SELECT gen_random_uuid(), p.id, '256GB', 'SP-256-001', 699.99, 10, true
FROM products p WHERE p.title = 'Smartphone 128GB';

INSERT INTO product_variants (id, product_id, name, sku, price, stock, active)
SELECT gen_random_uuid(), p.id, 'S', 'TS-S-001', 19.99, 25, true
FROM products p WHERE p.title = 'Cotton T-Shirt';

INSERT INTO product_variants (id, product_id, name, sku, price, stock, active)
SELECT gen_random_uuid(), p.id, 'M', 'TS-M-001', 19.99, 35, true
FROM products p WHERE p.title = 'Cotton T-Shirt';

INSERT INTO product_variants (id, product_id, name, sku, price, stock, active)
SELECT gen_random_uuid(), p.id, 'L', 'TS-L-001', 19.99, 40, true
FROM products p WHERE p.title = 'Cotton T-Shirt';

INSERT INTO product_variants (id, product_id, name, sku, price, stock, active)
SELECT gen_random_uuid(), p.id, '30', 'DJ-30-001', 49.99, 20, true
FROM products p WHERE p.title = 'Denim Jeans';

INSERT INTO product_variants (id, product_id, name, sku, price, stock, active)
SELECT gen_random_uuid(), p.id, '32', 'DJ-32-001', 49.99, 30, true
FROM products p WHERE p.title = 'Denim Jeans';

INSERT INTO product_variants (id, product_id, name, sku, price, stock, active)
SELECT gen_random_uuid(), p.id, '34', 'DJ-34-001', 49.99, 25, true
FROM products p WHERE p.title = 'Denim Jeans';

-- Variant attributes
INSERT INTO variant_attributes (variant_id, attribute_key, attribute_value)
SELECT v.id, 'color', 'Black'
FROM product_variants v WHERE v.sku = 'WH-BLK-001';

INSERT INTO variant_attributes (variant_id, attribute_key, attribute_value)
SELECT v.id, 'color', 'White'
FROM product_variants v WHERE v.sku = 'WH-WHT-001';

INSERT INTO variant_attributes (variant_id, attribute_key, attribute_value)
SELECT v.id, 'color', 'Silver'
FROM product_variants v WHERE v.sku = 'WH-SLV-001';

INSERT INTO variant_attributes (variant_id, attribute_key, attribute_value)
SELECT v.id, 'size', 'S'
FROM product_variants v WHERE v.sku = 'TS-S-001';

INSERT INTO variant_attributes (variant_id, attribute_key, attribute_value)
SELECT v.id, 'size', 'M'
FROM product_variants v WHERE v.sku = 'TS-M-001';

INSERT INTO variant_attributes (variant_id, attribute_key, attribute_value)
SELECT v.id, 'size', 'L'
FROM product_variants v WHERE v.sku = 'TS-L-001';

-- Carts for customer
INSERT INTO carts (id, user_id, created_at, updated_at)
SELECT gen_random_uuid(), u.id, NOW(), NOW()
FROM users u WHERE u.email = 'customer@test.com';

-- Cart items (customer has headphones, t-shirt, and building blocks in cart)
INSERT INTO cart_items (id, cart_id, product_id, quantity, added_at)
SELECT gen_random_uuid(), c.id, p.id, 2, NOW()
FROM carts c, products p, users u
WHERE u.email = 'customer@test.com' AND c.user_id = u.id
  AND p.title = 'Wireless Bluetooth Headphones';

INSERT INTO cart_items (id, cart_id, product_id, quantity, added_at)
SELECT gen_random_uuid(), c.id, p.id, 1, NOW()
FROM carts c, products p, users u
WHERE u.email = 'customer@test.com' AND c.user_id = u.id
  AND p.title = 'Cotton T-Shirt';

INSERT INTO cart_items (id, cart_id, product_id, quantity, added_at)
SELECT gen_random_uuid(), c.id, p.id, 1, NOW()
FROM carts c, products p, users u
WHERE u.email = 'customer@test.com' AND c.user_id = u.id
  AND p.title = 'Building Blocks Set';

-- Wishlists (customer favorites)
INSERT INTO wishlists (id, user_id, product_id, added_at)
SELECT gen_random_uuid(), u.id, p.id, NOW()
FROM users u, products p
WHERE u.email = 'customer@test.com' AND p.title = 'Smartphone 128GB';

INSERT INTO wishlists (id, user_id, product_id, added_at)
SELECT gen_random_uuid(), u.id, p.id, NOW()
FROM users u, products p
WHERE u.email = 'customer@test.com' AND p.title = 'Laptop 15.6 inch';

INSERT INTO wishlists (id, user_id, product_id, added_at)
SELECT gen_random_uuid(), u.id, p.id, NOW()
FROM users u, products p
WHERE u.email = 'customer@test.com' AND p.title = 'Running Shoes';

-- Orders (customer's past orders)
INSERT INTO orders (id, order_number, buyer_id, subtotal, shipping_cost, discount_amount, tax_amount, total_price,
                    status, payment_method, payment_status, delivery_option_id, address_line, city, region, zip_code, country,
                    recipient_name, recipient_phone, created_at, updated_at, delivered_at)
SELECT gen_random_uuid(), 'ORD-2024-001', u.id, 129.99, 9.99, 0, 0, 139.98,
       'DELIVERED', 'CARD', 'PAID', do1.id, '456 Oak Ave', 'Los Angeles', 'CA', '90001', 'USA',
       'John Doe', '+1234567890', NOW() - INTERVAL '30 days', NOW(), NOW() - INTERVAL '25 days'
FROM users u, delivery_options do1
WHERE u.email = 'customer@test.com' AND do1.name = 'Courier';

INSERT INTO orders (id, order_number, buyer_id, subtotal, shipping_cost, discount_amount, tax_amount, total_price,
                    status, payment_method, payment_status, delivery_option_id, address_line, city, region, zip_code, country,
                    recipient_name, recipient_phone, created_at, updated_at, delivered_at)
SELECT gen_random_uuid(), 'ORD-2024-002', u.id, 64.97, 0, 0, 0, 64.97,
       'SHIPPED', 'CARD', 'PAID', do2.id, '456 Oak Ave', 'Los Angeles', 'CA', '90001', 'USA',
       'John Doe', '+1234567890', NOW() - INTERVAL '5 days', NOW(), NULL
FROM users u, delivery_options do2
WHERE u.email = 'customer@test.com' AND do2.name = 'Pickup point';

-- Order items for first order
INSERT INTO order_items (id, order_id, product_id, seller_id, quantity, unit_price, total_price, status)
SELECT gen_random_uuid(), o.id, p.id, u.id, 1, 129.99, 129.99, 'DELIVERED'
FROM orders o, products p, users u
WHERE o.order_number = 'ORD-2024-001' AND p.title = 'Wireless Bluetooth Headphones'
  AND u.email = 'seller@test.com';

-- Order items for second order
INSERT INTO order_items (id, order_id, product_id, seller_id, quantity, unit_price, total_price, status)
SELECT gen_random_uuid(), o.id, p.id, u.id, 2, 19.99, 39.98, 'SHIPPED'
FROM orders o, products p, users u
WHERE o.order_number = 'ORD-2024-002' AND p.title = 'Cotton T-Shirt'
  AND u.email = 'seller@test.com';

INSERT INTO order_items (id, order_id, product_id, seller_id, quantity, unit_price, total_price, status)
SELECT gen_random_uuid(), o.id, p.id, u.id, 1, 24.99, 24.99, 'SHIPPED'
FROM orders o, products p, users u
WHERE o.order_number = 'ORD-2024-002' AND p.title = 'Yoga Mat'
  AND u.email = 'seller@test.com';

-- Chat conversations (customer <-> seller about products)
INSERT INTO chat_conversations (id, user1_id, user2_id, product_id, last_message_at, created_at)
SELECT gen_random_uuid(), u1.id, u2.id, p.id, NOW(), NOW() - INTERVAL '2 days'
FROM users u1, users u2, products p
WHERE u1.email = 'customer@test.com' AND u2.email = 'seller@test.com'
  AND p.title = 'Smartphone 128GB';

-- Chat messages
INSERT INTO chat_messages (id, conversation_id, sender_id, text, message_type, "read", timestamp)
SELECT gen_random_uuid(), c.id, u.id, 'Hi! Is the smartphone still available?', 'TEXT', true, NOW() - INTERVAL '2 days'
FROM chat_conversations c, users u
WHERE u.email = 'customer@test.com' AND c.user1_id = u.id;

INSERT INTO chat_messages (id, conversation_id, sender_id, text, message_type, "read", timestamp)
SELECT gen_random_uuid(), c.id, u.id, 'Yes, we have it in stock. Which storage option do you need?', 'TEXT', true, NOW() - INTERVAL '2 days' + INTERVAL '1 hour'
FROM chat_conversations c, users u
WHERE u.email = 'seller@test.com' AND c.user2_id = u.id;

INSERT INTO chat_messages (id, conversation_id, sender_id, text, message_type, "read", timestamp)
SELECT gen_random_uuid(), c.id, u.id, '256GB would be perfect. Can you ship today?', 'TEXT', true, NOW() - INTERVAL '1 day'
FROM chat_conversations c, users u
WHERE u.email = 'customer@test.com' AND c.user1_id = u.id;

INSERT INTO chat_messages (id, conversation_id, sender_id, text, message_type, "read", timestamp)
SELECT gen_random_uuid(), c.id, u.id, 'Sure! Order before 2 PM and we ship same day.', 'TEXT', false, NOW()
FROM chat_conversations c, users u
WHERE u.email = 'seller@test.com' AND c.user2_id = u.id;

-- Promotions
INSERT INTO promotions (id, title, description, code, discount_type, discount_value, min_order_amount, max_discount_amount,
                        start_date, end_date, usage_limit, usage_count, active, created_at) VALUES
                                                                                                (gen_random_uuid(), 'Welcome Sale', '10% off your first order', 'WELCOME10',
                                                                                                 'PERCENTAGE', 10, 50, 25, NOW() - INTERVAL '30 days', NOW() + INTERVAL '60 days', 1000, 42, true, NOW()),
                                                                                                (gen_random_uuid(), 'Electronics Deal', 'Fixed $50 off electronics over $200', 'TECH50',
                                                                                                 'FIXED_AMOUNT', 50, 200, 50, NOW() - INTERVAL '7 days', NOW() + INTERVAL '23 days', NULL, 8, true, NOW()),
                                                                                                (gen_random_uuid(), 'Free Shipping', 'Free shipping on orders over $100', 'FREESHIP100',
                                                                                                 'FIXED_AMOUNT', 9.99, 100, 9.99, NOW(), NOW() + INTERVAL '90 days', NULL, 0, true, NOW());

-- Promotion categories (link promotions to categories)
INSERT INTO promotion_categories (promotion_id, category_id)
SELECT p.id, c.id
FROM promotions p, categories c
WHERE p.code = 'TECH50' AND c.slug = 'electronics';

INSERT INTO promotion_categories (promotion_id, category_id)
SELECT p.id, c.id
FROM promotions p, categories c
WHERE p.code = 'FREESHIP100' AND c.slug = 'electronics';

INSERT INTO promotion_categories (promotion_id, category_id)
SELECT p.id, c.id
FROM promotions p, categories c
WHERE p.code = 'FREESHIP100' AND c.slug = 'clothing';

INSERT INTO promotion_categories (promotion_id, category_id)
SELECT p.id, c.id
FROM promotions p, categories c
WHERE p.code = 'FREESHIP100' AND c.slug = 'home-garden';