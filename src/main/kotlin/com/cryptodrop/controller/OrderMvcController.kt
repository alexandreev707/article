package com.cryptodrop.controller

import com.cryptodrop.service.UserService
import com.cryptodrop.service.OrderService
import org.springframework.data.domain.PageRequest
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam

@Controller
@RequestMapping("/orders")
@PreAuthorize("hasAnyRole('CUSTOMER', 'SELLER', 'ADMIN')")
class OrderMvcController(
    private val orderService: OrderService,
    private val userService: UserService
) {

    @GetMapping
    fun listOrders(
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "20") size: Int,
        model: Model
    ): String {
        val userId = userService.getCurrentUserId()
            ?: throw IllegalStateException("User not authenticated")
        
        val isSeller = userService.hasRole("SELLER") || userService.hasRole("ADMIN")
        val orders = if (isSeller) {
            orderService.findBySeller(userId, PageRequest.of(page, size))
        } else {
            orderService.findByBuyer(userId, PageRequest.of(page, size))
        }

        model.addAttribute("orders", orders.map { orderService.toDto(it) })
        model.addAttribute("currentPage", page)
        model.addAttribute("totalPages", orders.totalPages)
        model.addAttribute("currentUser", userService.getCurrentUser())

        return "orders/list"
    }
}
