package com.cryptodrop.service

import com.cryptodrop.dto.UserUpdateDto
import com.cryptodrop.model.User
import com.cryptodrop.repository.UserRepository
import org.slf4j.LoggerFactory
import org.springframework.cache.annotation.CacheEvict
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime

@Service
class AdminService(
    private val userRepository: UserRepository
) {
    private val logger = LoggerFactory.getLogger(javaClass)

    fun getAllUsers(pageable: Pageable): Page<User> {
        return userRepository.findAll(pageable)
    }

    fun getUsersByRole(role: String, pageable: Pageable): Page<User> {
        val users = userRepository.findByRolesContaining(role)
        val start = pageable.offset.toInt()
        val end = (start + pageable.pageSize).coerceAtMost(users.size)
        return org.springframework.data.domain.PageImpl(users.subList(start, end), pageable, users.size.toLong())
    }

    fun getBlockedUsers(pageable: Pageable): Page<User> {
        val users = userRepository.findByBlocked(true)
        val start = pageable.offset.toInt()
        val end = (start + pageable.pageSize).coerceAtMost(users.size)
        return org.springframework.data.domain.PageImpl(users.subList(start, end), pageable, users.size.toLong())
    }

    @CacheEvict(value = ["users"], allEntries = true)
    @Transactional
    fun updateUser(userId: String, dto: UserUpdateDto): User {
        val user = userRepository.findById(userId)
            .orElseThrow { IllegalArgumentException("User not found: $userId") }

        val updatedUser = user.copy(
            roles = dto.roles ?: user.roles,
            blocked = dto.blocked ?: user.blocked,
            updatedAt = LocalDateTime.now()
        )

        logger.info("User updated by admin: $userId, roles: ${updatedUser.roles}, blocked: ${updatedUser.blocked}")
        return userRepository.save(updatedUser)
    }

    @CacheEvict(value = ["users"], allEntries = true)
    @Transactional
    fun blockUser(userId: String): User {
        return updateUser(userId, UserUpdateDto(blocked = true))
    }

    @CacheEvict(value = ["users"], allEntries = true)
    @Transactional
    fun unblockUser(userId: String): User {
        return updateUser(userId, UserUpdateDto(blocked = false))
    }

    @CacheEvict(value = ["users"], allEntries = true)
    @Transactional
    fun grantRole(userId: String, role: String): User {
        val user = userRepository.findById(userId)
            .orElseThrow { IllegalArgumentException("User not found: $userId") }

        val updatedRoles = user.roles.toMutableSet().apply { add(role) }
        return updateUser(userId, UserUpdateDto(roles = updatedRoles))
    }

    @CacheEvict(value = ["users"], allEntries = true)
    @Transactional
    fun revokeRole(userId: String, role: String): User {
        val user = userRepository.findById(userId)
            .orElseThrow { IllegalArgumentException("User not found: $userId") }

        val updatedRoles = user.roles.toMutableSet().apply { remove(role) }
        return updateUser(userId, UserUpdateDto(roles = updatedRoles))
    }
}

