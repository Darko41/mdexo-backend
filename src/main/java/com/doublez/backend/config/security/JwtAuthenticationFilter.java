package com.doublez.backend.config.security;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.filter.OncePerRequestFilter;

import io.jsonwebtoken.ExpiredJwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtTokenUtil jwtTokenUtil;
    private final UserDetailsService userDetailsService;
    private static final Logger logger = LoggerFactory.getLogger(JwtAuthenticationFilter.class);

    // KEEP YOUR EXISTING EXCLUDED_PATHS
    private static final List<String> EXCLUDED_PATHS = Arrays.asList(
        // Auth endpoints
        "/api/auth/authenticate",
        
        // User registration
        "/api/users/register",
        "/api/users/check-email/*",
        
        // Public real estate browsing
        "/api/real-estates/search",
        "/api/real-estates/features",
        "/api/real-estates/*",
        "/api/real-estates/**/similar",
        
        // Email debug endpoint
        "/api/debug/**",
        
        // Public agency browsing ONLY
        "/api/agencies",           // GET all agencies
        "/api/agencies/*",         // GET specific agency
        
        // Static resources
        "/v3/api-docs/**",
        "/swagger-ui/**",
        "/swagger-resources/**",
        "/webjars/**"
    );

    private final AntPathMatcher pathMatcher = new AntPathMatcher();

    public JwtAuthenticationFilter(JwtTokenUtil jwtTokenUtil, UserDetailsService userDetailsService) {
        this.jwtTokenUtil = jwtTokenUtil;
        this.userDetailsService = userDetailsService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        final String requestURI = request.getRequestURI();
        final String method = request.getMethod();
        
        logger.debug("🛡️ JWT Filter processing: {} {}", method, requestURI);

        // COMPLETELY skip OPTIONS requests
        if (HttpMethod.OPTIONS.matches(method)) {
            logger.debug("🔄 Skipping JWT filter for OPTIONS request: {}", requestURI);
            filterChain.doFilter(request, response);
            return;
        }

        // Skip JWT filter for excluded paths
        if (shouldSkipAuthentication(request)) {
            logger.debug("✅ Skipping JWT filter for public endpoint: {}", requestURI);
            filterChain.doFilter(request, response);
            return;
        }

        // JWT validation for protected API endpoints
        try {
            String token = getJwtFromRequest(request);

            if (token == null) {
                logger.warn("No JWT token found in request to protected endpoint: {}", requestURI);
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                response.getWriter().write("{\"error\":\"No token provided\",\"message\":\"Authentication required\"}");
                return;
            }

            String username = jwtTokenUtil.extractEmail(token);
            logger.debug("Extracted username from token: {}", username);

            if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                if (jwtTokenUtil.validateToken(token, username)) {
                    UserDetails userDetails = userDetailsService.loadUserByUsername(username);

                    if (userDetails != null) {
                        Long userId = jwtTokenUtil.extractUserId(token);
                        
                        UsernamePasswordAuthenticationToken authentication =
                                new UsernamePasswordAuthenticationToken(
                                        userDetails,
                                        null,
                                        userDetails.getAuthorities());
                        authentication.setDetails(
                                new WebAuthenticationDetailsSource().buildDetails(request));

                        SecurityContext context = SecurityContextHolder.createEmptyContext();
                        context.setAuthentication(authentication);
                        
                        request.setAttribute("userId", userId);
                        
                        SecurityContextHolder.setContext(context);
                        logger.info("Authenticated user: {} (ID: {})", username, userId);
                    }
                } else {
                    logger.warn("Invalid JWT token for request: {}", requestURI);
                    response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                    response.getWriter().write("{\"error\":\"Invalid token\",\"message\":\"Token validation failed\"}");
                    return;
                }
            }
        } catch (ExpiredJwtException ex) {
            logger.warn("JWT token expired for request {}: {}", requestURI, ex.getMessage());
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.getWriter().write("{\"error\":\"Token expired\",\"message\":\"Please authenticate again\"}");
            return;
        } catch (Exception ex) {
            logger.error("Error processing JWT token for request {}: {}", requestURI, ex.getMessage());
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.getWriter().write("{\"error\":\"Invalid token\",\"message\":\"Authentication failed\"}");
            return;
        }

        filterChain.doFilter(request, response);
    }

    private boolean shouldSkipAuthentication(HttpServletRequest request) {
        String requestURI = request.getRequestURI();
        
        if (EXCLUDED_PATHS.stream().anyMatch(pattern -> pathMatcher.match(pattern, requestURI))) {
            return true;
        }
        
        // Skip if not an API request (let template filter handle it)
        if (!requestURI.startsWith("/api/")) {
            return true;
        }
        
        // Skip excluded API paths
        return EXCLUDED_PATHS.stream().anyMatch(pattern -> pathMatcher.match(pattern, requestURI));
    }

    private String getJwtFromRequest(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }

        // Check for token in cookies if not found in header
        if (request.getCookies() != null) {
            for (Cookie cookie : request.getCookies()) {
                if ("token".equals(cookie.getName())) {
                    return cookie.getValue();
                }
            }
        }

        return null;
    }
}