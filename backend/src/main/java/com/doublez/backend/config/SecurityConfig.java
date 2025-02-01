package com.doublez.backend.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import static org.springframework.security.config.Customizer.withDefaults;

@Configuration
@EnableWebSecurity
public class SecurityConfig {
	
	@Bean
	PasswordEncoder passwordEncoder() {
		System.out.println("BCryptPasswordEncoder Bean is being created");
		return new BCryptPasswordEncoder();
	}
	
	@Bean
	SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
		http
			.authorizeHttpRequests((authz) -> authz
					// Define public URLs (no authentication required)
					.requestMatchers("/api/register").permitAll()
					// Define URLs that require ADMIN role
					//.requestMatchers("/admin/**").hasRole("ADMIN")
					// Any other request must be authenticated
					.anyRequest().authenticated()
					)
					.httpBasic(withDefaults())
					.csrf(csrf -> csrf.disable());
		
		return http.build();
	}
	
	

}
