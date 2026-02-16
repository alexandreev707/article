package com.cryptodrop.service

import com.cryptodrop.persistence.category.CategoryRepository
import com.cryptodrop.persistence.promotion.Promotion
import com.cryptodrop.persistence.promotion.PromotionRepository
import com.cryptodrop.service.dto.PromotionCreateDto
import com.cryptodrop.service.dto.PromotionResponseDto
import com.cryptodrop.service.dto.PromotionUpdateDto
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.format.DateTimeFormatter
import java.util.UUID

@Service
class PromotionService(
    private val promotionRepository: PromotionRepository,
    private val categoryRepository: CategoryRepository
) {
    private val dateFormatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME

    fun findAll(pageable: Pageable): Page<PromotionResponseDto> {
        return promotionRepository.findAll(pageable).map { toDto(it) }
    }

    fun findActive(page: Int, size: Int): Page<PromotionResponseDto> {
        return promotionRepository.findByActiveTrue(PageRequest.of(page, size)).map { toDto(it) }
    }

    fun findById(id: UUID): Promotion {
        return promotionRepository.findById(id)
            .orElseThrow { IllegalArgumentException("Promotion not found: $id") }
    }

    fun getById(id: UUID): PromotionResponseDto = toDto(findById(id))

    @Transactional
    fun create(dto: PromotionCreateDto): PromotionResponseDto {
        if (dto.code != null && promotionRepository.findByCode(dto.code) != null) {
            throw IllegalArgumentException("Promotion with code '${dto.code}' already exists")
        }
        val categories = dto.categoryIds.mapNotNull { id ->
            categoryRepository.findById(UUID.fromString(id)).orElse(null)
        }.toMutableSet()
        val promotion = Promotion(
            title = dto.title,
            description = dto.description,
            code = dto.code,
            discountType = dto.discountType,
            discountValue = dto.discountValue,
            minOrderAmount = dto.minOrderAmount,
            maxDiscountAmount = dto.maxDiscountAmount,
            startDate = dto.startDate,
            endDate = dto.endDate,
            usageLimit = dto.usageLimit,
            applicableCategories = categories,
            active = dto.active
        )
        return toDto(promotionRepository.save(promotion))
    }

    @Transactional
    fun update(id: UUID, dto: PromotionUpdateDto): PromotionResponseDto {
        val existing = findById(id)
        if (dto.code != null && dto.code != existing.code) {
            if (promotionRepository.findByCode(dto.code) != null) {
                throw IllegalArgumentException("Promotion with code '${dto.code}' already exists")
            }
        }
        val categorySet = (dto.categoryIds ?: existing.applicableCategories.map { it.id!!.toString() })
            .mapNotNull { cid -> categoryRepository.findById(UUID.fromString(cid)).orElse(null) }
            .toMutableSet()
        val updated = Promotion(
            id = existing.id,
            title = dto.title ?: existing.title,
            description = dto.description ?: existing.description,
            code = dto.code ?: existing.code,
            discountType = dto.discountType ?: existing.discountType,
            discountValue = dto.discountValue ?: existing.discountValue,
            minOrderAmount = dto.minOrderAmount ?: existing.minOrderAmount,
            maxDiscountAmount = dto.maxDiscountAmount ?: existing.maxDiscountAmount,
            startDate = dto.startDate ?: existing.startDate,
            endDate = dto.endDate ?: existing.endDate,
            usageLimit = dto.usageLimit ?: existing.usageLimit,
            usageCount = existing.usageCount,
            applicableCategories = categorySet,
            active = dto.active ?: existing.active,
            createdAt = existing.createdAt
        )
        return toDto(promotionRepository.save(updated))
    }

    @Transactional
    fun delete(id: UUID) {
        if (!promotionRepository.existsById(id)) {
            throw IllegalArgumentException("Promotion not found: $id")
        }
        promotionRepository.deleteById(id)
    }

    fun toDto(p: Promotion): PromotionResponseDto = PromotionResponseDto(
        id = p.id!!.toString(),
        title = p.title,
        description = p.description,
        code = p.code,
        discountType = p.discountType,
        discountValue = p.discountValue,
        minOrderAmount = p.minOrderAmount,
        maxDiscountAmount = p.maxDiscountAmount,
        startDate = p.startDate.format(dateFormatter),
        endDate = p.endDate.format(dateFormatter),
        usageLimit = p.usageLimit,
        usageCount = p.usageCount,
        categoryIds = p.applicableCategories.map { it.id!!.toString() },
        active = p.active,
        createdAt = p.createdAt.format(dateFormatter)
    )
}
