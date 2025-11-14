package com.doublez.backend.service.email;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;
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
        
        // Log the configuration being used
        if (mailSender instanceof JavaMailSenderImpl) {
            JavaMailSenderImpl impl = (JavaMailSenderImpl) mailSender;
            logger.info("EmailService initialized with: {}:{}", impl.getHost(), impl.getPort());
        }
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

    // Simple email sending method for trial emails (without template service)
    public void sendSimpleEmail(String to, String subject, String body) {
        if (!emailProperties.isEnabled()) {
            logger.info("Email sending is disabled. Would send: To={}, Subject={}", to, subject);
            return;
        }

        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(emailProperties.getFromAddress());
            message.setTo(to);
            message.setSubject(subject);
            message.setText(body);
            
            mailSender.send(message);
            logger.info("Simple email sent successfully to: {}", to);

        } catch (Exception e) {
            logger.error("Failed to send simple email to: {}", to, e);
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
    
    // TRIAL EMAIL METHODS - FIXED VERSION
    public void sendTrialStartedEmail(String userEmail, String userName, int trialMonths) {
        try {
            String subject = "🎉 Dobrodošli - Vaš probni period je počeo!";
            String body = """
                Poštovani/poštovana %s,

                Dobrodošli na Real Estate Platform! Vaš %d-mesečni probni period je sada aktivan.

                Tokom probnog perioda, imate pristup svim premium funkcijama:
                • Neograničeno listanje nekretnina
                • Napredne filter opcije
                • Kontaktiranje vlasnika direktno
                • Čuvanje omiljenih oglasa

                Probni period traje do: %s

                Ako imate bilo kakvih pitanja, slobodno nas kontaktirajte.

                S poštovanjem,
                Real Estate Platform Team
                %s
                """.formatted(userName, trialMonths, 
                    LocalDate.now().plusMonths(trialMonths).format(DateTimeFormatter.ofPattern("dd.MM.yyyy")),
                    emailProperties.getSupportEmail());

            sendSimpleEmail(userEmail, subject, body);
            logger.info("Trial started email sent to: {}", userEmail);
            
        } catch (Exception e) {
            logger.error("Failed to send trial started email to {}: {}", userEmail, e.getMessage());
            throw e;
        }
    }

    public void sendTrialExpiringEmail(String userEmail, String userName, int daysRemaining) {
        try {
            String subject = "⏰ Vaš probni period ističe za " + daysRemaining + " dan(a)";
            String body = """
                Poštovani/poštovana %s,

                Želimo da Vas podsetimo da Vaš probni period ističe za %d dan(a).

                Nakon isteka probnog perioda, i dalje ćete imati pristup osnovnim funkcijama platforme.

                Ako želite da nastavite sa premium funkcijama, kontaktirajte nas.

                S poštovanjem,
                Real Estate Platform Team
                %s
                """.formatted(userName, daysRemaining, emailProperties.getSupportEmail());

            sendSimpleEmail(userEmail, subject, body);
            
        } catch (Exception e) {
            logger.error("Failed to send trial expiring email to {}: {}", userEmail, e.getMessage());
            throw e;
        }
    }

    public void sendTrialExpiredEmail(String userEmail, String userName, UserTier newTier) {
        try {
            String subject = "ℹ️ Vaš probni period je istekao";
            String body = """
                Poštovani/poštovana %s,

                Vaš probni period je istekao. Sada imate pristup %s paketu.

                Osnovne funkcije su i dalje dostupne. Za povratak premium funkcija, kontaktirajte nas.

                S poštovanjem,
                Real Estate Platform Team
                %s
                """.formatted(userName, getTierDisplayName(newTier), emailProperties.getSupportEmail());

            sendSimpleEmail(userEmail, subject, body);
            
        } catch (Exception e) {
            logger.error("Failed to send trial expired email to {}: {}", userEmail, e.getMessage());
            throw e;
        }
    }

    public void sendTrialExtendedEmail(String userEmail, String userName, int additionalMonths) {
        try {
            String subject = "🎁 Vaš probni period je produžen!";
            String body = """
                Poštovani/poštovana %s,

                Vaš probni period je produžen za %d meseci.

                Sada možete nastaviti da uživate u svim premium funkcijama do %s.

                Hvala Vam što koristite našu platformu!

                S poštovanjem,
                Real Estate Platform Team
                %s
                """.formatted(userName, additionalMonths,
                    LocalDate.now().plusMonths(additionalMonths).format(DateTimeFormatter.ofPattern("dd.MM.yyyy")),
                    emailProperties.getSupportEmail());

            sendSimpleEmail(userEmail, subject, body);
            
        } catch (Exception e) {
            logger.error("Failed to send trial extended email to {}: {}", userEmail, e.getMessage());
            throw e;
        }
    }

    private String getTierDisplayName(UserTier tier) {
        switch (tier) {
            case FREE_USER: return "Besplatni";
            case BASIC_USER: return "Osnovni";
            case PREMIUM_USER: return "Premium";
            case FREE_AGENT: return "Besplatni Agent";
            case BASIC_AGENT: return "Osnovni Agent";
            case PREMIUM_AGENT: return "Premium Agent";
            case FREE_INVESTOR: return "Besplatni Investitor";
            case BASIC_INVESTOR: return "Osnovni Investitor";
            case PREMIUM_INVESTOR: return "Premium Investitor";
            default: return "Osnovni";
        }
    }
}