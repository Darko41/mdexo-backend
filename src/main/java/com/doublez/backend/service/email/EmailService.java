package com.doublez.backend.service.email;

import java.time.LocalDate;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import com.doublez.backend.config.EmailProperties;
import com.doublez.backend.dto.email.EmailDTO;
import com.doublez.backend.enums.UserTier;

import jakarta.mail.internet.MimeMessage;
import jakarta.transaction.Transactional;

@Service
@Transactional
public class EmailService {

	private static final Logger logger = LoggerFactory.getLogger(EmailService.class);

	private final JavaMailSender mailSender;
	private final EmailProperties emailProperties;
	private final EmailTemplateService templateService;

	public EmailService(JavaMailSender mailSender, EmailProperties emailProperties,
			EmailTemplateService templateService) {
		this.mailSender = mailSender;
		this.emailProperties = emailProperties;
		this.templateService = templateService;
	}

	// Basic email sending method
	public void sendEmail(EmailDTO emailDTO) {
		if (!emailProperties.isEnabled()) {
			logger.info("Email sending is disabled. Would send: To={}, Subject={}", emailDTO.getTo(),
					emailDTO.getSubject());
			return;
		}

		try {
			MimeMessage message = mailSender.createMimeMessage();
			MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

			helper.setFrom(emailProperties.getFromAddress());
			helper.setTo(emailDTO.getTo());
			helper.setSubject(emailDTO.getSubject());
			helper.setText(emailDTO.getBody(), emailDTO.isHtml());

			if (emailDTO.getCc() != null && !emailDTO.getCc().isEmpty()) {
				helper.setCc(emailDTO.getCc().toArray(new String[0]));
			}

			if (emailDTO.getBcc() != null && !emailDTO.getBcc().isEmpty()) {
				helper.setBcc(emailDTO.getBcc().toArray(new String[0]));
			}

			mailSender.send(message);
			logger.info("Email sent successfully to: {}", emailDTO.getTo());

		} catch (Exception e) {
			logger.error("Failed to send email to: {}", emailDTO.getTo(), e);
			throw new RuntimeException("Email sending failed", e);
		}
	}

	// Convenience methods for common email types

	public void sendVerificationSubmitted(String userEmail, String userName, String documentType) {
		EmailDTO email = templateService.createVerificationSubmittedEmail(userEmail, userName, documentType);
		sendEmail(email);
	}

	public void sendVerificationApproved(String userEmail, String userName, String role) {
		EmailDTO email = templateService.createVerificationApprovedEmail(userEmail, userName, role);
		sendEmail(email);
	}

	public void sendVerificationRejected(String userEmail, String userName, String rejectionReason) {
		EmailDTO email = templateService.createVerificationRejectedEmail(userEmail, userName, rejectionReason);
		sendEmail(email);
	}

	public void sendLicenseExpiring(String userEmail, String userName, LocalDate expiryDate) {
		EmailDTO email = templateService.createLicenseExpiringEmail(userEmail, userName, expiryDate);
		sendEmail(email);
	}

	public void sendNewVerificationAdminNotification(String userName, String documentType) {
		EmailDTO email = templateService.createNewVerificationAdminEmail(userName, documentType);
		sendEmail(email);
	}

	// User registration welcome email
	public void sendWelcomeEmail(String userEmail, String userName) {
		String subject = "Dobrodošli na Real Estate Platform";
		String body = """
				Poštovani/poštovana %s,

				Dobrodošli na Real Estate Platform!

				Vaš nalog je uspešno kreiran. Sada možete:
				- Pretraživati nekretnine
				- Kontaktirati agente
				- Sačuvati omiljene oglase

				Ako imate bilo kakvih pitanja, slobodno nas kontaktirajte.

				S poštovanjem,
				Real Estate Platform Team
				%s
				""".formatted(userName, emailProperties.getSupportEmail());

		EmailDTO email = new EmailDTO(userEmail, subject, body);
		sendEmail(email);
	}

	// Password reset email
	public void sendPasswordResetEmail(String userEmail, String userName, String resetToken) {
		String subject = "Resetovanje lozinke - Real Estate Platform";
		String body = """
				Poštovani/poštovana %s,

				Primili smo zahtev za resetovanje vaše lozinke.

				Token za resetovanje: %s

				Ukoliko niste Vi zatražili resetovanje lozinke, ignorišite ovaj email.

				S poštovanjem,
				Real Estate Platform Team
				%s
				""".formatted(userName, resetToken, emailProperties.getSupportEmail());

		EmailDTO email = new EmailDTO(userEmail, subject, body);
		sendEmail(email);
	}
	
	// In EmailService.java
	public void sendTrialStartedEmail(String userEmail, String userName, int trialMonths) {
	    EmailDTO email = templateService.createTrialStartedEmail(userEmail, userName, trialMonths);
	    sendEmail(email);
	}

	public void sendTrialExpiringEmail(String userEmail, String userName, int daysRemaining) {
	    EmailDTO email = templateService.createTrialExpiringEmail(userEmail, userName, daysRemaining);
	    sendEmail(email);
	}

	public void sendTrialExpiredEmail(String userEmail, String userName, UserTier newTier) {
	    EmailDTO email = templateService.createTrialExpiredEmail(userEmail, userName, newTier);
	    sendEmail(email);
	}

	public void sendTrialExtendedEmail(String userEmail, String userName, int additionalMonths) {
	    EmailDTO email = templateService.createTrialExtendedEmail(userEmail, userName, additionalMonths);
	    sendEmail(email);
	}
}
