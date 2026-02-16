package com.cryptodrop.config

import com.cryptodrop.service.UserService
import com.cryptodrop.service.WishlistService
import org.springframework.web.bind.annotation.ControllerAdvice
import org.springframework.web.bind.annotation.ModelAttribute

@ControllerAdvice
class ViewControllerAdvice(
    private val userService: UserService,
    private val wishlistService: WishlistService
) {

    @ModelAttribute("currentUser")
    fun currentUser() = userService.getCurrentUser()

    @ModelAttribute("favoriteProductIds")
    fun favoriteProductIds(): List<String> {
        val userId = userService.getCurrentUserId() ?: return emptyList()
        return wishlistService.getFavoriteProductIds(userId).map { it.toString() }
    }
}
