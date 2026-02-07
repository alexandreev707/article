package com.cryptodrop.controller

import com.cryptodrop.service.UserService
import com.cryptodrop.service.OrderService
import com.cryptodrop.service.ProductService
import org.springframework.data.domain.PageRequest
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.*

@Controller
@RequestMapping("/seller")
@PreAuthorize("hasRole('SELLER') or hasRole('ADMIN')")
class SellerController(
    private val productService: ProductService,
    private val orderService: OrderService,
    private val userService: UserService
) {

    @GetMapping("/products")
    fun sellerProducts(
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "20") size: Int,
        model: Model
    ): String {
        val sellerId = userService.getCurrentUserId()
            ?: throw IllegalStateException("User not authenticated")
        
        val products = productService.findBySeller(sellerId, PageRequest.of(page, size))
        model.addAttribute("products", products.map { productService.toDto(it) })
        model.addAttribute("currentPage", page)
        model.addAttribute("totalPages", products.totalPages)
        return "seller/products"
    }

    @GetMapping("/orders")
    fun sellerOrders(
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "20") size: Int,
        model: Model
    ): String {
        val sellerId = userService.getCurrentUserId()
            ?: throw IllegalStateException("User not authenticated")
        
        val orders = orderService.findBySeller(sellerId, PageRequest.of(page, size))
        model.addAttribute("orders", orders.map { orderService.toDto(it) })
        model.addAttribute("currentPage", page)
        model.addAttribute("totalPages", orders.totalPages)
        return "seller/orders"
    }
}
