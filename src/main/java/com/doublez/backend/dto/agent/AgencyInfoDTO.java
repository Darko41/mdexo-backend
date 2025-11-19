package com.doublez.backend.dto.agent;


public class AgencyInfoDTO {
	private Long id;
	private String name;
	private String description;
	private String logo;
	private String contactInfo;
	private String position;

	// Constructors
	public AgencyInfoDTO() {
	}

	public AgencyInfoDTO(Long id, String name, String description) {
		this.id = id;
		this.name = name;
		this.description = description;
	}

	// Getters and setters
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

	public String getPosition() {
		return position;
	}

	public void setPosition(String position) {
		this.position = position;
	}
}
