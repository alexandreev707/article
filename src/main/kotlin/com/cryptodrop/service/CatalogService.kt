package com.cryptodrop.service

import com.cryptodrop.domain.model.Product
import com.cryptodrop.domain.model.ProductStatus
import com.cryptodrop.domain.repository.CategoryRepository
import com.cryptodrop.domain.repository.ProductRepository
import mu.KotlinLogging
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.math.BigDecimal

private val logger = KotlinLogging.logger {}

@Service
@Transactional
class CatalogService(
    private val productRepository: ProductRepository,
    private val categoryRepository: CategoryRepository
) {
    
    fun getProducts(
        categoryId: Long? = null,
        maxPrice: BigDecimal? = null,
        query: String? = null,
        page: Int = 0,
        size: Int = 20
    ): Page<Product> {
        val pageable: Pageable = PageRequest.of(page, size)
        
        return when {
            !query.isNullOrBlank() -> {
                productRepository.searchProducts(ProductStatus.ACTIVE, query, pageable)
            }
            else -> {
                productRepository.findActiveProducts(ProductStatus.ACTIVE, maxPrice, categoryId, pageable)
            }
        }
    }
    
    fun getProductById(id: Long): Product? {
        return productRepository.findById(id).orElse(null)
    }
    
    fun createProduct(product: Product): Product {
        logger.info { "Creating product: ${product.title} by seller ${product.sellerWallet}" }
        return productRepository.save(product)
    }
    
    fun updateProduct(id: Long, product: Product): Product? {
        val existing = productRepository.findById(id).orElse(null) ?: return null
        val updated = existing.copy(
            title = product.title,
            description = product.description,
            priceUsd = product.priceUsd,
            images = product.images,
            specs = product.specs,
            shippingProfiles = product.shippingProfiles,
            updatedAt = java.time.Instant.now()
        )
        return productRepository.save(updated)
    }
    
    fun getSellerProducts(sellerWallet: String, page: Int = 0, size: Int = 20): Page<Product> {
        val pageable = PageRequest.of(page, size)
        return productRepository.findBySellerWallet(sellerWallet, pageable)
    }
    
    fun getAllCategories() = categoryRepository.findAll()
    
    fun getCategoryBySlug(slug: String) = categoryRepository.findBySlug(slug)
    
    fun getRootCategories() = categoryRepository.findByParentIdIsNull()
}
