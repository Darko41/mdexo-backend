package com.doublez.backend.service.email;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.doublez.backend.config.security.JwtAuthenticationFilter;
import com.doublez.backend.entity.user.UserTier;
import com.resend.Resend;
import com.resend.core.exception.ResendException;
import com.resend.services.emails.model.CreateEmailOptions;
import com.resend.services.emails.model.CreateEmailResponse;

import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.annotation.Propagation;

@Service
@Transactional(propagation = Propagation.REQUIRES_NEW)
public class ResendEmailService {
    
    private static final Logger logger = LoggerFactory.getLogger(ResendEmailService.class);
    
    private final Resend resend;
    private final String fromAddress;
    private final String supportEmail;
    private final String adminEmail;
    private final boolean emailEnabled;
    
    public ResendEmailService(@Value("${resend.api-key}") String apiKey,
                            @Value("${app.email.from-address:noreply@iterials.com}") String fromAddress,
                            @Value("${app.email.support-email:support@iterials.com}") String supportEmail,
                            @Value("${app.email.admin-email:admin@iterials.com}") String adminEmail,
                            @Value("${app.email.enabled:true}") boolean emailEnabled) {
        this.resend = new Resend(apiKey);
        this.fromAddress = fromAddress;
        this.supportEmail = supportEmail;
        this.adminEmail = adminEmail;
        this.emailEnabled = emailEnabled;
    }
    
    // Basic email sending method
    public void sendEmail(String to, String subject, String htmlContent, String textContent) {
        if (!emailEnabled) {
            logger.info("Email sending is disabled. Would send: To={}, Subject={}", to, subject);
            return;
        }

        try {
            CreateEmailOptions params = CreateEmailOptions.builder()
                .from("Dwellia <" + fromAddress + ">")
                .to(to)
                .subject(subject)
                .html(htmlContent)
                .text(textContent)
                .build();
            
            CreateEmailResponse data = resend.emails().send(params);
            logger.info("✅ Email sent successfully to: {} with ID: {}", to, data.getId());
            
        } catch (Exception e) {
            logger.error("❌ Failed to send email to {}: {}", to, e.getMessage());
            throw new RuntimeException("Email sending failed", e);
        }
    }
    
    // Simple text email
    public void sendTextEmail(String to, String subject, String textContent) {
        sendEmail(to, subject, null, textContent);
    }
    
    // HTML email only
    public void sendHtmlEmail(String to, String subject, String htmlContent) {
        sendEmail(to, subject, htmlContent, null);
    }

    // USER REGISTRATION & AUTHENTICATION EMAILS
    public void sendWelcomeEmail(String userEmail, String userName) {
        String subject = "Dobrodošli na Real Estate Platform";
        String htmlContent = createWelcomeEmailHtml(userName);
        String textContent = createWelcomeEmailText(userName);
        sendEmail(userEmail, subject, htmlContent, textContent);
    }

    public void sendPasswordResetEmail(String userEmail, String userName, String resetToken) {
        String subject = "Resetovanje lozinke - Real Estate Platform";
        String htmlContent = createPasswordResetHtml(userName, resetToken);
        String textContent = createPasswordResetText(userName, resetToken);
        sendEmail(userEmail, subject, htmlContent, textContent);
    }

    // VERIFICATION EMAILS
    public void sendVerificationSubmitted(String userEmail, String userName, String documentType) {
        String subject = "Verifikacija dokumenta je podneta";
        String htmlContent = createVerificationSubmittedHtml(userName, documentType);
        String textContent = createVerificationSubmittedText(userName, documentType);
        sendEmail(userEmail, subject, htmlContent, textContent);
    }

    public void sendVerificationApproved(String userEmail, String userName, String role) {
        String subject = "Verifikacija odobrena";
        String htmlContent = createVerificationApprovedHtml(userName, role);
        String textContent = createVerificationApprovedText(userName, role);
        sendEmail(userEmail, subject, htmlContent, textContent);
    }

    public void sendVerificationRejected(String userEmail, String userName, String rejectionReason) {
        String subject = "Verifikacija odbijena";
        String htmlContent = createVerificationRejectedHtml(userName, rejectionReason);
        String textContent = createVerificationRejectedText(userName, rejectionReason);
        sendEmail(userEmail, subject, htmlContent, textContent);
    }

    public void sendLicenseExpiring(String userEmail, String userName, LocalDate expiryDate) {
        String subject = "Licenca ističe uskoro";
        String htmlContent = createLicenseExpiringHtml(userName, expiryDate);
        String textContent = createLicenseExpiringText(userName, expiryDate);
        sendEmail(userEmail, subject, htmlContent, textContent);
    }

    public void sendNewVerificationAdminNotification(String userName, String documentType) {
        String subject = "Nova verifikacija za pregled";
        String htmlContent = createNewVerificationAdminHtml(userName, documentType);
        String textContent = createNewVerificationAdminText(userName, documentType);
        sendEmail(adminEmail, subject, htmlContent, textContent);
    }

