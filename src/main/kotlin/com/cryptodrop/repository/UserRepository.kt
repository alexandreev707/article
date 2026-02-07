package com.cryptodrop.repository

import com.cryptodrop.model.User
import org.springframework.data.mongodb.repository.MongoRepository
import org.springframework.stereotype.Repository
import java.util.Optional

@Repository
interface UserRepository : MongoRepository<User, String> {
    fun findByKeycloakId(keycloakId: String): Optional<User>
    fun findByEmail(email: String): Optional<User>
    fun findByRolesContaining(role: String): List<User>
    fun findByBlocked(blocked: Boolean): List<User>
}

