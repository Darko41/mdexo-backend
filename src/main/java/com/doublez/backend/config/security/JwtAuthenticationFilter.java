package com.doublez.backend.config.security;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter{
	
	private final JwtTokenUtil jwtTokenUtil;
	private final UserDetailsService userDetailsService;
	
	private static final Logger logger = LoggerFactory.getLogger(JwtAuthenticationFilter.class);
	
	private static final List<String> EXCLUDED_PATHS = Arrays.asList(
	        "/api/authenticate"
	);

	public JwtAuthenticationFilter(JwtTokenUtil jwtTokenUtil, UserDetailsService userDetailsService) {
		this.jwtTokenUtil = jwtTokenUtil;
		this.userDetailsService = userDetailsService;
	}

	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
			throws ServletException, IOException {
		
		if (shouldSkip(request)) {
			filterChain.doFilter(request, response);
			return;
		}

		// Extract token from request header
		String token = getJwtFromRequest(request);

		if (token != null) {
			// Extract username from the token
			String username = extractUsernameFromToken(token);
			logger.info("Extracted username from token: {}", username);

			// Validate the token
			if (jwtTokenUtil.validateToken(token, username)) {
				logger.info("Token validated successfully for user: {}", username);

				// Fetch user details based on username (without password)
				UserDetails userDetails = userDetailsService.loadUserByUsername(username);

				if (userDetails != null) {
					logger.info("User found and details loaded for user: {}", username);

					// Create an authentication object
					UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
							userDetails, null, userDetails.getAuthorities());

					// Set details in the authentication object
					authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

					// Set the authentication object into the security context
					SecurityContextHolder.getContext().setAuthentication(authentication);
					logger.info("Authentication successfully set for user: {}", username);
				} else {
					logger.warn("User details not found for username: {}", username);
				}
			} else {
				// Log token validation failure and reject the request
				logger.warn("Invalid token or token expired for user: {}", username);
			}
		} else {
			logger.warn("No token found in request headers.");
		}

		// Continue the filter chain
		filterChain.doFilter(request, response);
	}

	// Helper method to check if the request URI should be skipped for JWT
	// validation
	private boolean shouldSkip(HttpServletRequest request) {
		String requestURI = request.getRequestURI();
		logger.debug("Checking if should skip path: {}", requestURI);

		return EXCLUDED_PATHS.stream().anyMatch(requestURI::matches);
	}

	// Helper method to extract username from the token
	private String extractUsernameFromToken(String token) {
		return jwtTokenUtil.extractUsername(token);
	}

	// Helper method to extract the JWT token from the request's Authorization
	// header
	private String getJwtFromRequest(HttpServletRequest request) {
		String bearerToken = request.getHeader("Authorization");
		logger.debug("Authorization header received: {}", bearerToken);
		if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
			return bearerToken.substring(7);
		}
		return null;
	}

}
