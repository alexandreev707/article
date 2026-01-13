package com.cryptodrop.api.dto

data class CategoryResponse(
    val id: Long,
    val name: String,
    val slug: String,
    val parentId: Long?,
    val iconUrl: String?
)
