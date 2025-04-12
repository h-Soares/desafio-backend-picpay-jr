package com.soaresdev.picpaytestjr.configs;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfig {
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http.
                authorizeHttpRequests(auth ->
                        auth.anyRequest().permitAll()).
                csrf(AbstractHttpConfigurer::disable).
                formLogin(AbstractHttpConfigurer::disable).
                httpBasic(AbstractHttpConfigurer::disable);
        return http.build();
    }
}