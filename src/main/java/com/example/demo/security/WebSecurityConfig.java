package com.example.demo.security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import com.example.demo.service.JwtService;

@Configuration
public class WebSecurityConfig {

        @Autowired
        private JwtAuthFilter jwtAuthFilter;

        @Bean
        public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
                http
                                .csrf(csrf -> csrf.disable())
                                .authorizeHttpRequests(auth -> auth
                                                .requestMatchers(
                                                                "/api/users/register",
                                                                "/api/users/login",
                                                                "/api/users/verify-email",
                                                                "/api/users/resend-code",
                                                                "/api/users/google-login",
                                                                "/api/users/google-auth",
                                                                "/api/users/facebook-auth",
                                                                "/api/users/reset-password/request",
                                                                "/api/users/reset-password/confirm",
                                                                "/api/users/reset-password/page",
                                                                "/inspirations/**",
                                                                "/reset-password",
                                                                "/api/partner/**",
                                                                "/api/images/**",
                                                                "/uploads/**",
                                                                "/avatars/**",
                                                                "/recaptcha.html")
                                                .permitAll()
                                                .anyRequest().authenticated())

                                .formLogin(form -> form.disable())
                                .httpBasic(basic -> basic.disable());

                http.addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

                return http.build();
        }
}