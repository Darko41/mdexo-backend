package com.doublez.backend.service.email;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.doublez.backend.config.security.JwtAuthenticationFilter;
import com.doublez.backend.enums.UserTier;
import com.resend.Resend;
import com.resend.core.exception.ResendException;
import com.resend.services.emails.model.CreateEmailOptions;
import com.resend.services.emails.model.CreateEmailResponse;

@Service
public class ResendEmailService {
    
    private static final Logger logger = LoggerFactory.getLogger(ResendEmailService.class);
    
    private final Resend resend;
    private final String fromAddress;
    
    public ResendEmailService(@Value("${resend.api-key}") String apiKey,
                            @Value("${app.email.from-address:noreply@iterials.com}") String fromAddress) {
        this.resend = new Resend(apiKey);
        this.fromAddress = fromAddress;
    }
    
    public void sendTrialStartedEmail(String toEmail, String userName, int trialMonths) {
        try {
            String subject = "🎉 Welcome to Dwellia - Your Trial Has Started!";
            String htmlContent = createTrialEmailHtml(userName, trialMonths);
            
            CreateEmailOptions params = CreateEmailOptions.builder()
                .from("Dwellia <" + fromAddress + ">")
                .to(toEmail)
                .subject(subject)
                .html(htmlContent)
                .build();
            
            CreateEmailResponse data = resend.emails().send(params);
            logger.info("✅ Resend trial email sent successfully to: {} with ID: {}", toEmail, data.getId());
            
        } catch (ResendException e) {
            logger.error("❌ Resend API error for {}: {}", toEmail, e.getMessage());
            throw new RuntimeException("Email sending failed: " + e.getMessage(), e);
        } catch (Exception e) {
            logger.error("❌ Failed to send Resend email to {}: {}", toEmail, e.getMessage());
            throw new RuntimeException("Email sending failed", e);
        }
    }
    
    public void sendTrialExpiringEmail(String toEmail, String userName, int daysRemaining) {
        try {
            String subject = "⏰ Your Dwellia Trial Expires in " + daysRemaining + " Day(s)";
            String htmlContent = createTrialExpiringHtml(userName, daysRemaining);
            
            CreateEmailOptions params = CreateEmailOptions.builder()
                .from("Dwellia <" + fromAddress + ">")
                .to(toEmail)
                .subject(subject)
                .html(htmlContent)
                .build();
            
            resend.emails().send(params);
            logger.info("✅ Resend trial expiring email sent to: {}", toEmail);
            
        } catch (Exception e) {
            logger.error("❌ Failed to send trial expiring email to {}: {}", toEmail, e.getMessage());
            throw new RuntimeException("Email sending failed", e);
        }
    }
    
    public void sendTrialExpiredEmail(String toEmail, String userName, UserTier newTier) {
        try {
            String subject = "📊 Your Dwellia Trial Has Ended";
            String htmlContent = createTrialExpiredHtml(userName, newTier);
            
            CreateEmailOptions params = CreateEmailOptions.builder()
                .from("Dwellia <" + fromAddress + ">")
                .to(toEmail)
                .subject(subject)
                .html(htmlContent)
                .build();
            
            resend.emails().send(params);
            logger.info("✅ Resend trial expired email sent to: {}", toEmail);
            
        } catch (Exception e) {
            logger.error("❌ Failed to send trial expired email to {}: {}", toEmail, e.getMessage());
            throw new RuntimeException("Email sending failed", e);
        }
    }
    
    public void sendTrialExtendedEmail(String toEmail, String userName, int additionalMonths) {
        try {
            String subject = "🎁 Your Dwellia Trial Has Been Extended!";
            String htmlContent = createTrialExtendedHtml(userName, additionalMonths);
            
            CreateEmailOptions params = CreateEmailOptions.builder()
                .from("Dwellia <" + fromAddress + ">")
                .to(toEmail)
                .subject(subject)
                .html(htmlContent)
                .build();
            
            resend.emails().send(params);
            logger.info("✅ Resend trial extended email sent to: {}", toEmail);
            
        } catch (Exception e) {
            logger.error("❌ Failed to send trial extended email to {}: {}", toEmail, e.getMessage());
            throw new RuntimeException("Email sending failed", e);
        }
    }
    
