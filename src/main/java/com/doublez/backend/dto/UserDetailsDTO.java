package com.doublez.backend.dto;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;
public class UserDetailsDTO {
	
	private Long id;
	@JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
	private List<String> roles;
	private String password;
	private String email;
	private LocalDate createdAt;
	private LocalDate updatedAt;
	
	public UserDetailsDTO() {
		super();
	}

	public UserDetailsDTO(Long id, String email, List<String> roles, LocalDate createdAt, LocalDate updatedAt) {
		this.id = id;
		this.email = email;
		this.roles = roles != null ? roles : new ArrayList<>();
		this.createdAt = createdAt;
        this.updatedAt = updatedAt;
	}
	
	public UserDetailsDTO(String email, List<String> roles) {
		this.email = email;
		this.roles = roles != null ? roles : new ArrayList<>();
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

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public LocalDate getCreatedAt() {
		return createdAt;
	}

	public void setCreatedAt(LocalDate createdAt) {
		this.createdAt = createdAt;
	}

	public LocalDate getUpdatedAt() {
		return updatedAt;
	}

	public void setUpdatedAt(LocalDate updatedAt) {
		this.updatedAt = updatedAt;
	} 

}
