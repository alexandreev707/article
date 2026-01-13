package com.cryptodrop.integration.solana

import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.web.reactive.function.client.WebClient

@Configuration
class SolanaConfig {
    
    @Bean
    fun solanaWebClient(@Value("\${solana.rpc-url}") rpcUrl: String): WebClient {
        return WebClient.builder()
            .baseUrl(rpcUrl)
            .build()
    }
}
