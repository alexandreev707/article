package com.cryptodrop.service

import com.cryptodrop.persistence.order.Address
import com.cryptodrop.persistence.order.Order
import com.cryptodrop.persistence.order.OrderItem
import com.cryptodrop.persistence.order.OrderRepository
import com.cryptodrop.persistence.order.OrderStatus
import com.cryptodrop.persistence.product.ProductRepository
import com.cryptodrop.service.dto.AddressDto
import com.cryptodrop.service.dto.CheckoutDto
import com.cryptodrop.service.dto.OrderCreateDto
import com.cryptodrop.service.dto.OrderResponseDto
import com.cryptodrop.service.dto.OrderStatusUpdateDto
import org.slf4j.LoggerFactory
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.math.BigDecimal
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.UUID

@Service
class OrderService(
    private val orderRepository: OrderRepository,
    private val productRepository: ProductRepository,
    private val productService: ProductService,
    private val userService: UserService,
    private val cartService: CartService,
    private val deliveryOptionService: DeliveryOptionService
) {
    private val logger = LoggerFactory.getLogger(javaClass)
    private val dateFormatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME

    private fun toAddress(dto: AddressDto) = Address(
        addressLine = dto.street,
        city = dto.city,
        region = dto.state,
        zipCode = dto.zipCode,
        country = dto.country
    )

    private fun orderNumber() = "ORD-${System.currentTimeMillis()}"

    @Transactional
    fun createOrder(buyerId: UUID, dto: OrderCreateDto): Order {
        val product = productRepository.findById(UUID.fromString(dto.productId))
            .orElseThrow { IllegalArgumentException("Product not found: ${dto.productId}") }
        if (!product.active) throw IllegalStateException("Product is not available")
        if (product.stock < dto.quantity) throw IllegalStateException("Insufficient stock")

        val buyer = userService.findById(buyerId)
        val unitPrice = product.discountPrice ?: product.price
        val totalPrice = unitPrice.multiply(BigDecimal.valueOf(dto.quantity.toLong()))
        val address = toAddress(dto.shippingAddress)

        val order = Order(
            orderNumber = orderNumber(),
            buyer = buyer,
            items = mutableListOf(),
            subtotal = totalPrice,
            totalPrice = totalPrice,
            shippingAddress = address
        )
        val orderItem = OrderItem(
            order = order,
            product = product,
            seller = product.seller,
            quantity = dto.quantity,
            unitPrice = unitPrice,
            totalPrice = totalPrice
        )
        order.items.add(orderItem)

        productRepository.save(product.copy(
            stock = product.stock - dto.quantity,
            updatedAt = LocalDateTime.now()
        ))

        logger.info("Order created: ${order.id} by buyer: $buyerId")
        return orderRepository.save(order)
    }

    @Transactional
    fun updateOrderStatus(orderId: UUID, sellerId: UUID, dto: OrderStatusUpdateDto): Order {
        val order = orderRepository.findById(orderId)
            .orElseThrow { IllegalArgumentException("Order not found: $orderId") }
        val hasSellerItem = order.items.any { it.seller.id == sellerId }
        if (!hasSellerItem) throw IllegalStateException("Only order seller can update status")

        order.status = dto.status
        order.updatedAt = LocalDateTime.now()
        logger.info("Order status updated: $orderId to ${dto.status}")
        return orderRepository.save(order)
    }

    @Transactional
    fun createOrdersFromCart(buyerId: UUID, dto: CheckoutDto): List<Order> {
        val cartItems = cartService.getCartItems(buyerId)
        if (cartItems.isEmpty()) throw IllegalStateException("Cart is empty")

        val delivery = deliveryOptionService.findById(UUID.fromString(dto.deliveryOptionId))
        val address = when {
            dto.shippingAddress != null -> toAddress(dto.shippingAddress)
            else -> Address(
                addressLine = delivery.pickupAddress ?: "",
                city = "",
                region = "",
                zipCode = "",
                country = ""
            )
        }

        val subtotal = cartItems.sumOf { item ->
            val p = item.product
            (p.discountPrice ?: p.price).multiply(BigDecimal(item.quantity))
        }
        val totalDiscount = dto.discountAmount ?: BigDecimal.ZERO
        val orders = mutableListOf<Order>()

        for (item in cartItems) {
            val product = item.product
            if (!product.active) throw IllegalStateException("Product ${product.title} is not available")
            if (product.stock < item.quantity) throw IllegalStateException("Insufficient stock for ${product.title}")

            val itemSubtotal = (product.discountPrice ?: product.price).multiply(BigDecimal(item.quantity))
            val itemDiscount = if (subtotal > BigDecimal.ZERO && totalDiscount > BigDecimal.ZERO) {
                itemSubtotal.multiply(totalDiscount).divide(subtotal, 2, java.math.RoundingMode.HALF_UP)
            } else BigDecimal.ZERO
            val totalPrice = itemSubtotal.subtract(itemDiscount)
            val unitPrice = totalPrice.divide(BigDecimal(item.quantity), 2, java.math.RoundingMode.HALF_UP)

            val buyer = userService.findById(buyerId)
            val order = Order(
                orderNumber = orderNumber(),
                buyer = buyer,
                items = mutableListOf(),
                subtotal = totalPrice,
                discountAmount = itemDiscount,
                totalPrice = totalPrice,
                shippingAddress = address,
                deliveryOption = delivery
            )
            val orderItem = OrderItem(
                order = order,
                product = product,
                seller = product.seller,
                quantity = item.quantity,
                unitPrice = unitPrice,
                totalPrice = totalPrice
            )
            order.items.add(orderItem)

            productRepository.save(product.copy(
                stock = product.stock - item.quantity,
                updatedAt = LocalDateTime.now()
            ))

            orders.add(orderRepository.save(order))
            cartService.removeItem(buyerId, product.id!!)
        }

        logger.info("Created ${orders.size} orders from cart for buyer: $buyerId")
        return orders
    }

    fun findByBuyer(buyerId: UUID, pageable: Pageable): Page<Order> {
        return orderRepository.findByBuyerId(buyerId, pageable)
    }

    fun findBySeller(sellerId: UUID, pageable: Pageable): Page<Order> {
        return orderRepository.findBySellerId(sellerId, pageable)
    }

    fun findById(orderId: UUID): Order {
        return orderRepository.findById(orderId)
            .orElseThrow { IllegalArgumentException("Order not found: $orderId") }
    }

    fun toDto(order: Order, productTitle: String? = null): OrderResponseDto {
        val item = order.items.firstOrNull()
            ?: throw IllegalStateException("Order has no items")
        val product = productTitle ?: item.product.title
        return OrderResponseDto(
            id = order.id.toString(),
            buyerId = order.buyer.id!!.toString(),
            sellerId = item.seller.id!!.toString(),
            productId = item.product.id!!.toString(),
            productTitle = product,
            quantity = item.quantity,
            totalPrice = item.totalPrice,
            discountAmount = order.discountAmount.takeIf { it > BigDecimal.ZERO },
            status = order.status,
            shippingAddress = AddressDto(
                street = order.shippingAddress.addressLine,
                city = order.shippingAddress.city,
                state = order.shippingAddress.region,
                zipCode = order.shippingAddress.zipCode,
                country = order.shippingAddress.country
            ),
            createdAt = order.createdAt.format(dateFormatter),
            updatedAt = order.updatedAt.format(dateFormatter)
        )
    }
}
