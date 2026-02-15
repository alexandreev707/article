package com.cryptodrop.service

import com.cryptodrop.dto.DeliveryOptionDto
import com.cryptodrop.model.DeliveryOption
import com.cryptodrop.repository.DeliveryOptionRepository
import org.springframework.stereotype.Service

@Service
class DeliveryOptionService(
    private val deliveryOptionRepository: DeliveryOptionRepository
) {
    fun findAllActive(): List<DeliveryOptionDto> {
        return deliveryOptionRepository.findByActiveTrue().map { toDto(it) }
    }

    fun findById(id: Long): DeliveryOption {
        return deliveryOptionRepository.findById(id)
            .orElseThrow { IllegalArgumentException("Delivery option not found: $id") }
    }

    private fun toDto(o: DeliveryOption): DeliveryOptionDto = DeliveryOptionDto(
        id = o.id!!,
        name = o.name,
        type = o.type,
        price = o.price,
        estimatedDays = o.estimatedDays,
        addressLine = o.addressLine,
        city = o.city,
        region = o.region,
        zipCode = o.zipCode,
        country = o.country
    )
}
