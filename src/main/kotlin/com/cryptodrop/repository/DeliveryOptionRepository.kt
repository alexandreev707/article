package com.cryptodrop.repository

import com.cryptodrop.model.DeliveryOption
import com.cryptodrop.model.DeliveryType
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface DeliveryOptionRepository : JpaRepository<DeliveryOption, Long> {
    fun findByActiveTrue(): List<DeliveryOption>
    fun findByActiveTrueAndType(type: DeliveryType): List<DeliveryOption>
}
