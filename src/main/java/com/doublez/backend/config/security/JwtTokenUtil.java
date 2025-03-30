package com.doublez.backend.config.security;

import java.util.Date;
import java.util.List;

import javax.crypto.SecretKey;

import org.springframework.stereotype.Component;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

@Component
public class JwtTokenUtil {
	
	private static final String SECRET_KEY = "your-secure-long-secret-key-here-that-is-at-least-256-bits-long";
	private static final long EXPIRATION_TIME = 86400000;
	
	// Generate JWT Token
	public String generateToken(String email, List<String> roles) {
		SecretKey key = Keys.hmacShaKeyFor(SECRET_KEY.getBytes());
		return Jwts.builder()
				.claim("sub", email)
				.claim("roles", roles)
				.claim("iat", new Date())	// Issued at time
				.claim("exp", new Date(System.currentTimeMillis() + EXPIRATION_TIME))	// Expiration time
				.signWith(key)	// Sign the token with the secret key
				.compact();	// Return the compact token
	}
	
	public Claims extractClaims(String token) {
		SecretKey key = Keys.hmacShaKeyFor(SECRET_KEY.getBytes());
		return Jwts.parser()
				.verifyWith(key)
				.build()
				.parseSignedClaims(token)
				.getPayload();
	}
	
	// Extract Email from Token
	public String extractEmail(String token) {
		return extractClaims(token).getSubject();
	}
	
	// Extract roles from the token
	public List<String> extractRoles(String token) {
		return (List<String>) extractClaims(token).get("roles");
	}
	
	// Check if Token is Expired
	public boolean isTokenExpired(String token) {
		return extractExpiration(token).before(new Date());
	}
	
	// Extract expiration date
	public Date extractExpiration(String token) {
		return extractClaims(token).getExpiration();
	}

	// Validate Token
	public boolean validateToken(String token, String email) {
		return (email.equals(extractEmail(token)) && !isTokenExpired(token));
	}

}
