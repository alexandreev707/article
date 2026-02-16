package com.cryptodrop.persistence.category

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.util.Optional
import java.util.UUID

@Repository
interface CategoryRepository : JpaRepository<Category, UUID> {
    fun findByName(name: String): Optional<Category>
    fun findBySlug(slug: String): Optional<Category>
    fun findByActiveTrueOrderByDisplayOrderAsc(): List<Category>
}
