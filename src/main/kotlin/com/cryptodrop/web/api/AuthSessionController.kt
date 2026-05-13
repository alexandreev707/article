package com.cryptodrop.web.api

import com.cryptodrop.security.JwtService
import jakarta.servlet.http.HttpServletRequest
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.authentication.AnonymousAuthenticationToken
import org.springframework.security.core.Authentication
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/auth")
class AuthSessionController(
    private val jwtService: JwtService,
) {

    @GetMapping("/session")
    fun session(request: HttpServletRequest, authentication: Authentication?): ResponseEntity<SessionResponse> {
        if (authentication == null || !authentication.isAuthenticated || authentication is AnonymousAuthenticationToken) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build()
        }
        val token = jwtService.readTokenCookie(request)
        val expiresAt = token?.let { jwtService.parseExpirationEpochSeconds(it) }
        return ResponseEntity.ok(
            SessionResponse(
                authenticated = true,
                username = authentication.name,
                expiresAtEpochSeconds = expiresAt,
            ),
        )
    }
}

data class SessionResponse(
    val authenticated: Boolean,
    val username: String,
    val expiresAtEpochSeconds: Long?,
)