    // HTML Email Templates - FIXED SYNTAX
    private String createTrialEmailHtml(String userName, int trialMonths) {
        String endDate = LocalDate.now().plusMonths(trialMonths)
            .format(DateTimeFormatter.ofPattern("MMMM dd, yyyy"));
            
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
            "            <h1>🎉 Welcome to Dwellia!</h1>" +
            "            <p>Your " + trialMonths + "-Month Free Trial Is Active</p>" +
            "        </div>" +
            "        <div class=\"content\">" +
            "            <h2>Hello " + userName + ",</h2>" +
            "            <p>We're excited to have you on board! Your premium trial includes:</p>" +
            "            " +
            "            <div class=\"feature\">" +
            "                <strong>🏠 Unlimited Property Listings</strong>" +
            "                <p>Browse and save unlimited properties</p>" +
            "            </div>" +
            "            " +
            "            <div class=\"feature\">" +
            "                <strong>🔍 Advanced Search Filters</strong>" +
            "                <p>Find exactly what you're looking for</p>" +
            "            </div>" +
            "            " +
            "            <div class=\"feature\">" +
            "                <strong>📱 Direct Owner Contact</strong>" +
            "                <p>Connect directly with property owners</p>" +
            "            </div>" +
            "            " +
            "            <p><strong>📅 Trial End Date:</strong> " + endDate + "</p>" +
            "            " +
            "            <a href=\"https://dwellia.rs\" class=\"button\">Start Exploring Properties</a>" +
            "            " +
            "            <p>Happy house hunting!<br>The Dwellia Team</p>" +
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
            "            <p>" + daysRemaining + " day(s) remaining</p>" +
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
    
    private String createTrialExpiredHtml(String userName, UserTier newTier) {
        String tierName = getTierDisplayName(newTier);
        return "<!DOCTYPE html>" +
            "<html>" +
            "<head>" +
            "    <style>" +
            "        body { font-family: Arial, sans-serif; line-height: 1.6; color: #333; }" +
            "        .container { max-width: 600px; margin: 0 auto; padding: 20px; }" +
            "        .header { background: linear-gradient(135deg, #4facfe 0%, #00f2fe 100%); color: white; padding: 30px; text-align: center; border-radius: 10px 10px 0 0; }" +
            "        .content { padding: 30px; background: #f8f9fa; border-radius: 0 0 10px 10px; }" +
            "        .button { background: #4facfe; color: white; padding: 12px 30px; text-decoration: none; border-radius: 5px; display: inline-block; margin: 20px 0; }" +
            "    </style>" +
            "</head>" +
            "<body>" +
            "    <div class=\"container\">" +
            "        <div class=\"header\">" +
            "            <h1>📊 Trial Completed</h1>" +
            "            <p>Welcome to " + tierName + " Plan</p>" +
            "        </div>" +
            "        <div class=\"content\">" +
            "            <h2>Hello " + userName + ",</h2>" +
            "            <p>Your trial period has ended. You now have access to the <strong>" + tierName + "</strong> plan.</p>" +
            "            <p>Basic features remain available. Upgrade to continue enjoying premium benefits.</p>" +
            "            <a href=\"https://dwellia.rs/pricing\" class=\"button\">Upgrade Your Plan</a>" +
            "        </div>" +
            "    </div>" +
            "</body>" +
            "</html>";
    }
    
    private String createTrialExtendedHtml(String userName, int additionalMonths) {
        String endDate = LocalDate.now().plusMonths(additionalMonths)
            .format(DateTimeFormatter.ofPattern("MMMM dd, yyyy"));
            
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
    
    private String getTierDisplayName(UserTier tier) {
        switch (tier) {
            case BASIC_USER: return "Basic";
            case PREMIUM_USER: return "Premium";
            case BASIC_AGENT: return "Basic Agent";
            case PREMIUM_AGENT: return "Premium Agent";
            case BASIC_INVESTOR: return "Basic Investor";
            case PREMIUM_INVESTOR: return "Premium Investor";
            default: return "Free";
        }
    }
}