package com.doublez.backend.controller;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.doublez.backend.dto.agent.AgencyDTO;
import com.doublez.backend.dto.realestate.RealEstateResponseDTO;
import com.doublez.backend.enums.ListingType;
import com.doublez.backend.enums.PropertyType;
import com.doublez.backend.response.ApiResponse;
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
    
    private static final Logger logger = LoggerFactory.getLogger(AgencyController.class);

    public AgencyController(AgencyService agencyService, UserService userService, RealEstateService realEstateService) {
        this.agencyService = agencyService;
        this.userService = userService;
        this.realEstateService = realEstateService;
    }

    // ========================
    // AGENCY CRUD OPERATIONS
    // ========================

    /**
     * CREATE AGENCY
     * Admins can create agencies for any user
     * Agency admins can create their own agency
     */
    @PostMapping
    @PreAuthorize("hasRole('ADMIN') or hasRole('AGENCY_ADMIN')")
    public ResponseEntity<AgencyDTO> createAgency(@Valid @RequestBody AgencyDTO.Create createDto) {
        Long currentUserId = userService.getCurrentUserId();
        logger.info("🏢 Creating agency '{}' for user ID: {}", createDto.getName(), currentUserId);
        
        AgencyDTO agency = agencyService.createAgency(createDto, currentUserId);
        
        logger.info("✅ Agency created successfully - ID: {}, Name: {}", agency.getId(), agency.getName());
        return ResponseEntity.status(HttpStatus.CREATED)
                .header("Location", "/api/agencies/" + agency.getId())
                .body(agency);
    }

    /**
     * GET ALL AGENCIES
     * Public endpoint - anyone can view agencies
     */
    @GetMapping
    public ResponseEntity<List<AgencyDTO>> getAllAgencies() {
        logger.info("📋 Fetching all agencies");
        List<AgencyDTO> agencies = agencyService.getAllAgencies();
        logger.info("✅ Found {} agencies", agencies.size());
        return ResponseEntity.ok(agencies);
    }

    /**
     * GET AGENCY BY ID
     * Public endpoint - anyone can view agency details
     */
    @GetMapping("/{agencyId}")
    public ResponseEntity<AgencyDTO> getAgency(@PathVariable Long agencyId) {
        logger.info("🔍 Fetching agency by ID: {}", agencyId);
        AgencyDTO agency = agencyService.getAgencyById(agencyId);
        logger.info("✅ Agency found - ID: {}, Name: {}", agency.getId(), agency.getName());
        return ResponseEntity.ok(agency);
    }

    /**
     * GET CURRENT USER'S AGENCY
     * Only for agency admins
     */
    @GetMapping("/my-agency")
    @PreAuthorize("hasRole('AGENCY_ADMIN')")
    public ResponseEntity<AgencyDTO> getMyAgency() {
        Long currentUserId = userService.getCurrentUserId();
        logger.info("👤 Agency admin fetching their agency - User ID: {}", currentUserId);
        
        AgencyDTO agency = agencyService.getAgencyByAdminId(currentUserId);
        
        if (agency != null) {
            logger.info("✅ Agency found for user - ID: {}, Name: {}", agency.getId(), agency.getName());
        } else {
            logger.warn("⚠️ No agency found for user ID: {}", currentUserId);
        }
        
        return ResponseEntity.ok(agency);
    }

    /**
     * UPDATE AGENCY
     * Only agency admin or system admin can update
     */
    @PutMapping("/{agencyId}")
    @PreAuthorize("hasRole('AGENCY_ADMIN') and @agencyService.isAgencyAdmin(#agencyId, authentication.principal.id) or hasRole('ADMIN')")
    public ResponseEntity<AgencyDTO> updateAgency(@PathVariable Long agencyId, 
                                                 @Valid @RequestBody AgencyDTO.Update updateDto) {
        logger.info("✏️ Updating agency ID: {}", agencyId);
        
        AgencyDTO updatedAgency = agencyService.updateAgency(agencyId, updateDto);
        
        logger.info("✅ Agency updated successfully - ID: {}, Name: {}", updatedAgency.getId(), updatedAgency.getName());
        return ResponseEntity.ok(updatedAgency);
    }

    // ========================
    // AGENCY PROPERTIES
    // ========================

    /**
     * GET AGENCY PROPERTIES
     * Agency admins can view their properties, admins can view any agency's properties
     */
    @GetMapping("/{agencyId}/properties")
    @PreAuthorize("hasRole('AGENCY_ADMIN') and @agencyService.isAgencyAdmin(#agencyId, authentication.principal.id) or hasRole('ADMIN')")
    public ResponseEntity<List<RealEstateResponseDTO>> getAgencyProperties(@PathVariable Long agencyId) {
        logger.info("🏠 Fetching properties for agency ID: {}", agencyId);
        
        List<RealEstateResponseDTO> properties = realEstateService.getAgencyProperties(agencyId);
        
        logger.info("✅ Found {} properties for agency ID: {}", properties.size(), agencyId);
        return ResponseEntity.ok(properties);
    }

    // ========================
    // AGENCY STATUS MANAGEMENT
    // ========================

    /**
     * DEACTIVATE AGENCY
     * Agency admins can deactivate their own agency, admins can deactivate any agency
     */
    @PostMapping("/{agencyId}/deactivate")
    @PreAuthorize("hasRole('AGENCY_ADMIN') and @agencyService.isAgencyAdmin(#agencyId, authentication.principal.id) or hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<String>> deactivateAgency(@PathVariable Long agencyId) {
        logger.info("⏸️ Deactivating agency ID: {}", agencyId);
        
        agencyService.deactivateAgency(agencyId);
        
        logger.info("✅ Agency deactivated successfully - ID: {}", agencyId);
        return ResponseEntity.ok(ApiResponse.success("Agency deactivated successfully"));
    }

    /**
     * ACTIVATE AGENCY
     * Agency admins can activate their own agency, admins can activate any agency
     */
    @PostMapping("/{agencyId}/activate")
    @PreAuthorize("hasRole('AGENCY_ADMIN') and @agencyService.isAgencyAdmin(#agencyId, authentication.principal.id) or hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<String>> activateAgency(@PathVariable Long agencyId) {
        logger.info("▶️ Activating agency ID: {}", agencyId);
        
        agencyService.activateAgency(agencyId);
        
        logger.info("✅ Agency activated successfully - ID: {}", agencyId);
        return ResponseEntity.ok(ApiResponse.success("Agency activated successfully"));
    }

    // ========================
    // AGENCY ANALYTICS & STATISTICS
    // ========================

    /**
     * AGENCY STATISTICS
     * Agency admins can view their stats, admins can view any agency's stats
     */
    @GetMapping("/{agencyId}/statistics")
    @PreAuthorize("hasRole('AGENCY_ADMIN') and @agencyService.isAgencyAdmin(#agencyId, authentication.principal.id) or hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> getAgencyStatistics(@PathVariable Long agencyId) {
        logger.info("📊 Fetching statistics for agency ID: {}", agencyId);
        
        List<RealEstateResponseDTO> properties = realEstateService.getAgencyProperties(agencyId);
        
        long totalProperties = properties.size();
        long activeProperties = properties.stream()
            .filter(property -> property.getIsActive() != null && property.getIsActive())
            .count();
        long featuredProperties = properties.stream()
            .filter(property -> property.getIsFeatured() != null && property.getIsFeatured())
            .count();

        // Calculate total portfolio value
        BigDecimal totalPortfolioValue = properties.stream()
            .map(RealEstateResponseDTO::getPrice)
            .filter(Objects::nonNull)
            .reduce(BigDecimal.ZERO, BigDecimal::add);

        // Group by property type
        Map<PropertyType, Long> propertiesByType = properties.stream()
            .collect(Collectors.groupingBy(RealEstateResponseDTO::getPropertyType, Collectors.counting()));

        Map<String, Object> stats = Map.of(
            "totalProperties", totalProperties,
            "activeProperties", activeProperties,
            "featuredProperties", featuredProperties,
            "inactiveProperties", totalProperties - activeProperties,
            "totalPortfolioValue", totalPortfolioValue,
            "averagePropertyValue", totalProperties > 0 ? 
                totalPortfolioValue.divide(BigDecimal.valueOf(totalProperties), 2, RoundingMode.HALF_UP) : BigDecimal.ZERO,
            "propertiesByType", propertiesByType,
            "activationRate", totalProperties > 0 ? (double) activeProperties / totalProperties : 0.0
        );

        logger.info("✅ Statistics calculated for agency ID: {} - {} active out of {} total properties", 
            agencyId, activeProperties, totalProperties);
        
        return ResponseEntity.ok(stats);
    }

    // 🆕 ADDITIONAL USEFUL ENDPOINTS

    /**
     * GET AGENCY PROPERTIES WITH PAGINATION
     */
    @GetMapping("/{agencyId}/properties/paged")
    @PreAuthorize("hasRole('AGENCY_ADMIN') and @agencyService.isAgencyAdmin(#agencyId, authentication.principal.id) or hasRole('ADMIN')")
    public ResponseEntity<Page<RealEstateResponseDTO>> getAgencyPropertiesPaged(
            @PathVariable Long agencyId,
            @RequestParam(required = false) String searchTerm,
            @RequestParam(required = false) PropertyType propertyType,
            @RequestParam(required = false) ListingType listingType,
            Pageable pageable) {
        
        logger.info("🏠 Fetching paged properties for agency ID: {} - page: {}, size: {}", 
            agencyId, pageable.getPageNumber(), pageable.getPageSize());
        
        // You might need to implement this method in RealEstateService
        // For now, we'll manually paginate the existing list
        List<RealEstateResponseDTO> allProperties = realEstateService.getAgencyProperties(agencyId);
        
        // Apply basic filtering
        List<RealEstateResponseDTO> filteredProperties = allProperties.stream()
            .filter(property -> searchTerm == null || 
                property.getTitle().toLowerCase().contains(searchTerm.toLowerCase()) ||
                property.getDescription().toLowerCase().contains(searchTerm.toLowerCase()))
            .filter(property -> propertyType == null || property.getPropertyType() == propertyType)
            .filter(property -> listingType == null || property.getListingType() == listingType)
            .collect(Collectors.toList());
        
        // Manual pagination
        int start = (int) pageable.getOffset();
        int end = Math.min((start + pageable.getPageSize()), filteredProperties.size());
        List<RealEstateResponseDTO> pagedProperties = filteredProperties.subList(start, end);
        
        Page<RealEstateResponseDTO> page = new PageImpl<>(
            pagedProperties, pageable, filteredProperties.size());
        
        logger.info("✅ Found {} properties (showing {}-{})", 
            filteredProperties.size(), start, Math.min(end, filteredProperties.size()));
        
        return ResponseEntity.ok(page);
    }

    /**
     * SEARCH AGENCIES BY NAME OR LOCATION
     */
    @GetMapping("/search")
    public ResponseEntity<List<AgencyDTO>> searchAgencies(
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String city) {
        
        logger.info("🔍 Searching agencies - name: {}, city: {}", name, city);
        
        List<AgencyDTO> allAgencies = agencyService.getAllAgencies();
        List<AgencyDTO> filteredAgencies = allAgencies.stream()
            .filter(agency -> name == null || agency.getName().toLowerCase().contains(name.toLowerCase()))
            .filter(agency -> city == null || (agency.getCity() != null && 
                agency.getCity().toLowerCase().contains(city.toLowerCase())))
            .collect(Collectors.toList());
        
        logger.info("✅ Found {} agencies matching search criteria", filteredAgencies.size());
        return ResponseEntity.ok(filteredAgencies);
    }
}