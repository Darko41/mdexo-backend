package com.doublez.backend.controller.db;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class DbStatusApiController {
	
	@Autowired
	private JdbcTemplate jdbcTemplate;
	
	@GetMapping("/db-status")
	public ResponseEntity<String> checkDatabase() {
		try {
			jdbcTemplate.queryForObject("SELECT 1", Integer.class);
			return ResponseEntity.ok("Database is connected!");
		} catch (Exception e) {
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Database connection failed!");
		}
	}

}