    // TRIAL EMAILS
    public void sendTrialStartedEmail(String userEmail, String userName, int trialMonths) {
        String subject = "🎉 Dobrodošli - Vaš probni period je počeo!";
        String htmlContent = createTrialStartedHtml(userName, trialMonths);
        String textContent = createTrialStartedText(userName, trialMonths);
        sendEmail(userEmail, subject, htmlContent, textContent);
    }

    public void sendTrialExpiringEmail(String userEmail, String userName, int daysRemaining) {
        String subject = "⏰ Vaš probni period ističe za " + daysRemaining + " dan(a)";
        String htmlContent = createTrialExpiringHtml(userName, daysRemaining);
        String textContent = createTrialExpiringText(userName, daysRemaining);
        sendEmail(userEmail, subject, htmlContent, textContent);
    }

//    public void sendTrialExpiredEmail(String userEmail, String userName, UserTier newTier) {
//        try {
//            logger.info("🔄 Starting trial expired email for: {}, Tier: {}", userEmail, newTier);
//            
//            // Check if tier is null
//            if (newTier == null) {
//                logger.warn("⚠️ UserTier is null for trial expired email, using BASIC_USER as default");
//                newTier = UserTier.BASIC_USER; // Default fallback
//            }
//            
//            String subject = "ℹ️ Vaš probni period je istekao";
//            logger.info("🔄 Subject: {}", subject);
//            
//            // Generate HTML content
//            String htmlContent = createTrialExpiredHtml(userName, newTier);
//            logger.info("🔄 HTML content generated, length: {}", htmlContent.length());
//            
//            // Generate text content
//            String textContent = createTrialExpiredText(userName, newTier);
//            logger.info("🔄 Text content generated, length: {}", textContent.length());
//            
//            // Send the email
//            sendEmail(userEmail, subject, htmlContent, textContent);
//            logger.info("✅ Trial expired email sent successfully to: {}", userEmail);
//            
//        } catch (Exception e) {
//            logger.error("❌ Failed to send trial expired email to {}: {}", userEmail, e.getMessage(), e);
//            throw new RuntimeException("Email sending failed", e);
//        }
//    }

    public void sendTrialExtendedEmail(String userEmail, String userName, int additionalMonths) {
        String subject = "🎁 Vaš probni period je produžen!";
        String htmlContent = createTrialExtendedHtml(userName, additionalMonths);
        String textContent = createTrialExtendedText(userName, additionalMonths);
        sendEmail(userEmail, subject, htmlContent, textContent);
    }

    // HTML TEMPLATES
    private String createWelcomeEmailHtml(String userName) {
        return "<!DOCTYPE html>" +
            "<html>" +
            "<head>" +
            "    <style>" +
            "        body { font-family: Arial, sans-serif; line-height: 1.6; color: #333; }" +
            "        .container { max-width: 600px; margin: 0 auto; padding: 20px; }" +
            "        .header { background: linear-gradient(135deg, #667eea 0%, #764ba2 100%); color: white; padding: 30px; text-align: center; border-radius: 10px 10px 0 0; }" +
            "        .content { padding: 30px; background: #f8f9fa; border-radius: 0 0 10px 10px; }" +
            "        .button { background: #667eea; color: white; padding: 12px 30px; text-decoration: none; border-radius: 5px; display: inline-block; margin: 20px 0; }" +
            "    </style>" +
            "</head>" +
            "<body>" +
            "    <div class=\"container\">" +
            "        <div class=\"header\">" +
            "            <h1>Dobrodošli!</h1>" +
            "            <p>Vaš nalog je uspešno kreiran</p>" +
            "        </div>" +
            "        <div class=\"content\">" +
            "            <h2>Poštovani/poštovana " + userName + ",</h2>" +
            "            <p>Dobrodošli na Real Estate Platform! Vaš nalog je uspešno kreiran.</p>" +
            "            <p>Sada možete pretraživati nekretnine, kontaktirati agente i sačuvati omiljene oglase.</p>" +
            "            <a href=\"https://dwellia.rs\" class=\"button\">Započnite istraživanje</a>" +
            "            <p>Ako imate pitanja, kontaktirajte nas: <a href=\"mailto:" + supportEmail + "\">" + supportEmail + "</a></p>" +
            "            <p>S poštovanjem,<br>Real Estate Platform Team</p>" +
            "        </div>" +
            "    </div>" +
            "</body>" +
            "</html>";
    }

    private String createPasswordResetHtml(String userName, String resetToken) {
        return "<!DOCTYPE html>" +
            "<html>" +
            "<head>" +
            "    <style>" +
            "        body { font-family: Arial, sans-serif; line-height: 1.6; color: #333; }" +
            "        .container { max-width: 600px; margin: 0 auto; padding: 20px; }" +
            "        .header { background: linear-gradient(135deg, #f093fb 0%, #f5576c 100%); color: white; padding: 30px; text-align: center; border-radius: 10px 10px 0 0; }" +
            "        .content { padding: 30px; background: #f8f9fa; border-radius: 0 0 10px 10px; }" +
            "        .token { background: white; padding: 15px; border: 2px dashed #f5576c; text-align: center; font-size: 18px; font-weight: bold; margin: 20px 0; }" +
            "    </style>" +
            "</head>" +
            "<body>" +
            "    <div class=\"container\">" +
            "        <div class=\"header\">" +
            "            <h1>Resetovanje Lozinke</h1>" +
            "        </div>" +
            "        <div class=\"content\">" +
            "            <h2>Poštovani/poštovana " + userName + ",</h2>" +
            "            <p>Primili smo zahtev za resetovanje vaše lozinke.</p>" +
            "            <div class=\"token\">" + resetToken + "</div>" +
            "            <p>Ukoliko niste Vi zatražili resetovanje, ignorišite ovaj email.</p>" +
            "            <p>S poštovanjem,<br>Real Estate Platform Team<br><a href=\"mailto:" + supportEmail + "\">" + supportEmail + "</a></p>" +
            "        </div>" +
            "    </div>" +
            "</body>" +
            "</html>";
    }

