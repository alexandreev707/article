package com.cryptodrop.config

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer
import org.springframework.security.core.userdetails.User
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.security.provisioning.InMemoryUserDetailsManager
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
    fun userDetailsService(): UserDetailsService {
        val passwordEncoder = passwordEncoder()

        val customer = User.withUsername("customer")
            .password(passwordEncoder.encode("password"))
            .roles("CUSTOMER")
            .build()

        val seller = User.withUsername("seller")
            .password(passwordEncoder.encode("password"))
            .roles("SELLER", "CUSTOMER")
            .build()

        val admin = User.withUsername("admin")
            .password(passwordEncoder.encode("password"))
            .roles("ADMIN", "SELLER", "CUSTOMER")
            .build()

        return InMemoryUserDetailsManager(customer, seller, admin)
    }

    @Bean
    fun filterChain(http: HttpSecurity): SecurityFilterChain {
        http
            .csrf { it.disable() }
            .authorizeHttpRequests { requests ->
                requests
                    // ✅ СТАТИЧЕСКИЕ РЕСУРСЫ ПЕРВЫМИ - обязательно!
                    .requestMatchers("/css/**", "/js/**", "/images/**", "/static/**").permitAll()
                    .requestMatchers("/products/**", "/api/products/**").permitAll()
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
