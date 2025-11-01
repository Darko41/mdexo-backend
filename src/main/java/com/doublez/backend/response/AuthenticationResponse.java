package com.doublez.backend.response;

import java.util.List;

public class AuthenticationResponse {
	
	private String token;
	private List<String> roles;
	private Long userId; // ✅ NEW: Add user ID field
	
	public AuthenticationResponse(String token, List<String> roles, Long userId) {
		this.token = token;
		this.roles = roles;
		this.userId = userId;
	}

	public String getToken() {
		return token;
	}

	public void setToken(String token) {
		this.token = token;
	}

	public List<String> getRoles() {
		return roles;
	}

	public void setRoles(List<String> roles) {
		this.roles = roles;
	}

	public Long getUserId() {
		return userId;
	}

	public void setUserId(Long userId) {
		this.userId = userId;
	}
}