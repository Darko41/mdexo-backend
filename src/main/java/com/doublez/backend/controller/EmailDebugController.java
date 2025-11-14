package com.doublez.backend.controller;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/debug")
public class EmailDebugController {
    
    private final JavaMailSender mailSender;
    
    @Value("${spring.mail.host:not-set}")
    private String smtpHost;
    
    @Value("${spring.mail.port:0}")
    private int smtpPort;
    
    @Value("${spring.mail.username:not-set}")
    private String smtpUsername;
    
    @Value("${spring.mail.password:not-set}")
    private String smtpPassword;

    public EmailDebugController(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }
    
    @GetMapping("/email-config")
    public ResponseEntity<Map<String, String>> getEmailConfig() {
        Map<String, String> config = new HashMap<>();
        config.put("host", smtpHost);
        config.put("port", String.valueOf(smtpPort));
        config.put("username", smtpUsername);
        config.put("password_set", smtpPassword.equals("not-set") ? "NO" : "YES");
        config.put("password_length", String.valueOf(smtpPassword.length()));
        
        return ResponseEntity.ok(config);
    }
    
    @PostMapping("/test-smtp-connection")
    public ResponseEntity<Map<String, String>> testSmtpConnection() {
        Map<String, String> response = new HashMap<>();
        
        try {
            JavaMailSenderImpl mailSenderImpl = (JavaMailSenderImpl) mailSender;
            mailSenderImpl.testConnection();
            
            response.put("status", "SUCCESS");
            response.put("message", "SMTP connection successful to " + smtpHost + ":" + smtpPort);
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            response.put("status", "ERROR");
            response.put("message", "SMTP connection failed: " + e.getMessage());
            response.put("host", smtpHost);
            response.put("port", String.valueOf(smtpPort));
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
    
    @PostMapping("/send-test-email")
    public ResponseEntity<Map<String, String>> sendTestEmail(@RequestParam String toEmail) {
        Map<String, String> response = new HashMap<>();
        
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(toEmail);
            message.setSubject("Test Email from Your Application");
            message.setText("This is a test email to verify your SMTP configuration is working correctly.\n\n" +
                          "If you receive this, your email configuration is working!");
            message.setFrom(smtpUsername);
            
            mailSender.send(message);
            
            response.put("status", "SUCCESS");
            response.put("message", "Test email sent successfully to " + toEmail);
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            response.put("status", "ERROR");
            response.put("message", "Failed to send test email: " + e.getMessage());
            response.put("from", smtpUsername);
            response.put("to", toEmail);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
    
    @PostMapping("/test-different-configs")
    public ResponseEntity<Map<String, Object>> testDifferentConfigs(@RequestParam String toEmail) {
        Map<String, Object> results = new HashMap<>();
        
        // Test different SMTP configurations
        String[][] configs = {
            // {host, port, ssl, starttls, description}
            {"pro.turbo-smtp.com", "465", "true", "false", "Business Email SSL"},
            {"pro.turbo-smtp.com", "587", "false", "true", "Business Email STARTTLS"},
            {"smtpout.secureserver.net", "465", "true", "false", "Workspace Email SSL"},
            {"smtpout.secureserver.net", "587", "false", "true", "Workspace Email STARTTLS"},
            {"smtp.office365.com", "587", "false", "true", "Office 365"}
        };
        
        for (String[] config : configs) {
            String host = config[0];
            int port = Integer.parseInt(config[1]);
            boolean ssl = Boolean.parseBoolean(config[2]);
            boolean starttls = Boolean.parseBoolean(config[3]);
            String description = config[4];
            
            Map<String, String> testResult = testSpecificConfig(host, port, ssl, starttls, toEmail);
            results.put(description, testResult);
        }
        
        return ResponseEntity.ok(results);
    }
    
    private Map<String, String> testSpecificConfig(String host, int port, boolean ssl, boolean starttls, String toEmail) {
        Map<String, String> result = new HashMap<>();
        
        try {
            JavaMailSenderImpl testMailSender = new JavaMailSenderImpl();
            testMailSender.setHost(host);
            testMailSender.setPort(port);
            testMailSender.setUsername(smtpUsername);
            testMailSender.setPassword(smtpPassword);
            
            Properties props = new Properties();
            props.put("mail.smtp.auth", "true");
            props.put("mail.smtp.ssl.enable", String.valueOf(ssl));
            props.put("mail.smtp.starttls.enable", String.valueOf(starttls));
            props.put("mail.smtp.connectiontimeout", "5000");
            props.put("mail.smtp.timeout", "5000");
            
            testMailSender.setJavaMailProperties(props);
            
            // Test connection
            testMailSender.testConnection();
            result.put("connection", "SUCCESS");
            
            // Try to send email
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(toEmail);
            message.setSubject("Test: " + host);
            message.setText("Testing configuration: " + host + ":" + port);
            message.setFrom(smtpUsername);
            
            testMailSender.send(message);
            result.put("email", "SUCCESS");
            result.put("message", "Working configuration!");
            
        } catch (Exception e) {
            result.put("connection", "FAILED");
            result.put("email", "FAILED");
            result.put("error", e.getMessage());
        }
        
        return result;
    }
}