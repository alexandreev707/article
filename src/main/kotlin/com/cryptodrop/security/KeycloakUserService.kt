package com.cryptodrop.security

import com.cryptodrop.model.User
import com.cryptodrop.service.UserService
import jakarta.servlet.http.HttpServletRequest
import org.keycloak.KeycloakPrincipal
import org.keycloak.adapters.springsecurity.token.KeycloakAuthenticationToken
import org.slf4j.LoggerFactory
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.stereotype.Component

@Component
class KeycloakUserService(
    private val userService: UserService
) {
    private val logger = LoggerFactory.getLogger(javaClass)

    fun getCurrentUser(): User? {
        return try {
            val authentication = SecurityContextHolder.getContext().authentication
            if (authentication is KeycloakAuthenticationToken) {
                val principal = authentication.principal as? KeycloakPrincipal<*>
                val keycloakId = principal?.keycloakSecurityContext?.token?.subject
                val email = principal?.keycloakSecurityContext?.token?.email
                val username = principal?.keycloakSecurityContext?.token?.preferredUsername
                val roles = authentication.authorities.map { it.authority }.toSet()

                if (keycloakId != null && email != null && username != null) {
                    userService.findOrCreateUser(keycloakId, email, username, roles)
                } else {
                    null
                }
            } else {
                null
            }
        } catch (e: Exception) {
            logger.error("Error getting current user", e)
            null
        }
    }

    fun getCurrentUserId(): String? {
        return getCurrentUser()?.id
    }

    fun getCurrentUserRoles(): Set<String> {
        return try {
            val authentication = SecurityContextHolder.getContext().authentication
            if (authentication is KeycloakAuthenticationToken) {
                authentication.authorities.map { it.authority }.toSet()
            } else {
                emptySet()
            }
        } catch (e: Exception) {
            logger.error("Error getting current user roles", e)
            emptySet()
        }
    }

    fun hasRole(role: String): Boolean {
        return getCurrentUserRoles().contains(role) || getCurrentUserRoles().contains("ROLE_$role")
    }
}

