package com.doublez.backend.config.security;

import java.io.IOException;

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

	public JwtAuthenticationFilter(JwtTokenUtil jwtTokenUtil, UserDetailsService userDetailsService) {
		this.jwtTokenUtil = jwtTokenUtil;
		this.userDetailsService = userDetailsService;
	}

	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
			throws ServletException, IOException {
		
		// Extract token from request header
		String token = getJwtFromRequest(request);
		
		if (token == null) {
			logger.warn("No token found in request headers.");
		} else {
			logger.info("JWT Token found in request headers.");
		}
		
		if (token != null) {
			// Extract username from token
			String username = extractUsernameFromToken(token);
			logger.info("Extracted username from token: {}", username);

			if (jwtTokenUtil.validateToken(token, username)) {
				logger.info("Token validated successfully for user: {}", username);

				// Fetch user details without password
				UserDetails userDetails = userDetailsService.loadUserByUsername(username);

				if (userDetails != null) {
					logger.info("User found and details loaded for user: {}", username);

					// Create an authentication object
					UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
							userDetails, null, userDetails.getAuthorities());
					logger.debug("Authentication ceontext after setting: {}", authentication);

					// Set details
					authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

					// Set authentication in the SecurityContext
					SecurityContextHolder.getContext().setAuthentication(authentication);
					logger.info("Authentication successfully set for user: {}", username);
				} else {
					logger.warn("User details not found for username: {}", username);
				}
			} else {
				logger.warn("Token validation failed for user: {}", username);
			}
			
			if (!jwtTokenUtil.validateToken(token, username)) {
			    logger.warn("Invalid token or token expired for user: {}", username);
			}
		}
		
		// Continue the filter chain
		filterChain.doFilter(request, response);
	}
	
	// Helper method to extract username from the token
	private String extractUsernameFromToken(String token) {
		return jwtTokenUtil.extractUsername(token);
	}

	// Helper method to extract the JWT token from the request's Authorization header
	private String getJwtFromRequest(HttpServletRequest request) {
		String bearerToken = request.getHeader("Authorization");
		logger.debug("Authorization header received: {}", bearerToken);
		if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
			return bearerToken.substring(7);
		}
		return null;
	}

}