    private String createVerificationSubmittedHtml(String userName, String documentType) {
        return "<!DOCTYPE html>" +
            "<html>" +
            "<head>" +
            "    <style>" +
            "        body { font-family: Arial, sans-serif; line-height: 1.6; color: #333; }" +
            "        .container { max-width: 600px; margin: 0 auto; padding: 20px; }" +
            "        .header { background: linear-gradient(135deg, #4facfe 0%, #00f2fe 100%); color: white; padding: 30px; text-align: center; border-radius: 10px 10px 0 0; }" +
            "        .content { padding: 30px; background: #f8f9fa; border-radius: 0 0 10px 10px; }" +
            "    </style>" +
            "</head>" +
            "<body>" +
            "    <div class=\"container\">" +
            "        <div class=\"header\">" +
            "            <h1>Verifikacija Podneta</h1>" +
            "        </div>" +
            "        <div class=\"content\">" +
            "            <h2>Poštovani/poštovana " + userName + ",</h2>" +
            "            <p>Uspešno ste podneli dokument za verifikaciju: <strong>" + documentType + "</strong>.</p>" +
            "            <p>Naš tim će pregledati vašu prijavu u najkraćem mogućem roku.</p>" +
            "            <p>Bićete obavešteni o statusu verifikacije.</p>" +
            "            <p>S poštovanjem,<br>Real Estate Platform Team</p>" +
            "        </div>" +
            "    </div>" +
            "</body>" +
            "</html>";
    }

    private String createVerificationApprovedHtml(String userName, String role) {
        return "<!DOCTYPE html>" +
            "<html>" +
            "<head>" +
            "    <style>" +
            "        body { font-family: Arial, sans-serif; line-height: 1.6; color: #333; }" +
            "        .container { max-width: 600px; margin: 0 auto; padding: 20px; }" +
            "        .header { background: linear-gradient(135deg, #43e97b 0%, #38f9d7 100%); color: white; padding: 30px; text-align: center; border-radius: 10px 10px 0 0; }" +
            "        .content { padding: 30px; background: #f8f9fa; border-radius: 0 0 10px 10px; }" +
            "        .button { background: #43e97b; color: white; padding: 12px 30px; text-decoration: none; border-radius: 5px; display: inline-block; margin: 20px 0; }" +
            "    </style>" +
            "</head>" +
            "<body>" +
            "    <div class=\"container\">" +
            "        <div class=\"header\">" +
            "            <h1>✅ Verifikacija Odobrena</h1>" +
            "        </div>" +
            "        <div class=\"content\">" +
            "            <h2>Poštovani/poštovana " + userName + ",</h2>" +
            "            <p>Čestitamo! Vaša verifikacija je odobrena.</p>" +
            "            <p>Sada imate <strong>" + role + "</strong> status na platformi.</p>" +
            "            <p>Možete početi da koristite sve privilegije vašeg novog statusa.</p>" +
            "            <a href=\"https://dwellia.rs\" class=\"button\">Nastavite na Platformu</a>" +
            "            <p>S poštovanjem,<br>Real Estate Platform Team</p>" +
            "        </div>" +
            "    </div>" +
            "</body>" +
            "</html>";
    }

    private String createVerificationRejectedHtml(String userName, String rejectionReason) {
        return "<!DOCTYPE html>" +
            "<html>" +
            "<head>" +
            "    <style>" +
            "        body { font-family: Arial, sans-serif; line-height: 1.6; color: #333; }" +
            "        .container { max-width: 600px; margin: 0 auto; padding: 20px; }" +
            "        .header { background: linear-gradient(135deg, #ff6b6b 0%, #ee5a24 100%); color: white; padding: 30px; text-align: center; border-radius: 10px 10px 0 0; }" +
            "        .content { padding: 30px; background: #f8f9fa; border-radius: 0 0 10px 10px; }" +
            "        .reason { background: white; padding: 15px; border-left: 4px solid #ff6b6b; margin: 15px 0; }" +
            "    </style>" +
            "</head>" +
            "<body>" +
            "    <div class=\"container\">" +
            "        <div class=\"header\">" +
            "            <h1>❌ Verifikacija Odbijena</h1>" +
            "        </div>" +
            "        <div class=\"content\">" +
            "            <h2>Poštovani/poštovana " + userName + ",</h2>" +
            "            <p>Nažalost, vaša verifikacija je odbijena.</p>" +
            "            <div class=\"reason\">" +
            "                <strong>Razlog:</strong><br>" + rejectionReason +
            "            </div>" +
            "            <p>Možete podneti novu prijavu sa ispravljenim dokumentima.</p>" +
            "            <p>Ako imate pitanja, kontaktirajte nas: <a href=\"mailto:" + supportEmail + "\">" + supportEmail + "</a></p>" +
            "            <p>S poštovanjem,<br>Real Estate Platform Team</p>" +
            "        </div>" +
            "    </div>" +
            "</body>" +
            "</html>";
    }

