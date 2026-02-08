package com.cryptodrop.controller

import com.cryptodrop.service.ProductService
import com.cryptodrop.service.UserService
import org.springframework.data.domain.PageRequest
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestParam

@Controller
class HomeController(
    private val productService: ProductService,
    private val userService: UserService
) {

    @GetMapping("/")
    fun home(
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "12") size: Int,
        model: Model
    ): String {
        val currentUser = userService.getCurrentUser()
        val userId = currentUser?.id

        val recommendedProducts = productService.getRecommendedProducts(userId, 8)
        val popularCategories = productService.getPopularCategories(6)
        val allProducts = productService.findAllActive(PageRequest.of(page, size))

        model.addAttribute("title", "Главная - Marketplace")
        model.addAttribute("recommendedProducts", recommendedProducts.map { productService.toDto(it) })
        model.addAttribute("popularCategories", popularCategories)
        model.addAttribute("products", allProducts.map { productService.toDto(it) })
        model.addAttribute("currentPage", page)
        model.addAttribute("totalPages", allProducts.totalPages)
        model.addAttribute("currentUser", currentUser)

        return "index"
    }
}
