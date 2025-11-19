package com.doublez.backend.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.doublez.backend.entity.agency.Agency;
import com.doublez.backend.entity.user.User;
import com.doublez.backend.service.usage.TierBenefitsService;
import com.doublez.backend.service.usage.TrialService;
import com.doublez.backend.service.user.UserService;

@RestController
@RequestMapping("/api/tiers")
public class TierController {

	private final TierBenefitsService tierBenefitsService;
	private final TrialService trialService;
	private final UserService userService;

	public TierController(TierBenefitsService tierBenefitsService, TrialService trialService, UserService userService) {
		this.tierBenefitsService = tierBenefitsService;
		this.trialService = trialService;
		this.userService = userService;
	}

	@GetMapping("/benefits")
	public ResponseEntity<List<Map<String, Object>>> getAllTierBenefits() {
		List<Map<String, Object>> benefits = tierBenefitsService.getAllTierBenefits();
		return ResponseEntity.ok(benefits);
	}

	@GetMapping("/benefits/individual")
	public ResponseEntity<List<Map<String, Object>>> getIndividualTiers() {
		List<Map<String, Object>> tiers = tierBenefitsService.getIndividualTiers();
		return ResponseEntity.ok(tiers);
	}

	@GetMapping("/benefits/agency")
	public ResponseEntity<List<Map<String, Object>>> getAgencyTiers() {
		List<Map<String, Object>> tiers = tierBenefitsService.getAgencyTiers();
		return ResponseEntity.ok(tiers);
	}

	@GetMapping("/benefits/investor")
	public ResponseEntity<List<Map<String, Object>>> getInvestorTiers() {
		List<Map<String, Object>> tiers = tierBenefitsService.getInvestorTiers();
		return ResponseEntity.ok(tiers);
	}

	@GetMapping("/my-tier")
	@PreAuthorize("isAuthenticated()")
	public ResponseEntity<?> getMyTierBenefits() {
		try {
			Long currentUserId = userService.getCurrentUserId();
			User user = userService.getUserEntityById(currentUserId);

			Map<String, Object> response = new HashMap<>();
			response.put("currentTier", user.getTier());
			response.put("benefits", tierBenefitsService.getTierBenefits(user.getTier()));
			response.put("inTrial", trialService.isInTrial(user));
			response.put("trialDaysRemaining", trialService.getTrialDaysRemaining(user));

			if (user.isAgencyAdmin() && !user.getOwnedAgencies().isEmpty()) {
				Agency agency = user.getOwnedAgencies().get(0);
				response.put("agency",
						Map.of("id", agency.getId(), "name", agency.getName(), "isActive", agency.getIsActive()));
			}

			return ResponseEntity.ok(response);

		} catch (Exception e) {
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
					.body(Map.of("error", "Failed to retrieve tier information: " + e.getMessage()));
		}
	}
}