    private String createLicenseExpiringHtml(String userName, LocalDate expiryDate) {
        String formattedDate = expiryDate.format(DateTimeFormatter.ofPattern("dd.MM.yyyy"));
        return "<!DOCTYPE html>" +
            "<html>" +
            "<head>" +
            "    <style>" +
            "        body { font-family: Arial, sans-serif; line-height: 1.6; color: #333; }" +
            "        .container { max-width: 600px; margin: 0 auto; padding: 20px; }" +
            "        .header { background: linear-gradient(135deg, #ffa726 0%, #ff9800 100%); color: white; padding: 30px; text-align: center; border-radius: 10px 10px 0 0; }" +
            "        .content { padding: 30px; background: #f8f9fa; border-radius: 0 0 10px 10px; }" +
            "        .warning { background: #fff3e0; padding: 15px; border-left: 4px solid #ffa726; margin: 15px 0; }" +
            "    </style>" +
            "</head>" +
            "<body>" +
            "    <div class=\"container\">" +
            "        <div class=\"header\">" +
            "            <h1>⚠️ Licenca Ističe</h1>" +
            "        </div>" +
            "        <div class=\"content\">" +
            "            <h2>Poštovani/poštovana " + userName + ",</h2>" +
            "            <div class=\"warning\">" +
            "                <strong>Vaša licenca ističe: " + formattedDate + "</strong>" +
            "            </div>" +
            "            <p>Obnovite licencu na vreme kako biste nastavili da koristite sve funkcije platforme.</p>" +
            "            <p>S poštovanjem,<br>Real Estate Platform Team<br><a href=\"mailto:" + supportEmail + "\">" + supportEmail + "</a></p>" +
            "        </div>" +
            "    </div>" +
            "</body>" +
            "</html>";
    }

    private String createNewVerificationAdminHtml(String userName, String documentType) {
        return "<!DOCTYPE html>" +
            "<html>" +
            "<head>" +
            "    <style>" +
            "        body { font-family: Arial, sans-serif; line-height: 1.6; color: #333; }" +
            "        .container { max-width: 600px; margin: 0 auto; padding: 20px; }" +
            "        .header { background: linear-gradient(135deg, #8e2de2 0%, #4a00e0 100%); color: white; padding: 30px; text-align: center; border-radius: 10px 10px 0 0; }" +
            "        .content { padding: 30px; background: #f8f9fa; border-radius: 0 0 10px 10px; }" +
            "        .button { background: #8e2de2; color: white; padding: 12px 30px; text-decoration: none; border-radius: 5px; display: inline-block; margin: 20px 0; }" +
            "    </style>" +
            "</head>" +
            "<body>" +
            "    <div class=\"container\">" +
            "        <div class=\"header\">" +
            "            <h1>📋 Nova Verifikacija</h1>" +
            "        </div>" +
            "        <div class=\"content\">" +
            "            <h2>Administrativni pregled</h2>" +
            "            <p>Korisnik <strong>" + userName + "</strong> je podneo novu verifikaciju.</p>" +
            "            <p><strong>Tip dokumenta:</strong> " + documentType + "</p>" +
            "            <a href=\"https://dwellia.rs/admin/verifications\" class=\"button\">Pregledaj Verifikacije</a>" +
            "            <p>Real Estate Platform Admin</p>" +
            "        </div>" +
            "    </div>" +
            "</body>" +
            "</html>";
    }

    private String createTrialStartedHtml(String userName, int trialMonths) {
        String endDate = LocalDate.now().plusMonths(trialMonths)
            .format(DateTimeFormatter.ofPattern("dd.MM.yyyy"));
        return "<!DOCTYPE html>" +
            "<html>" +
            "<head>" +
            "    <style>" +
            "        body { font-family: Arial, sans-serif; line-height: 1.6; color: #333; }" +
            "        .container { max-width: 600px; margin: 0 auto; padding: 20px; }" +
            "        .header { background: linear-gradient(135deg, #667eea 0%, #764ba2 100%); color: white; padding: 30px; text-align: center; border-radius: 10px 10px 0 0; }" +
            "        .content { padding: 30px; background: #f8f9fa; border-radius: 0 0 10px 10px; }" +
            "        .feature { background: white; padding: 15px; margin: 10px 0; border-radius: 5px; border-left: 4px solid #667eea; }" +
            "        .button { background: #667eea; color: white; padding: 12px 30px; text-decoration: none; border-radius: 5px; display: inline-block; margin: 20px 0; }" +
            "    </style>" +
            "</head>" +
            "<body>" +
            "    <div class=\"container\">" +
            "        <div class=\"header\">" +
            "            <h1>🎉 Dobrodošli!</h1>" +
            "            <p>Vaš " + trialMonths + "-mesečni probni period je aktivan</p>" +
            "        </div>" +
            "        <div class=\"content\">" +
            "            <h2>Poštovani/poštovana " + userName + ",</h2>" +
            "            <p>Dobrodošli na Real Estate Platform! Vaš " + trialMonths + "-mesečni probni period je sada aktivan.</p>" +
            "            <div class=\"feature\"><strong>🏠 Neograničeno listanje nekretnina</strong></div>" +
            "            <div class=\"feature\"><strong>🔍 Napredne filter opcije</strong></div>" +
            "            <div class=\"feature\"><strong>📱 Direktan kontakt sa vlasnicima</strong></div>" +
            "            <p><strong>📅 Kraj probnog perioda:</strong> " + endDate + "</p>" +
            "            <a href=\"https://dwellia.rs\" class=\"button\">Započnite istraživanje</a>" +
            "            <p>Srećno traženje doma!<br>Real Estate Platform Team</p>" +
            "        </div>" +
            "    </div>" +
            "</body>" +
            "</html>";
    }

