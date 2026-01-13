package com.cryptodrop.domain.model

import jakarta.persistence.*
import java.time.Instant

@Entity
@Table(name = "categories")
data class Category(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,
    
    @Column(name = "parent_id")
    val parentId: Long? = null,
    
    @Column(name = "name", nullable = false, length = 255)
    val name: String,
    
    @Column(name = "slug", unique = true, length = 50)
    val slug: String,
    
    @Column(name = "attributes", columnDefinition = "JSONB")
    val attributes: String? = null, // list of required attribute IDs
    
    @Column(name = "icon_url", length = 500)
    val iconUrl: String? = null,
    
    @Column(name = "created_at", nullable = false)
    val createdAt: Instant = Instant.now()
)
