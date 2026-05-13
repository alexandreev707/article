package com.cryptodrop.config

import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component

@Component
class OAuth2LoginAvailability(
    @Value("\${spring.security.oauth2.client.registration.google.client-id:}") private val googleClientId: String,
    @Value("\${spring.security.oauth2.client.registration.facebook.client-id:}") private val facebookClientId: String,
) {
    fun googleEnabled(): Boolean = isConfigured(googleClientId)

    fun facebookEnabled(): Boolean = isConfigured(facebookClientId)

    private fun isConfigured(value: String): Boolean =
        value.isNotBlank() && value != "not-configured"
}
