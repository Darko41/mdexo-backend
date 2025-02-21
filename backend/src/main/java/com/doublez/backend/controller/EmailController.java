package com.doublez.backend.controller;

import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.doublez.backend.dto.EmailForm;

/* 	todo
 	Since we’ll likely be running the backend and frontend on different ports during development
	(Spring Boot on localhost:8080 and React on localhost:3000), we need to make sure to handle CORS properly.
	We can enable CORS in Spring Boot for our /api/email endpoint like this:
 	@CrossOrigin(origins = "http://localhost:3000")  // Replace with frontend's URL*/
@RestController
@RequestMapping("/api/email")
public class EmailController {
	
	@Autowired
	private JavaMailSender javaMailSender;

	@PostMapping("send-email")
	public ResponseEntity<Map<String, String>> sendEmail(@RequestBody EmailForm emailForm) {
		SimpleMailMessage message = new SimpleMailMessage();
		message.setFrom("darkok16@gmail.com");
		message.setTo(emailForm.getTo());
		message.setSubject(emailForm.getSubject());
		message.setText(emailForm.getMessage());
		
		try {
			javaMailSender.send(message);
			Map<String, String> response = new HashMap<>();
			response.put("status", "success");
			response.put("message", "Email sent successfully!");
			return ResponseEntity.ok(response);
		} catch (Exception e) {
			Map<String, String> response = new HashMap<>();
			response.put("status", "error");
			response.put("message", "Failed to send email: " + e.getMessage());
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
		}
	
	}
}
