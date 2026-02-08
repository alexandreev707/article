package com.cryptodrop.controller

import com.cryptodrop.dto.UserUpdateDto
import com.cryptodrop.model.UserRole
import com.cryptodrop.service.AdminService
import com.cryptodrop.service.UserService
import org.springframework.data.domain.PageRequest
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.*

@Controller
@RequestMapping("/admin")
@PreAuthorize("hasRole('ADMIN')")
class AdminController(
    private val adminService: AdminService,
    private val userService: UserService
) {

    @GetMapping
    fun adminPanel(model: Model): String {
        model.addAttribute("title", "Панель администратора - Marketplace")
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

        model.addAttribute("title", "Управление пользователями - Marketplace")
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
    fun updateUser(
        @PathVariable id: String,
        @RequestBody dto: UserUpdateDto
    ): ResponseEntity<com.cryptodrop.dto.UserResponseDto> {
        val user = adminService.updateUser(id.toLong(), dto)
        return ResponseEntity.ok(userService.toDto(user))
    }

    @PostMapping("/users/{id}/block")
    fun blockUser(@PathVariable id: String): ResponseEntity<com.cryptodrop.dto.UserResponseDto> {
        val user = adminService.blockUser(id.toLong())
        return ResponseEntity.ok(userService.toDto(user))
    }

    @PostMapping("/users/{id}/unblock")
    fun unblockUser(@PathVariable id: String): ResponseEntity<com.cryptodrop.dto.UserResponseDto> {
        val user = adminService.unblockUser(id.toLong())
        return ResponseEntity.ok(userService.toDto(user))
    }

    @PostMapping("/users/{id}/roles/{role}")
    fun grantRole(
        @PathVariable id: String,
        @PathVariable role: String
    ): ResponseEntity<com.cryptodrop.dto.UserResponseDto> {
        val user = adminService.grantRole(id.toLong(), role)
        return ResponseEntity.ok(userService.toDto(user))
    }

    @DeleteMapping("/users/{id}/roles/{role}")
    fun revokeRole(
        @PathVariable id: String,
        @PathVariable role: String
    ): ResponseEntity<com.cryptodrop.dto.UserResponseDto> {
        val user = adminService.revokeRole(id.toLong(), role)
        return ResponseEntity.ok(userService.toDto(user))
    }
}
