package com.cryptodrop.persistence.productvariant

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.util.UUID

@Repository
interface ProductVariantRepository : JpaRepository<ProductVariant, UUID> {
    fun findByProductId(productId: UUID): List<ProductVariant>
}
