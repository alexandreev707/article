package com.cryptodrop.security

import com.cryptodrop.persistence.user.User
import com.cryptodrop.persistence.user.UserRepository
import com.cryptodrop.persistence.user.UserRole
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest
import org.springframework.security.oauth2.core.OAuth2AuthenticationException
import org.springframework.security.oauth2.core.OAuth2Error
import org.springframework.security.oauth2.core.user.DefaultOAuth2User
import org.springframework.security.oauth2.core.user.OAuth2User
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService
import org.springframework.stereotype.Service
import java.time.LocalDateTime
import java.util.UUID

@Service
class CustomOAuth2UserService(
    private val userRepository: UserRepository,
    private val passwordEncoder: PasswordEncoder,
) : OAuth2UserService<OAuth2UserRequest, OAuth2User> {

    private val delegate = DefaultOAuth2UserService()

    override fun loadUser(userRequest: OAuth2UserRequest): OAuth2User {
        val oauthUser = delegate.loadUser(userRequest)
        val provider = userRequest.clientRegistration.registrationId
        val subject = oauthUser.name

        val email = oauthUser.attributes["email"] as? String
            ?: throw OAuth2AuthenticationException(
                OAuth2Error("email_required", "Email permission is required for $provider login", null),
            )

        var user = userRepository.findByOauthProviderAndOauthSubject(provider, subject).orElse(null)
        if (user == null) {
            val byEmail = userRepository.findByEmail(email).orElse(null)
            user = if (byEmail != null) {
                userRepository.save(
                    byEmail.copy(
                        oauthProvider = provider,
                        oauthSubject = subject,
                        updatedAt = LocalDateTime.now(),
                    ),
                )
            } else {
                userRepository.save(createNewUser(email, oauthUser, provider, subject))
            }
        }

        if (user.blocked) {
            throw OAuth2AuthenticationException(OAuth2Error("access_denied", "Account is blocked", null))
        }

        val authorities = user.roles.map { SimpleGrantedAuthority("ROLE_${it.name}") }
        val attrs = LinkedHashMap<String, Any>(oauthUser.attributes)
        attrs[APP_USERNAME_ATTR] = user.username
        return DefaultOAuth2User(authorities, attrs, APP_USERNAME_ATTR)
    }

    private fun createNewUser(
        email: String,
        oauthUser: OAuth2User,
        provider: String,
        subject: String,
    ): User {
        val localPart = email.substringBefore('@').replace(Regex("[^a-zA-Z0-9_]"), "")
        var base = localPart.ifBlank { "user" }
        var candidate = base
        var n = 0
        while (userRepository.findByUsername(candidate).isPresent) {
            n++
            candidate = "$base$n"
        }
        val fullName = oauthUser.attributes["name"] as? String
        val verified = when (val v = oauthUser.attributes["email_verified"]) {
            is Boolean -> v
            is String -> v.equals("true", ignoreCase = true)
            else -> false
        }
        return User(
            email = email,
            username = candidate,
            password = passwordEncoder.encode(UUID.randomUUID().toString()),
            fullName = fullName,
            roles = setOf(UserRole.CUSTOMER),
            emailVerified = verified,
            oauthProvider = provider,
            oauthSubject = subject,
        )
    }

    companion object {
        private const val APP_USERNAME_ATTR = "app_username"
    }
}
