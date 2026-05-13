package com.cryptodrop.web.controller

import com.cryptodrop.config.OAuth2LoginAvailability
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.GetMapping

@Controller
class LoginController(
    private val oauth2LoginAvailability: OAuth2LoginAvailability,
) {

    @GetMapping("/login")
    fun login(model: Model): String {
        model.addAttribute("title", "Sign in - Marketplace")
        model.addAttribute("oauth2GoogleEnabled", oauth2LoginAvailability.googleEnabled())
        model.addAttribute("oauth2FacebookEnabled", oauth2LoginAvailability.facebookEnabled())
        return "login"
    }
}
