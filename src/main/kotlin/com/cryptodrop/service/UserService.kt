package com.cryptodrop.service

import com.cryptodrop.dto.UserResponseDto
import com.cryptodrop.model.User
import com.cryptodrop.repository.UserRepository
import org.slf4j.LoggerFactory
import org.springframework.cache.annotation.Cacheable
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime

@Service
class UserService(
    private val userRepository: UserRepository
) {
    private val logger = LoggerFactory.getLogger(javaClass)

    fun findOrCreateUser(keycloakId: String, email: String, username: String, roles: Set<String>): User {
        return userRepository.findByKeycloakId(keycloakId)
            .orElseGet {
                logger.info("Creating new user: $email")
                val newUser = User(
                    keycloakId = keycloakId,
                    email = email,
                    username = username,
                    roles = roles
                )
                userRepository.save(newUser)
            }
    }

    @Transactional
    fun updateUserRoles(userId: String, roles: Set<String>): User {
        val user = userRepository.findById(userId)
            .orElseThrow { IllegalArgumentException("User not found: $userId") }
        val updatedUser = user.copy(
            roles = roles,
            updatedAt = LocalDateTime.now()
        )
        return userRepository.save(updatedUser)
    }

    @Transactional
    fun toggleFavorite(userId: String, productId: String): User {
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

    @Cacheable("users")
    fun findById(userId: String): User {
        return userRepository.findById(userId)
            .orElseThrow { IllegalArgumentException("User not found: $userId") }
    }

    fun findByKeycloakId(keycloakId: String): User? {
        return userRepository.findByKeycloakId(keycloakId).orElse(null)
    }

    fun toDto(user: User): UserResponseDto {
        return UserResponseDto(
            id = user.id!!,
            email = user.email,
            username = user.username,
            roles = user.roles,
            blocked = user.blocked
        )
    }
}

