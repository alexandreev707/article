package com.cryptodrop.web.controller

import com.cryptodrop.service.PromotionService
import com.cryptodrop.service.dto.PromotionCreateDto
import com.cryptodrop.service.dto.PromotionResponseDto
import com.cryptodrop.service.dto.PromotionUpdateDto
import jakarta.validation.Valid
import org.springframework.data.domain.PageRequest
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.*
import java.util.UUID

@RestController
@RequestMapping("/api/promotions")
class PromotionController(
    private val promotionService: PromotionService
) {

    @GetMapping
    fun getAll(
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "20") size: Int,
        @RequestParam(defaultValue = "false") activeOnly: Boolean
    ): ResponseEntity<Map<String, Any>> {
        val result = if (activeOnly) {
            promotionService.findActive(page, size)
        } else {
            promotionService.findAll(PageRequest.of(page, size))
        }
        return ResponseEntity.ok(mapOf(
            "promotions" to result.content,
            "totalPages" to result.totalPages,
            "currentPage" to result.number,
            "totalElements" to result.totalElements
        ))
    }

    @GetMapping("/{id}")
    fun getById(@PathVariable id: String): ResponseEntity<PromotionResponseDto> {
        return ResponseEntity.ok(promotionService.getById(UUID.fromString(id)))
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    fun create(@Valid @RequestBody dto: PromotionCreateDto): ResponseEntity<PromotionResponseDto> {
        val created = promotionService.create(dto)
        return ResponseEntity.status(HttpStatus.CREATED).body(created)
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    fun update(
        @PathVariable id: String,
        @Valid @RequestBody dto: PromotionUpdateDto
    ): ResponseEntity<PromotionResponseDto> {
        return ResponseEntity.ok(promotionService.update(UUID.fromString(id), dto))
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    fun delete(@PathVariable id: String): ResponseEntity<Void> {
        promotionService.delete(UUID.fromString(id))
        return ResponseEntity.noContent().build()
    }
}
