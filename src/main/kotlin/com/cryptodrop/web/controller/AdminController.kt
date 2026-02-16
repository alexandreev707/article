package com.cryptodrop.web.controller

import com.cryptodrop.persistence.user.UserRole
import com.cryptodrop.service.AdminService
import com.cryptodrop.service.UserService
import com.cryptodrop.service.dto.UserResponseDto
import com.cryptodrop.service.dto.UserUpdateDto
import org.springframework.data.domain.PageRequest
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.*
import java.util.UUID

@Controller
@RequestMapping("/admin")
@PreAuthorize("hasRole('ADMIN')")
class AdminController(
    private val adminService: AdminService,
    private val userService: UserService
) {

    @GetMapping
    fun adminPanel(model: Model): String {
        model.addAttribute("title", "Admin panel - Marketplace")
        model.addAttribute("currentUser", userService.getCurrentUser())
        return "admin/index"
    }

    @GetMapping("/users")
    fun usersList(
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "20") size: Int,
        @RequestParam(required = false) role: String?,
        @RequestParam(required = false) blocked: Boolean?,
        model: Model
    ): String {
        val users = when {
            role != null -> {
                val userRole = UserRole.valueOf(role.removePrefix("ROLE_").uppercase())
                adminService.getUsersByRole(userRole, PageRequest.of(page, size))
            }
            blocked == true -> adminService.getBlockedUsers(PageRequest.of(page, size))
            else -> adminService.getAllUsers(PageRequest.of(page, size))
        }
        model.addAttribute("title", "User management - Marketplace")
        model.addAttribute("users", users.map { userService.toDto(it) })
        model.addAttribute("currentPage", page)
        model.addAttribute("totalPages", users.totalPages)
        model.addAttribute("currentUser", userService.getCurrentUser())
        return "admin/users"
    }
}

@RestController
@RequestMapping("/api/admin")
@PreAuthorize("hasRole('ADMIN')")
class AdminApiController(
    private val adminService: AdminService,
    private val userService: UserService
) {

    @GetMapping("/users/{id}")
    fun getUser(@PathVariable id: String): ResponseEntity<UserResponseDto> {
        val user = adminService.getUserById(UUID.fromString(id))
        return ResponseEntity.ok(userService.toDto(user))
    }

    @GetMapping("/users")
    fun getAllUsers(
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "20") size: Int,
        @RequestParam(required = false) role: String?,
        @RequestParam(required = false) blocked: Boolean?
    ): ResponseEntity<Map<String, Any>> {
        val users = when {
            role != null -> {
                val userRole = UserRole.valueOf(role.removePrefix("ROLE_").uppercase())
                adminService.getUsersByRole(userRole, PageRequest.of(page, size))
            }
            blocked == true -> adminService.getBlockedUsers(PageRequest.of(page, size))
            else -> adminService.getAllUsers(PageRequest.of(page, size))
        }
        return ResponseEntity.ok(mapOf(
            "users" to users.map { userService.toDto(it) },
            "totalPages" to users.totalPages,
            "currentPage" to page
        ))
    }

    @PutMapping("/users/{id}")
    fun updateUser(@PathVariable id: String, @RequestBody dto: UserUpdateDto): ResponseEntity<UserResponseDto> {
        val user = adminService.updateUser(UUID.fromString(id), dto)
        return ResponseEntity.ok(userService.toDto(user))
    }

    @PostMapping("/users/{id}/block")
    fun blockUser(@PathVariable id: String): ResponseEntity<UserResponseDto> {
        val user = adminService.blockUser(UUID.fromString(id))
        return ResponseEntity.ok(userService.toDto(user))
    }

    @PostMapping("/users/{id}/unblock")
    fun unblockUser(@PathVariable id: String): ResponseEntity<UserResponseDto> {
        val user = adminService.unblockUser(UUID.fromString(id))
        return ResponseEntity.ok(userService.toDto(user))
    }

    @PostMapping("/users/{id}/roles/{role}")
    fun grantRole(@PathVariable id: String, @PathVariable role: String): ResponseEntity<UserResponseDto> {
        val user = adminService.grantRole(UUID.fromString(id), role)
        return ResponseEntity.ok(userService.toDto(user))
    }

    @DeleteMapping("/users/{id}/roles/{role}")
    fun revokeRole(@PathVariable id: String, @PathVariable role: String): ResponseEntity<UserResponseDto> {
        val user = adminService.revokeRole(UUID.fromString(id), role)
        return ResponseEntity.ok(userService.toDto(user))
    }
}
