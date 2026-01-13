package com.cryptodrop.api.controller

import com.cryptodrop.api.dto.ProductCreateRequest
import com.cryptodrop.api.dto.ProductResponse
import com.cryptodrop.api.dto.ProductsPageResponse
import com.cryptodrop.domain.model.Product
import com.cryptodrop.service.CatalogService
import jakarta.validation.Valid
import org.springframework.data.domain.Page
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.math.BigDecimal

@RestController
@RequestMapping("/v1/products")
class ProductController(
    private val catalogService: CatalogService
) {
    
    @GetMapping
    fun getProducts(
        @RequestParam(required = false) category: Long?,
        @RequestParam(required = false) max_price: BigDecimal?,
        @RequestParam(required = false) query: String?,
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "20") limit: Int
    ): ResponseEntity<ProductsPageResponse> {
        val productsPage = catalogService.getProducts(category, max_price, query, page, limit)
        
        val response = ProductsPageResponse(
            products = productsPage.content.map { it.toResponse() },
            total = productsPage.totalElements,
            page = productsPage.number,
            limit = productsPage.size
        )
        
        return ResponseEntity.ok(response)
    }
    
    @GetMapping("/{id}")
    fun getProduct(@PathVariable id: Long): ResponseEntity<ProductResponse> {
        val product = catalogService.getProductById(id)
            ?: return ResponseEntity.notFound().build()
        
        return ResponseEntity.ok(product.toResponse())
    }
    
    @PostMapping
    fun createProduct(
        @Valid @RequestBody request: ProductCreateRequest,
        @RequestHeader("X-Wallet-Address") sellerWallet: String
    ): ResponseEntity<ProductResponse> {
        val product = request.toProduct(sellerWallet)
        val created = catalogService.createProduct(product)
        
        return ResponseEntity.status(HttpStatus.CREATED).body(created.toResponse())
    }
    
    @PutMapping("/{id}")
    fun updateProduct(
        @PathVariable id: Long,
        @Valid @RequestBody request: ProductCreateRequest,
        @RequestHeader("X-Wallet-Address") sellerWallet: String
    ): ResponseEntity<ProductResponse> {
        val product = request.toProduct(sellerWallet)
        val updated = catalogService.updateProduct(id, product)
            ?: return ResponseEntity.notFound().build()
        
        return ResponseEntity.ok(updated.toResponse())
    }
    
    @GetMapping("/seller/{wallet}")
    fun getSellerProducts(
        @PathVariable wallet: String,
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "20") limit: Int
    ): ResponseEntity<ProductsPageResponse> {
        val productsPage = catalogService.getSellerProducts(wallet, page, limit)
        
        val response = ProductsPageResponse(
            products = productsPage.content.map { it.toResponse() },
            total = productsPage.totalElements,
            page = productsPage.number,
            limit = productsPage.size
        )
        
        return ResponseEntity.ok(response)
    }
    
    private fun Product.toResponse() = ProductResponse(
        id = id,
        title = title,
        description = description,
        sellerWallet = sellerWallet,
        priceUsd = priceUsd,
        images = parseJsonArray(images),
        specs = specs,
        shippingProfiles = shippingProfiles,
        categoryId = categoryId,
        status = status.name.lowercase(),
        createdAt = createdAt.toString()
    )
    
    private fun parseJsonArray(json: String?): List<String> {
        // Simplified JSON parsing
        if (json == null || json.isBlank()) return emptyList()
        return json.trim('[', ']').split(',').map { it.trim('"', ' ') }
    }
}
