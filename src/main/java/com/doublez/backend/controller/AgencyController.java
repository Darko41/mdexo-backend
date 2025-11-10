package com.doublez.backend.controller;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.doublez.backend.dto.CustomUserDetails;
import com.doublez.backend.dto.agent.AgencyDTO;
import com.doublez.backend.dto.agent.AgencyMembershipDTO;
import com.doublez.backend.dto.user.UserDTO;
import com.doublez.backend.request.PromoteAgencyAdminRequest;
import com.doublez.backend.service.agency.AgencyService;
import com.doublez.backend.service.user.RolePromotionService;
import com.doublez.backend.service.user.UserService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.swagger.v3.oas.annotations.parameters.RequestBody;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/agencies")
public class AgencyController {

	private final AgencyService agencyService;
	private final RolePromotionService rolePromotionService;
	private final UserService userService;

	// FIXED: Add constructor injection
	public AgencyController(AgencyService agencyService, RolePromotionService rolePromotionService,
			UserService userService) {
		this.agencyService = agencyService;
		this.rolePromotionService = rolePromotionService;
		this.userService = userService;
	}

	@PostMapping
	@PreAuthorize("hasRole('ADMIN') or hasRole('AGENCY_ADMIN')")
	public ResponseEntity<AgencyDTO> createAgency(@Valid @RequestBody AgencyDTO.Create createDto) {
		Long currentUserId = userService.getCurrentUserId();
		AgencyDTO agency = agencyService.createAgency(createDto, currentUserId);
		return ResponseEntity.status(HttpStatus.CREATED).body(agency);
	}

	@PostMapping("/{agencyId}/apply")
	@PreAuthorize("hasRole('AGENT')")
	public ResponseEntity<AgencyMembershipDTO> applyToAgency(@PathVariable Long agencyId) {
		Long currentUserId = userService.getCurrentUserId();
		AgencyMembershipDTO membership = agencyService.applyToAgency(agencyId, currentUserId);
		return ResponseEntity.ok(membership);
	}

	@PostMapping("/promote/agent")
	@PreAuthorize("hasRole('ADMIN')")
	public ResponseEntity<UserDTO> promoteToAgent(@RequestParam Long userId) {
		UserDTO user = rolePromotionService.promoteToAgent(userId);
		return ResponseEntity.ok(user);
	}

	@GetMapping
	public ResponseEntity<List<AgencyDTO>> getAllAgencies() {
		List<AgencyDTO> agencies = agencyService.getAllAgencies();
		return ResponseEntity.ok(agencies);
	}

	@GetMapping("/{agencyId}")
	public ResponseEntity<AgencyDTO> getAgency(@PathVariable Long agencyId) {
		AgencyDTO agency = agencyService.getAgencyById(agencyId);
		return ResponseEntity.ok(agency);
	}

	@PostMapping("/memberships/{membershipId}/approve")
	@PreAuthorize("hasRole('AGENCY_ADMIN')")
	public ResponseEntity<AgencyMembershipDTO> approveMembership(@PathVariable Long membershipId) {
		Long currentUserId = userService.getCurrentUserId();
		AgencyMembershipDTO membership = agencyService.approveMembership(membershipId, currentUserId);
		return ResponseEntity.ok(membership);
	}

	@GetMapping("/{agencyId}/pending-memberships")
	@PreAuthorize("hasRole('AGENCY_ADMIN')")
	public ResponseEntity<List<AgencyMembershipDTO>> getPendingMemberships(@PathVariable Long agencyId) {
		Long currentUserId = userService.getCurrentUserId();
		List<AgencyMembershipDTO> memberships = agencyService.getPendingMemberships(agencyId, currentUserId);
		return ResponseEntity.ok(memberships);
	}
	
	// Promote to agency admin
	@PostMapping("/promote/agency-admin")
	@PreAuthorize("hasRole('ADMIN')")
	public ResponseEntity<UserDTO> promoteToAgencyAdmin(HttpServletRequest request) {
	    try {
	        // Read the raw body from the request
	        String body = request.getReader().lines().collect(Collectors.joining());
	        System.out.println("🎯 RAW BODY: " + body);
	        
	        // Parse the JSON manually
	        ObjectMapper mapper = new ObjectMapper();
	        JsonNode jsonNode = mapper.readTree(body);
	        
	        // Extract values
	        Long userId = jsonNode.get("userId").asLong();
	        String name = jsonNode.get("name").asText();
	        String description = jsonNode.get("description").asText();
	        
	        System.out.println("🎯 PARSED VALUES:");
	        System.out.println("🎯 userId: " + userId);
	        System.out.println("🎯 name: " + name);
	        System.out.println("🎯 description: " + description);
	        
	        // Create agency DTO
	        AgencyDTO.Create agencyDto = new AgencyDTO.Create();
	        agencyDto.setName(name);
	        agencyDto.setDescription(description);
	        
	        // Call service
	        UserDTO user = rolePromotionService.promoteToAgencyAdmin(userId, agencyDto);
	        return ResponseEntity.ok(user);
	        
	    } catch (Exception e) {
	        System.out.println("🎯 ERROR: " + e.getMessage());
	        e.printStackTrace();
	        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
	    }
	}

	// Create this DTO class
	public static class PromoteRequest {
	    private Long userId;
	    private AgencyDTO.Create agencyDto;
	    
	    // getters and setters
	    public Long getUserId() { return userId; }
	    public void setUserId(Long userId) { this.userId = userId; }
	    public AgencyDTO.Create getAgencyDto() { return agencyDto; }
	    public void setAgencyDto(AgencyDTO.Create agencyDto) { this.agencyDto = agencyDto; }
	}

	// Demote Endpoint
	@PostMapping("/demote/agent")
	@PreAuthorize("hasRole('ADMIN')")
	public ResponseEntity<UserDTO> demoteFromAgent(@RequestParam Long userId) {
		UserDTO user = rolePromotionService.demoteFromAgent(userId);
		return ResponseEntity.ok(user);
	}
	
	// Cancel application Endpoint
	@DeleteMapping("/memberships/{membershipId}")
	@PreAuthorize("hasRole('AGENT') and @agencyService.isMembershipOwner(#membershipId, authentication.principal.id)")
	public ResponseEntity<Void> cancelApplication(@PathVariable Long membershipId) {
	    agencyService.cancelMembership(membershipId);
	    return ResponseEntity.noContent().build();
	}
	
	// Get current user's agency memberships
    @GetMapping("/my-memberships")
    @PreAuthorize("hasRole('AGENT')")
    public ResponseEntity<List<AgencyMembershipDTO>> getMyMemberships(Authentication authentication) {
        try {
            CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
            List<AgencyMembershipDTO> memberships = agencyService.getUserMemberships(userDetails.getId());
            return ResponseEntity.ok(memberships);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }
}