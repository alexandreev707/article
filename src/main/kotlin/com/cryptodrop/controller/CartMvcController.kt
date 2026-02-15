package com.cryptodrop.controller

import com.cryptodrop.service.DeliveryOptionService
import com.cryptodrop.service.UserService
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping

@Controller
@RequestMapping("/cart")
@PreAuthorize("hasAnyRole('CUSTOMER', 'ADMIN')")
class CartMvcController(
    private val userService: UserService,
    private val deliveryOptionService: DeliveryOptionService
) {

    @GetMapping
    fun cartPage(model: Model): String {
        model.addAttribute("title", "Cart - Marketplace")
        model.addAttribute("currentUser", userService.getCurrentUser())
        return "cart/cart"
    }

    @GetMapping("/checkout")
    fun checkoutPage(model: Model): String {
        model.addAttribute("title", "Checkout - Marketplace")
        model.addAttribute("currentUser", userService.getCurrentUser())
        model.addAttribute("deliveryOptions", deliveryOptionService.findAllActive())
        return "cart/checkout"
    }
}
