package com.doublez.backend.controller.usage;

import java.util.Collections;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.doublez.backend.dto.FeaturedUsageStatsDTO;
import com.doublez.backend.dto.user.UsageStatsDTO;
import com.doublez.backend.repository.UserRepository;
import com.doublez.backend.service.user.LimitationService;
import com.doublez.backend.service.user.UserService;

//controller/UsageController.java
@RestController
@RequestMapping("/api/usage")
//@CrossOrigin(origins = "http://localhost:5173")
public class UsageController {

	private final LimitationService limitationService;
	private final UserService userService;
	private final UserRepository userRepository;

	public UsageController(LimitationService limitationService, UserService userService,
			UserRepository userRepository) {
		this.limitationService = limitationService;
		this.userService = userService;
		this.userRepository = userRepository;
	}

	private Long getCurrentUserId() {
		return userService.getCurrentUserId();
	}

	@GetMapping("/stats")
	public ResponseEntity<UsageStatsDTO> getCurrentUserUsageStats() {
		try {
			Long userId = getCurrentUserId();
			UsageStatsDTO stats = limitationService.getUsageStats(userId);
			return ResponseEntity.ok(stats);
		} catch (Exception e) {
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
		}
	}

	@GetMapping("/can-create-realestate")
	public ResponseEntity<Map<String, Boolean>> canCreateRealEstate() {
		try {
			Long userId = getCurrentUserId();
			boolean canCreate = limitationService.canCreateRealEstate(userId);
			return ResponseEntity.ok(Collections.singletonMap("canCreate", canCreate));
		} catch (Exception e) {
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
		}
	}

	@GetMapping("/can-upload-image")
	public ResponseEntity<Map<String, Boolean>> canUploadImage() {
		try {
			Long userId = getCurrentUserId();
			boolean canUpload = limitationService.canUploadImage(userId);
			return ResponseEntity.ok(Collections.singletonMap("canUpload", canUpload));
		} catch (Exception e) {
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
		}
	}

	// Featured listing usage endpoint
	@GetMapping("/can-feature-listing")
	public ResponseEntity<Map<String, Boolean>> canFeatureListing() {
		try {
			Long userId = getCurrentUserId();
			boolean canFeature = limitationService.canFeatureRealEstate(userId);
			return ResponseEntity.ok(Collections.singletonMap("canFeature", canFeature));
		} catch (Exception e) {
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
		}
	}

	// Get featured usage details
	@GetMapping("/featured-stats")
	public ResponseEntity<FeaturedUsageStatsDTO> getFeaturedUsageStats() {
		try {
			Long userId = getCurrentUserId();
			FeaturedUsageStatsDTO stats = limitationService.getFeaturedUsageStats(userId);
			return ResponseEntity.ok(stats);
		} catch (Exception e) {
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
		}
	}
}