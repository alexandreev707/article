package com.cryptodrop.service

import com.cryptodrop.persistence.category.Category
import com.cryptodrop.persistence.category.CategoryRepository
import com.cryptodrop.service.dto.CategoryCreateDto
import com.cryptodrop.service.dto.CategoryResponseDto
import com.cryptodrop.service.dto.CategoryUpdateDto
import org.springframework.cache.annotation.CacheEvict
import org.springframework.cache.annotation.Cacheable
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.format.DateTimeFormatter
import java.util.UUID

@Service
class CategoryService(
    private val categoryRepository: CategoryRepository
) {
    private val dateFormatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME

    @Cacheable("categories")
    fun findAll(): List<CategoryResponseDto> {
        return categoryRepository.findAll()
            .sortedBy { it.displayOrder }
            .map { toDto(it) }
    }

    @Cacheable("categories")
    fun findActive(): List<CategoryResponseDto> {
        return categoryRepository.findByActiveTrueOrderByDisplayOrderAsc().map { toDto(it) }
    }

    fun findById(id: UUID): Category {
        return categoryRepository.findById(id)
            .orElseThrow { IllegalArgumentException("Category not found: $id") }
    }

    fun getById(id: UUID): CategoryResponseDto = toDto(findById(id))

    private fun slugFromName(name: String): String =
        name.lowercase().replace(Regex("[^a-z0-9\\s-]"), "").replace(Regex("\\s+"), "-").trim()

    @CacheEvict(value = ["categories", "products"], allEntries = true)
    @Transactional
    fun create(dto: CategoryCreateDto): CategoryResponseDto {
        val slug = dto.slug ?: slugFromName(dto.name)
        if (categoryRepository.findBySlug(slug).isPresent) {
            throw IllegalArgumentException("Category with slug '$slug' already exists")
        }
        val parent = dto.parentId?.let { categoryRepository.findById(UUID.fromString(it)).orElse(null) }
        val category = Category(
            name = dto.name,
            slug = slug,
            description = dto.description,
            iconUrl = dto.iconUrl,
            bannerUrl = dto.bannerUrl,
            parent = parent,
            displayOrder = dto.displayOrder,
            active = dto.active
        )
        return toDto(categoryRepository.save(category))
    }

    @CacheEvict(value = ["categories", "products"], allEntries = true)
    @Transactional
    fun update(id: UUID, dto: CategoryUpdateDto): CategoryResponseDto {
        val existing = findById(id)
        val slug = dto.slug ?: existing.slug
        if (dto.slug != null && slug != existing.slug) {
            if (categoryRepository.findBySlug(slug).isPresent) {
                throw IllegalArgumentException("Category with slug '$slug' already exists")
            }
        }
        val parent = dto.parentId?.let { categoryRepository.findById(UUID.fromString(it)).orElse(null) }
            ?: existing.parent
        val updated = Category(
            id = existing.id,
            name = dto.name ?: existing.name,
            slug = slug,
            description = dto.description ?: existing.description,
            iconUrl = dto.iconUrl ?: existing.iconUrl,
            bannerUrl = dto.bannerUrl ?: existing.bannerUrl,
            parent = parent,
            displayOrder = dto.displayOrder ?: existing.displayOrder,
            active = dto.active ?: existing.active,
            createdAt = existing.createdAt
        )
        return toDto(categoryRepository.save(updated))
    }

    @CacheEvict(value = ["categories", "products"], allEntries = true)
    @Transactional
    fun delete(id: UUID) {
        if (!categoryRepository.existsById(id)) {
            throw IllegalArgumentException("Category not found: $id")
        }
        categoryRepository.deleteById(id)
    }

    fun toDto(c: Category): CategoryResponseDto = CategoryResponseDto(
        id = c.id!!.toString(),
        name = c.name,
        slug = c.slug,
        description = c.description,
        iconUrl = c.iconUrl,
        bannerUrl = c.bannerUrl,
        parentId = c.parent?.id?.toString(),
        displayOrder = c.displayOrder,
        active = c.active,
        createdAt = c.createdAt.format(dateFormatter)
    )
}
