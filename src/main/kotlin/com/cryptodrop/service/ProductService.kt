package com.cryptodrop.service

import com.cryptodrop.dto.ProductCreateDto
import com.cryptodrop.dto.ProductFilterDto
import com.cryptodrop.dto.ProductResponseDto
import com.cryptodrop.dto.ProductUpdateDto
import com.cryptodrop.model.Product
import com.cryptodrop.repository.ProductRepository
import org.slf4j.LoggerFactory
import org.springframework.cache.annotation.CacheEvict
import org.springframework.cache.annotation.Cacheable
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Pageable
import org.springframework.data.domain.Sort
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

@Service
class ProductService(
    private val productRepository: ProductRepository,
    private val userService: UserService
) {
    private val logger = LoggerFactory.getLogger(javaClass)
    private val dateFormatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME

    @CacheEvict(value = ["products", "categories"], allEntries = true)
    @Transactional
    fun createProduct(sellerId: Long, dto: ProductCreateDto): Product {
        logger.info("Creating product: ${dto.title} by seller: $sellerId")
        val product = Product(
            sellerId = sellerId,
            title = dto.title,
            description = dto.description,
            price = dto.price,
            category = dto.category,
            images = dto.images.toMutableList(),
            attributes = dto.attributes,
            stock = dto.stock
        )
        return productRepository.save(product)
    }

    @CacheEvict(value = ["products"], allEntries = true)
    @Transactional
    fun updateProduct(productId: Long, sellerId: Long, dto: ProductUpdateDto): Product {
        val product = productRepository.findById(productId)
            .orElseThrow { IllegalArgumentException("Product not found: $productId") }
        
        if (product.sellerId != sellerId) {
            throw IllegalStateException("Only product owner can update it")
        }

        val updatedProduct = product.copy(
            title = dto.title ?: product.title,
            description = dto.description ?: product.description,
            price = dto.price ?: product.price,
            category = dto.category ?: product.category,
            images = dto.images?.toMutableList() ?: product.images,
            attributes = dto.attributes ?: product.attributes,
            stock = dto.stock ?: product.stock,
            active = dto.active ?: product.active,
            updatedAt = LocalDateTime.now()
        )
        return productRepository.save(updatedProduct)
    }

    @Cacheable("products")
    fun findById(productId: Long): Product {
        return productRepository.findById(productId)
            .orElseThrow { IllegalArgumentException("Product not found: $productId") }
    }

    fun findAllActive(pageable: Pageable): Page<Product> {
        return productRepository.findAllActive(pageable)
    }

    fun findBySeller(sellerId: Long, pageable: Pageable): Page<Product> {
        return productRepository.findBySellerId(sellerId, pageable)
    }

    fun findByCategory(category: String, pageable: Pageable): Page<Product> {
        return productRepository.findByCategory(category, pageable)
    }

    fun searchProducts(filter: ProductFilterDto, page: Int, size: Int): Page<Product> {
        val sort = when (filter.sortBy) {
            "price" -> Sort.by(if (filter.sortOrder == "asc") Sort.Direction.ASC else Sort.Direction.DESC, "price")
            "rating" -> Sort.by(if (filter.sortOrder == "asc") Sort.Direction.ASC else Sort.Direction.DESC, "rating")
            "reviewCount" -> Sort.by(if (filter.sortOrder == "asc") Sort.Direction.ASC else Sort.Direction.DESC, "reviewCount")
            else -> Sort.by(if (filter.sortOrder == "asc") Sort.Direction.ASC else Sort.Direction.DESC, "createdAt")
        }
        
        val pageable = PageRequest.of(page, size, sort)
        
        return when {
            filter.category != null && filter.minPrice != null && filter.maxPrice != null -> {
                productRepository.findByCategoryAndPriceBetween(
                    filter.category,
                    filter.minPrice,
                    filter.maxPrice,
                    pageable
                )
            }
            filter.minRating != null -> {
                productRepository.findByRatingGreaterThanEqual(filter.minRating, pageable)
            }
            filter.category != null -> {
                productRepository.findByCategory(filter.category, pageable)
            }
            else -> {
                productRepository.findAllActive(pageable)
            }
        }
    }

    @Cacheable("categories")
    fun getPopularCategories(limit: Int = 10): List<String> {
        val products = productRepository.findAllActive(PageRequest.of(0, 1000))
        return products.content
            .groupBy { it.category }
            .mapValues { it.value.size }
            .toList()
            .sortedByDescending { it.second }
            .take(limit)
            .map { it.first }
    }

    fun getRecommendedProducts(userId: Long?, limit: Int = 10): List<Product> {
        val user = userId?.let { userService.findById(it) }
        val favoriteCategories = user?.favoriteProductIds?.let { productIds ->
            productRepository.findByIdIn(productIds.toList())
                .map { it.category }
                .distinct()
        } ?: emptyList()

        return if (favoriteCategories.isNotEmpty()) {
            favoriteCategories.flatMap { category ->
                productRepository.findByCategory(category, PageRequest.of(0, limit)).content
            }.distinctBy { it.id }.take(limit)
        } else {
            productRepository.findAllActive(PageRequest.of(0, limit)).content
        }
    }

    fun findByIds(ids: List<Long>, pageable: Pageable): List<ProductResponseDto> {
        if (ids.isEmpty()) return emptyList()

        val products = productRepository.findByIdIn(ids)
            // Сортируем по порядку ID из избранного
            .sortedBy { ids.indexOf(it.id!!) }

        // Пагинация на уровне сервиса
        val start = (pageable.pageNumber * pageable.pageSize).coerceAtMost(products.size)
        val end = (start + pageable.pageSize).coerceAtMost(products.size)

        return products.subList(start, end).map {
            toDto(it, userService.findById(it.sellerId).username)
        }
    }

    fun toDto(product: Product, sellerName: String? = null): ProductResponseDto {
        val seller = sellerName ?: userService.findById(product.sellerId).username
        return ProductResponseDto(
            id = product.id.toString(),
            sellerId = product.sellerId.toString(),
            sellerName = seller,
            title = product.title,
            description = product.description,
            price = product.price,
            category = product.category,
            images = product.images,
            rating = product.rating,
            reviewCount = product.reviewCount,
            attributes = product.attributes,
            stock = product.stock,
            active = product.active,
            createdAt = product.createdAt.format(dateFormatter),
            updatedAt = product.updatedAt.format(dateFormatter)
        )
    }
}
