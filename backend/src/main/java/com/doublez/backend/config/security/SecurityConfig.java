package com.doublez.backend.config.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import com.doublez.backend.service.CustomUserDetailsService;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Configuration
@EnableWebSecurity
public class SecurityConfig {
	
	private final JwtTokenUtil jwtTokenUtil;
	private final JwtAuthenticationFilter jwtAuthenticationFilter;
	private final CustomUserDetailsService userDetailsService;
	
	private static final Logger logger = LoggerFactory.getLogger(SecurityConfig.class);
	
	public SecurityConfig(JwtTokenUtil jwtTokenUtil, JwtAuthenticationFilter jwtAuthenticationFilter, CustomUserDetailsService userDetailsService) {
        this.jwtTokenUtil = jwtTokenUtil;
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
        this.userDetailsService = userDetailsService;
    }

	@Bean
	PasswordEncoder passwordEncoder() {
		return new BCryptPasswordEncoder();
	}
	
	@Bean
    AuthenticationManager authenticationManager(HttpSecurity http) throws Exception {
		return http.getSharedObject(AuthenticationManagerBuilder.class).build();
	}
	
	@Bean
	SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
		http
			.authorizeHttpRequests((authz) -> authz
					// Define public URLs (no authentication required)
					.requestMatchers(
							"/swagger-ui/**",
							"/v3/api-docs/**",
							"/swagger-resources/**",
							"/webjars/**"
							//"/api/authenticate",
							//"/api/real-estates/**",
							//"/api/email/send-email"
							).permitAll()
					
					// Define URLs that require authentication
					.requestMatchers("/api/users/**").authenticated()
					
					// Define URLs that require ADMIN role
					.requestMatchers("/api/admin/**").hasRole("ADMIN")
					
					// Any other request must be authenticated
					.anyRequest().authenticated()
					)
					.addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
					.csrf(csrf -> csrf.disable());
		
		logger.debug("Security filter chain configured");
		
		return http.build();
	}

}
