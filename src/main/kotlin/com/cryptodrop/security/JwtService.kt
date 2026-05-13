package com.cryptodrop.security

import com.cryptodrop.config.JwtProperties
import io.jsonwebtoken.JwtException
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.security.Keys
import jakarta.servlet.http.Cookie
import jakarta.servlet.http.HttpServletRequest
import org.springframework.security.core.Authentication
import org.springframework.stereotype.Service
import java.nio.charset.StandardCharsets
import java.security.MessageDigest
import java.time.Instant
import java.util.Date
import javax.crypto.SecretKey

@Service
class JwtService(
    private val jwtProperties: JwtProperties,
) {

    private val signingKey: SecretKey by lazy {
        val raw = jwtProperties.secret.toByteArray(StandardCharsets.UTF_8)
        if (raw.size >= 32) {
            Keys.hmacShaKeyFor(raw)
        } else {
            val digest = MessageDigest.getInstance("SHA-256").digest(raw)
            Keys.hmacShaKeyFor(digest)
        }
    }

    fun cookieName(): String = jwtProperties.cookieName

    fun cookieMaxAgeSeconds(): Int =
        (jwtProperties.expirationMinutes * 60).toInt().coerceAtLeast(60)

    fun createToken(authentication: Authentication): String {
        val username = authentication.name
        val roles = authentication.authorities.map { it.authority.removePrefix("ROLE_") }
        val now = Instant.now()
        val exp = now.plusSeconds(jwtProperties.expirationMinutes * 60)
        return Jwts.builder()
            .subject(username)
            .claim(ROLES_CLAIM, roles)
            .issuedAt(Date.from(now))
            .expiration(Date.from(exp))
            .signWith(signingKey)
            .compact()
    }

    fun parseUsername(token: String): String? =
        try {
            Jwts.parser()
                .verifyWith(signingKey)
                .build()
                .parseSignedClaims(token)
                .payload
                .subject
        } catch (_: JwtException) {
            null
        }

    fun parseExpirationEpochSeconds(token: String): Long? =
        try {
            val exp = Jwts.parser()
                .verifyWith(signingKey)
                .build()
                .parseSignedClaims(token)
                .payload
                .expiration
            exp.time / 1000
        } catch (_: JwtException) {
            null
        }

    fun readTokenCookie(request: HttpServletRequest): String? =
        request.cookies
            ?.firstOrNull { it.name == jwtProperties.cookieName }
            ?.value
            ?.takeIf { it.isNotBlank() }

    fun buildCookie(token: String, maxAgeSeconds: Int): Cookie =
        Cookie(jwtProperties.cookieName, token).apply {
            isHttpOnly = true
            path = "/"
            secure = jwtProperties.cookieSecure
            this.maxAge = maxAgeSeconds
        }

    fun clearCookie(): Cookie =
        Cookie(jwtProperties.cookieName, "").apply {
            isHttpOnly = true
            path = "/"
            secure = jwtProperties.cookieSecure
            maxAge = 0
        }

    companion object {
        private const val ROLES_CLAIM = "roles"
    }
}
