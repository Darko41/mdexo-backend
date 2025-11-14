package com.doublez.backend.config.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
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
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;
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
    @Order(1) // Higher priority - processes API requests first
    SecurityFilterChain apiFilterChain(HttpSecurity http) throws Exception {
        http
            .securityMatcher("/api/**", "/public/**") // ONLY match API and public paths
            .authorizeHttpRequests((authz) -> authz
                // Public static resources - public URLs (no authentication required)
                .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                .requestMatchers(
                    "/api/debug/**"           // EmailDebugController
                ).permitAll()
                
                // Public API endpoints (EXPLICITLY LIST ALL PUBLIC API ENDPOINTS)
                .requestMatchers(
                    "/api/users/register",
                    "/api/agencies",
                    "/api/auth/authenticate",
                    "/api/auth/me"
                ).permitAll()
                
                // PUBLIC VERIFICATION ENDPOINTS
                .requestMatchers(HttpMethod.GET, "/api/verification/public/user/**").permitAll()
                
                // Public real estate API endpoints (search and get by ID)
                .requestMatchers(HttpMethod.GET, "/api/real-estates/search").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/real-estates/features").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/real-estates/{propertyId}").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/real-estates/**").permitAll()
                
                // PUBLIC USAGE TRACKING ENDPOINTS
                .requestMatchers(HttpMethod.GET, "/api/usage/**").authenticated() // Users can check their own usage
                .requestMatchers(HttpMethod.GET, "/api/usage-tracking/**").authenticated() // Users can check their own usage
                
                // Protected real estate endpoints (JWT authentication)
                .requestMatchers(HttpMethod.POST, "/api/real-estates/**").authenticated()
                .requestMatchers(HttpMethod.PUT, "/api/real-estates/**").authenticated() 
                .requestMatchers(HttpMethod.DELETE, "/api/real-estates/**").authenticated()
                
                // VERIFICATION ENDPOINTS - USER ROLE
                .requestMatchers(HttpMethod.POST, "/api/verification/submit").hasRole("USER")
                .requestMatchers(HttpMethod.GET, "/api/verification/my-status").hasRole("USER")
                .requestMatchers(HttpMethod.GET, "/api/verification/my-history").hasRole("USER")
                .requestMatchers(HttpMethod.GET, "/api/verification/can-apply/**").hasRole("USER")
                
                // VERIFICATION ENDPOINTS - ADMIN ROLE
                .requestMatchers(HttpMethod.GET, "/api/verification/admin/**").hasRole("ADMIN")
                .requestMatchers(HttpMethod.PUT, "/api/verification/admin/**").hasRole("ADMIN")
                
                // INVESTOR ENDPOINTS
                .requestMatchers(HttpMethod.POST, "/api/investor/apply").hasRole("USER")
                .requestMatchers(HttpMethod.PUT, "/api/investor/profile").hasRole("INVESTOR")
                .requestMatchers(HttpMethod.GET, "/api/investor/profile").hasRole("INVESTOR")
                
                // USER PROMOTION ENDPOINTS
                .requestMatchers(HttpMethod.POST, "/api/users/request-agent-promotion").hasRole("USER")
                .requestMatchers(HttpMethod.POST, "/api/users/request-investor-promotion").hasRole("USER")
                
                // AGENCY ENDPOINTS - AGENT ROLE
                .requestMatchers(HttpMethod.POST, "/api/agencies/{agencyId}/apply").hasRole("AGENT")
                .requestMatchers(HttpMethod.GET, "/api/agencies/my-memberships").hasRole("AGENT")
                .requestMatchers(HttpMethod.DELETE, "/api/agencies/memberships/{membershipId}").hasRole("AGENT")
                
                // AGENCY ENDPOINTS - AGENCY_ADMIN ROLE
                .requestMatchers(HttpMethod.POST, "/api/agencies/memberships/{membershipId}/approve").hasRole("AGENCY_ADMIN")
                .requestMatchers(HttpMethod.GET, "/api/agencies/{agencyId}/pending-memberships").hasRole("AGENCY_ADMIN")
                
                // AGENCY ENDPOINTS - ADMIN ROLE (Promotion endpoints)
                .requestMatchers(HttpMethod.POST, "/api/agencies/promote/agent").hasRole("ADMIN")
                .requestMatchers(HttpMethod.POST, "/api/agencies/promote/agency-admin").hasRole("ADMIN")
                .requestMatchers(HttpMethod.POST, "/api/agencies/demote/agent").hasRole("ADMIN")
                
                // Admin API endpoints (JWT + ROLE_ADMIN)
                .requestMatchers("/api/admin/**").hasRole("ADMIN")
                
                // General API protection (catch-all for other API endpoints)
                .requestMatchers("/api/**").authenticated()
            )
            // DISABLE form login for API paths
            .formLogin(form -> form.disable())
            .httpBasic(basic -> basic.disable())
            .sessionManagement(sess -> sess
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS) // STATELESS for API
            )
            // JWT filter for API requests
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))
            .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
            .csrf(csrf -> csrf.disable()) // Disable CSRF for API
            .exceptionHandling(ex -> ex
                .authenticationEntryPoint((request, response, authException) -> {
                    // API calls always get 401 JSON
                    response.setContentType("application/json");
                    response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                    response.getWriter().write("{\"error\":\"Unauthorized\",\"message\":\"Authentication required\"}");
                })
            );
        
        return http.build();
    }

    // FILTER CHAIN 2: TEMPLATE ENDPOINTS (Session-based)
    @Bean
    @Order(2) // Lower priority - handles everything else
    SecurityFilterChain templateFilterChain(HttpSecurity http) throws Exception {
        http
            .securityMatcher("/**") // Match everything else (templates, static resources, etc.)
            .authorizeHttpRequests((authz) -> authz
                // Public static resources
                .requestMatchers(
                    "/",
                    "/dist/**",
                    "/pages/**", 
                    "/plugins/**",
                    "/css/**",
                    "/js/**",
                    "/images/**",
                    "/static/**",
                    "/assets/**",
                    "/favicon.ico",
                    "/manifest.json",
                    "/robots.txt",
                    "/v3/api-docs/**",
                    "/swagger-ui/**", 
                    "/swagger-resources/**",
                    "/webjars/**"
                ).permitAll()
                
                // Public auth endpoints
                .requestMatchers("/auth/**").permitAll()
                
                // Admin dashboard endpoints (Session + ROLE_ADMIN)  
                .requestMatchers("/admin/**").hasRole("ADMIN")
                
                // Any other request must be authenticated
                .anyRequest().authenticated()
            )
            // Form login for templates
            .formLogin(form -> form
                .loginPage("/auth/login")
                .loginProcessingUrl("/auth/login") 
                .defaultSuccessUrl("/auth/success", true)
                .failureUrl("/auth/login?error=true")
                .permitAll()
            )
            .logout(logout -> logout
                .logoutUrl("/auth/logout")
                .logoutSuccessUrl("/auth/login?logout=true")
                .invalidateHttpSession(true)
                .deleteCookies("JSESSIONID")
                .permitAll()
            )
            .sessionManagement(sess -> sess
                .sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED) // SESSIONS for templates
            )
            .csrf(csrf -> csrf
                .csrfTokenRepository(CookieCsrfTokenRepository.withHttpOnlyFalse())
            )
            .exceptionHandling(ex -> ex
                .authenticationEntryPoint((request, response, authException) -> {
                    // Web pages get redirect to login (Session)
                    String requestUri = request.getRequestURI();
                    if (requestUri.startsWith("/admin/")) {
                        response.sendRedirect("/auth/login?admin=true");
                    } else {
                        response.sendRedirect("/auth/login");
                    }
                })
            );
        
        return http.build();
    }
    
    @Bean
    CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOriginPatterns(Arrays.asList(
            "http://localhost:5173", 
            "https://dwellia.rs",
            "https://www.dwellia.rs",
            "https://api.dwellia.rs",
            "http://localhost:8080"
        ));
        
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH"));
        configuration.setAllowedHeaders(Arrays.asList("*"));
        configuration.setAllowCredentials(true);
        configuration.setExposedHeaders(Arrays.asList("Set-Cookie", "Authorization"));
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
            
            // 🆕 UPDATED ROLE HIERARCHY WITH NEW ROLES
            if (authentication.get().getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"))) {
                return new AuthorizationDecision(true);
            }
            
            if (authentication.get().getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_AGENCY_ADMIN"))) {
                return new AuthorizationDecision(true);
            }
            
            if (authentication.get().getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_AGENT"))) {
                return new AuthorizationDecision(true);
            }
            
            if (authentication.get().getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_INVESTOR"))) {
                return new AuthorizationDecision(true);
            }
            
            return new AuthorizationDecision(
                authentication.get().getAuthorities().stream()
                    .anyMatch(a -> a.getAuthority().equals("ROLE_USER"))
            );
        };
    }
}
