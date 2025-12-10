package com.doublez.backend.dto.warnings;

import java.time.LocalDateTime;

public class SystemWarningDTO {
    private String code;
    private String title;
    private String description;
    private String severity; // LOW, MEDIUM, HIGH, CRITICAL
    private LocalDateTime generatedAt;
    private Long relatedId; // agentId, listingId, etc
    
    public String getCode() {
		return code;
	}

	public void setCode(String code) {
		this.code = code;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getSeverity() {
		return severity;
	}

	public void setSeverity(String severity) {
		this.severity = severity;
	}

	public LocalDateTime getGeneratedAt() {
		return generatedAt;
	}

	public void setGeneratedAt(LocalDateTime generatedAt) {
		this.generatedAt = generatedAt;
	}

	public Long getRelatedId() {
		return relatedId;
	}

	public void setRelatedId(Long relatedId) {
		this.relatedId = relatedId;
	}

	public SystemWarningDTO() {}
    
    public SystemWarningDTO(String code, String title, String description, 
                           String severity, LocalDateTime generatedAt, Long relatedId) {
        this.code = code;
        this.title = title;
        this.description = description;
        this.severity = severity;
        this.generatedAt = generatedAt;
        this.relatedId = relatedId;
    }

}
