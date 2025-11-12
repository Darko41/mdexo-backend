package com.doublez.backend.dto.verification;

import com.doublez.backend.enums.VerificationStatus;

public class VerificationReviewDTO {
	private VerificationStatus status;
	private String rejectionReason;
	private String notes;

	// Constructors
	public VerificationReviewDTO() {
	}

	public VerificationReviewDTO(VerificationStatus status, String rejectionReason, String notes) {
		this.status = status;
		this.rejectionReason = rejectionReason;
		this.notes = notes;
	}

	// Getters and Setters
	public VerificationStatus getStatus() {
		return status;
	}

	public void setStatus(VerificationStatus status) {
		this.status = status;
	}

	public String getRejectionReason() {
		return rejectionReason;
	}

	public void setRejectionReason(String rejectionReason) {
		this.rejectionReason = rejectionReason;
	}

	public String getNotes() {
		return notes;
	}

	public void setNotes(String notes) {
		this.notes = notes;
	}
}
