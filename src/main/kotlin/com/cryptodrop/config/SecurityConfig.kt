package com.cryptodrop.config

import com.cryptodrop.security.CustomOAuth2UserService
import com.cryptodrop.security.JwtAuthenticationFilter
import com.cryptodrop.security.JwtIssuingAuthenticationSuccessHandler
import com.cryptodrop.security.JwtService
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.HttpMethod
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.web.SecurityFilterChain
import org.springframework.security.web.context.SecurityContextHolderFilter

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
class SecurityConfig(
    private val customOAuth2UserService: CustomOAuth2UserService,
    private val jwtService: JwtService,
    private val databaseUserDetailsService: DatabaseUserDetailsService,
) {

    @Bean
    fun jwtAuthenticationFilter(): JwtAuthenticationFilter =
        JwtAuthenticationFilter(jwtService, databaseUserDetailsService)

    @Bean
    fun jwtIssuingAuthenticationSuccessHandler(): JwtIssuingAuthenticationSuccessHandler =
        JwtIssuingAuthenticationSuccessHandler(jwtService)

    @Bean
    fun filterChain(
        http: HttpSecurity,
        jwtAuthenticationFilter: JwtAuthenticationFilter,
        jwtIssuingAuthenticationSuccessHandler: JwtIssuingAuthenticationSuccessHandler,
    ): SecurityFilterChain {
        http
            .csrf { it.disable() }
            .addFilterAfter(jwtAuthenticationFilter, SecurityContextHolderFilter::class.java)
            .authorizeHttpRequests { requests ->
                requests
                    .requestMatchers("/css/**", "/js/**", "/images/**", "/static/**").permitAll()
                    .requestMatchers(
                        "/products/**", "/api/products/**",
                        "/api/categories", "/api/categories/*",
                        "/api/delivery-options", "/api/delivery-options/*",
                        "/api/reviews/product/**",
                        "/api/checkout/oxapay/callback",
                    ).permitAll()
                    .requestMatchers(
                        "/",
                        "/login",
                        "/logout",
                        "/error",
                        "/h2-console/**",
                        "/oauth2/**",
                        "/login/oauth2/**",
                    ).permitAll()
                    .requestMatchers(HttpMethod.GET, "/api/auth/session").permitAll()
                    .requestMatchers("/actuator/health", "/actuator/info").permitAll()
                    .requestMatchers("/api/admin/**", "/admin/**").hasRole("ADMIN")
                    .requestMatchers("/api/seller/**", "/seller/**").hasAnyRole("SELLER", "ADMIN")
                    .anyRequest().authenticated()
            }
            .oauth2Login { oauth2 ->
                oauth2
                    .loginPage("/login")
                    .userInfoEndpoint { userInfo -> userInfo.userService(customOAuth2UserService) }
                    .successHandler(jwtIssuingAuthenticationSuccessHandler)
            }
            .formLogin { form ->
                form
                    .loginPage("/login")
                    .loginProcessingUrl("/login")
                    .successHandler(jwtIssuingAuthenticationSuccessHandler)
                    .failureUrl("/login?error")
                    .permitAll()
            }
            .logout { logout ->
                logout.logoutUrl("/logout")
                    .logoutSuccessUrl("/login?logout")
                    .addLogoutHandler { _, response, _ -> response.addCookie(jwtService.clearCookie()) }
                    .permitAll()
            }
            .headers { headers ->
                headers.frameOptions { it.sameOrigin() }
            }
        return http.build()
    }
}
