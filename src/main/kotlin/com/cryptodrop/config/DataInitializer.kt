package com.cryptodrop.config

import com.cryptodrop.model.*
import com.cryptodrop.repository.*
import org.springframework.boot.CommandLineRunner
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Component
import java.math.BigDecimal
import java.time.LocalDateTime

@Component
class DataInitializer(
    private val userRepository: UserRepository,
    private val productRepository: ProductRepository,
    private val reviewRepository: ReviewRepository,
    private val orderRepository: OrderRepository,
    private val deliveryOptionRepository: DeliveryOptionRepository,
    private val passwordEncoder: PasswordEncoder
) : CommandLineRunner {

    override fun run(vararg args: String?) {
        // Create test users
        val customer = userRepository.save(
            User(
                email = "customer@test.com",
                username = "customer",
                password = passwordEncoder.encode("password"),
                roles = setOf(UserRole.CUSTOMER)
            )
        )

        val seller = userRepository.save(
            User(
                email = "seller@test.com",
                username = "seller",
                password = passwordEncoder.encode("password"),
                roles = setOf(UserRole.SELLER, UserRole.CUSTOMER)
            )
        )

        val admin = userRepository.save(
            User(
                email = "admin@test.com",
                username = "admin",
                password = passwordEncoder.encode("password"),
                roles = setOf(UserRole.ADMIN, UserRole.SELLER, UserRole.CUSTOMER)
            )
        )

        // Create test products
        val categories = listOf("Electronics", "Clothing", "Home & Garden", "Sports", "Books", "Toys")
        val products = mutableListOf<Product>()

        // Electronics
        products.add(Product(
            sellerId = seller.id!!,
            title = "Wireless Bluetooth Headphones",
            description = "Premium noise-cancelling wireless headphones with 30-hour battery life. Perfect for music lovers and professionals.",
            price = BigDecimal("129.99"),
            category = "Electronics",
            images = mutableListOf("/images/img.png"),
            stock = 50,
            active = true
        ))

        products.add(Product(
            sellerId = seller.id!!,
            title = "Smartphone 128GB",
            description = "Latest generation smartphone with high-resolution display, powerful processor, and excellent camera system.",
            price = BigDecimal("599.99"),
            category = "Electronics",
            images = mutableListOf("/images/img_1.png"),
            stock = 25,
            active = true
        ))

        products.add(Product(
            sellerId = seller.id!!,
            title = "Laptop 15.6 inch",
            description = "High-performance laptop with fast SSD, 16GB RAM, and long battery life. Ideal for work and entertainment.",
            price = BigDecimal("899.99"),
            category = "Electronics",
            images = mutableListOf("/images/img_2.png"),
            stock = 15,
            active = true
        ))

        // Clothing
        products.add(Product(
            sellerId = seller.id!!,
            title = "Cotton T-Shirt",
            description = "Comfortable 100% cotton t-shirt in various colors. Perfect for everyday wear.",
            price = BigDecimal("19.99"),
            category = "Clothing",
            images = mutableListOf("/images/img_3.png"),
            stock = 100,
            active = true
        ))

        products.add(Product(
            sellerId = seller.id!!,
            title = "Denim Jeans",
            description = "Classic fit denim jeans made from premium cotton. Durable and stylish.",
            price = BigDecimal("49.99"),
            category = "Clothing",
            images = mutableListOf("/images/img_4.png"),
            stock = 75,
            active = true
        ))

        // Home & Garden
        products.add(Product(
            sellerId = seller.id!!,
            title = "Indoor Plant Set",
            description = "Set of 3 beautiful indoor plants perfect for home decoration. Includes care instructions.",
            price = BigDecimal("34.99"),
            category = "Home & Garden",
            images = mutableListOf("/images/img_9.png"),
            stock = 30,
            active = true
        ))

        products.add(Product(
            sellerId = seller.id!!,
            title = "Coffee Maker",
            description = "Programmable coffee maker with thermal carafe. Makes up to 12 cups of delicious coffee.",
            price = BigDecimal("79.99"),
            category = "Home & Garden",
            images = mutableListOf("/images/img_5.png"),
            stock = 40,
            active = true
        ))

        // Sports
        products.add(Product(
            sellerId = seller.id!!,
            title = "Yoga Mat",
            description = "Non-slip yoga mat with carrying strap. Perfect for yoga, pilates, and fitness exercises.",
            price = BigDecimal("24.99"),
            category = "Sports",
            images = mutableListOf("/images/img_6.png"),
            stock = 60,
            active = true
        ))

        products.add(Product(
            sellerId = seller.id!!,
            title = "Running Shoes",
            description = "Lightweight running shoes with cushioned sole. Designed for comfort and performance.",
            price = BigDecimal("89.99"),
            category = "Sports",
            images = mutableListOf("/images/img_7.png"),
            stock = 45,
            active = true
        ))

        // Books
        products.add(Product(
            sellerId = seller.id!!,
            title = "Programming Guide Book",
            description = "Comprehensive guide to modern programming practices. Perfect for beginners and experienced developers.",
            price = BigDecimal("29.99"),
            category = "Books",
            images = mutableListOf("/images/img_8.png"),
            stock = 80,
            active = true
        ))

        products.add(Product(
            sellerId = seller.id!!,
            title = "Cookbook Collection",
            description = "Set of 5 cookbooks with recipes from around the world. Great gift for cooking enthusiasts.",
            price = BigDecimal("49.99"),
            category = "Books",
            images = mutableListOf("/images/img_10.png"),
            stock = 35,
            active = true
        ))

        // Toys
        products.add(Product(
            sellerId = seller.id!!,
            title = "Building Blocks Set",
            description = "Educational building blocks set with 200 pieces. Encourages creativity and problem-solving.",
            price = BigDecimal("39.99"),
            category = "Toys",
            images = mutableListOf("/images/img_11.png"),
            stock = 55,
            active = true
        ))

        val savedProducts = productRepository.saveAll(products)

        // Create some reviews
        savedProducts.take(5).forEach { product ->
            reviewRepository.save(
                Review(
                    productId = product.id!!,
                    authorId = customer.id!!,
                    authorName = customer.username,
                    rating = (4..5).random(),
                    comment = "Great product! Highly recommend it. Quality is excellent and delivery was fast."
                )
            )
            reviewRepository.save(
                Review(
                    productId = product.id!!,
                    authorId = admin.id!!,
                    authorName = admin.username,
                    rating = (3..5).random(),
                    comment = "Good value for money. Works as expected."
                )
            )
        }

        // Update product ratings
        savedProducts.forEach { product ->
            val reviews = reviewRepository.findByProductId(product.id!!, org.springframework.data.domain.PageRequest.of(0, 100))
            if (reviews.totalElements > 0) {
                val avgRating = reviews.content.map { it.rating }.average()
                val reviewCount = reviews.totalElements.toInt()
                productRepository.save(
                    product.copy(
                        rating = avgRating,
                        reviewCount = reviewCount
                    )
                )
            }
        }

        // Delivery options (only if none exist)
        if (deliveryOptionRepository.count() == 0L) {
            deliveryOptionRepository.save(
                DeliveryOption(
                name = "Pickup point",
                type = DeliveryType.PICKUP,
                price = BigDecimal.ZERO,
                estimatedDays = 3,
                addressLine = "123 Main St",
                city = "New York",
                region = "NY",
                zipCode = "10001",
                country = "USA",
                active = true
            )
        )
        deliveryOptionRepository.save(
            DeliveryOption(
                name = "Courier",
                type = DeliveryType.COURIER,
                price = BigDecimal("9.99"),
                estimatedDays = 2,
                city = "New York",
                region = "NY",
                country = "USA",
                active = true
            )
        )
        }

        println("✅ Test data initialized successfully!")
        println("   Users: customer, seller, admin (password: password)")
        println("   Products: ${savedProducts.size} items created")
    }
}




