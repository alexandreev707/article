package com.cryptodrop.web.controller

import com.cryptodrop.service.CategoryService
import com.cryptodrop.service.dto.CategoryCreateDto
import com.cryptodrop.service.dto.CategoryResponseDto
import com.cryptodrop.service.dto.CategoryUpdateDto
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.*
import java.util.UUID

@RestController
@RequestMapping("/api/categories")
class CategoryController(
    private val categoryService: CategoryService
) {

    @GetMapping
    fun getAll(
        @RequestParam(defaultValue = "false") activeOnly: Boolean
    ): ResponseEntity<List<CategoryResponseDto>> {
        val categories = if (activeOnly) categoryService.findActive() else categoryService.findAll()
        return ResponseEntity.ok(categories)
    }

    @GetMapping("/{id}")
    fun getById(@PathVariable id: String): ResponseEntity<CategoryResponseDto> {
        return ResponseEntity.ok(categoryService.getById(UUID.fromString(id)))
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    fun create(@Valid @RequestBody dto: CategoryCreateDto): ResponseEntity<CategoryResponseDto> {
        val created = categoryService.create(dto)
        return ResponseEntity.status(HttpStatus.CREATED).body(created)
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    fun update(
        @PathVariable id: String,
        @Valid @RequestBody dto: CategoryUpdateDto
    ): ResponseEntity<CategoryResponseDto> {
        return ResponseEntity.ok(categoryService.update(UUID.fromString(id), dto))
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    fun delete(@PathVariable id: String): ResponseEntity<Void> {
        categoryService.delete(UUID.fromString(id))
        return ResponseEntity.noContent().build()
    }
}
