package com.cryptodrop.service.dto

import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size

data class CategoryCreateDto(
    @field:NotBlank(message = "Name is required")
    @field:Size(max = 255)
    val name: String,
    @field:Size(max = 255)
    val slug: String? = null,
    val description: String? = null,
    val iconUrl: String? = null,
    val bannerUrl: String? = null,
    val parentId: String? = null,
    val displayOrder: Int = 0,
    val active: Boolean = true
)

data class CategoryUpdateDto(
    @field:Size(max = 255)
    val name: String? = null,
    @field:Size(max = 255)
    val slug: String? = null,
    val description: String? = null,
    val iconUrl: String? = null,
    val bannerUrl: String? = null,
    val parentId: String? = null,
    val displayOrder: Int? = null,
    val active: Boolean? = null
)

data class CategoryResponseDto(
    val id: String,
    val name: String,
    val slug: String,
    val description: String?,
    val iconUrl: String?,
    val bannerUrl: String?,
    val parentId: String?,
    val displayOrder: Int,
    val active: Boolean,
    val createdAt: String
)
