package com.cryptodrop.web.controller

import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.GetMapping

@Controller
class LoginController {

    @GetMapping("/login")
    fun login(model: Model): String {
        model.addAttribute("title", "Sign in - Marketplace")
        return "login"
    }
}
