package com.doublez.backend.config.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authorization.AuthorizationDecision;
import org.springframework.security.authorization.AuthorizationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
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

import com.doublez.backend.service.user.CustomUserDetailsService;

import jakarta.servlet.http.HttpServletResponse;

import java.util.Arrays;

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
	AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
	    return config.getAuthenticationManager();
	}
	
	@Bean
	SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
	    http
	        .authorizeHttpRequests((authz) -> authz
	            // Define public URLs (no authentication required)
	            .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
	            .requestMatchers(
	                "/",
	                "/static/**",
	                "/assets/**", 
	                "/css/**",
	                "/js/**",
	                "/images/**",
	                "/favicon.ico",
	                "/manifest.json", 
	                "/robots.txt",
	                "/dist/**",           
	                "/plugins/**",          
	                "/pages/**",
	                "/v3/api-docs/**",
	                "/swagger-ui/**",
	                "/swagger-resources/**",
	                "/webjars/**"
	            ).permitAll()
	            
	            // Public API endpoints
	            .requestMatchers(
	                "/api/users/register",
	                "/api/auth/authenticate"
	            ).permitAll()
	            
	            // Public real estate endpoints (search and get by ID)
	            .requestMatchers(HttpMethod.GET, "/api/real-estates/search").permitAll()
	            .requestMatchers(HttpMethod.GET, "/api/real-estates/*").permitAll()
	            
	            // Public auth endpoints for admin login
	            .requestMatchers(
	                "/auth/**",
	                "/admin/login", 
	                "/admin/access"
	            ).permitAll()
	            
	            // PROTECTED real estate endpoints (JWT authentication)
	            .requestMatchers(HttpMethod.POST, "/api/real-estates").authenticated()
	            .requestMatchers(HttpMethod.POST, "/api/real-estates/with-images").authenticated()
	            .requestMatchers(HttpMethod.PUT, "/api/real-estates/**").authenticated()
	            .requestMatchers(HttpMethod.DELETE, "/api/real-estates/**").authenticated()
	            
	            // Admin API endpoints (JWT + ROLE_ADMIN)
	            .requestMatchers("/api/admin/**").hasRole("ADMIN")
	            
	            // General API protection (JWT)
	            .requestMatchers("/api/**").authenticated()
	            
	            // Admin dashboard endpoints (Session + ROLE_ADMIN)  
	            .requestMatchers("/admin/**").hasRole("ADMIN")
	            
	            // Any other request must be authenticated
	            .anyRequest().authenticated()
	        )
	        // Form login ONLY for admin pages (session-based)
	        .formLogin(form -> form
	            .loginPage("/auth/login")
	            .loginProcessingUrl("/auth/login") 
	            .defaultSuccessUrl("/auth/success")
	            .failureUrl("/auth/login?error")
	            .permitAll()
	        )
	        // Logout ONLY for admin pages (session-based)
	        .logout(logout -> logout
	            .logoutUrl("/auth/logout")
	            .logoutSuccessUrl("/auth/login")
	            .invalidateHttpSession(true)
	            .deleteCookies("JSESSIONID")
	        )
	        .sessionManagement(sess -> sess
	            .sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED)
	        )
	        // JWT filter for API requests
	        .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
	        .cors(cors -> cors.configurationSource(corsConfigurationSource()))
	        .csrf(csrf -> csrf.disable())
	        .exceptionHandling(ex -> ex
	            .authenticationEntryPoint((request, response, authException) -> {
	                String requestUri = request.getRequestURI();
	                if (requestUri.startsWith("/api/")) {
	                    // API calls get 401 (JWT)
	                    response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Unauthorized");
	                } else {
	                    // Web pages get redirect to login (Session)
	                    response.sendRedirect("/auth/login");
	                }
	            })
	        );
	    
	    return http.build();
	}
	
	@Bean
	CorsConfigurationSource corsConfigurationSource() {
		CorsConfiguration configuration = new CorsConfiguration();
		configuration.setAllowedOrigins(Arrays.asList(
		        "http://localhost:5173", 
		        "https://mdexo-frontend.onrender.com",
		        "http://localhost:8080"
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
