package com.cryptodrop.web.controller

import com.cryptodrop.service.DeliveryOptionService
import com.cryptodrop.service.dto.DeliveryOptionCreateDto
import com.cryptodrop.service.dto.DeliveryOptionDto
import com.cryptodrop.service.dto.DeliveryOptionUpdateDto
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.*
import java.util.UUID

@RestController
@RequestMapping("/api/delivery-options")
class DeliveryController(
    private val deliveryOptionService: DeliveryOptionService
) {

    @GetMapping
    fun list(): ResponseEntity<List<DeliveryOptionDto>> {
        return ResponseEntity.ok(deliveryOptionService.findAllActive())
    }

    @GetMapping("/admin")
    @PreAuthorize("hasRole('ADMIN')")
    fun listAll(): ResponseEntity<List<DeliveryOptionDto>> {
        return ResponseEntity.ok(deliveryOptionService.findAll())
    }

    @GetMapping("/{id}")
    fun getById(@PathVariable id: String): ResponseEntity<DeliveryOptionDto> {
        return ResponseEntity.ok(deliveryOptionService.getById(UUID.fromString(id)))
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    fun create(@Valid @RequestBody dto: DeliveryOptionCreateDto): ResponseEntity<DeliveryOptionDto> {
        val created = deliveryOptionService.create(dto)
        return ResponseEntity.status(HttpStatus.CREATED).body(created)
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    fun update(
        @PathVariable id: String,
        @Valid @RequestBody dto: DeliveryOptionUpdateDto
    ): ResponseEntity<DeliveryOptionDto> {
        return ResponseEntity.ok(deliveryOptionService.update(UUID.fromString(id), dto))
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    fun delete(@PathVariable id: String): ResponseEntity<Void> {
        deliveryOptionService.delete(UUID.fromString(id))
        return ResponseEntity.noContent().build()
    }
}
