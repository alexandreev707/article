package com.cryptodrop.web.controller

import com.cryptodrop.service.ProductVariantService
import com.cryptodrop.service.dto.ProductVariantCreateDto
import com.cryptodrop.service.dto.ProductVariantResponseDto
import com.cryptodrop.service.dto.ProductVariantUpdateDto
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.*
import java.util.UUID

@RestController
@RequestMapping("/api/products/{productId}/variants")
class ProductVariantController(
    private val productVariantService: ProductVariantService
) {

    @GetMapping
    fun list(@PathVariable productId: String): ResponseEntity<List<ProductVariantResponseDto>> {
        return ResponseEntity.ok(productVariantService.findByProductId(UUID.fromString(productId)))
    }

    @GetMapping("/{variantId}")
    fun get(
        @PathVariable productId: String,
        @PathVariable variantId: String
    ): ResponseEntity<ProductVariantResponseDto> {
        return ResponseEntity.ok(
            productVariantService.getById(UUID.fromString(productId), UUID.fromString(variantId))
        )
    }

    @PostMapping
    @PreAuthorize("hasRole('SELLER') or hasRole('ADMIN')")
    fun create(
        @PathVariable productId: String,
        @Valid @RequestBody dto: ProductVariantCreateDto
    ): ResponseEntity<ProductVariantResponseDto> {
        val created = productVariantService.create(UUID.fromString(productId), dto)
        return ResponseEntity.status(HttpStatus.CREATED).body(created)
    }

    @PutMapping("/{variantId}")
    @PreAuthorize("hasRole('SELLER') or hasRole('ADMIN')")
    fun update(
        @PathVariable productId: String,
        @PathVariable variantId: String,
        @Valid @RequestBody dto: ProductVariantUpdateDto
    ): ResponseEntity<ProductVariantResponseDto> {
        return ResponseEntity.ok(
            productVariantService.update(
                UUID.fromString(productId),
                UUID.fromString(variantId),
                dto
            )
        )
    }

    @DeleteMapping("/{variantId}")
    @PreAuthorize("hasRole('SELLER') or hasRole('ADMIN')")
    fun delete(
        @PathVariable productId: String,
        @PathVariable variantId: String
    ): ResponseEntity<Void> {
        productVariantService.delete(UUID.fromString(productId), UUID.fromString(variantId))
        return ResponseEntity.noContent().build()
    }
}
