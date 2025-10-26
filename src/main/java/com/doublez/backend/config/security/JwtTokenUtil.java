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
            @Value("${jwt.secret}") String secretKey,  // No fallback!
            @Value("${jwt.expiration:86400000}") long expirationTime) {
        
        if (secretKey == null || secretKey.trim().isEmpty()) {
            throw new IllegalStateException("JWT secret key is not configured. Set JWT_SECRET environment variable.");
        }
        
        this.secretKey = secretKey;
        this.expirationTime = expirationTime;
        
    }
	
	// Generate JWT Token
	public String generateToken(String email, List<String> roles) {
		SecretKey key = Keys.hmacShaKeyFor(this.secretKey.getBytes());
		return Jwts.builder()
				.claim("sub", email)
				.claim("roles", roles)
				.claim("iat", new Date())	// Issued at time
				.claim("exp", new Date(System.currentTimeMillis() + this.expirationTime))	// Expiration time
				.signWith(key)	// Sign the token with the secret key
				.compact();	// Return the compact token
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
