package com.cryptodrop.persistence.productvariant

import com.cryptodrop.persistence.product.Product
import jakarta.persistence.*
import org.hibernate.annotations.GenericGenerator
import java.math.BigDecimal
import java.util.UUID

@Entity
@Table(name = "product_variants")
data class ProductVariant(
    @Id
    @GeneratedValue(generator = "UUID")
    @GenericGenerator(name = "UUID", strategy = "org.hibernate.id.UUIDGenerator")
    @Column(updatable = false, nullable = false)
    val id: UUID? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    val product: Product,

    @Column(nullable = false)
    val name: String,

    @Column(unique = true)
    val sku: String? = null,

    @Column(precision = 19, scale = 2)
    val price: BigDecimal? = null,

    @Column(nullable = false)
    val stock: Int = 0,

    @ElementCollection
    @CollectionTable(name = "variant_attributes", joinColumns = [JoinColumn(name = "variant_id")])
    @MapKeyColumn(name = "attribute_key")
    @Column(name = "attribute_value")
    val attributes: Map<String, String> = emptyMap(),

    @Column(name = "image_url")
    val imageUrl: String? = null,

    val active: Boolean = true
)
