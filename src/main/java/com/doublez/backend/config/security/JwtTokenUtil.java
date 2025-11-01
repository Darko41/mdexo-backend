package com.doublez.backend.config.security;

import java.util.Date;
import java.util.List;

import javax.crypto.SecretKey;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

@Component
public class JwtTokenUtil {
	
	private final String secretKey;
	private final long expirationTime;
	
	public JwtTokenUtil(
            @Value("${jwt.secret}") String secretKey,
            @Value("${jwt.expiration:86400000}") long expirationTime) {
        
        if (secretKey == null || secretKey.trim().isEmpty()) {
            throw new IllegalStateException("JWT secret key is not configured. Set JWT_SECRET environment variable.");
        }
        
        this.secretKey = secretKey;
        this.expirationTime = expirationTime;
    }
	
	// Generate JWT Token - UPDATED to include userId
	public String generateToken(String email, Long userId, List<String> roles) { // ← ADD userId parameter
		SecretKey key = Keys.hmacShaKeyFor(this.secretKey.getBytes());
		return Jwts.builder()
				.claim("sub", email)
				.claim("userId", userId) // ← ADD THIS LINE
				.claim("roles", roles)
				.claim("iat", new Date())
				.claim("exp", new Date(System.currentTimeMillis() + this.expirationTime))
				.signWith(key)
				.compact();
	}
	
	public Claims extractClaims(String token) {
		SecretKey key = Keys.hmacShaKeyFor(this.secretKey.getBytes());
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
	
	// Extract User ID from Token - NEW METHOD
	public Long extractUserId(String token) {
		return extractClaims(token).get("userId", Long.class);
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