package com.cryptodrop.service

import com.cryptodrop.persistence.user.User
import com.cryptodrop.persistence.user.UserRepository
import com.cryptodrop.persistence.user.UserRole
import com.cryptodrop.service.dto.UserResponseDto
import org.slf4j.LoggerFactory
import org.springframework.cache.annotation.Cacheable
import org.springframework.security.core.Authentication
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime
import java.util.UUID

@Service
class UserService(
    private val userRepository: UserRepository
) {
    private val logger = LoggerFactory.getLogger(javaClass)

    fun getCurrentUser(): User? {
        return try {
            val authentication: Authentication? = SecurityContextHolder.getContext().authentication
            if (authentication != null && authentication.isAuthenticated) {
                val username = authentication.name
                userRepository.findByUsername(username).orElse(null)
            } else {
                null
            }
        } catch (e: Exception) {
            null
        }
    }

    fun getCurrentUserId(): UUID? {
        return getCurrentUser()?.id
    }

    fun getCurrentUserRoles(): Set<UserRole> {
        return getCurrentUser()?.roles ?: emptySet()
    }

    fun hasRole(role: UserRole): Boolean {
        return getCurrentUserRoles().contains(role)
    }

    fun hasRole(role: String): Boolean {
        return try {
            val userRole = UserRole.valueOf(role.removePrefix("ROLE_").uppercase())
            hasRole(userRole)
        } catch (e: Exception) {
            false
        }
    }

    fun isAdmin(userId: UUID): Boolean {
        return findById(userId).roles.contains(UserRole.ADMIN)
    }

    @Transactional
    fun updateUserRoles(userId: UUID, roles: Set<UserRole>): User {
        val user = userRepository.findById(userId)
            .orElseThrow { IllegalArgumentException("User not found: $userId") }
        val updatedUser = user.copy(
            roles = roles,
            updatedAt = LocalDateTime.now()
        )
        return userRepository.save(updatedUser)
    }

    fun findById(userId: UUID): User {
        return userRepository.findById(userId)
            .orElseThrow { IllegalArgumentException("User not found: $userId") }
    }

    fun findByEmail(email: String): User? {
        return userRepository.findByEmail(email).orElse(null)
    }

    fun findByUsername(username: String): User? {
        return userRepository.findByUsername(username).orElse(null)
    }

    fun toDto(user: User): UserResponseDto {
        return UserResponseDto(
            id = user.id.toString(),
            email = user.email,
            username = user.username,
            roles = user.roles.map { "ROLE_${it.name}" }.toSet(),
            blocked = user.blocked,
            emailVerified = user.emailVerified
        )
    }
}
