-- Test users (password: password, BCrypt hash)
INSERT INTO users (id, email, username, password, blocked, email_verified, created_at, updated_at) VALUES
    ('00000001-0000-0000-0000-000000000001', 'customer@test.com', 'customer', '$2a$10$dXJ3SW6G7P50lGmMkkmwe.20cQQubK3.HZWzG3YB1tlRy.fqvM/BG', false, false, NOW(), NOW()),
    ('00000001-0000-0000-0000-000000000002', 'seller@test.com', 'seller', '$2a$10$dXJ3SW6G7P50lGmMkkmwe.20cQQubK3.HZWzG3YB1tlRy.fqvM/BG', false, false, NOW(), NOW()),
    ('00000001-0000-0000-0000-000000000003', 'admin@test.com', 'admin', '$2a$10$dXJ3SW6G7P50lGmMkkmwe.20cQQubK3.HZWzG3YB1tlRy.fqvM/BG', false, false, NOW(), NOW());

INSERT INTO user_roles (user_id, role) VALUES
    ('00000001-0000-0000-0000-000000000001', 'CUSTOMER'),
    ('00000001-0000-0000-0000-000000000002', 'SELLER'),
    ('00000001-0000-0000-0000-000000000002', 'CUSTOMER'),
    ('00000001-0000-0000-0000-000000000003', 'ADMIN'),
    ('00000001-0000-0000-0000-000000000003', 'SELLER'),
    ('00000001-0000-0000-0000-000000000003', 'CUSTOMER');

-- Categories
INSERT INTO categories (id, name, slug, display_order, active, created_at) VALUES
    ('10000001-0000-0000-0000-000000000001', 'Electronics', 'electronics', 1, true, NOW()),
    ('10000001-0000-0000-0000-000000000002', 'Clothing', 'clothing', 2, true, NOW()),
    ('10000001-0000-0000-0000-000000000003', 'Home & Garden', 'home-garden', 3, true, NOW()),
    ('10000001-0000-0000-0000-000000000004', 'Sports', 'sports', 4, true, NOW()),
    ('10000001-0000-0000-0000-000000000005', 'Books', 'books', 5, true, NOW()),
    ('10000001-0000-0000-0000-000000000006', 'Toys', 'toys', 6, true, NOW());

-- Products
INSERT INTO products (id, seller_id, category_id, title, slug, description, price, stock, active, created_at, updated_at) VALUES
    ('20000001-0000-0000-0000-000000000001', '00000001-0000-0000-0000-000000000002', '10000001-0000-0000-0000-000000000001', 'Wireless Bluetooth Headphones', 'wireless-bluetooth-headphones', 'Premium noise-cancelling wireless headphones with 30-hour battery life.', 129.99, 50, true, NOW(), NOW()),
    ('20000001-0000-0000-0000-000000000002', '00000001-0000-0000-0000-000000000002', '10000001-0000-0000-0000-000000000001', 'Smartphone 128GB', 'smartphone-128gb', 'Latest generation smartphone with high-resolution display.', 599.99, 25, true, NOW(), NOW()),
    ('20000001-0000-0000-0000-000000000003', '00000001-0000-0000-0000-000000000002', '10000001-0000-0000-0000-000000000001', 'Laptop 15.6 inch', 'laptop-15-6-inch', 'High-performance laptop with fast SSD, 16GB RAM.', 899.99, 15, true, NOW(), NOW()),
    ('20000001-0000-0000-0000-000000000004', '00000001-0000-0000-0000-000000000002', '10000001-0000-0000-0000-000000000002', 'Cotton T-Shirt', 'cotton-t-shirt', 'Comfortable 100% cotton t-shirt.', 19.99, 100, true, NOW(), NOW()),
    ('20000001-0000-0000-0000-000000000005', '00000001-0000-0000-0000-000000000002', '10000001-0000-0000-0000-000000000002', 'Denim Jeans', 'denim-jeans', 'Classic fit denim jeans.', 49.99, 75, true, NOW(), NOW()),
    ('20000001-0000-0000-0000-000000000006', '00000001-0000-0000-0000-000000000002', '10000001-0000-0000-0000-000000000003', 'Indoor Plant Set', 'indoor-plant-set', 'Set of 3 beautiful indoor plants.', 34.99, 30, true, NOW(), NOW()),
    ('20000001-0000-0000-0000-000000000007', '00000001-0000-0000-0000-000000000002', '10000001-0000-0000-0000-000000000003', 'Coffee Maker', 'coffee-maker', 'Programmable coffee maker with thermal carafe.', 79.99, 40, true, NOW(), NOW()),
    ('20000001-0000-0000-0000-000000000008', '00000001-0000-0000-0000-000000000002', '10000001-0000-0000-0000-000000000004', 'Yoga Mat', 'yoga-mat', 'Non-slip yoga mat with carrying strap.', 24.99, 60, true, NOW(), NOW()),
    ('20000001-0000-0000-0000-000000000009', '00000001-0000-0000-0000-000000000002', '10000001-0000-0000-0000-000000000004', 'Running Shoes', 'running-shoes', 'Lightweight running shoes with cushioned sole.', 89.99, 45, true, NOW(), NOW()),
    ('20000001-0000-0000-0000-000000000010', '00000001-0000-0000-0000-000000000002', '10000001-0000-0000-0000-000000000005', 'Programming Guide Book', 'programming-guide-book', 'Comprehensive guide to modern programming.', 29.99, 80, true, NOW(), NOW()),
    ('20000001-0000-0000-0000-000000000011', '00000001-0000-0000-0000-000000000002', '10000001-0000-0000-0000-000000000005', 'Cookbook Collection', 'cookbook-collection', 'Set of 5 cookbooks with recipes.', 49.99, 35, true, NOW(), NOW()),
    ('20000001-0000-0000-0000-000000000012', '00000001-0000-0000-0000-000000000002', '10000001-0000-0000-0000-000000000006', 'Building Blocks Set', 'building-blocks-set', 'Educational building blocks set with 200 pieces.', 39.99, 55, true, NOW(), NOW());

