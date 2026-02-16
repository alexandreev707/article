package com.cryptodrop.persistence.deliveryoption

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.util.UUID

@Repository
interface DeliveryOptionRepository : JpaRepository<DeliveryOption, UUID> {
    fun findByActiveTrue(): List<DeliveryOption>
    fun findByActiveTrueAndType(type: DeliveryType): List<DeliveryOption>
}