    private String createTrialExpiringHtml(String userName, int daysRemaining) {
        return "<!DOCTYPE html>" +
            "<html>" +
            "<head>" +
            "    <style>" +
            "        body { font-family: Arial, sans-serif; line-height: 1.6; color: #333; }" +
            "        .container { max-width: 600px; margin: 0 auto; padding: 20px; }" +
            "        .header { background: linear-gradient(135deg, #f093fb 0%, #f5576c 100%); color: white; padding: 30px; text-align: center; border-radius: 10px 10px 0 0; }" +
            "        .content { padding: 30px; background: #f8f9fa; border-radius: 0 0 10px 10px; }" +
            "        .button { background: #f5576c; color: white; padding: 12px 30px; text-decoration: none; border-radius: 5px; display: inline-block; margin: 20px 0; }" +
            "    </style>" +
            "</head>" +
            "<body>" +
            "    <div class=\"container\">" +
            "        <div class=\"header\">" +
            "            <h1>⏰ Trial Ending Soon</h1>" +
            "            <p>" + daysRemaining + " dan(a) remaining</p>" +
            "        </div>" +
            "        <div class=\"content\">" +
            "            <h2>Hello " + userName + ",</h2>" +
            "            <p>Your Dwellia trial expires in <strong>" + daysRemaining + " day(s)</strong>.</p>" +
            "            <p>After your trial ends, you'll still have access to basic features.</p>" +
            "            <a href=\"https://dwellia.rs/pricing\" class=\"button\">View Pricing Plans</a>" +
            "        </div>" +
            "    </div>" +
            "</body>" +
            "</html>";
    }

//    private String createTrialExpiredHtml(String userName, UserTier newTier) {
//        String tierName = getTierDisplayName(newTier);
//        return "<!DOCTYPE html>" +
//            "<html>" +
//            "<head>" +
//            "    <style>" +
//            "        body { font-family: Arial, sans-serif; line-height: 1.6; color: #333; }" +
//            "        .container { max-width: 600px; margin: 0 auto; padding: 20px; }" +
//            "        .header { background: linear-gradient(135deg, #4facfe 0%, #00f2fe 100%); color: white; padding: 30px; text-align: center; border-radius: 10px 10px 0 0; }" +
//            "        .content { padding: 30px; background: #f8f9fa; border-radius: 0 0 10px 10px; }" +
//            "        .button { background: #4facfe; color: white; padding: 12px 30px; text-decoration: none; border-radius: 5px; display: inline-block; margin: 20px 0; }" +
//            "    </style>" +
//            "</head>" +
//            "<body>" +
//            "    <div class=\"container\">" +
//            "        <div class=\"header\">" +
//            "            <h1>📊 Trial Completed</h1>" +
//            "            <p>Welcome to " + tierName + " Plan</p>" +
//            "        </div>" +
//            "        <div class=\"content\">" +
//            "            <h2>Hello " + userName + ",</h2>" +
//            "            <p>Your trial period has ended. You now have access to the <strong>" + tierName + "</strong> plan.</p>" +
//            "            <p>Basic features remain available. Upgrade to continue enjoying premium benefits.</p>" +
//            "            <a href=\"https://dwellia.rs/pricing\" class=\"button\">Upgrade Your Plan</a>" +
//            "        </div>" +
//            "    </div>" +
//            "</body>" +
//            "</html>";
//    }

