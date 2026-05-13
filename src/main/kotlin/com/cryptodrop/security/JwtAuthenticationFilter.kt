package com.cryptodrop.security

import com.cryptodrop.config.DatabaseUserDetailsService
import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.security.authentication.AnonymousAuthenticationToken
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource
import org.springframework.web.filter.OncePerRequestFilter

class JwtAuthenticationFilter(
    private val jwtService: JwtService,
    private val userDetailsService: DatabaseUserDetailsService,
) : OncePerRequestFilter() {

    override fun shouldNotFilter(request: HttpServletRequest): Boolean {
        val path = request.requestURI
        return path.startsWith("/oauth2/") ||
            path.startsWith("/login/oauth2/") ||
            path.startsWith("/css/") ||
            path.startsWith("/js/") ||
            path.startsWith("/images/")
    }

    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain,
    ) {
        val existing = SecurityContextHolder.getContext().authentication
        if (existing != null && existing.isAuthenticated && existing !is AnonymousAuthenticationToken) {
            filterChain.doFilter(request, response)
            return
        }

        val token = jwtService.readTokenCookie(request)
        if (token.isNullOrBlank()) {
            filterChain.doFilter(request, response)
            return
        }

        val username = jwtService.parseUsername(token)
        if (username == null) {
            response.addCookie(jwtService.clearCookie())
            filterChain.doFilter(request, response)
            return
        }

        val userDetails = try {
            userDetailsService.loadUserByUsername(username)
        } catch (_: Exception) {
            response.addCookie(jwtService.clearCookie())
            filterChain.doFilter(request, response)
            return
        }

        val auth = UsernamePasswordAuthenticationToken(userDetails, null, userDetails.authorities)
        auth.details = WebAuthenticationDetailsSource().buildDetails(request)
        SecurityContextHolder.getContext().authentication = auth
        filterChain.doFilter(request, response)
    }
}
