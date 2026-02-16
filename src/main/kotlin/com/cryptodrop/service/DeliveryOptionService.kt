package com.cryptodrop.service

import com.cryptodrop.persistence.deliveryoption.DeliveryOption
import com.cryptodrop.persistence.deliveryoption.DeliveryOptionRepository
import com.cryptodrop.service.dto.DeliveryOptionCreateDto
import com.cryptodrop.service.dto.DeliveryOptionDto
import com.cryptodrop.service.dto.DeliveryOptionUpdateDto
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.UUID

@Service
class DeliveryOptionService(
    private val deliveryOptionRepository: DeliveryOptionRepository
) {
    fun findAllActive(): List<DeliveryOptionDto> {
        return deliveryOptionRepository.findByActiveTrue().map { toDto(it) }
    }

    fun findAll(): List<DeliveryOptionDto> {
        return deliveryOptionRepository.findAll().map { toDto(it) }
    }

    fun findById(id: UUID): DeliveryOption {
        return deliveryOptionRepository.findById(id)
            .orElseThrow { IllegalArgumentException("Delivery option not found: $id") }
    }

    fun getById(id: UUID): DeliveryOptionDto = toDto(findById(id))

    @Transactional
    fun create(dto: DeliveryOptionCreateDto): DeliveryOptionDto {
        val entity = DeliveryOption(
            name = dto.name,
            type = dto.type,
            description = dto.description,
            price = dto.price,
            freeShippingThreshold = dto.freeShippingThreshold,
            estimatedDaysMin = dto.estimatedDaysMin,
            estimatedDaysMax = dto.estimatedDaysMax,
            pickupAddress = dto.pickupAddress,
            latitude = dto.latitude,
            longitude = dto.longitude,
            active = dto.active
        )
        return toDto(deliveryOptionRepository.save(entity))
    }

    @Transactional
    fun update(id: UUID, dto: DeliveryOptionUpdateDto): DeliveryOptionDto {
        val existing = findById(id)
        val updated = DeliveryOption(
            id = existing.id,
            name = dto.name ?: existing.name,
            type = dto.type ?: existing.type,
            description = dto.description ?: existing.description,
            price = dto.price ?: existing.price,
            freeShippingThreshold = dto.freeShippingThreshold ?: existing.freeShippingThreshold,
            estimatedDaysMin = dto.estimatedDaysMin ?: existing.estimatedDaysMin,
            estimatedDaysMax = dto.estimatedDaysMax ?: existing.estimatedDaysMax,
            pickupAddress = dto.pickupAddress ?: existing.pickupAddress,
            latitude = dto.latitude ?: existing.latitude,
            longitude = dto.longitude ?: existing.longitude,
            active = dto.active ?: existing.active,
            createdAt = existing.createdAt
        )
        return toDto(deliveryOptionRepository.save(updated))
    }

    @Transactional
    fun delete(id: UUID) {
        if (!deliveryOptionRepository.existsById(id)) {
            throw IllegalArgumentException("Delivery option not found: $id")
        }
        deliveryOptionRepository.deleteById(id)
    }

    fun toDto(o: DeliveryOption): DeliveryOptionDto = DeliveryOptionDto(
        id = o.id!!.toString(),
        name = o.name,
        type = o.type,
        price = o.price,
        estimatedDays = o.estimatedDaysMin ?: o.estimatedDaysMax,
        addressLine = o.pickupAddress,
        city = null,
        region = null,
        zipCode = null,
        country = null
    )
}
