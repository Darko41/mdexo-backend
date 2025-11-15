package com.doublez.backend.controller;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.doublez.backend.enums.UserTier;
import com.doublez.backend.service.email.ResendEmailService;

@RestController
@RequestMapping("/api/debug")
public class ResendDebugController {
    
    private final ResendEmailService resendEmailService;
    
    private static final Logger logger = LoggerFactory.getLogger(ResendDebugController.class);
    
    @Value("${resend.api-key:not-set}")
    private String resendApiKey;
    
    @Value("${app.email.from-address:not-set}")
    private String fromAddress;
    
    @Value("${app.email.enabled:true}")
    private boolean emailEnabled;

    public ResendDebugController(ResendEmailService resendEmailService) {
        this.resendEmailService = resendEmailService;
        logger.info("✅ ResendDebugController initialized and registered!");
    }
    
    @GetMapping("/resend-config")
    public ResponseEntity<Map<String, Object>> getResendConfig() {
        Map<String, Object> config = new HashMap<>();
        config.put("resend_api_key_set", !resendApiKey.equals("not-set"));
        config.put("resend_api_key_length", resendApiKey.length());
        config.put("from_address", fromAddress);
        config.put("email_enabled", emailEnabled);
        config.put("api_key_prefix", resendApiKey.length() > 4 ? resendApiKey.substring(0, 4) + "..." : "none");
        config.put("environment", getEnvironment());
        
        return ResponseEntity.ok(config);
    }
    
    @PostMapping("/test-resend-email")
    public ResponseEntity<Map<String, Object>> testResendEmail(
            @RequestParam String toEmail,
            @RequestParam(defaultValue = "Test User") String userName,
            @RequestParam(defaultValue = "6") int trialMonths) {
        
        Map<String, Object> response = new HashMap<>();
        response.put("timestamp", LocalDateTime.now().toString());
        response.put("to_email", toEmail);
        response.put("user_name", userName);
        response.put("trial_months", trialMonths);
        
        try {
            // Validate email format
            if (!isValidEmail(toEmail)) {
                response.put("status", "ERROR");
                response.put("message", "Invalid email format");
                return ResponseEntity.badRequest().body(response);
            }
            
            resendEmailService.sendTrialStartedEmail(toEmail, userName, trialMonths);
            
            response.put("status", "SUCCESS");
            response.put("message", "Resend test email sent successfully");
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            response.put("status", "ERROR");
            response.put("message", "Failed to send Resend email: " + e.getMessage());
            response.put("error_details", getErrorDetails(e));
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
    
    @PostMapping("/test-resend-all-types")
    public ResponseEntity<Map<String, Object>> testAllResendEmails(@RequestParam String toEmail) {
        Map<String, Object> results = new HashMap<>();
        results.put("timestamp", LocalDateTime.now().toString());
        results.put("test_email", toEmail);
        
        if (!isValidEmail(toEmail)) {
            results.put("overall_status", "ERROR");
            results.put("error", "Invalid email format");
            return ResponseEntity.badRequest().body(results);
        }

        // Test all email types with delays to avoid rate limiting
        Map<String, Map<String, String>> emailResults = new LinkedHashMap<>();
        
        emailResults.put("trial_started", testEmailSending(() -> 
            resendEmailService.sendTrialStartedEmail(toEmail, "Test User", 6)));
        
        // Add 1 second delay between emails
        addDelay(1000);
        emailResults.put("trial_expiring", testEmailSending(() -> 
            resendEmailService.sendTrialExpiringEmail(toEmail, "Test User", 7)));
        
        addDelay(1000);
        emailResults.put("trial_expired", testEmailSending(() -> 
            resendEmailService.sendTrialExpiredEmail(toEmail, "Test User", UserTier.BASIC_USER)));
        
        addDelay(1000);
        emailResults.put("trial_extended", testEmailSending(() -> 
            resendEmailService.sendTrialExtendedEmail(toEmail, "Test User", 3)));
        
        addDelay(1000);
        emailResults.put("welcome_email", testEmailSending(() -> 
            resendEmailService.sendWelcomeEmail(toEmail, "Test User")));
        
        addDelay(1000);
        emailResults.put("password_reset", testEmailSending(() -> 
            resendEmailService.sendPasswordResetEmail(toEmail, "Test User", "TEST-TOKEN-12345")));

        results.put("email_results", emailResults);
        
        // Calculate overall status
        boolean allSuccessful = emailResults.values().stream()
            .allMatch(result -> "SUCCESS".equals(result.get("status")));
        results.put("overall_status", allSuccessful ? "SUCCESS" : "PARTIAL");
        
        return ResponseEntity.ok(results);
    }

    // Helper method to add delays
    private void addDelay(long milliseconds) {
        try {
            Thread.sleep(milliseconds);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            logger.warn("Delay interrupted", e);
        }
    }
    
    @PostMapping("/test-basic-email")
    public ResponseEntity<Map<String, Object>> testBasicEmail(
            @RequestParam String toEmail,
            @RequestParam String subject,
            @RequestParam String message) {
        
        Map<String, Object> response = new HashMap<>();
        response.put("timestamp", LocalDateTime.now().toString());
        response.put("to_email", toEmail);
        response.put("subject", subject);
        
        try {
            resendEmailService.sendTextEmail(toEmail, subject, message);
            
            response.put("status", "SUCCESS");
            response.put("message", "Basic email sent successfully");
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            response.put("status", "ERROR");
            response.put("message", "Failed to send basic email: " + e.getMessage());
            response.put("error_details", getErrorDetails(e));
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
    
    @GetMapping("/health")
    public ResponseEntity<Map<String, Object>> healthCheck() {
        Map<String, Object> health = new HashMap<>();
        health.put("status", "UP");
        health.put("timestamp", LocalDateTime.now().toString());
        health.put("service", "Resend Email Service");
        health.put("email_enabled", emailEnabled);
        health.put("resend_configured", !resendApiKey.equals("not-set"));
        
        return ResponseEntity.ok(health);
    }
    
    // Helper methods
    private Map<String, String> testEmailSending(Runnable emailMethod) {
        Map<String, String> result = new HashMap<>();
        try {
            emailMethod.run();
            result.put("status", "SUCCESS");
            result.put("message", "Email sent successfully");
        } catch (Exception e) {
            result.put("status", "ERROR");
            result.put("message", e.getMessage());
            result.put("error_type", e.getClass().getSimpleName());
        }
        return result;
    }
    
    private boolean isValidEmail(String email) {
        return email != null && email.matches("^[A-Za-z0-9+_.-]+@(.+)$");
    }
    
    private Map<String, String> getErrorDetails(Exception e) {
        Map<String, String> details = new HashMap<>();
        details.put("exception_type", e.getClass().getName());
        details.put("exception_message", e.getMessage());
        if (e.getCause() != null) {
            details.put("root_cause", e.getCause().getMessage());
        }
        return details;
    }
    
    private String getEnvironment() {
        String profile = System.getenv("SPRING_PROFILES_ACTIVE");
        if (profile == null || profile.isEmpty()) {
            profile = "default";
        }
        return profile;
    }
}