package com.cryptodrop.config

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "app.jwt")
data class JwtProperties(
    var secret: String = "",
    var expirationMinutes: Long = 120,
    var cookieName: String = "ACCESS_TOKEN",
    var cookieSecure: Boolean = false,
)