    private String createTrialExtendedHtml(String userName, int additionalMonths) {
        String endDate = LocalDate.now().plusMonths(additionalMonths)
            .format(DateTimeFormatter.ofPattern("dd.MM.yyyy"));
        return "<!DOCTYPE html>" +
            "<html>" +
            "<head>" +
            "    <style>" +
            "        body { font-family: Arial, sans-serif; line-height: 1.6; color: #333; }" +
            "        .container { max-width: 600px; margin: 0 auto; padding: 20px; }" +
            "        .header { background: linear-gradient(135deg, #43e97b 0%, #38f9d7 100%); color: white; padding: 30px; text-align: center; border-radius: 10px 10px 0 0; }" +
            "        .content { padding: 30px; background: #f8f9fa; border-radius: 0 0 10px 10px; }" +
            "        .button { background: #43e97b; color: white; padding: 12px 30px; text-decoration: none; border-radius: 5px; display: inline-block; margin: 20px 0; }" +
            "    </style>" +
            "</head>" +
            "<body>" +
            "    <div class=\"container\">" +
            "        <div class=\"header\">" +
            "            <h1>🎁 Trial Extended!</h1>" +
            "            <p>+" + additionalMonths + " extra months</p>" +
            "        </div>" +
            "        <div class=\"content\">" +
            "            <h2>Hello " + userName + ",</h2>" +
            "            <p>Great news! Your Dwellia trial has been extended by <strong>" + additionalMonths + " month(s)</strong>.</p>" +
            "            <p>Continue enjoying all premium features until " + endDate + ".</p>" +
            "            <a href=\"https://dwellia.rs\" class=\"button\">Continue Exploring</a>" +
            "        </div>" +
            "    </div>" +
            "</body>" +
            "</html>";
    }
    
    private String createTeamInvitationHtml(String userName, String agencyName, String inviterName, 
                                           String role, String token) {
        String invitationLink = "https://yourdomain.com/accept-invitation?token=" + token;
        
        return """
            <!DOCTYPE html>
            <html>
            <head>
                <style>
                    body { font-family: Arial, sans-serif; line-height: 1.6; color: #333; }
                    .container { max-width: 600px; margin: 0 auto; padding: 20px; }
                    .header { background-color: #4CAF50; color: white; padding: 20px; text-align: center; }
                    .content { padding: 30px; background-color: #f9f9f9; }
                    .button { display: inline-block; padding: 12px 24px; background-color: #4CAF50; 
                             color: white; text-decoration: none; border-radius: 4px; margin: 20px 0; }
                    .footer { margin-top: 30px; padding-top: 20px; border-top: 1px solid #ddd; 
                             font-size: 12px; color: #666; }
                </style>
            </head>
            <body>
                <div class="container">
                    <div class="header">
                        <h1>Pozivnica za tim</h1>
                    </div>
                    <div class="content">
                        <h2>Pozdrav %s,</h2>
                        <p><strong>%s</strong> vas poziva da se pridružite timu agencije <strong>%s</strong>.</p>
                        <p>Pozicija: <strong>%s</strong></p>
                        <p>Pozivnica važi 7 dana.</p>
                        <a href="%s" class="button">Prihvati pozivnicu</a>
                        <p>Ili kopirajte ovaj link u pretraživač: %s</p>
                        <p>Ako niste zainteresovani, ignorišite ovaj email.</p>
                    </div>
                    <div class="footer">
                        <p>Ovo je automatski generisan email. Molimo ne odgovarajte na njega.</p>
                        <p>&copy; 2024 Real Estate Platform. Sva prava zadržana.</p>
                    </div>
                </div>
            </body>
            </html>
            """.formatted(userName, inviterName, agencyName, role, invitationLink, invitationLink);
    }

    private String createInvitationAcceptedHtml(String inviterName, String newMemberName, 
                                              String agencyName, String role) {
        return """
            <!DOCTYPE html>
            <html>
            <head>
                <style>
                    body { font-family: Arial, sans-serif; line-height: 1.6; color: #333; }
                    .container { max-width: 600px; margin: 0 auto; padding: 20px; }
                    .header { background-color: #4CAF50; color: white; padding: 20px; text-align: center; }
                    .content { padding: 30px; background-color: #f9f9f9; }
                    .footer { margin-top: 30px; padding-top: 20px; border-top: 1px solid #ddd; 
                             font-size: 12px; color: #666; }
                </style>
            </head>
            <body>
                <div class="container">
                    <div class="header">
                        <h1>Pozivnica prihvaćena</h1>
                    </div>
                    <div class="content">
                        <h2>Poštovani/poštovana %s,</h2>
                        <p><strong>%s</strong> je prihvatio/la vašu pozivnicu i sada je deo tima agencije <strong>%s</strong>.</p>
                        <p>Pozicija: <strong>%s</strong></p>
                        <p>Novi član tima je sada aktivan i može početi sa radom.</p>
                        <p>Pozdrav,<br>Real Estate Platform Team</p>
                    </div>
                    <div class="footer">
                        <p>Ovo je automatski generisan email. Molimo ne odgovarajte na njega.</p>
                        <p>&copy; 2024 Real Estate Platform. Sva prava zadržana.</p>
                    </div>
                </div>
            </body>
            </html>
            """.formatted(inviterName, newMemberName, agencyName, role);
    }

