package com.cryptodrop.domain.repository

import com.cryptodrop.domain.model.Category
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface CategoryRepository : JpaRepository<Category, Long> {
    
    fun findBySlug(slug: String): Category?
    
    fun findByParentIdIsNull(): List<Category>
    
    fun findByParentId(parentId: Long): List<Category>
}
