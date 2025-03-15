package com.doublez.backend.dto;

import java.util.ArrayList;
import java.util.List;

public class UserDetailsDTO {
	
	private String username;
	private List<String> roles;
	private String password;
	
	public UserDetailsDTO() {
		super();
	}

	public UserDetailsDTO(String username, List<String> roles, String password) {
		this.username = username;
		this.roles = roles != null ? roles : new ArrayList<>();
		this.password = password;
	}
	
	public UserDetailsDTO(String username, List<String> roles) {
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

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

}
