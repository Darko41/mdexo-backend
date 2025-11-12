package com.doublez.backend.dto.agent;

public class PromoteRequestDTO {
	private Long userId;
	private AgencyDTO.Create agencyDto;

	// Constructors
	public PromoteRequestDTO() {
	}

	public PromoteRequestDTO(Long userId, AgencyDTO.Create agencyDto) {
		this.userId = userId;
		this.agencyDto = agencyDto;
	}

	// Getters and Setters
	public Long getUserId() {
		return userId;
	}

	public void setUserId(Long userId) {
		this.userId = userId;
	}

	public AgencyDTO.Create getAgencyDto() {
		return agencyDto;
	}

	public void setAgencyDto(AgencyDTO.Create agencyDto) {
		this.agencyDto = agencyDto;
	}
}
