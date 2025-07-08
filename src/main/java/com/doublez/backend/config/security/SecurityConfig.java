package com.doublez.backend.config.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authorization.AuthorizationDecision;
import org.springframework.security.authorization.AuthorizationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.intercept.RequestAuthorizationContext;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import com.doublez.backend.service.user.CustomUserDetailsService;

import java.util.Arrays;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {
	
	private final JwtTokenUtil jwtTokenUtil;
	private final JwtAuthenticationFilter jwtAuthenticationFilter;
	private final CustomUserDetailsService userDetailsService;
	
	private static final Logger logger = LoggerFactory.getLogger(SecurityConfig.class);
	
	public SecurityConfig(JwtTokenUtil jwtTokenUtil,
			JwtAuthenticationFilter jwtAuthenticationFilter,
			CustomUserDetailsService userDetailsService) {
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
					.requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
					.requestMatchers(
//							"/api/authenticate/",
//							"/api/real-estates/**",
//							"/api/**",
							"/v3/api-docs/**",
			                "/swagger-ui/**",
			                "/swagger-resources/**",
			                "/webjars/**"
//			                "/api/users/**",
//			                "/api/agents/**"
//							"/api/email/send-email",
//							"/api/db-status",
//							"/admin/**",
//							"/dist/**",
//							"/pages/**",
//							"/plugins/**",
//							"/static/**",
//							"/admin/**",
//							"/api/users/**",
//							"/api/real-estates/**"
							).permitAll()
					
					// Define URLs that require specific role				
					.requestMatchers("/api/admin/**", "admin/**").hasRole("ADMIN")							// TODO Resolve cors problems, why this works on Postman but not deployed
					
					// Define URLs that require authentication
//					.requestMatchers("/api/**").authenticated()
					
					// Any other request must be authenticated
					.anyRequest().authenticated()
//					.anyRequest().permitAll()
					)
					.sessionManagement(sess -> sess.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
			
					.addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
					.cors(cors -> cors.configurationSource(corsConfigurationSource()))
					.csrf(csrf -> csrf.disable());
		
		logger.debug("Security filter chain configured");
		
		return http.build();
	}
	
	@Bean
	CorsConfigurationSource corsConfigurationSource() {
		CorsConfiguration configuration = new CorsConfiguration();
		configuration.setAllowedOrigins(Arrays.asList(
		        "http://localhost:5173", 
		        "https://mdexo-frontend.onrender.com"
		    ));
		configuration.setAllowedMethods(Arrays.asList("*"));
		configuration.setAllowedHeaders(Arrays.asList("*"));
		configuration.setAllowCredentials(true);
		configuration.setMaxAge(3600L);
		
		UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
		source.registerCorsConfiguration("/**", configuration);
		return source;
	}
	
	@Bean
    AuthorizationManager<RequestAuthorizationContext> authorizationManager() {
        return (authentication, context) -> {
            if (authentication.get() == null) {
                return new AuthorizationDecision(false);
            }
            
            // Check roles with hierarchy logic
            if (authentication.get().getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"))) {
                return new AuthorizationDecision(true);
            }
            
            if (authentication.get().getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_AGENT"))) {
                return new AuthorizationDecision(true);
            }
            
            return new AuthorizationDecision(
                authentication.get().getAuthorities().stream()
                    .anyMatch(a -> a.getAuthority().equals("ROLE_USER"))
            );
        };
    }

}
