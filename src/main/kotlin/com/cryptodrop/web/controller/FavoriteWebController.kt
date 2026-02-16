package com.cryptodrop.web.controller

import com.cryptodrop.service.UserService
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping

@Controller
@RequestMapping("/favorites")
class FavoriteWebController(private val userService: UserService) {

    @GetMapping
    fun favorites(model: Model): String {
        model.addAttribute("currentUser", userService.getCurrentUser())
        model.addAttribute("title", "Favorites - Marketplace")
        return "favorites/list"
    }
}
