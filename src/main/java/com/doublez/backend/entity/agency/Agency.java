package com.doublez.backend.entity.agency;

import java.time.LocalDate;

import com.doublez.backend.entity.user.User;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
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
	
	@Column(name = "contact_email")
	private String contactEmail;
	
	@Column(name = "contact_phone")
	private String contactPhone;
	
	@Column(name = "website")
	private String website;
	
	@Column(name = "license_number", nullable = true)
	private String licenseNumber;
	
	@Column(name = "is_active", nullable = true)
	private Boolean isActive = true;

	@OneToOne
	@JoinColumn(name = "admin_id", nullable = false)
	private User admin;

	@Column(name = "created_at", nullable = false, updatable = false)
	private LocalDate createdAt;

	@PrePersist
	public void prePersist() {
		this.createdAt = LocalDate.now();
	}

	// Constructors
	public Agency() {
	}

	public Agency(String name, String description, User admin, String licenseNumber) {
		this.name = name;
		this.description = description;
		this.admin = admin;
		this.licenseNumber = licenseNumber;
	}

	public Agency(String name, String description, String logo, String contactEmail, 
	              String contactPhone, String website, String licenseNumber, User admin) {
		this.name = name;
		this.description = description;
		this.logo = logo;
		this.contactEmail = contactEmail;
		this.contactPhone = contactPhone;
		this.website = website;
		this.licenseNumber = licenseNumber;
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

	public String getContactEmail() {
		return contactEmail;
	}

	public void setContactEmail(String contactEmail) {
		this.contactEmail = contactEmail;
	}

	public String getContactPhone() {
		return contactPhone;
	}

	public void setContactPhone(String contactPhone) {
		this.contactPhone = contactPhone;
	}

	public String getWebsite() {
		return website;
	}

	public void setWebsite(String website) {
		this.website = website;
	}

	public String getLicenseNumber() {
		return licenseNumber;
	}

	public void setLicenseNumber(String licenseNumber) {
		this.licenseNumber = licenseNumber;
	}

	public Boolean getIsActive() {
		return isActive;
	}

	public void setIsActive(Boolean isActive) {
		this.isActive = isActive;
	}

	public User getAdmin() {
		return admin;
	}

	public void setAdmin(User admin) {
		this.admin = admin;
	}

	public LocalDate getCreatedAt() {
		return createdAt;
	}

	public void setCreatedAt(LocalDate createdAt) {
		this.createdAt = createdAt;
	}

	// Helper methods
	public String getDisplayContactInfo() {
		StringBuilder sb = new StringBuilder();
		if (contactEmail != null && !contactEmail.trim().isEmpty()) {
			sb.append("Email: ").append(contactEmail);
		}
		if (contactPhone != null && !contactPhone.trim().isEmpty()) {
			if (sb.length() > 0) sb.append(" | ");
			sb.append("Phone: ").append(contactPhone);
		}
		if (website != null && !website.trim().isEmpty()) {
			if (sb.length() > 0) sb.append(" | ");
			sb.append("Website: ").append(website);
		}
		return sb.length() > 0 ? sb.toString() : "No contact information";
	}

	public boolean hasWebsite() {
		return website != null && !website.trim().isEmpty();
	}

	public boolean hasContactEmail() {
		return contactEmail != null && !contactEmail.trim().isEmpty();
	}

	public boolean hasContactPhone() {
		return contactPhone != null && !contactPhone.trim().isEmpty();
	}
}
