package com.cryptodrop.persistence.promotion

import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.util.UUID

@Repository
interface PromotionRepository : JpaRepository<Promotion, UUID> {
    fun findByActiveTrue(pageable: Pageable): Page<Promotion>
    fun findByCode(code: String): Promotion?
}
