package com.cryptodrop.persistence.order

import jakarta.persistence.Column
import jakarta.persistence.Embeddable

@Embeddable
data class Address(
    @Column(name = "address_line", nullable = false)
    val addressLine: String,

    @Column(nullable = false)
    val city: String,

    @Column(nullable = false)
    val region: String,

    @Column(name = "zip_code", nullable = false)
    val zipCode: String,

    @Column(nullable = false)
    val country: String,

    @Column(name = "recipient_name")
    val recipientName: String? = null,

    @Column(name = "recipient_phone")
    val recipientPhone: String? = null
)
