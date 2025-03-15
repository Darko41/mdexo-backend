package com.doublez.backend.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

@Configuration
public class CorsConfig {

	@Bean
	CorsConfigurationSource corsConfigurationSource() {
		CorsConfiguration corsConfiguration = new CorsConfiguration();
		corsConfiguration.addAllowedOrigin("http://localhost:5173"); // Frontend URL
		corsConfiguration.addAllowedMethod("*"); // Allow all methods
		corsConfiguration.addAllowedHeader("*"); // Allow all headers
		
		// Create a source for CORS configuration
		UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
		source.registerCorsConfiguration("/api/**", corsConfiguration); // Apply to /api/** endpoints
		
		return source;
	}
	
}
