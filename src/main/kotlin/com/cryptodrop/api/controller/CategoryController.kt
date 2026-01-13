package com.cryptodrop.api.controller

import com.cryptodrop.api.dto.CategoryResponse
import com.cryptodrop.service.CatalogService
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/v1/categories")
class CategoryController(
    private val catalogService: CatalogService
) {
    
    @GetMapping
    fun getAllCategories(): ResponseEntity<List<CategoryResponse>> {
        val categories = catalogService.getAllCategories()
        val response = categories.map { 
            CategoryResponse(
                id = it.id,
                name = it.name,
                slug = it.slug,
                parentId = it.parentId,
                iconUrl = it.iconUrl
            )
        }
        return ResponseEntity.ok(response)
    }
    
    @GetMapping("/roots")
    fun getRootCategories(): ResponseEntity<List<CategoryResponse>> {
        val categories = catalogService.getRootCategories()
        val response = categories.map { 
            CategoryResponse(
                id = it.id,
                name = it.name,
                slug = it.slug,
                parentId = it.parentId,
                iconUrl = it.iconUrl
            )
        }
        return ResponseEntity.ok(response)
    }
    
    @GetMapping("/{slug}")
    fun getCategoryBySlug(@PathVariable slug: String): ResponseEntity<CategoryResponse> {
        val category = catalogService.getCategoryBySlug(slug)
            ?: return ResponseEntity.notFound().build()
        
        return ResponseEntity.ok(
            CategoryResponse(
                id = category.id,
                name = category.name,
                slug = category.slug,
                parentId = category.parentId,
                iconUrl = category.iconUrl
            )
        )
    }
}
