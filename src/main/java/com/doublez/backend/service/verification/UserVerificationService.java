package com.doublez.backend.service.verification;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import com.doublez.backend.dto.email.EmailDTO;
import com.doublez.backend.entity.user.User;
import com.doublez.backend.entity.user.UserVerification;
import com.doublez.backend.enums.DocumentType;
import com.doublez.backend.enums.VerificationStatus;
import com.doublez.backend.exception.UserNotFoundException;
import com.doublez.backend.repository.UserRepository;
import com.doublez.backend.repository.UserVerificationRepository;
import com.doublez.backend.service.email.EmailService;
import com.doublez.backend.service.user.UserService;

import jakarta.transaction.Transactional;

@Service
@Transactional
public class UserVerificationService {

	private final UserVerificationRepository verificationRepository;
	private final UserRepository userRepository;
	private final UserService userService;
	private final EmailService emailService;

	private static final Logger logger = LoggerFactory.getLogger(UserVerificationService.class);

	public UserVerificationService(UserVerificationRepository verificationRepository, UserRepository userRepository,
			UserService userService, EmailService emailService) {
		this.verificationRepository = verificationRepository;
		this.userRepository = userRepository;
		this.userService = userService;
		this.emailService = emailService;
	}

	// Submit verification application
	public UserVerification submitVerification(Long userId, DocumentType documentType, String documentNumber,
			String documentImagePath, String licenseNumber, LocalDate licenseExpiry) {
		User user = userRepository.findById(userId).orElseThrow(() -> new UserNotFoundException(userId));

		UserVerification verification = new UserVerification(user, documentType);
		verification.setDocumentNumber(documentNumber);
		verification.setDocumentImagePath(documentImagePath);
		verification.setLicenseNumber(licenseNumber);
		verification.setLicenseExpiry(licenseExpiry);
		verification.setStatus(VerificationStatus.PENDING);

		UserVerification saved = verificationRepository.save(verification);

		// SEND EMAIL NOTIFICATIONS
		try {
			// Notify user
			emailService.sendVerificationSubmitted(user.getEmail(), getUserDisplayName(user), documentType.toString());

			// Notify admins
			emailService.sendNewVerificationAdminNotification(getUserDisplayName(user), documentType.toString());

		} catch (Exception e) {
			logger.warn("Failed to send email notifications for verification: {}", e.getMessage());
			// Don't fail the whole operation if email fails
		}

		return saved;
	}

	// Admin reviews verification
	public UserVerification reviewVerification(Long verificationId, VerificationStatus status, String rejectionReason,
			String notes) {
		UserVerification verification = verificationRepository.findById(verificationId)
				.orElseThrow(() -> new RuntimeException("Verification not found"));

		if (!verification.canBeReviewed()) {
			throw new RuntimeException(
					"Verification cannot be reviewed in current status: " + verification.getStatus());
		}

		User currentAdmin = userService.getAuthenticatedUser();
		if (!currentAdmin.isAdmin()) {
			throw new RuntimeException("Only admins can review verifications");
		}

		verification.setStatus(status);
		verification.setReviewedAt(LocalDateTime.now());
		verification.setReviewedBy(currentAdmin);
		verification.setRejectionReason(rejectionReason);
		verification.setNotes(notes);

		UserVerification updated = verificationRepository.save(verification);

		// ✅ SEND EMAIL NOTIFICATION TO USER
		try {
			User user = verification.getUser();
			if (status == VerificationStatus.APPROVED) {
				// 🆕 DETERMINE ROLE BASED ON DOCUMENT TYPE
				String role = determineRoleFromDocumentType(verification.getDocumentType());
				emailService.sendVerificationApproved(user.getEmail(), getUserDisplayName(user), role);
			} else if (status == VerificationStatus.REJECTED) {
				emailService.sendVerificationRejected(user.getEmail(), getUserDisplayName(user), rejectionReason);
			}
		} catch (Exception e) {
			logger.warn("Failed to send verification result email: {}", e.getMessage());
			// Don't fail the whole operation if email fails
		}

		return updated;
	}

	// HELPER METHOD TO DETERMINE ROLE FROM DOCUMENT TYPE
	private String determineRoleFromDocumentType(DocumentType documentType) {
		switch (documentType) {
		case AGENT_LICENSE:
		case AGENCY_LICENSE:
			return "ROLE_AGENT";
		case TAX_REGISTRATION:
		case BUSINESS_REGISTER:
			return "ROLE_INVESTOR";
		default:
			return "ROLE_USER";
		}
	}

