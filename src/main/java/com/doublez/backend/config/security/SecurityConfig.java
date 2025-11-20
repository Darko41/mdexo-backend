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
    SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .authorizeHttpRequests((authz) -> authz
                // =============================================
                // PUBLIC API ENDPOINTS (No authentication required)
                // =============================================
                
                // Public static resources - public URLs (no authentication required)
                .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                
                // =============================================
                // TEMPORARY DEBUG ENDPOINTS
                // =============================================
                
                // Email Debug Controller
                .requestMatchers("/api/debug/**").permitAll()
                // CDN test endpoint
                .requestMatchers("/api/cdn-test").permitAll()
                
                // =============================================
                // PUBLIC AUTH & REGISTRATION ENDPOINTS
                // =============================================
                .requestMatchers(
                    "/api/users/register",
                    "/api/auth/authenticate",
                    "/api/auth/me",
                    "/api/auth/validate",
                    "/api/auth/refresh"
                ).permitAll()
                
                // =============================================
                // PUBLIC REAL ESTATE ENDPOINTS
                // =============================================
                .requestMatchers(HttpMethod.GET, "/api/real-estates/search").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/real-estates/features").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/real-estates/{propertyId}").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/real-estates/featured/active").permitAll()
                
                // =============================================
                // PUBLIC FEATURED LISTINGS ENDPOINTS
                // =============================================
                .requestMatchers(HttpMethod.GET, "/api/featured/active").permitAll()
                
                // =============================================
                // PUBLIC AGENCY ENDPOINTS
                // =============================================
                .requestMatchers(HttpMethod.GET, "/api/agencies").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/agencies/{agencyId}").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/agencies/search").permitAll()
                
                // =============================================
                // PUBLIC TIER & BENEFITS ENDPOINTS
                // =============================================
                .requestMatchers(HttpMethod.GET, "/api/tiers/benefits").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/tiers/benefits/individual").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/tiers/benefits/agency").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/tiers/benefits/investor").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/tiers/benefits/{tier}").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/tiers/compare").permitAll()
                
                // =============================================
                // PUBLIC VERIFICATION ENDPOINTS
                // =============================================
                .requestMatchers(HttpMethod.GET, "/api/verification/public/user/**").permitAll()
                
                // =============================================
                // AUTHENTICATED USER ENDPOINTS (JWT authentication)
                // =============================================
                
                // USAGE TRACKING ENDPOINTS
                .requestMatchers(HttpMethod.GET, "/api/usage/**").authenticated()
                .requestMatchers(HttpMethod.GET, "/api/usage-tracking/**").authenticated()
                
                // =============================================
                // REAL ESTATE MANAGEMENT ENDPOINTS
                // =============================================
                .requestMatchers(HttpMethod.POST, "/api/real-estates/**").authenticated()
                .requestMatchers(HttpMethod.PUT, "/api/real-estates/**").authenticated()
                .requestMatchers(HttpMethod.DELETE, "/api/real-estates/**").authenticated()
                .requestMatchers(HttpMethod.GET, "/api/real-estates/my-properties").authenticated()
                
                // =============================================
                // USER MANAGEMENT ENDPOINTS
                // =============================================
                .requestMatchers(HttpMethod.GET, "/api/users/me").hasRole("USER")
                .requestMatchers(HttpMethod.PUT, "/api/users/me").hasRole("USER")
                .requestMatchers(HttpMethod.DELETE, "/api/users/me").hasRole("USER")
                .requestMatchers(HttpMethod.GET, "/api/users/me/**").hasRole("USER")
                .requestMatchers(HttpMethod.POST, "/api/users/me/start-trial").hasRole("USER")
                
                // User endpoints with ID-based access control
                .requestMatchers(HttpMethod.GET, "/api/users/{id}").hasRole("USER")
                .requestMatchers(HttpMethod.PUT, "/api/users/{id}").hasRole("USER")
                .requestMatchers(HttpMethod.DELETE, "/api/users/{id}").hasRole("USER")
                
                // =============================================
                // TRIAL MANAGEMENT ENDPOINTS
                // =============================================
                .requestMatchers(HttpMethod.GET, "/api/trial/my-status").hasRole("USER")
                .requestMatchers(HttpMethod.GET, "/api/trial/my-progress").hasRole("USER")
                .requestMatchers(HttpMethod.GET, "/api/trial/can-start").hasRole("USER")
                .requestMatchers(HttpMethod.POST, "/api/trial/start").hasRole("USER")
                .requestMatchers(HttpMethod.GET, "/api/trial/agency-status").hasRole("AGENCY_ADMIN")
                
                // =============================================
                // TIER MANAGEMENT ENDPOINTS
                // =============================================
                .requestMatchers(HttpMethod.GET, "/api/tiers/my-tier").authenticated()
                
                // =============================================
                // INVESTOR ENDPOINTS
                // =============================================
                .requestMatchers("/api/investor/**").hasRole("INVESTOR")
                
                // =============================================
                // FEATURED LISTINGS MANAGEMENT ENDPOINTS
                // =============================================
                .requestMatchers(HttpMethod.POST, "/api/featured/{realEstateId}").authenticated()
                .requestMatchers(HttpMethod.DELETE, "/api/featured/{realEstateId}").authenticated()
                .requestMatchers(HttpMethod.GET, "/api/featured/can-feature/{realEstateId}").authenticated()
                .requestMatchers(HttpMethod.GET, "/api/featured/my-featured").authenticated()
                
                // =============================================
                // AGENCY MANAGEMENT ENDPOINTS
                // =============================================
                // Agency creation
                .requestMatchers(HttpMethod.POST, "/api/agencies").hasAnyRole("ADMIN", "AGENCY_ADMIN")
                
                // Agency admin endpoints
                .requestMatchers(HttpMethod.GET, "/api/agencies/my-agency").hasRole("AGENCY_ADMIN")
                .requestMatchers(HttpMethod.PUT, "/api/agencies/{agencyId}").hasAnyRole("AGENCY_ADMIN", "ADMIN")
                .requestMatchers(HttpMethod.GET, "/api/agencies/{agencyId}/properties").hasAnyRole("AGENCY_ADMIN", "ADMIN")
                .requestMatchers(HttpMethod.GET, "/api/agencies/{agencyId}/properties/paged").hasAnyRole("AGENCY_ADMIN", "ADMIN")
                .requestMatchers(HttpMethod.POST, "/api/agencies/{agencyId}/deactivate").hasAnyRole("AGENCY_ADMIN", "ADMIN")
                .requestMatchers(HttpMethod.POST, "/api/agencies/{agencyId}/activate").hasAnyRole("AGENCY_ADMIN", "ADMIN")
                .requestMatchers(HttpMethod.GET, "/api/agencies/{agencyId}/statistics").hasAnyRole("AGENCY_ADMIN", "ADMIN")
                
                // Agency membership endpoints
                .requestMatchers(HttpMethod.POST, "/api/agencies/{agencyId}/apply").hasRole("AGENT")
                .requestMatchers(HttpMethod.GET, "/api/agencies/my-memberships").hasRole("AGENT")
                .requestMatchers(HttpMethod.DELETE, "/api/agencies/memberships/{membershipId}").hasRole("AGENT")
                .requestMatchers(HttpMethod.POST, "/api/agencies/memberships/{membershipId}/approve").hasRole("AGENCY_ADMIN")
                .requestMatchers(HttpMethod.GET, "/api/agencies/{agencyId}/pending-memberships").hasRole("AGENCY_ADMIN")
                
                // =============================================
                // VERIFICATION ENDPOINTS
                // =============================================
                .requestMatchers(HttpMethod.POST, "/api/verification/submit").hasRole("USER")
                .requestMatchers(HttpMethod.GET, "/api/verification/my-status").hasRole("USER")
                .requestMatchers(HttpMethod.GET, "/api/verification/my-history").hasRole("USER")
                .requestMatchers(HttpMethod.GET, "/api/verification/can-apply/**").hasRole("USER")
                
                // Verification admin endpoints
                .requestMatchers(HttpMethod.GET, "/api/verification/admin/**").hasRole("ADMIN")
                .requestMatchers(HttpMethod.PUT, "/api/verification/admin/**").hasRole("ADMIN")
                
                // =============================================
                // USER PROMOTION ENDPOINTS
                // =============================================
                .requestMatchers(HttpMethod.POST, "/api/users/request-agent-promotion").hasRole("USER")
                .requestMatchers(HttpMethod.POST, "/api/users/request-investor-promotion").hasRole("USER")
                
                // Agency promotion endpoints (admin only)
                .requestMatchers(HttpMethod.POST, "/api/agencies/promote/agent").hasRole("ADMIN")
                .requestMatchers(HttpMethod.POST, "/api/agencies/promote/agency-admin").hasRole("ADMIN")
                .requestMatchers(HttpMethod.POST, "/api/agencies/demote/agent").hasRole("ADMIN")
                
                // =============================================
                // ADMIN API ENDPOINTS
                // =============================================
                .requestMatchers("/api/admin/**").hasRole("ADMIN")
                
                // Admin-specific user management
                .requestMatchers(HttpMethod.POST, "/api/users").hasRole("ADMIN")
                .requestMatchers(HttpMethod.GET, "/api/users").hasRole("ADMIN")
                .requestMatchers(HttpMethod.GET, "/api/users/statistics").hasRole("ADMIN")
                .requestMatchers(HttpMethod.GET, "/api/users/{id}/image-count").hasRole("ADMIN")
                
                // Admin trial management
                .requestMatchers(HttpMethod.POST, "/api/trial/{id}/extend-trial").hasRole("ADMIN")
                .requestMatchers(HttpMethod.POST, "/api/trial/{id}/expire-trial").hasRole("ADMIN")
                .requestMatchers(HttpMethod.GET, "/api/trial/statistics").hasRole("ADMIN")
                .requestMatchers(HttpMethod.GET, "/api/trial/expiring-soon").hasRole("ADMIN")
                
                // Admin featured listings
                .requestMatchers(HttpMethod.GET, "/api/featured/statistics").hasRole("ADMIN")
                
                // =============================================
                // GENERAL API PROTECTION (catch-all)
                // =============================================
                .requestMatchers("/api/**").authenticated()
                
                // =============================================
                // TEMPLATE ENDPOINTS (Session-based authentication)
                // =============================================
                
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
            // =============================================
            // AUTHENTICATION CONFIGURATION
            // =============================================
            
            // Form login for templates (Session-based)
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
                .sessionCreationPolicy(SessionCreationPolicy.ALWAYS) // Allow sessions for templates
            )
            // JWT filter for API requests
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))
            .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
            // CSRF configuration - disabled for API, enabled for templates
            .csrf(csrf -> csrf
                .csrfTokenRepository(CookieCsrfTokenRepository.withHttpOnlyFalse())
                .ignoringRequestMatchers(
                        "/api/**",                    // Disable CSRF for API endpoints
                        "/api/auth/authenticate")    // Specific auth endpoint
            )
            // =============================================
            // EXCEPTION HANDLING
            // =============================================
            .exceptionHandling(ex -> ex
                .authenticationEntryPoint((request, response, authException) -> {
                    String requestUri = request.getRequestURI();
                    
                    if (requestUri.startsWith("/api/")) {
                        // API calls always get 401 JSON
                        response.setContentType("application/json");
                        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                        response.getWriter().write("{\"error\":\"Unauthorized\",\"message\":\"Authentication required\"}");
                    } else {
                        // Web pages get redirect to login (Session)
                        if (requestUri.startsWith("/admin/")) {
                            response.sendRedirect("/auth/login?admin=true");
                        } else {
                            response.sendRedirect("/auth/login");
                        }
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