package com.doublez.backend.dto;

import java.util.List;

public class UserDetailsDTO {
	
	private String username;
	private List<String> roles;
	
	public UserDetailsDTO(String username, List<String> roles) {
		super();
		this.username = username;
		this.roles = roles;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public List<String> getRoles() {
		return roles;
	}

	public void setRoles(List<String> roles) {
		this.roles = roles;
	}

}