-- Product images
INSERT INTO product_images (product_id, image_order, image_url) VALUES
    ('20000001-0000-0000-0000-000000000001', 0, '/images/img.png'),
    ('20000001-0000-0000-0000-000000000002', 0, '/images/img_1.png'),
    ('20000001-0000-0000-0000-000000000003', 0, '/images/img_2.png'),
    ('20000001-0000-0000-0000-000000000004', 0, '/images/img_3.png'),
    ('20000001-0000-0000-0000-000000000005', 0, '/images/img_4.png'),
    ('20000001-0000-0000-0000-000000000006', 0, '/images/img_9.png'),
    ('20000001-0000-0000-0000-000000000007', 0, '/images/img_5.png'),
    ('20000001-0000-0000-0000-000000000008', 0, '/images/img_6.png'),
    ('20000001-0000-0000-0000-000000000009', 0, '/images/img_7.png'),
    ('20000001-0000-0000-0000-000000000010', 0, '/images/img_8.png'),
    ('20000001-0000-0000-0000-000000000011', 0, '/images/img_10.png'),
    ('20000001-0000-0000-0000-000000000012', 0, '/images/img_11.png');

-- Reviews
INSERT INTO reviews (id, product_id, author_id, rating, comment, created_at, updated_at) VALUES
    (gen_random_uuid(), '20000001-0000-0000-0000-000000000001', '00000001-0000-0000-0000-000000000001', 5, 'Great product! Highly recommend it.', NOW(), NOW()),
    (gen_random_uuid(), '20000001-0000-0000-0000-000000000001', '00000001-0000-0000-0000-000000000003', 4, 'Good value for money. Works as expected.', NOW(), NOW()),
    (gen_random_uuid(), '20000001-0000-0000-0000-000000000002', '00000001-0000-0000-0000-000000000001', 5, 'Great product! Highly recommend it.', NOW(), NOW()),
    (gen_random_uuid(), '20000001-0000-0000-0000-000000000002', '00000001-0000-0000-0000-000000000003', 5, 'Good value for money. Works as expected.', NOW(), NOW()),
    (gen_random_uuid(), '20000001-0000-0000-0000-000000000003', '00000001-0000-0000-0000-000000000001', 4, 'Great product! Highly recommend it.', NOW(), NOW()),
    (gen_random_uuid(), '20000001-0000-0000-0000-000000000003', '00000001-0000-0000-0000-000000000003', 4, 'Good value for money. Works as expected.', NOW(), NOW()),
    (gen_random_uuid(), '20000001-0000-0000-0000-000000000004', '00000001-0000-0000-0000-000000000001', 5, 'Great product! Highly recommend it.', NOW(), NOW()),
    (gen_random_uuid(), '20000001-0000-0000-0000-000000000004', '00000001-0000-0000-0000-000000000003', 3, 'Good value for money. Works as expected.', NOW(), NOW()),
    (gen_random_uuid(), '20000001-0000-0000-0000-000000000005', '00000001-0000-0000-0000-000000000001', 4, 'Great product! Highly recommend it.', NOW(), NOW()),
    (gen_random_uuid(), '20000001-0000-0000-0000-000000000005', '00000001-0000-0000-0000-000000000003', 5, 'Good value for money. Works as expected.', NOW(), NOW());

-- Update product ratings
UPDATE products SET rating = 4.5, review_count = 2, updated_at = NOW() WHERE id = '20000001-0000-0000-0000-000000000001';
UPDATE products SET rating = 5.0, review_count = 2, updated_at = NOW() WHERE id = '20000001-0000-0000-0000-000000000002';
UPDATE products SET rating = 4.0, review_count = 2, updated_at = NOW() WHERE id = '20000001-0000-0000-0000-000000000003';
UPDATE products SET rating = 4.0, review_count = 2, updated_at = NOW() WHERE id = '20000001-0000-0000-0000-000000000004';
UPDATE products SET rating = 4.5, review_count = 2, updated_at = NOW() WHERE id = '20000001-0000-0000-0000-000000000005';

-- Delivery options
INSERT INTO delivery_options (id, name, type, price, estimated_days_min, estimated_days_max, pickup_address, active, created_at) VALUES
    ('30000001-0000-0000-0000-000000000001', 'Pickup point', 'PICKUP', 0, 3, 5, '123 Main St, New York, NY 10001', true, NOW()),
    ('30000001-0000-0000-0000-000000000002', 'Courier', 'COURIER', 9.99, 2, 4, NULL, true, NOW());
