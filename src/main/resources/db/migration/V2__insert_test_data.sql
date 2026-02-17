-- Test users (password: password, BCrypt hash)
INSERT INTO users (id, email, username, password, blocked, email_verified, created_at, updated_at)
VALUES (gen_random_uuid(), 'customer@test.com', 'customer',
        '$2a$10$dXJ3SW6G7P50lGmMkkmwe.20cQQubK3.HZWzG3YB1tlRy.fqvM/BG', false, false, NOW(), NOW()),
       (gen_random_uuid(), 'seller@test.com', 'seller', '$2a$10$dXJ3SW6G7P50lGmMkkmwe.20cQQubK3.HZWzG3YB1tlRy.fqvM/BG',
        false, false, NOW(), NOW()),
       (gen_random_uuid(), 'admin@test.com', 'admin', '$2a$10$dXJ3SW6G7P50lGmMkkmwe.20cQQubK3.HZWzG3YB1tlRy.fqvM/BG',
        false, false, NOW(), NOW());

-- Сохраняем сгенерированные UUID во временные переменные (для PostgreSQL)
DO
$$
    DECLARE
        customer_id UUID;
        seller_id   UUID;
        admin_id    UUID;
    BEGIN
        SELECT id INTO customer_id FROM users WHERE email = 'customer@test.com';
        SELECT id INTO seller_id FROM users WHERE email = 'seller@test.com';
        SELECT id INTO admin_id FROM users WHERE email = 'admin@test.com';

        -- User roles
        INSERT INTO user_roles (user_id, role)
        VALUES (customer_id, 'CUSTOMER'),
               (seller_id, 'SELLER'),
               (seller_id, 'CUSTOMER'),
               (admin_id, 'ADMIN'),
               (admin_id, 'SELLER'),
               (admin_id, 'CUSTOMER');

        -- Categories
        INSERT INTO categories (id, name, slug, display_order, active, created_at)
        VALUES (gen_random_uuid(), 'Electronics', 'electronics', 1, true, NOW()),
               (gen_random_uuid(), 'Clothing', 'clothing', 2, true, NOW()),
               (gen_random_uuid(), 'Home & Garden', 'home-garden', 3, true, NOW()),
               (gen_random_uuid(), 'Sports', 'sports', 4, true, NOW()),
               (gen_random_uuid(), 'Books', 'books', 5, true, NOW()),
               (gen_random_uuid(), 'Toys', 'toys', 6, true, NOW());

        -- Products
        INSERT INTO products (id, seller_id, category_id, title, slug, description, price, stock, active, created_at,
                              updated_at)
        SELECT gen_random_uuid(),
               seller_id,
               c.id,
               'Wireless Bluetooth Headphones',
               'wireless-bluetooth-headphones',
               'Premium noise-cancelling wireless headphones with 30-hour battery life.',
               129.99,
               50,
               true,
               NOW(),
               NOW()
        FROM categories c
        WHERE c.slug = 'electronics';

        INSERT INTO products (id, seller_id, category_id, title, slug, description, price, stock, active, created_at,
                              updated_at)
        SELECT gen_random_uuid(),
               seller_id,
               c.id,
               'Smartphone 128GB',
               'smartphone-128gb',
               'Latest generation smartphone with high-resolution display.',
               599.99,
               25,
               true,
               NOW(),
               NOW()
        FROM categories c
        WHERE c.slug = 'electronics';

        INSERT INTO products (id, seller_id, category_id, title, slug, description, price, stock, active, created_at,
                              updated_at)
        SELECT gen_random_uuid(),
               seller_id,
               c.id,
               'Laptop 15.6 inch',
               'laptop-15-6-inch',
               'High-performance laptop with fast SSD, 16GB RAM.',
               899.99,
               15,
               true,
               NOW(),
               NOW()
        FROM categories c
        WHERE c.slug = 'electronics';

        INSERT INTO products (id, seller_id, category_id, title, slug, description, price, stock, active, created_at,
                              updated_at)
        SELECT gen_random_uuid(),
               seller_id,
               c.id,
               'Cotton T-Shirt',
               'cotton-t-shirt',
               'Comfortable 100% cotton t-shirt.',
               19.99,
               100,
               true,
               NOW(),
               NOW()
        FROM categories c
        WHERE c.slug = 'clothing';

        INSERT INTO products (id, seller_id, category_id, title, slug, description, price, stock, active, created_at,
                              updated_at)
        SELECT gen_random_uuid(),
               seller_id,
               c.id,
               'Denim Jeans',
               'denim-jeans',
               'Classic fit denim jeans.',
               49.99,
               75,
               true,
               NOW(),
               NOW()
        FROM categories c
        WHERE c.slug = 'clothing';

        INSERT INTO products (id, seller_id, category_id, title, slug, description, price, stock, active, created_at,
                              updated_at)
        SELECT gen_random_uuid(),
               seller_id,
               c.id,
               'Indoor Plant Set',
               'indoor-plant-set',
               'Set of 3 beautiful indoor plants.',
               34.99,
               30,
               true,
               NOW(),
               NOW()
        FROM categories c
        WHERE c.slug = 'home-garden';

        INSERT INTO products (id, seller_id, category_id, title, slug, description, price, stock, active, created_at,
                              updated_at)
        SELECT gen_random_uuid(),
               seller_id,
               c.id,
               'Coffee Maker',
               'coffee-maker',
               'Programmable coffee maker with thermal carafe.',
               79.99,
               40,
               true,
               NOW(),
               NOW()
        FROM categories c
        WHERE c.slug = 'home-garden';

        INSERT INTO products (id, seller_id, category_id, title, slug, description, price, stock, active, created_at,
                              updated_at)
        SELECT gen_random_uuid(),
               seller_id,
               c.id,
               'Yoga Mat',
               'yoga-mat',
               'Non-slip yoga mat with carrying strap.',
               24.99,
               60,
               true,
               NOW(),
               NOW()
        FROM categories c
        WHERE c.slug = 'sports';

        INSERT INTO products (id, seller_id, category_id, title, slug, description, price, stock, active, created_at,
                              updated_at)
        SELECT gen_random_uuid(),
               seller_id,
               c.id,
               'Running Shoes',
               'running-shoes',
               'Lightweight running shoes with cushioned sole.',
               89.99,
               45,
               true,
               NOW(),
               NOW()
        FROM categories c
        WHERE c.slug = 'sports';

        INSERT INTO products (id, seller_id, category_id, title, slug, description, price, stock, active, created_at,
                              updated_at)
        SELECT gen_random_uuid(),
               seller_id,
               c.id,
               'Programming Guide Book',
               'programming-guide-book',
               'Comprehensive guide to modern programming.',
               29.99,
               80,
               true,
               NOW(),
               NOW()
        FROM categories c
        WHERE c.slug = 'books';

        INSERT INTO products (id, seller_id, category_id, title, slug, description, price, stock, active, created_at,
                              updated_at)
        SELECT gen_random_uuid(),
               seller_id,
               c.id,
               'Cookbook Collection',
               'cookbook-collection',
               'Set of 5 cookbooks with recipes.',
               49.99,
               35,
               true,
               NOW(),
               NOW()
        FROM categories c
        WHERE c.slug = 'books';

        INSERT INTO products (id, seller_id, category_id, title, slug, description, price, stock, active, created_at,
                              updated_at)
        SELECT gen_random_uuid(),
               seller_id,
               c.id,
               'Building Blocks Set',
               'building-blocks-set',
               'Educational building blocks set with 200 pieces.',
               39.99,
               55,
               true,
               NOW(),
               NOW()
        FROM categories c
        WHERE c.slug = 'toys';

        -- Product images (для всех продуктов)
        INSERT INTO product_images (product_id, image_order, image_url)
        SELECT id, 0, '/images/img.png'
        FROM products
        WHERE title = 'Wireless Bluetooth Headphones';

        INSERT INTO product_images (product_id, image_order, image_url)
        SELECT id, 0, '/images/img_1.png'
        FROM products
        WHERE title = 'Smartphone 128GB';

        INSERT INTO product_images (product_id, image_order, image_url)
        SELECT id, 0, '/images/img_2.png'
        FROM products
        WHERE title = 'Laptop 15.6 inch';

        INSERT INTO product_images (product_id, image_order, image_url)
        SELECT id, 0, '/images/img_3.png'
        FROM products
        WHERE title = 'Cotton T-Shirt';

        INSERT INTO product_images (product_id, image_order, image_url)
        SELECT id, 0, '/images/img_4.png'
        FROM products
        WHERE title = 'Denim Jeans';

        INSERT INTO product_images (product_id, image_order, image_url)
        SELECT id, 0, '/images/img_9.png'
        FROM products
        WHERE title = 'Indoor Plant Set';

        INSERT INTO product_images (product_id, image_order, image_url)
        SELECT id, 0, '/images/img_5.png'
        FROM products
        WHERE title = 'Coffee Maker';

        INSERT INTO product_images (product_id, image_order, image_url)
        SELECT id, 0, '/images/img_6.png'
        FROM products
        WHERE title = 'Yoga Mat';

        INSERT INTO product_images (product_id, image_order, image_url)
        SELECT id, 0, '/images/img_7.png'
        FROM products
        WHERE title = 'Running Shoes';

        INSERT INTO product_images (product_id, image_order, image_url)
        SELECT id, 0, '/images/img_8.png'
        FROM products
        WHERE title = 'Programming Guide Book';

        INSERT INTO product_images (product_id, image_order, image_url)
        SELECT id, 0, '/images/img_10.png'
        FROM products
        WHERE title = 'Cookbook Collection';

        INSERT INTO product_images (product_id, image_order, image_url)
        SELECT id, 0, '/images/img_11.png'
        FROM products
        WHERE title = 'Building Blocks Set';

        -- Reviews
        INSERT INTO reviews (id, product_id, author_id, rating, comment, created_at, updated_at)
        SELECT gen_random_uuid(), p.id, customer_id, 5, 'Great product! Highly recommend it.', NOW(), NOW()
        FROM products p
        WHERE p.title IN ('Wireless Bluetooth Headphones', 'Smartphone 128GB', 'Cotton T-Shirt');

        INSERT INTO reviews (id, product_id, author_id, rating, comment, created_at, updated_at)
        SELECT gen_random_uuid(), p.id, admin_id, 4, 'Good value for money. Works as expected.', NOW(), NOW()
        FROM products p
        WHERE p.title IN ('Wireless Bluetooth Headphones', 'Laptop 15.6 inch', 'Denim Jeans');

        -- Update product ratings (только для продуктов с отзывами)
        UPDATE products
        SET rating       = 4.5,
            review_count = 2,
            updated_at   = NOW()
        WHERE title = 'Wireless Bluetooth Headphones';

        UPDATE products
        SET rating       = 5.0,
            review_count = 1,
            updated_at   = NOW()
        WHERE title = 'Smartphone 128GB';

        UPDATE products
        SET rating       = 4.0,
            review_count = 1,
            updated_at   = NOW()
        WHERE title = 'Laptop 15.6 inch';

        UPDATE products
        SET rating       = 5.0,
            review_count = 1,
            updated_at   = NOW()
        WHERE title = 'Cotton T-Shirt';

        UPDATE products
        SET rating       = 4.0,
            review_count = 1,
            updated_at   = NOW()
        WHERE title = 'Denim Jeans';

        -- Delivery options
        INSERT INTO delivery_options (id, name, type, price, estimated_days_min, estimated_days_max, pickup_address,
                                      active, created_at)
        VALUES (gen_random_uuid(), 'Pickup point', 'PICKUP', 0, 3, 5, '123 Main St, New York, NY 10001', true, NOW()),
               (gen_random_uuid(), 'Courier', 'COURIER', 9.99, 2, 4, NULL, true, NOW());

    END
$$;