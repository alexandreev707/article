package com.cryptodrop.web.controller

import com.cryptodrop.service.UserService
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping

@Controller
@RequestMapping("/profile")
@PreAuthorize("isAuthenticated()")
class ProfileMvcController(
    private val userService: UserService
) {

    @GetMapping
    fun profilePage(model: Model): String {
        model.addAttribute("title", "Профиль - Marketplace")
        model.addAttribute("currentUser", userService.getCurrentUser())
        return "profile/index"
    }
}
