package com.cryptodrop.security

import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.security.core.Authentication
import org.springframework.security.web.authentication.SavedRequestAwareAuthenticationSuccessHandler

/**
 * After form or OAuth login: persist JWT in an HTTP-only cookie alongside the session.
 */
class JwtIssuingAuthenticationSuccessHandler(
    private val jwtService: JwtService,
) : SavedRequestAwareAuthenticationSuccessHandler() {

    init {
        setDefaultTargetUrl("/")
        setAlwaysUseDefaultTargetUrl(false)
    }

    override fun onAuthenticationSuccess(
        request: HttpServletRequest,
        response: HttpServletResponse,
        authentication: Authentication,
    ) {
        val token = jwtService.createToken(authentication)
        response.addCookie(jwtService.buildCookie(token, jwtService.cookieMaxAgeSeconds()))
        super.onAuthenticationSuccess(request, response, authentication)
    }
}
