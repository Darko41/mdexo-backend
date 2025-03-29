package com.doublez.backend.config.security;

import java.util.Date;

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
	public String generateToken(String email) {
		SecretKey key = Keys.hmacShaKeyFor(SECRET_KEY.getBytes());
		return Jwts.builder()
				.claim("sub", email)
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
	
	// Extract Username from Token
	public String extractUsername(String token) {
		return extractClaims(token).getSubject();
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
	public boolean validateToken(String token, String username) {
		return (username.equals(extractUsername(token)) && !isTokenExpired(token));
	}

}