    private String createInvitationRejectedHtml(String inviterName, String rejecterName, String agencyName) {
        return """
            <!DOCTYPE html>
            <html>
            <head>
                <style>
                    body { font-family: Arial, sans-serif; line-height: 1.6; color: #333; }
                    .container { max-width: 600px; margin: 0 auto; padding: 20px; }
                    .header { background-color: #f44336; color: white; padding: 20px; text-align: center; }
                    .content { padding: 30px; background-color: #f9f9f9; }
                    .footer { margin-top: 30px; padding-top: 20px; border-top: 1px solid #ddd; 
                             font-size: 12px; color: #666; }
                </style>
            </head>
            <body>
                <div class="container">
                    <div class="header">
                        <h1>Pozivnica odbijena</h1>
                    </div>
                    <div class="content">
                        <h2>Poštovani/poštovana %s,</h2>
                        <p><strong>%s</strong> je odbio/la vašu pozivnicu za pridruživanje timu agencije <strong>%s</strong>.</p>
                        <p>Možete poslati novu pozivnicu drugom kandidatu ili kontaktirati ovu osobu direktno za više informacija.</p>
                        <p>Pozdrav,<br>Real Estate Platform Team</p>
                    </div>
                    <div class="footer">
                        <p>Ovo je automatski generisan email. Molimo ne odgovarajte na njega.</p>
                        <p>&copy; 2024 Real Estate Platform. Sva prava zadržana.</p>
                    </div>
                </div>
            </body>
            </html>
            """.formatted(inviterName, rejecterName, agencyName);
    }

    // PLAIN TEXT TEMPLATES
    private String createWelcomeEmailText(String userName) {
        return "Poštovani/poštovana " + userName + ",\n\nDobrodošli na Real Estate Platform!\n\nVaš nalog je uspešno kreiran. Sada možete pretraživati nekretnine, kontaktirati agente i sačuvati omiljene oglase.\n\nAko imate pitanja, kontaktirajte nas: " + supportEmail + "\n\nS poštovanjem,\nReal Estate Platform Team";
    }

    private String createPasswordResetText(String userName, String resetToken) {
        return "Poštovani/poštovana " + userName + ",\n\nPrimili smo zahtev za resetovanje vaše lozinke.\n\nToken za resetovanje: " + resetToken + "\n\nUkoliko niste Vi zatražili resetovanje, ignorišite ovaj email.\n\nS poštovanjem,\nReal Estate Platform Team\n" + supportEmail;
    }

    private String createVerificationSubmittedText(String userName, String documentType) {
        return "Poštovani/poštovana " + userName + ",\n\nUspešno ste podneli dokument za verifikaciju: " + documentType + ".\n\nNaš tim će pregledati vašu prijavu u najkraćem mogućem roku.\n\nBićete obavešteni o statusu verifikacije.\n\nS poštovanjem,\nReal Estate Platform Team";
    }

    private String createVerificationApprovedText(String userName, String role) {
        return "Poštovani/poštovana " + userName + ",\n\nČestitamo! Vaša verifikacija je odobrena.\n\nSada imate " + role + " status na platformi.\n\nMožete početi da koristite sve privilegije vašeg novog statusa.\n\nS poštovanjem,\nReal Estate Platform Team";
    }

    private String createVerificationRejectedText(String userName, String rejectionReason) {
        return "Poštovani/poštovana " + userName + ",\n\nNažalost, vaša verifikacija je odbijena.\n\nRazlog: " + rejectionReason + "\n\nMožete podneti novu prijavu sa ispravljenim dokumentima.\n\nAko imate pitanja, kontaktirajte nas: " + supportEmail + "\n\nS poštovanjem,\nReal Estate Platform Team";
    }

    private String createLicenseExpiringText(String userName, LocalDate expiryDate) {
        String formattedDate = expiryDate.format(DateTimeFormatter.ofPattern("dd.MM.yyyy"));
        return "Poštovani/poštovana " + userName + ",\n\nVaša licenca ističe: " + formattedDate + "\n\nObnovite licencu na vreme kako biste nastavili da koristite sve funkcije platforme.\n\nS poštovanjem,\nReal Estate Platform Team\n" + supportEmail;
    }

    private String createNewVerificationAdminText(String userName, String documentType) {
        return "Administrativni pregled\n\nKorisnik " + userName + " je podneo novu verifikaciju.\n\nTip dokumenta: " + documentType + "\n\nPregledajte verifikacije: https://dwellia.rs/admin/verifications\n\nReal Estate Platform Admin";
    }

    private String createTrialStartedText(String userName, int trialMonths) {
        String endDate = LocalDate.now().plusMonths(trialMonths).format(DateTimeFormatter.ofPattern("dd.MM.yyyy"));
        return "Poštovani/poštovana " + userName + ",\n\nDobrodošli na Real Estate Platform! Vaš " + trialMonths + "-mesečni probni period je sada aktivan.\n\nTokom probnog perioda imate pristup svim premium funkcijama.\n\nProbni period traje do: " + endDate + "\n\nAko imate pitanja, kontaktirajte nas: " + supportEmail + "\n\nS poštovanjem,\nReal Estate Platform Team";
    }

