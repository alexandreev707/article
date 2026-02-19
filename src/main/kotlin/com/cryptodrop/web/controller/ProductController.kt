package com.cryptodrop.web.controller

import com.cryptodrop.service.ProductService
import com.cryptodrop.service.UserService
import com.cryptodrop.service.dto.ProductCreateDto
import com.cryptodrop.service.dto.ProductFilterDto
import com.cryptodrop.service.dto.ProductResponseDto
import com.cryptodrop.service.dto.ProductUpdateDto
import jakarta.validation.Valid
import org.springframework.data.domain.PageRequest
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.*
import org.springframework.web.server.ResponseStatusException
import java.util.UUID

@Controller
@RequestMapping("/products")
class ProductController(
    private val productService: ProductService,
    private val userService: UserService
) {

    @GetMapping
    fun listProducts(
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "12") size: Int,
        @RequestParam(required = false) category: String?,
        @RequestParam(required = false) minPrice: java.math.BigDecimal?,
        @RequestParam(required = false) maxPrice: java.math.BigDecimal?,
        @RequestParam(required = false) minRating: Double?,
        @RequestParam(required = false) search: String?,
        @RequestParam(defaultValue = "createdAt") sortBy: String,
        @RequestParam(defaultValue = "desc") sortOrder: String,
        model: Model
    ): String {
        val filter = ProductFilterDto(category, minPrice, maxPrice, minRating, search, sortBy, sortOrder)
        val products = productService.searchProducts(filter, page, size)
        val categories = productService.getPopularCategories(20)
        model.addAttribute("title", "Catalog - Marketplace")
        model.addAttribute("products", products.map { productService.toDto(it) })
        model.addAttribute("categories", categories)
        model.addAttribute("filter", filter)
        model.addAttribute("currentPage", page)
        model.addAttribute("totalPages", products.totalPages)
        model.addAttribute("currentUser", userService.getCurrentUser())
        return "products/list"
    }

    @GetMapping("/{id}")
    fun viewProduct(@PathVariable id: String, model: Model): String {
        val productId = try {
            UUID.fromString(id)
        } catch (e: IllegalArgumentException) {
            throw ResponseStatusException(HttpStatus.NOT_FOUND, "Product not found")
        }
        val product = try {
            productService.findById(productId)
        } catch (e: IllegalArgumentException) {
            throw ResponseStatusException(HttpStatus.NOT_FOUND, "Product not found")
        }
        val productDto = productService.toDto(product)
        model.addAttribute("title", "${productDto.title} - Marketplace")
        model.addAttribute("product", productDto)
        model.addAttribute("currentUser", userService.getCurrentUser())
        return "products/detail"
    }
}

@RestController
@RequestMapping("/api/products")
class ProductApiController(
    private val productService: ProductService,
    private val userService: UserService
) {

    @GetMapping
    fun getAllProducts(
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "12") size: Int,
        @RequestParam(required = false) category: String?,
        @RequestParam(required = false) minPrice: java.math.BigDecimal?,
        @RequestParam(required = false) maxPrice: java.math.BigDecimal?,
        @RequestParam(required = false) minRating: Double?,
        @RequestParam(required = false) search: String?,
        @RequestParam(defaultValue = "createdAt") sortBy: String,
        @RequestParam(defaultValue = "desc") sortOrder: String
    ): ResponseEntity<Map<String, Any>> {
        val filter = ProductFilterDto(category, minPrice, maxPrice, minRating, search, sortBy, sortOrder)
        val products = productService.searchProducts(filter, page, size)
        return ResponseEntity.ok(mapOf(
            "products" to products.map { productService.toDto(it) },
            "totalPages" to products.totalPages,
            "currentPage" to page,
            "totalElements" to products.totalElements
        ))
    }

    @GetMapping("/{id}")
    fun getProduct(@PathVariable id: String): ResponseEntity<ProductResponseDto> {
        val product = productService.findById(UUID.fromString(id))
        return ResponseEntity.ok(productService.toDto(product))
    }

    @PostMapping
    @PreAuthorize("hasRole('SELLER') or hasRole('ADMIN')")
    fun createProduct(@Valid @RequestBody dto: ProductCreateDto): ResponseEntity<ProductResponseDto> {
        val sellerId = userService.getCurrentUserId() ?: throw IllegalStateException("User not authenticated")
        val product = productService.createProduct(sellerId, dto)
        return ResponseEntity.status(HttpStatus.CREATED).body(productService.toDto(product))
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('SELLER') or hasRole('ADMIN')")
    fun updateProduct(@PathVariable id: String, @Valid @RequestBody dto: ProductUpdateDto): ResponseEntity<ProductResponseDto> {
        val sellerId = userService.getCurrentUserId() ?: throw IllegalStateException("User not authenticated")
        val product = productService.updateProduct(UUID.fromString(id), sellerId, dto)
        return ResponseEntity.ok(productService.toDto(product))
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('SELLER') or hasRole('ADMIN')")
    fun deleteProduct(@PathVariable id: String): ResponseEntity<Unit> {
        val sellerId = userService.getCurrentUserId() ?: throw IllegalStateException("User not authenticated")
        productService.deleteProduct(UUID.fromString(id), sellerId)
        return ResponseEntity.noContent().build()
    }

    @PostMapping("/{id}/publish")
    @PreAuthorize("hasRole('SELLER') or hasRole('ADMIN')")
    fun publishProduct(@PathVariable id: String): ResponseEntity<ProductResponseDto> {
        val sellerId = userService.getCurrentUserId() ?: throw IllegalStateException("User not authenticated")
        val product = productService.publishProduct(UUID.fromString(id), sellerId)
        return ResponseEntity.ok(productService.toDto(product))
    }

    @PostMapping("/{id}/unpublish")
    @PreAuthorize("hasRole('SELLER') or hasRole('ADMIN')")
    fun unpublishProduct(@PathVariable id: String): ResponseEntity<ProductResponseDto> {
        val sellerId = userService.getCurrentUserId() ?: throw IllegalStateException("User not authenticated")
        val product = productService.unpublishProduct(UUID.fromString(id), sellerId)
        return ResponseEntity.ok(productService.toDto(product))
    }

    @GetMapping("/recommended")
    fun getRecommended(): ResponseEntity<List<ProductResponseDto>> {
        val userId = userService.getCurrentUserId()
        val products = productService.getRecommendedProducts(userId, 10)
        return ResponseEntity.ok(products.map { productService.toDto(it) })
    }

    @GetMapping("/categories")
    fun getCategories(): ResponseEntity<List<String>> {
        return ResponseEntity.ok(productService.getPopularCategories(20))
    }
}