	// Get user's verification status
	public VerificationStatus getUserVerificationStatus(Long userId) {
		List<UserVerification> verifications = verificationRepository.findByUserId(userId);

		if (verifications.isEmpty()) {
			return VerificationStatus.PENDING; // No application submitted
		}

		// Return the highest status (APPROVED > UNDER_REVIEW > PENDING > etc.)
		return verifications.stream().map(UserVerification::getStatus).max(Comparator.comparing(Enum::ordinal))
				.orElse(VerificationStatus.PENDING);
	}

	// Check if user is verified for specific role
	public boolean isUserVerifiedForRole(Long userId, String role) {
		VerificationStatus status = getUserVerificationStatus(userId);

		switch (role) {
		case "ROLE_AGENT":
		case "ROLE_INVESTOR":
			return status == VerificationStatus.APPROVED;
		case "ROLE_USER":
			return true; // Basic users don't need verification
		default:
			return false;
		}
	}

	// Get all pending verifications for admin
	public List<UserVerification> getPendingVerifications() {
		return verificationRepository.findByStatus(VerificationStatus.PENDING);
	}

	// Get verification by ID
	public UserVerification getVerificationById(Long verificationId) {
		return verificationRepository.findById(verificationId)
				.orElseThrow(() -> new RuntimeException("Verification not found"));
	}

	// Get user's verification history
	public List<UserVerification> getUserVerificationHistory(Long userId) {
		return verificationRepository.findByUserIdOrderBySubmittedAtDesc(userId);
	}

	// Scheduled task to check for expiring licenses
	@Scheduled(cron = "0 0 8 * * ?") // Run daily at 8 AM
	public void checkExpiringLicenses() {
		List<UserVerification> expiringSoon = verificationRepository.findAll().stream()
				.filter(uv -> uv.getLicenseExpiry() != null).filter(uv -> uv.getStatus() == VerificationStatus.APPROVED)
				.filter(uv -> {
					long daysUntilExpiry = ChronoUnit.DAYS.between(LocalDate.now(), uv.getLicenseExpiry());
					return daysUntilExpiry <= 30 && daysUntilExpiry > 0; // Expiring in next 30 days
				}).collect(Collectors.toList());

		for (UserVerification verification : expiringSoon) {
			notifyUserAboutExpiringLicense(verification);
		}

		// Handle expired licenses
		List<UserVerification> expired = verificationRepository.findExpiredLicenses();
		for (UserVerification verification : expired) {
			verification.setStatus(VerificationStatus.EXPIRED);
			verificationRepository.save(verification);
			notifyUserAboutExpiredLicense(verification);
		}

		if (!expiringSoon.isEmpty() || !expired.isEmpty()) {
			logger.info("License check: {} expiring soon, {} expired", expiringSoon.size(), expired.size());
		}
	}

	// ✅ UPDATE NOTIFICATION METHODS TO USE EMAIL SERVICE
	private void notifyUserAboutExpiringLicense(UserVerification verification) {
		try {
			User user = verification.getUser();
			emailService.sendLicenseExpiring(user.getEmail(), getUserDisplayName(user),
					verification.getLicenseExpiry());
		} catch (Exception e) {
			logger.warn("Failed to send license expiration email: {}", e.getMessage());
		}
	}

	private void notifyUserAboutExpiredLicense(UserVerification verification) {
		try {
			User user = verification.getUser();
			String subject = "Licenca istekla - Real Estate Platform";
			String body = """
					Poštovani/poštovana %s,

					Vaša licenca je istekla %s.
					Molimo Vas da je obnovite kako biste nastavili da koristite sve funkcije platforme.

					S poštovanjem,
					Real Estate Platform Team
					""".formatted(getUserDisplayName(user),
					verification.getLicenseExpiry().format(DateTimeFormatter.ofPattern("dd.MM.yyyy.")));

			EmailDTO email = new EmailDTO(user.getEmail(), subject, body);
			emailService.sendEmail(email);
		} catch (Exception e) {
			logger.warn("Failed to send license expired email: {}", e.getMessage());
		}
	}

	private String getUserDisplayName(User user) {
		if (user.getUserProfile() != null && user.getUserProfile().getFirstName() != null) {
			return user.getUserProfile().getFirstName() + " "
					+ (user.getUserProfile().getLastName() != null ? user.getUserProfile().getLastName() : "");
		}
		return user.getEmail();
	}

	// 🆕 ADDITIONAL HELPER METHODS
	public boolean hasUserSubmittedVerification(Long userId, DocumentType documentType) {
		return verificationRepository.findByUserIdAndDocumentType(userId, documentType).isPresent();
	}

	public long countPendingVerifications() {
		return verificationRepository.findByStatus(VerificationStatus.PENDING).size();
	}

	public List<UserVerification> getAllVerifications() {
		return verificationRepository.findAll();
	}
}