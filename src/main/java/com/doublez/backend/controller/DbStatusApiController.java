package com.doublez.backend.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
	
	private static final Logger logger = LoggerFactory.getLogger(DbStatusApiController.class);
	
	@GetMapping("/db-status")
	public ResponseEntity<String> checkDatabase() {
		try {
			jdbcTemplate.queryForObject("SELECT 1", Integer.class);
			return ResponseEntity.ok("Database is connected!");
		} catch (Exception e) {
			logger.error("Database connection failed!", e);
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Database connection failed!");
		}
	}

}
