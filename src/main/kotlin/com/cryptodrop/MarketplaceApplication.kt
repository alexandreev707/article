package com.cryptodrop

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.boot.runApplication
import org.springframework.cache.annotation.EnableCaching
import com.cryptodrop.config.JwtProperties

@SpringBootApplication
@EnableCaching
@EnableConfigurationProperties(JwtProperties::class)
class MarketplaceApplication

fun main(args: Array<String>) {
    runApplication<MarketplaceApplication>(*args)
}