    private String createTrialExpiringText(String userName, int daysRemaining) {
        return "Poštovani/poštovana " + userName + ",\n\nŽelimo da Vas podsetimo da Vaš probni period ističe za " + daysRemaining + " dan(a).\n\nNakon isteka probnog perioda, i dalje ćete imati pristup osnovnim funkcijama platforme.\n\nAko želite da nastavite sa premium funkcijama, kontaktirajte nas.\n\nS poštovanjem,\nReal Estate Platform Team\n" + supportEmail;
    }

//    private String createTrialExpiredText(String userName, UserTier newTier) {
//        String tierName = getTierDisplayName(newTier);
//        return "Poštovani/poštovana " + userName + ",\n\n" +
//               "Vaš probni period je istekao. Sada imate pristup " + tierName + " paketu.\n\n" +
//               "Osnovne funkcije su i dalje dostupne. Za povratak premium funkcija, kontaktirajte nas.\n\n" +
//               "S poštovanjem,\nReal Estate Platform Team\n" + supportEmail;
//    }

    private String createTrialExtendedText(String userName, int additionalMonths) {
        String endDate = LocalDate.now().plusMonths(additionalMonths).format(DateTimeFormatter.ofPattern("dd.MM.yyyy"));
        return "Poštovani/poštovana " + userName + ",\n\nVaš probni period je produžen za " + additionalMonths + " meseci.\n\nSada možete nastaviti da uživate u svim premium funkcijama do " + endDate + ".\n\nHvala Vam što koristite našu platformu!\n\nS poštovanjem,\nReal Estate Platform Team\n" + supportEmail;
    }

//    private String getTierDisplayName(UserTier tier) {
//        if (tier == null) {
//            return "Osnovni"; // Default fallback
//        }
//        
//        switch (tier) {
//            case FREE_USER: return "Besplatni";
//            case BASIC_USER: return "Osnovni";
//            case PREMIUM_USER: return "Premium";
//            case AGENCY_BASIC: return "Osnovna Agencija";
//            case AGENCY_PREMIUM: return "Premium Agencija";
//            case FREE_INVESTOR: return "Besplatni Investitor";
//            case BASIC_INVESTOR: return "Osnovni Investitor";
//            case PREMIUM_INVESTOR: return "Premium Investitor";
//            case ADMIN: return "Administrator";
//            default: return "Osnovni";
//        }
//    }
    
    
    
    
    
 // Team invitation email
    public void sendTeamInvitationEmail(String userEmail, String userName, String agencyName, 
                                       String inviterName, String role, String token) {
        String subject = "Pozivnica za pridruživanje timu - " + agencyName;
        String htmlContent = createTeamInvitationHtml(userName, agencyName, inviterName, role, token);
        String textContent = createTeamInvitationText(userName, agencyName, inviterName, role, token);
        sendEmail(userEmail, subject, htmlContent, textContent);
    }

    // Invitation accepted email (to inviter)
    public void sendInvitationAcceptedEmail(String inviterEmail, String inviterName, 
                                           String newMemberName, String agencyName, String role) {
        String subject = "Pozivnica prihvaćena - " + newMemberName + " se pridružio/la timu";
        String htmlContent = createInvitationAcceptedHtml(inviterName, newMemberName, agencyName, role);
        String textContent = createInvitationAcceptedText(inviterName, newMemberName, agencyName, role);
        sendEmail(inviterEmail, subject, htmlContent, textContent);
    }

    // Invitation rejected email (to inviter)
    public void sendInvitationRejectedEmail(String inviterEmail, String inviterName, 
                                           String rejecterName, String agencyName) {
        String subject = "Pozivnica odbijena - " + rejecterName;
        String htmlContent = createInvitationRejectedHtml(inviterName, rejecterName, agencyName);
        String textContent = createInvitationRejectedText(inviterName, rejecterName, agencyName);
        sendEmail(inviterEmail, subject, htmlContent, textContent);
    }

    

    // Plain text templates (similar structure but plain text)
    private String createTeamInvitationText(String userName, String agencyName, String inviterName, 
                                           String role, String token) {
        String invitationLink = "https://yourdomain.com/accept-invitation?token=" + token;
        
        return """
            Pozdrav %s,
            
            %s vas poziva da se pridružite timu agencije %s.
            
            Pozicija: %s
            
            Pozivnica važi 7 dana.
            
            Prihvatite pozivnicu ovde: %s
            
            Ako niste zainteresovani, ignorišite ovaj email.
            
            Pozdrav,
            Real Estate Platform Team
            """.formatted(userName, inviterName, agencyName, role, invitationLink);
    }

    private String createInvitationAcceptedText(String inviterName, String newMemberName, 
                                              String agencyName, String role) {
        return """
            Poštovani/poštovana %s,
            
            %s je prihvatio/la vašu pozivnicu i sada je deo tima agencije %s.
            
            Pozicija: %s
            
            Novi član tima je sada aktivan i može početi sa radom.
            
            Pozdrav,
            Real Estate Platform Team
            """.formatted(inviterName, newMemberName, agencyName, role);
    }

    private String createInvitationRejectedText(String inviterName, String rejecterName, String agencyName) {
        return """
            Poštovani/poštovana %s,
            
            %s je odbio/la vašu pozivnicu za pridruživanje timu agencije %s.
            
            Možete poslati novu pozivnicu drugom kandidatu ili kontaktirati ovu osobu direktno za više informacija.
            
            Pozdrav,
            Real Estate Platform Team
            """.formatted(inviterName, rejecterName, agencyName);
    }
}
