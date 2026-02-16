package com.cryptodrop.config

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.security.web.SecurityFilterChain

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
class SecurityConfig {

    @Bean
    fun passwordEncoder(): PasswordEncoder {
        return BCryptPasswordEncoder()
    }

    @Bean
    fun filterChain(http: HttpSecurity): SecurityFilterChain {
        http
            .csrf { it.disable() }
            .authorizeHttpRequests { requests ->
                requests
                    // ✅ СТАТИЧЕСКИЕ РЕСУРСЫ ПЕРВЫМИ - обязательно!
                    .requestMatchers("/css/**", "/js/**", "/images/**", "/static/**").permitAll()
                    .requestMatchers(
                        "/products/**", "/api/products/**",
                        "/api/categories", "/api/categories/*",
                        "/api/delivery-options", "/api/delivery-options/*"
                    ).permitAll()
                    .requestMatchers("/", "/login", "/logout", "/error", "/h2-console/**").permitAll()

                    // ✅ Роли после публичных ресурсов
                    .requestMatchers("/api/admin/**", "/admin/**").hasRole("ADMIN")
                    .requestMatchers("/api/seller/**", "/seller/**").hasAnyRole("SELLER", "ADMIN")
                    .anyRequest().authenticated()
            }
            .formLogin { form ->
                form
                    .loginPage("/login")
                    .loginProcessingUrl("/login")
                    .defaultSuccessUrl("/", true)
                    .failureUrl("/login?error")
                    .permitAll()
            }
            .logout { logout ->
                logout.logoutUrl("/logout")
                    .logoutSuccessUrl("/login?logout")
                    .permitAll()
            }
            .headers { headers ->
                headers.frameOptions { it.sameOrigin() }
            }
        return http.build()
    }



}
