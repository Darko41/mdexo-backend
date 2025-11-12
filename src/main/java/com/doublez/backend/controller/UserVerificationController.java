package com.doublez.backend.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.doublez.backend.dto.verification.UserVerificationDTO;
import com.doublez.backend.dto.verification.VerificationReviewDTO;
import com.doublez.backend.dto.verification.VerificationSubmissionDTO;
import com.doublez.backend.entity.user.User;
import com.doublez.backend.entity.user.UserVerification;
import com.doublez.backend.enums.VerificationStatus;
import com.doublez.backend.mapper.UserVerificationMapper;
import com.doublez.backend.service.user.UserService;
import com.doublez.backend.service.verification.UserVerificationService;

import io.swagger.v3.oas.annotations.parameters.RequestBody;
import jakarta.validation.Valid;

//controller/UserVerificationController.java
@RestController
@RequestMapping("/api/verification")
//@CrossOrigin(origins = "http://localhost:5173")
public class UserVerificationController {

	private final UserVerificationService verificationService;
	private final UserService userService;
	private final UserVerificationMapper verificationMapper;

	public UserVerificationController(UserVerificationService verificationService, UserService userService,
			UserVerificationMapper verificationMapper) {
		this.verificationService = verificationService;
		this.userService = userService;
		this.verificationMapper = verificationMapper;
	}

	// 🟢 USER ENDPOINTS

	// Submit verification application
	@PostMapping("/submit")
	@PreAuthorize("hasRole('USER')")
	public ResponseEntity<UserVerificationDTO> submitVerification(
			@Valid @RequestBody VerificationSubmissionDTO submissionDto) {
		try {
			Long userId = userService.getCurrentUserId();

			UserVerification verification = verificationService.submitVerification(userId,
					submissionDto.getDocumentType(), submissionDto.getDocumentNumber(),
					submissionDto.getDocumentImagePath(), submissionDto.getLicenseNumber(),
					submissionDto.getLicenseExpiry());

			UserVerificationDTO response = verificationMapper.toDTO(verification);
			return ResponseEntity.status(HttpStatus.CREATED).body(response);

		} catch (Exception e) {
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
		}
	}

	// Get current user's verification status
	@GetMapping("/my-status")
	@PreAuthorize("hasRole('USER')")
	public ResponseEntity<Map<String, Object>> getMyVerificationStatus() {
		try {
			Long userId = userService.getCurrentUserId();
			VerificationStatus status = verificationService.getUserVerificationStatus(userId);

			Map<String, Object> response = new HashMap<>();
			response.put("status", status);
			response.put("userId", userId);

			return ResponseEntity.ok(response);

		} catch (Exception e) {
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
		}
	}

	// Get current user's verification history
	@GetMapping("/my-history")
	@PreAuthorize("hasRole('USER')")
	public ResponseEntity<List<UserVerificationDTO>> getMyVerificationHistory() {
		try {
			Long userId = userService.getCurrentUserId();
			List<UserVerification> verifications = verificationService.getUserVerificationHistory(userId);
			List<UserVerificationDTO> response = verificationMapper.toDTOList(verifications);

			return ResponseEntity.ok(response);

		} catch (Exception e) {
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
		}
	}

	// Check if user can apply for specific role
	@GetMapping("/can-apply/{role}")
	@PreAuthorize("hasRole('USER')")
	public ResponseEntity<Map<String, Boolean>> canApplyForRole(@PathVariable String role) {
		try {
			Long userId = userService.getCurrentUserId();
			boolean isVerified = verificationService.isUserVerifiedForRole(userId, role);

			// Also check if they already have the role
			User user = userService.getUserEntityById(userId);
			boolean hasRole = user.hasRole(role);

			Map<String, Boolean> response = new HashMap<>();
			response.put("canApply", isVerified && !hasRole);
			response.put("isVerified", isVerified);
			response.put("hasRole", hasRole);

			return ResponseEntity.ok(response);

		} catch (Exception e) {
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
		}
	}

	// 🟡 ADMIN ENDPOINTS

	// Get all pending verifications
	@GetMapping("/admin/pending")
	@PreAuthorize("hasRole('ADMIN')")
	public ResponseEntity<List<UserVerificationDTO>> getPendingVerifications() {
		try {
			List<UserVerification> pending = verificationService.getPendingVerifications();
			List<UserVerificationDTO> response = verificationMapper.toDTOList(pending);

			return ResponseEntity.ok(response);

		} catch (Exception e) {
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
		}
	}

	// Get verification by ID (admin view)
	@GetMapping("/admin/{verificationId}")
	@PreAuthorize("hasRole('ADMIN')")
	public ResponseEntity<UserVerificationDTO> getVerificationById(@PathVariable Long verificationId) {
		try {
			UserVerification verification = verificationService.getVerificationById(verificationId);
			UserVerificationDTO response = verificationMapper.toDTO(verification);

			return ResponseEntity.ok(response);

		} catch (RuntimeException e) {
			return ResponseEntity.notFound().build();
		} catch (Exception e) {
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
		}
	}

	// Review verification (approve/reject)
	@PutMapping("/admin/{verificationId}/review")
	@PreAuthorize("hasRole('ADMIN')")
	public ResponseEntity<UserVerificationDTO> reviewVerification(@PathVariable Long verificationId,
			@Valid @RequestBody VerificationReviewDTO reviewDto) {
		try {
			UserVerification verification = verificationService.reviewVerification(verificationId,
					reviewDto.getStatus(), reviewDto.getRejectionReason(), reviewDto.getNotes());

			UserVerificationDTO response = verificationMapper.toDTO(verification);
			return ResponseEntity.ok(response);

		} catch (RuntimeException e) {
			return ResponseEntity.badRequest().build();
		} catch (Exception e) {
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
		}
	}

	// Get verification statistics for admin dashboard
	@GetMapping("/admin/statistics")
	@PreAuthorize("hasRole('ADMIN')")
	public ResponseEntity<Map<String, Object>> getVerificationStatistics() {
		try {
			List<UserVerification> allVerifications = verificationService.getPendingVerifications();

			long pendingCount = allVerifications.stream().filter(v -> v.getStatus() == VerificationStatus.PENDING)
					.count();

			long underReviewCount = allVerifications.stream()
					.filter(v -> v.getStatus() == VerificationStatus.UNDER_REVIEW).count();

			long approvedCount = allVerifications.stream().filter(v -> v.getStatus() == VerificationStatus.APPROVED)
					.count();

			long rejectedCount = allVerifications.stream().filter(v -> v.getStatus() == VerificationStatus.REJECTED)
					.count();

			Map<String, Object> stats = new HashMap<>();
			stats.put("pending", pendingCount);
			stats.put("underReview", underReviewCount);
			stats.put("approved", approvedCount);
			stats.put("rejected", rejectedCount);
			stats.put("total", allVerifications.size());

			return ResponseEntity.ok(stats);

		} catch (Exception e) {
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
		}
	}

	// 🟢 PUBLIC ENDPOINTS (for checking verification status)

	// Check if user is verified (public info)
	@GetMapping("/public/user/{userId}/status")
	public ResponseEntity<Map<String, Object>> getPublicVerificationStatus(@PathVariable Long userId) {
		try {
			VerificationStatus status = verificationService.getUserVerificationStatus(userId);

			Map<String, Object> response = new HashMap<>();
			response.put("userId", userId);
			response.put("status", status);
			response.put("isVerified", status == VerificationStatus.APPROVED);

			return ResponseEntity.ok(response);

		} catch (Exception e) {
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
		}
	}
}
