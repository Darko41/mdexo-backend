package com.doublez.backend.controller;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.GetMapping;

public class DbController {
    
    @Autowired
    private JdbcTemplate jdbcTemplate;

    @GetMapping("/schema-check")
    public ResponseEntity<?> checkSchema() {
        try {
            List<String> tables = jdbcTemplate.queryForList(
                "SELECT table_name FROM information_schema.tables WHERE table_schema='public'", 
                String.class
            );
            return ResponseEntity.ok(Map.of(
                "status", "OK",
                "tables", tables
            ));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(e.getMessage());
        }
    }

}
