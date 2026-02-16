package com.cryptodrop.persistence.category

import jakarta.persistence.*
import org.hibernate.annotations.GenericGenerator
import java.time.LocalDateTime
import java.util.UUID

@Entity
@Table(name = "categories")
data class Category(
    @Id
    @GeneratedValue(generator = "UUID")
    @GenericGenerator(name = "UUID", strategy = "org.hibernate.id.UUIDGenerator")
    @Column(updatable = false, nullable = false)
    val id: UUID? = null,

    @Column(nullable = false, unique = true)
    val name: String,

    @Column(nullable = false, unique = true)
    val slug: String,

    @Column(columnDefinition = "TEXT")
    val description: String? = null,

    @Column(name = "icon_url")
    val iconUrl: String? = null,

    @Column(name = "banner_url")
    val bannerUrl: String? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_id")
    val parent: Category? = null,

    @Column(name = "display_order")
    val displayOrder: Int = 0,

    val active: Boolean = true,

    @Column(name = "created_at", nullable = false, updatable = false)
    val createdAt: LocalDateTime = LocalDateTime.now()
)
