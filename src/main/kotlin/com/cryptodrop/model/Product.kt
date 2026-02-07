package com.cryptodrop.model

import jakarta.persistence.*
import java.math.BigDecimal
import java.time.LocalDateTime

@Entity
@Table(name = "products")
data class Product(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,
    
    @Column(nullable = false)
    val sellerId: Long,
    
    @Column(nullable = false)
    val title: String,
    
    @Column(columnDefinition = "TEXT")
    val description: String = "",
    
    @Column(nullable = false, precision = 19, scale = 2)
    val price: BigDecimal,
    
    @Column(nullable = false)
    val category: String,
    
    @ElementCollection
    @CollectionTable(name = "product_images", joinColumns = [JoinColumn(name = "product_id")])
    @Column(name = "image_url")
    val images: MutableList<String> = mutableListOf(),
    
    val rating: Double = 0.0,
    
    val reviewCount: Int = 0,
    
    @ElementCollection
    @CollectionTable(name = "product_attributes", joinColumns = [JoinColumn(name = "product_id")])
    @MapKeyColumn(name = "attribute_key")
    @Column(name = "attribute_value")
    val attributes: Map<String, String> = emptyMap(),
    
    val stock: Int = 0,
    
    val active: Boolean = true,
    
    val createdAt: LocalDateTime = LocalDateTime.now(),
    
    val updatedAt: LocalDateTime = LocalDateTime.now()
)
