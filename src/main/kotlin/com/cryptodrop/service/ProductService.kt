package com.cryptodrop.service

import com.cryptodrop.persistence.category.Category
import com.cryptodrop.persistence.category.CategoryRepository
import com.cryptodrop.persistence.product.Product
import com.cryptodrop.persistence.product.ProductRepository
import com.cryptodrop.persistence.product.ProductStatus
import com.cryptodrop.service.dto.ProductCreateDto
import com.cryptodrop.service.dto.ProductFilterDto
import com.cryptodrop.service.dto.ProductResponseDto
import com.cryptodrop.service.dto.ProductUpdateDto
import org.slf4j.LoggerFactory
import org.springframework.cache.annotation.CacheEvict
import org.springframework.cache.annotation.Cacheable
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Pageable
import org.springframework.data.domain.Sort
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.math.BigDecimal
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.UUID

@Service
class ProductService(
    private val productRepository: ProductRepository,
    private val categoryRepository: CategoryRepository,
    private val userService: UserService,
    private val wishlistService: WishlistService
) {
    private val logger = LoggerFactory.getLogger(javaClass)
    private val dateFormatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME

    private fun getOrCreateCategory(name: String): Category {
        return categoryRepository.findByName(name)
            .orElseGet {
                val slug = name.lowercase().replace(" ", "-").replace(Regex("[^a-z0-9-]"), "")
                categoryRepository.save(Category(name = name, slug = slug))
            }
    }

    private fun slugFromTitle(title: String): String {
        return title.lowercase()
            .replace(Regex("[^a-z0-9\\s-]"), "")
            .replace(Regex("\\s+"), "-")
            .replace(Regex("-+"), "-")
            .trim()
    }

    private val allowedImageExtensions = setOf("png", "jpg", "jpeg")
    private fun validateImageUrls(images: List<String>) {
        for (url in images) {
            val pathPart = url.substringBefore('?')
            val ext = pathPart.substringAfterLast('.', "").lowercase()
            if (ext !in allowedImageExtensions) {
                throw IllegalArgumentException("Image URLs must end with .png, .jpg or .jpeg, got: $url")
            }
        }
    }

    @CacheEvict(value = ["products", "categories"], allEntries = true)
    @Transactional
    fun createProduct(sellerId: UUID, dto: ProductCreateDto): Product {
        logger.info("Creating product: ${dto.title} by seller: $sellerId")
        validateImageUrls(dto.images)
        val seller = userService.findById(sellerId)
        val category = getOrCreateCategory(dto.category)
        val product = Product(
            seller = seller,
            category = category,
            title = dto.title,
            slug = slugFromTitle(dto.title),
            description = dto.description,
            price = dto.price,
            images = dto.images.toMutableList(),
            attributes = dto.attributes.toMutableMap(),
            stock = dto.stock,
            status = ProductStatus.DRAFT
        )
        return productRepository.save(product)
    }

    @CacheEvict(value = ["products"], allEntries = true)
    @Transactional
    fun updateProduct(productId: UUID, sellerId: UUID, dto: ProductUpdateDto): Product {
        val product = productRepository.findById(productId)
            .orElseThrow { IllegalArgumentException("Product not found: $productId") }

        if (product.seller.id != sellerId) {
            throw IllegalStateException("Only product owner can update it")
        }

        val newImages = dto.images ?: product.images
        validateImageUrls(newImages)

        val category = dto.category?.let { getOrCreateCategory(it) } ?: product.category
        val newStatus = dto.status ?: product.status
        val updatedProduct = product.copy(
            title = dto.title ?: product.title,
            description = dto.description ?: product.description,
            price = dto.price ?: product.price,
            category = category,
            images = newImages.toMutableList(),
            attributes = dto.attributes?.toMutableMap() ?: product.attributes,
            stock = dto.stock ?: product.stock,
            active = dto.active ?: product.active,
            status = newStatus,
            updatedAt = LocalDateTime.now()
        )
        return productRepository.save(updatedProduct)
    }

    @Cacheable("products")
    fun findById(productId: UUID): Product {
        return productRepository.findById(productId)
            .orElseThrow { IllegalArgumentException("Product not found: $productId") }
    }

    fun findAllActive(pageable: Pageable): Page<Product> {
        return productRepository.findAllActive(pageable)
    }

    fun findBySeller(sellerId: UUID, pageable: Pageable): Page<Product> {
        return productRepository.findBySellerId(sellerId, pageable)
    }

    fun findByCategory(category: String, pageable: Pageable): Page<Product> {
        return productRepository.findByCategoryName(category, pageable)
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
                productRepository.findByRatingGreaterThanEqual(
                    BigDecimal.valueOf(filter.minRating),
                    pageable
                )
            }
            filter.category != null -> {
                productRepository.findByCategoryName(filter.category, pageable)
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
            .groupBy { it.category.name }
            .mapValues { it.value.size }
            .toList()
            .sortedByDescending { it.second }
            .take(limit)
            .map { it.first }
    }

    fun getRecommendedProducts(userId: UUID?, limit: Int = 10): List<Product> {
        val favoriteIds = userId?.let { wishlistService.getFavoriteProductIds(it) } ?: emptyList()
        val favoriteCategories = if (favoriteIds.isNotEmpty()) {
            productRepository.findByIdIn(favoriteIds)
                .map { it.category.name }
                .distinct()
        } else emptyList()

        return if (favoriteCategories.isNotEmpty()) {
            favoriteCategories.flatMap { category ->
                productRepository.findByCategoryName(category, PageRequest.of(0, limit)).content
            }.distinctBy { it.id }.take(limit)
        } else {
            productRepository.findAllActive(PageRequest.of(0, limit)).content
        }
    }

    fun findByIds(ids: List<UUID>, pageable: Pageable): List<ProductResponseDto> {
        if (ids.isEmpty()) return emptyList()
        val products = productRepository.findByIdIn(ids)
            .sortedBy { ids.indexOf(it.id) }
        val start = (pageable.pageNumber * pageable.pageSize).coerceAtMost(products.size)
        val end = (start + pageable.pageSize).coerceAtMost(products.size)
        return products.subList(start, end).map { toDto(it, it.seller.username) }
    }

    @CacheEvict(value = ["products"], allEntries = true)
    @Transactional
    fun deleteProduct(productId: UUID, sellerId: UUID): Unit {
        val product = productRepository.findById(productId)
            .orElseThrow { IllegalArgumentException("Product not found: $productId") }
        if (product.seller.id != sellerId) {
            throw IllegalStateException("Only product owner can delete it")
        }
        productRepository.delete(product)
        logger.info("Deleted product: $productId")
    }

    @CacheEvict(value = ["products"], allEntries = true)
    @Transactional
    fun publishProduct(productId: UUID, sellerId: UUID): Product {
        val product = productRepository.findById(productId)
            .orElseThrow { IllegalArgumentException("Product not found: $productId") }
        if (product.seller.id != sellerId) {
            throw IllegalStateException("Only product owner can publish it")
        }
        val updated = product.copy(
            status = ProductStatus.PUBLISHED,
            active = true,
            updatedAt = LocalDateTime.now()
        )
        return productRepository.save(updated)
    }

    fun findFeatured(limit: Int): List<Product> {
        return productRepository.findByIsFeaturedTrueOrderByUpdatedAtDesc(PageRequest.of(0, limit)).content
    }

    @CacheEvict(value = ["products"], allEntries = true)
    @Transactional
    fun setFeatured(productId: UUID, featured: Boolean): Product {
        val product = productRepository.findById(productId)
            .orElseThrow { IllegalArgumentException("Product not found: $productId") }
        val updated = product.copy(
            isFeatured = featured,
            updatedAt = LocalDateTime.now()
        )
        return productRepository.save(updated)
    }

    @CacheEvict(value = ["products"], allEntries = true)
    @Transactional
    fun unpublishProduct(productId: UUID, sellerId: UUID): Product {
        val product = productRepository.findById(productId)
            .orElseThrow { IllegalArgumentException("Product not found: $productId") }
        if (product.seller.id != sellerId) {
            throw IllegalStateException("Only product owner can unpublish it")
        }
        val updated = product.copy(
            status = ProductStatus.DRAFT,
            active = false,
            updatedAt = LocalDateTime.now()
        )
        return productRepository.save(updated)
    }

    fun toDto(product: Product, sellerName: String? = null): ProductResponseDto {
        val seller = sellerName ?: product.seller.username
        return ProductResponseDto(
            id = product.id.toString(),
            sellerId = product.seller.id.toString(),
            sellerName = seller,
            title = product.title,
            description = product.description,
            price = product.price,
            category = product.category.name,
            images = product.images,
            rating = product.rating.toDouble(),
            reviewCount = product.reviewCount,
            attributes = product.attributes,
            stock = product.stock,
            active = product.active,
            status = product.status.name,
            isFeatured = product.isFeatured,
            createdAt = product.createdAt.format(dateFormatter),
            updatedAt = product.updatedAt.format(dateFormatter)
        )
    }
}
