package com.doublez.backend.mapper;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;

import com.doublez.backend.dto.verification.UserVerificationDTO;
import com.doublez.backend.entity.user.User;
import com.doublez.backend.entity.user.UserVerification;

@Component
public class UserVerificationMapper {

	public UserVerificationDTO toDTO(UserVerification verification) {
		if (verification == null) {
			return null;
		}

		UserVerificationDTO dto = new UserVerificationDTO();
		dto.setId(verification.getId());
		dto.setUserId(verification.getUser().getId());
		dto.setUserEmail(verification.getUser().getEmail());
		dto.setUserName(getUserDisplayName(verification.getUser()));
		dto.setStatus(verification.getStatus());
		dto.setDocumentType(verification.getDocumentType());
		dto.setDocumentNumber(verification.getDocumentNumber());
		dto.setDocumentImagePath(verification.getDocumentImagePath());
		dto.setLicenseNumber(verification.getLicenseNumber());
		dto.setLicenseExpiry(verification.getLicenseExpiry());
		dto.setSubmittedAt(verification.getSubmittedAt());
		dto.setReviewedAt(verification.getReviewedAt());
		dto.setRejectionReason(verification.getRejectionReason());
		dto.setNotes(verification.getNotes());

		if (verification.getReviewedBy() != null) {
			dto.setReviewedBy(getUserDisplayName(verification.getReviewedBy()));
		}

		return dto;
	}

	public List<UserVerificationDTO> toDTOList(List<UserVerification> verifications) {
		if (verifications == null) {
			return List.of();
		}

		return verifications.stream().map(this::toDTO).collect(Collectors.toList());
	}

	private String getUserDisplayName(User user) {
		if (user.getUserProfile() != null && user.getUserProfile().getFirstName() != null) {
			return user.getUserProfile().getFirstName() + " "
					+ (user.getUserProfile().getLastName() != null ? user.getUserProfile().getLastName() : "");
		}
		return user.getEmail();
	}
}
