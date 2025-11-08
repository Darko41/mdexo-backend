package com.doublez.backend.dto.agent;

import java.time.LocalDate;

import com.doublez.backend.dto.user.UserDTO;
import com.doublez.backend.entity.agency.AgencyMembership;

public class AgencyMembershipDTO {
	private Long id;
	private UserDTO user;
	private AgencyDTO agency;
	private AgencyMembership.MembershipStatus status; // Use the entity enum
	private String position;
	private LocalDate joinDate;
	private LocalDate createdAt;

	// Getters and setters
	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public UserDTO getUser() {
		return user;
	}

	public void setUser(UserDTO user) {
		this.user = user;
	}

	public AgencyDTO getAgency() {
		return agency;
	}

	public void setAgency(AgencyDTO agency) {
		this.agency = agency;
	}

	public AgencyMembership.MembershipStatus getStatus() {
		return status;
	}

	public void setStatus(AgencyMembership.MembershipStatus status) {
		this.status = status;
	}

	public String getPosition() {
		return position;
	}

	public void setPosition(String position) {
		this.position = position;
	}

	public LocalDate getJoinDate() {
		return joinDate;
	}

	public void setJoinDate(LocalDate joinDate) {
		this.joinDate = joinDate;
	}

	public LocalDate getCreatedAt() {
		return createdAt;
	}

	public void setCreatedAt(LocalDate createdAt) {
		this.createdAt = createdAt;
	}
}
