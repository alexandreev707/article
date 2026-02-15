package com.cryptodrop.controller

import com.cryptodrop.service.DeliveryOptionService
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/delivery-options")
class DeliveryController(
    private val deliveryOptionService: DeliveryOptionService
) {

    @GetMapping
    fun list(): ResponseEntity<List<com.cryptodrop.dto.DeliveryOptionDto>> {
        return ResponseEntity.ok(deliveryOptionService.findAllActive())
    }
}
