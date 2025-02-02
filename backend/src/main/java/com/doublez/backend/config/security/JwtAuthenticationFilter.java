package com.doublez.backend.config.security;

import java.io.IOException;

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

	public JwtAuthenticationFilter(JwtTokenUtil jwtTokenUtil, UserDetailsService userDetailsService) {
		this.jwtTokenUtil = jwtTokenUtil;
		this.userDetailsService = userDetailsService;
	}

	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
			throws ServletException, IOException {

		String token = getJwtFromRequest(request);
		
		if (token != null && jwtTokenUtil.validateToken(token, extractUsernameFromToken(token))) {
			// Fetch user details (without using password)
			UserDetails userDetails = userDetailsService.loadUserByUsername(jwtTokenUtil.extractUsername(token));
			
			if (userDetails != null) {
				// Creating an authentication object
				UsernamePasswordAuthenticationToken authentication = 
						new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities()
						);
				// Set details (optional)
				authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
				
				// Set authentication in the SecurityContext
				SecurityContextHolder.getContext().setAuthentication(authentication);
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
		if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
			return bearerToken.substring(7);
		}
		return null;
	}

}
