package com.cryptodrop.service

import com.cryptodrop.dto.UserUpdateDto
import com.cryptodrop.model.User
import com.cryptodrop.model.UserRole
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

    fun getUsersByRole(role: UserRole, pageable: Pageable): Page<User> {
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
    fun updateUser(userId: Long, dto: UserUpdateDto): User {
        val user = userRepository.findById(userId)
            .orElseThrow { IllegalArgumentException("User not found: $userId") }

        val updatedRoles = dto.roles?.map { roleString ->
            UserRole.valueOf(roleString.removePrefix("ROLE_").uppercase())
        }?.toSet() ?: user.roles

        val updatedUser = user.copy(
            roles = updatedRoles,
            blocked = dto.blocked ?: user.blocked,
            updatedAt = LocalDateTime.now()
        )

        logger.info("User updated by admin: $userId, roles: ${updatedUser.roles}, blocked: ${updatedUser.blocked}")
        return userRepository.save(updatedUser)
    }

    @CacheEvict(value = ["users"], allEntries = true)
    @Transactional
    fun blockUser(userId: Long): User {
        return updateUser(userId, UserUpdateDto(blocked = true))
    }

    @CacheEvict(value = ["users"], allEntries = true)
    @Transactional
    fun unblockUser(userId: Long): User {
        return updateUser(userId, UserUpdateDto(blocked = false))
    }

    @CacheEvict(value = ["users"], allEntries = true)
    @Transactional
    fun grantRole(userId: Long, role: String): User {
        val user = userRepository.findById(userId)
            .orElseThrow { IllegalArgumentException("User not found: $userId") }

        val userRole = UserRole.valueOf(role.removePrefix("ROLE_").uppercase())
        val updatedRoles = user.roles.toMutableSet().apply { add(userRole) }
        return updateUser(userId, UserUpdateDto(roles = updatedRoles.map { "ROLE_${it.name}" }.toSet()))
    }

    @CacheEvict(value = ["users"], allEntries = true)
    @Transactional
    fun revokeRole(userId: Long, role: String): User {
        val user = userRepository.findById(userId)
            .orElseThrow { IllegalArgumentException("User not found: $userId") }

        val userRole = UserRole.valueOf(role.removePrefix("ROLE_").uppercase())
        val updatedRoles = user.roles.toMutableSet().apply { remove(userRole) }
        return updateUser(userId, UserUpdateDto(roles = updatedRoles.map { "ROLE_${it.name}" }.toSet()))
    }
}
