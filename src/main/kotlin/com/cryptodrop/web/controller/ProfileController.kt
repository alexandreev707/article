package com.cryptodrop.web.controller

import com.cryptodrop.service.UserService
import com.cryptodrop.service.dto.UpdateProfileDto
import jakarta.validation.Valid
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/profile")
class ProfileController(
    private val userService: UserService
) {

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    fun getProfile(): ResponseEntity<Any> {
        val userId = userService.getCurrentUserId() ?: return ResponseEntity.status(401).build()
        val profile = userService.getProfile(userId) ?: return ResponseEntity.notFound().build()
        return ResponseEntity.ok(profile)
    }

    @PutMapping
    @PreAuthorize("isAuthenticated()")
    fun updateProfile(@Valid @RequestBody dto: UpdateProfileDto): ResponseEntity<Any> {
        val userId = userService.getCurrentUserId() ?: return ResponseEntity.status(401).build()
        val updated = userService.updateProfile(userId, dto)
        return ResponseEntity.ok(updated)
    }
}
