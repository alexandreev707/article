package com.cryptodrop.web.controller

import jakarta.servlet.RequestDispatcher
import jakarta.servlet.http.HttpServletRequest
import org.springframework.boot.web.servlet.error.ErrorController
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.RequestMapping

@Controller
class CustomErrorController : ErrorController {

    @RequestMapping("/error")
    fun handleError(request: HttpServletRequest, model: Model): String {
        val status = request.getAttribute(RequestDispatcher.ERROR_STATUS_CODE) as? HttpStatus
            ?: HttpStatus.INTERNAL_SERVER_ERROR
        model.addAttribute("status", status.value())
        model.addAttribute("error", status.reasonPhrase)
        model.addAttribute("message", request.getAttribute(RequestDispatcher.ERROR_MESSAGE) ?: "An error occurred")
        return "error"
    }
}
