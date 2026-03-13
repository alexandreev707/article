package com.cryptodrop.config

import org.springframework.boot.SpringApplication
import org.springframework.boot.env.EnvironmentPostProcessor
import org.springframework.core.env.ConfigurableEnvironment
import org.springframework.core.env.MapPropertySource
import java.net.URI

/**
 * Render (и др.) передают DATABASE_URL вида postgresql://user:pass@host:5432/dbname.
 * Парсим в spring.datasource.* и добавляем sslmode=require (иначе часто EOF при auth).
 */
class DatabaseUrlEnvironmentPostProcessor : EnvironmentPostProcessor {

    override fun postProcessEnvironment(environment: ConfigurableEnvironment, application: SpringApplication) {
        val raw = environment.getProperty("DATABASE_URL")
            ?: System.getenv("DATABASE_URL")
            ?: return
        val normalized = raw.trim().replaceFirst(Regex("^postgres://"), "postgresql://")
        if (!normalized.startsWith("postgresql://")) return

        val parsed = parsePostgresqlUri(normalized) ?: return
        val sslMode = environment.getProperty("DB_SSL_MODE")
            ?: System.getenv("DB_SSL_MODE")
            ?: "require"
        val jdbcUrl = "jdbc:postgresql://${parsed.host}:${parsed.port}/${parsed.database}?sslmode=$sslMode"

        val source = MapPropertySource(
            "renderDatabaseUrl",
            mapOf(
                "spring.datasource.url" to jdbcUrl,
                "spring.datasource.username" to parsed.username,
                "spring.datasource.password" to parsed.password,
                "spring.datasource.driver-class-name" to "org.postgresql.Driver"
            )
        )
        environment.propertySources.addFirst(source)
    }

    private data class Parsed(val host: String, val port: Int, val database: String, val username: String, val password: String)

    private fun parsePostgresqlUri(uriString: String): Parsed? {
        return try {
            val uri = URI(uriString)
            val userInfo = uri.userInfo ?: return null
            val colon = userInfo.indexOf(':')
            val username = if (colon >= 0) userInfo.substring(0, colon) else userInfo
            val password = if (colon >= 0) userInfo.substring(colon + 1) else ""
            val decodedUser = java.net.URLDecoder.decode(username, Charsets.UTF_8)
            val decodedPass = java.net.URLDecoder.decode(password, Charsets.UTF_8)
            val host = uri.host ?: return null
            val port = if (uri.port > 0) uri.port else 5432
            val path = uri.path?.trim('/') ?: return null
            val database = path.substringAfterLast('/').ifEmpty { return null }
            Parsed(host, port, database, decodedUser, decodedPass)
        } catch (_: Exception) {
            null
        }
    }
}
