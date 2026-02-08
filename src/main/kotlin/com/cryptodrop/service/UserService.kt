package com.cryptodrop.service

import com.cryptodrop.dto.UserResponseDto
import com.cryptodrop.model.User
import com.cryptodrop.model.UserRole
import com.cryptodrop.repository.UserRepository
import org.slf4j.LoggerFactory
import org.springframework.cache.annotation.Cacheable
import org.springframework.security.core.Authentication
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime

@Service
class UserService(
    private val userRepository: UserRepository
) {
    private val logger = LoggerFactory.getLogger(javaClass)

    // Security context methods
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

    fun getCurrentUserId(): Long? {
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

    // User management methods
    @Transactional
    fun updateUserRoles(userId: Long, roles: Set<UserRole>): User {
        val user = userRepository.findById(userId)
            .orElseThrow { IllegalArgumentException("User not found: $userId") }
        val updatedUser = user.copy(
            roles = roles,
            updatedAt = LocalDateTime.now()
        )
        return userRepository.save(updatedUser)
    }

    @Transactional
    fun toggleFavorite(userId: Long, productId: Long): User {
        val user = userRepository.findById(userId)
            .orElseThrow { IllegalArgumentException("User not found: $userId") }
        
        val updatedFavorites = if (user.favoriteProductIds.contains(productId)) {
            user.favoriteProductIds.apply { remove(productId) }
        } else {
            user.favoriteProductIds.apply { add(productId) }
        }
        
        val updatedUser = user.copy(
            favoriteProductIds = updatedFavorites,
            updatedAt = LocalDateTime.now()
        )
        return userRepository.save(updatedUser)
    }

    fun addFavorite(userId: Long, productId: Long): User {
        val user = findById(userId)
        if (!user.favoriteProductIds.contains(productId)) {
            user.favoriteProductIds.add(productId)
            return userRepository.save(user)
        }
        return user
    }

    fun removeFavorite(userId: Long, productId: Long): User {
        val user = findById(userId)
        user.favoriteProductIds.remove(productId)
        return userRepository.save(user)
    }

   // @Cacheable("users")
    fun findById(userId: Long): User {
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
            blocked = user.blocked
        )
    }
}
