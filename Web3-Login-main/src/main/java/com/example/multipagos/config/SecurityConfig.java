package com.example.multipagos.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Bean;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;

import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
public class SecurityConfig {

    @Autowired
    private JwtFilter jwtFilter;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {

        http.csrf().disable();
        http.cors();

        http.sessionManagement()
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS);

        http
            .authorizeHttpRequests() // ← NUEVO Y OBLIGATORIO (2.7+)
            
            /* ==========================================================
               🔓 RUTAS PÚBLICAS (SIN TOKEN)
            ========================================================== */
            .antMatchers(
                    "/api/public/auth/login",
                    "/api/public/auth/login/**",
                    "/api/public/auth/register",
                    "/api/public/auth/register/**",
                    "/api/public/auth/me/context",
                    "/api/public/**",
                    "/h2-console/**"
            ).permitAll()

            /* ==========================================================
               🔐 SOLO ADMIN
            ========================================================== */
            .antMatchers("/api/admin/**")
            .hasRole("ADMIN")

            /* ==========================================================
               🔐 PROVIDER o ADMIN
            ========================================================== */
            .antMatchers("/api/provider/**")
            .hasAnyRole("PROVIDER", "ADMIN")

            /* ==========================================================
               🔐 CUALQUIER OTRA RUTA → REQUIERE TOKEN
            ========================================================== */
            .anyRequest()
            .authenticated();

        // Permitir consola H2
        http.headers().frameOptions().disable();

        // Insertar filtro JWT antes del login de Spring
        http.addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
