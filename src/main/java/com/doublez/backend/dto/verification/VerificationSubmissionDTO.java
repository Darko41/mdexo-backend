package com.doublez.backend.dto.verification;

import java.time.LocalDate;

import com.doublez.backend.enums.DocumentType;

public class VerificationSubmissionDTO {
	private DocumentType documentType;
	private String documentNumber;
	private String documentImagePath; // URL/path to uploaded document
	private String licenseNumber;
	private LocalDate licenseExpiry;

	// Constructors
	public VerificationSubmissionDTO() {
	}

	public VerificationSubmissionDTO(DocumentType documentType, String documentNumber, String documentImagePath,
			String licenseNumber, LocalDate licenseExpiry) {
		this.documentType = documentType;
		this.documentNumber = documentNumber;
		this.documentImagePath = documentImagePath;
		this.licenseNumber = licenseNumber;
		this.licenseExpiry = licenseExpiry;
	}

	// Getters and Setters
	public DocumentType getDocumentType() {
		return documentType;
	}

	public void setDocumentType(DocumentType documentType) {
		this.documentType = documentType;
	}

	public String getDocumentNumber() {
		return documentNumber;
	}

	public void setDocumentNumber(String documentNumber) {
		this.documentNumber = documentNumber;
	}

	public String getDocumentImagePath() {
		return documentImagePath;
	}

	public void setDocumentImagePath(String documentImagePath) {
		this.documentImagePath = documentImagePath;
	}

	public String getLicenseNumber() {
		return licenseNumber;
	}

	public void setLicenseNumber(String licenseNumber) {
		this.licenseNumber = licenseNumber;
	}

	public LocalDate getLicenseExpiry() {
		return licenseExpiry;
	}

	public void setLicenseExpiry(LocalDate licenseExpiry) {
		this.licenseExpiry = licenseExpiry;
	}
}
