package com.doublez.backend.controller;

import java.util.List;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.doublez.backend.dto.CustomUserDetails;
import com.doublez.backend.dto.agent.AgencyDTO;
import com.doublez.backend.dto.realestate.RealEstateResponseDTO;
import com.doublez.backend.service.agency.AgencyService;
import com.doublez.backend.service.realestate.RealEstateService;
import com.doublez.backend.service.user.UserService;

import io.swagger.v3.oas.annotations.parameters.RequestBody;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/agencies")
public class AgencyController {

	private final AgencyService agencyService;
	private final UserService userService;
	private final RealEstateService realEstateService;

	public AgencyController(AgencyService agencyService, UserService userService, RealEstateService realEstateService) {
		this.agencyService = agencyService;
		this.userService = userService;
		this.realEstateService = realEstateService;
	}

	// CREATE AGENCY
	@PostMapping
    @PreAuthorize("hasRole('ADMIN') or hasRole('AGENCY_ADMIN')")
    public ResponseEntity<AgencyDTO> createAgency(@Valid @RequestBody AgencyDTO.Create createDto) {
        Long currentUserId = userService.getCurrentUserId();
        AgencyDTO agency = agencyService.createAgency(createDto, currentUserId);
        return ResponseEntity.status(HttpStatus.CREATED).body(agency);
    }

	// GET ALL AGENCIES
    @GetMapping
    public ResponseEntity<List<AgencyDTO>> getAllAgencies() {
        List<AgencyDTO> agencies = agencyService.getAllAgencies();
        return ResponseEntity.ok(agencies);
    }

    // GET AGENCY BY ID
    @GetMapping("/{agencyId}")
    public ResponseEntity<AgencyDTO> getAgency(@PathVariable Long agencyId) {
        AgencyDTO agency = agencyService.getAgencyById(agencyId);
        return ResponseEntity.ok(agency);
    }

    // GET CURRENT USER'S AGENCY
    @GetMapping("/my-agency")
    @PreAuthorize("hasRole('AGENCY_ADMIN')")
    public ResponseEntity<AgencyDTO> getMyAgency() {
        Long currentUserId = userService.getCurrentUserId();
        AgencyDTO agency = agencyService.getAgencyByAdminId(currentUserId);
        return ResponseEntity.ok(agency);
    }

    // UPDATE AGENCY
    @PutMapping("/{agencyId}")
    @PreAuthorize("hasRole('AGENCY_ADMIN') and @agencyService.isAgencyAdmin(#agencyId, authentication.principal.id)")
    public ResponseEntity<AgencyDTO> updateAgency(@PathVariable Long agencyId, 
                                                 @Valid @RequestBody AgencyDTO.Update updateDto) {
        AgencyDTO updatedAgency = agencyService.updateAgency(agencyId, updateDto);
        return ResponseEntity.ok(updatedAgency);
    }

    // GET AGENCY PROPERTIES
    @GetMapping("/{agencyId}/properties")
    @PreAuthorize("hasRole('AGENCY_ADMIN') and @agencyService.isAgencyAdmin(#agencyId, authentication.principal.id) or hasRole('ADMIN')")
    public ResponseEntity<List<RealEstateResponseDTO>> getAgencyProperties(@PathVariable Long agencyId) {
        List<RealEstateResponseDTO> properties = realEstateService.getAgencyProperties(agencyId);
        return ResponseEntity.ok(properties);
    }

    // DEACTIVATE AGENCY
    @PostMapping("/{agencyId}/deactivate")
    @PreAuthorize("hasRole('AGENCY_ADMIN') and @agencyService.isAgencyAdmin(#agencyId, authentication.principal.id) or hasRole('ADMIN')")
    public ResponseEntity<?> deactivateAgency(@PathVariable Long agencyId) {
        agencyService.deactivateAgency(agencyId);
        return ResponseEntity.ok(Map.of("message", "Agency deactivated successfully"));
    }

    // ACTIVATE AGENCY
    @PostMapping("/{agencyId}/activate")
    @PreAuthorize("hasRole('AGENCY_ADMIN') and @agencyService.isAgencyAdmin(#agencyId, authentication.principal.id) or hasRole('ADMIN')")
    public ResponseEntity<?> activateAgency(@PathVariable Long agencyId) {
        agencyService.activateAgency(agencyId);
        return ResponseEntity.ok(Map.of("message", "Agency activated successfully"));
    }

	// AGENCY STATISTICS
    @GetMapping("/{agencyId}/statistics")
    @PreAuthorize("hasRole('AGENCY_ADMIN') and @agencyService.isAgencyAdmin(#agencyId, authentication.principal.id) or hasRole('ADMIN')")
    public ResponseEntity<?> getAgencyStatistics(@PathVariable Long agencyId) {
        List<RealEstateResponseDTO> properties = realEstateService.getAgencyProperties(agencyId);
        
        long totalProperties = properties.size();
        long activeProperties = properties.stream()
            .filter(property -> property.getIsActive() != null && property.getIsActive())
            .count();
        long featuredProperties = properties.stream()
            .filter(property -> property.getIsFeatured() != null && property.getIsFeatured())
            .count();

        Map<String, Object> stats = Map.of(
            "totalProperties", totalProperties,
            "activeProperties", activeProperties,
            "featuredProperties", featuredProperties,
            "inactiveProperties", totalProperties - activeProperties
        );

        return ResponseEntity.ok(stats);
    }
}