package com.doublez.backend.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.doublez.backend.entity.RealEstate;
import com.doublez.backend.repository.TestRepository;

@RestController
public class TestController {
	
	private final TestRepository repository;
	
	public TestController(TestRepository repository) {
		super();
		this.repository = repository;
	}

	@GetMapping("/test")
	public Iterable<RealEstate> testEndpoint() {
		return this.repository.findAll();
	}

	@PostMapping("/add")
	public ResponseEntity<RealEstate> createRealEstate(@RequestBody RealEstate realEstate) {
		RealEstate savedRealEstate = repository.save(realEstate);
		return ResponseEntity.status(HttpStatus.CREATED).body(savedRealEstate);
	}
}
