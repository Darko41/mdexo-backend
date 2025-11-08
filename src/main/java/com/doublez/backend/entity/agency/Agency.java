package com.doublez.backend.entity.agency;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import com.doublez.backend.entity.User;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;

@Entity
@Table(name = "agencies")
public class Agency {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@NotBlank
	@Column(unique = true, nullable = false)
	private String name;

	private String description;
	private String logo;
	private String contactInfo;

	@OneToOne
	@JoinColumn(name = "admin_id", nullable = false)
	private User admin;

	@OneToMany(mappedBy = "agency", cascade = CascadeType.ALL)
	private List<AgencyMembership> memberships = new ArrayList<>();

	@Column(name = "created_at", nullable = false, updatable = false)
	private LocalDate createdAt;

	@PrePersist
	public void prePersist() {
		this.createdAt = LocalDate.now();
	}

	// Constructors
	public Agency() {
	}

	public Agency(String name, String description, User admin) {
		this.name = name;
		this.description = description;
		this.admin = admin;
	}

	// Getters and Setters
	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getLogo() {
		return logo;
	}

	public void setLogo(String logo) {
		this.logo = logo;
	}

	public String getContactInfo() {
		return contactInfo;
	}

	public void setContactInfo(String contactInfo) {
		this.contactInfo = contactInfo;
	}

	public User getAdmin() {
		return admin;
	}

	public void setAdmin(User admin) {
		this.admin = admin;
	}

	public List<AgencyMembership> getMemberships() {
		return memberships;
	}

	public void setMemberships(List<AgencyMembership> memberships) {
		this.memberships = memberships;
	}

	public LocalDate getCreatedAt() {
		return createdAt;
	}

	public void setCreatedAt(LocalDate createdAt) {
		this.createdAt = createdAt;
	}
}
