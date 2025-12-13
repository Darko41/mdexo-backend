package com.doublez.backend.testscenarios;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Pageable;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.doublez.backend.dto.agency.AgencyDTO;
import com.doublez.backend.dto.auth.CustomUserDetails;
import com.doublez.backend.dto.realestate.RealEstateResponseDTO;
import com.doublez.backend.dto.realestate.RealEstateCreateDTO;
import com.doublez.backend.dto.user.UserDTO;
import com.doublez.backend.entity.realestate.RealEstate;
import com.doublez.backend.entity.user.User;
import com.doublez.backend.entity.user.UserLimitation;
import com.doublez.backend.entity.user.UserTier;
import com.doublez.backend.enums.ListingType;
import com.doublez.backend.enums.property.HeatingType;
import com.doublez.backend.enums.property.PropertyCondition;
import com.doublez.backend.enums.property.PropertyType;
import com.doublez.backend.exception.LimitationExceededException;
import com.doublez.backend.exception.UserNotFoundException;
import com.doublez.backend.repository.RealEstateRepository;
import com.doublez.backend.repository.RoleRepository;
import com.doublez.backend.repository.UserRepository;
import com.doublez.backend.service.agency.AgencyService;
import com.doublez.backend.service.realestate.RealEstateAuthorizationService;
import com.doublez.backend.service.realestate.RealEstateService;
import com.doublez.backend.service.usage.TrialService;
import com.doublez.backend.service.user.UserService;

//@Component
//@Transactional
//public class SystemTestScenarios {
//    
//    private static final Logger logger = LoggerFactory.getLogger(SystemTestScenarios.class);
//
//    private final UserService userService;
//    private final UserRepository userRepository;
//    private final AgencyService agencyService;
//    private final RealEstateService realEstateService;
//    private final TrialService trialService;
//    private final RealEstateAuthorizationService authService;
//    private final RoleRepository roleRepository;
//    private final RealEstateRepository realEstateRepository;
//
//    public SystemTestScenarios(UserService userService, AgencyService agencyService,
//                              RealEstateService realEstateService, TrialService trialService,
//                              RealEstateAuthorizationService authService, UserRepository userRepository,
//                              RoleRepository roleRepository, RealEstateRepository realEstateRepository) {
//        this.userService = userService;
//        this.agencyService = agencyService;
//        this.realEstateService = realEstateService;
//        this.trialService = trialService;
//        this.authService = authService;
//        this.userRepository = userRepository;
//        this.roleRepository = roleRepository;
//        this.realEstateRepository = realEstateRepository;
//    }
//
//    @Transactional(propagation = Propagation.NOT_SUPPORTED)
//    public void cleanupTestData() {
//        logger.info("🧹 Cleaning up test data...");
//        
//        String[] testEmails = {
//            "test.investor@example.com",
//            "test.agency@example.com", 
//            "test.regular@example.com"
//        };
//        
//        for (String email : testEmails) {
//            try {
//                userRepository.findByEmail(email).ifPresent(user -> {
//                    try {
//                        // Delete user properties first
//                        List<RealEstate> userProperties = realEstateRepository.findByUserId(user.getId());
//                        if (!userProperties.isEmpty()) {
//                            realEstateRepository.deleteAll(userProperties);
//                            logger.info("🗑️ Deleted {} properties for user: {}", userProperties.size(), email);
//                        }
//                        
//                        // Then delete the user
//                        userRepository.delete(user);
//                        logger.info("🗑️ Deleted test user: {}", email);
//                    } catch (Exception e) {
//                        logger.warn("⚠️ Could not delete user {}: {}", email, e.getMessage());
//                    }
//                });
//            } catch (Exception e) {
//                logger.warn("⚠️ Error cleaning up user {}: {}", email, e.getMessage());
//            }
//        }
//    }
//
//    @Transactional(propagation = Propagation.REQUIRES_NEW)
//    public void runAllTests() {
//        logger.info("🧪 STARTING COMPREHENSIVE SYSTEM TESTS...");
//        
//        testUserRegistrationFlows();
//        testAgencyRegistrationFlows();
//        testTrialSystem();
//        testAuthorizationFlows();
//        testPropertyCreationLimits();
//        testAgencyPropertyManagement();
//        testInvestorFlows();
//        
//        logger.info("✅ ALL TESTS COMPLETED SUCCESSFULLY!");
//    }
//
//    @Transactional(propagation = Propagation.REQUIRES_NEW)
//    private void testUserRegistrationFlows() {
//        logger.info("🔍 Testing User Registration Flows...");
//        
//        try {
//            // Test 1: Regular user registration
//            UserDTO.Create regularUser = new UserDTO.Create();
//            regularUser.setEmail("test.regular@example.com");
//            regularUser.setPassword("password123");
//            regularUser.setTier(UserTier.FREE_USER);
//            
//            UserDTO regularResult = userService.registerUser(regularUser, false);
//            
//            // Force initialization of roles within transactional context
//            User regularEntity = userService.getUserEntityByEmail("test.regular@example.com");
//            initializeRoles(regularEntity);
//            
//            assert regularEntity.getRoles().stream().anyMatch(role -> role.getName().equals("ROLE_USER"));
//            assert regularEntity.getRoles().stream().noneMatch(role -> role.getName().equals("ROLE_AGENCY_ADMIN"));
//            logger.info("✅ Regular user registration: PASSED");
//
//            // Test 2: User with agency registration
//            UserDTO.Create agencyUser = new UserDTO.Create();
//            agencyUser.setEmail("test.agency@example.com");
//            agencyUser.setPassword("password123");
//            
//            AgencyDTO.Create agencyDto = new AgencyDTO.Create();
//            agencyDto.setName("Test Agency");
//            agencyDto.setDescription("Test Agency Description");
//            agencyDto.setLicenseNumber("TEST-LICENSE-001");
//            agencyUser.setAgency(agencyDto);
//            
//            UserDTO agencyResult = userService.registerUser(agencyUser, false);
//            
//            // Force initialization of roles within transactional context
//            User agencyEntity = userService.getUserEntityByEmail("test.agency@example.com");
//            initializeRoles(agencyEntity);
//            
//            assert agencyEntity.getRoles().stream().anyMatch(role -> role.getName().equals("ROLE_USER"));
//            assert agencyEntity.getRoles().stream().anyMatch(role -> role.getName().equals("ROLE_AGENCY_ADMIN"));
//            logger.info("✅ Agency user registration: PASSED");
//            
//        } catch (Exception e) {
//            logger.error("❌ User registration tests failed: {}", e.getMessage());
//            throw e;
//        }
//    }
//
//    private void testAgencyRegistrationFlows() {
//        logger.info("🔍 Testing Agency Registration Flows...");
//        
//        try {
//            // SETUP SECURITY CONTEXT for agency admin
//            setupSecurityContext("test.agency@example.com");
//            
//            // Test agency creation and retrieval
//            User agencyUser = userService.getAuthenticatedUser();
//            AgencyDTO agency = agencyService.getAgencyByAdminId(agencyUser.getId());
//            assert agency != null;
//            assert agency.getName().equals("Test Agency");
//            assert agency.getIsActive();
//            logger.info("✅ Agency creation and retrieval: PASSED");
//            
//        } catch (Exception e) {
//            logger.error("❌ Agency registration tests failed: {}", e.getMessage());
//            throw e;
//        } finally {
//            clearSecurityContext();
//        }
//    }
//
//    private void testTrialSystem() {
//        logger.info("🔍 Testing Trial System...");
//        
//        try {
//            // SETUP SECURITY CONTEXT
//            setupSecurityContext("test.agency@example.com");
//            
//            User testUser = userService.getAuthenticatedUser();
//            
//            // Test trial status
//            assert trialService.isInTrial(testUser);
//            assert testUser.getTier() == UserTier.AGENCY_BASIC; // During trial
//            
//            long daysRemaining = trialService.getTrialDaysRemaining(testUser);
//            assert daysRemaining > 0;
//            logger.info("✅ Trial system: PASSED ({} days remaining)", daysRemaining);
//            
//        } catch (Exception e) {
//            logger.error("❌ Trial system tests failed: {}", e.getMessage());
//            throw e;
//        } finally {
//            clearSecurityContext();
//        }
//    }
//
//    private void testAuthorizationFlows() {
//        logger.info("🔍 Testing Authorization Flows...");
//        
//        try {
//            // SETUP SECURITY CONTEXT for regular user
//            setupSecurityContext("test.regular@example.com");
//            
//            User regularUser = userService.getAuthenticatedUser();
//            
//            // Test regular user permissions
//            assert authService.hasRealEstateCreateAccess();
//            logger.info("✅ Regular user authorization: PASSED");
//            
//            clearSecurityContext();
//            
//            // SETUP SECURITY CONTEXT for agency admin
//            setupSecurityContext("test.agency@example.com");
//            
//            User agencyUser = userService.getAuthenticatedUser();
//            assert authService.isAgencyAdmin(agencyUser.getId());
//            logger.info("✅ Agency admin authorization: PASSED");
//            
//        } catch (Exception e) {
//            logger.error("❌ Authorization tests failed: {}", e.getMessage());
//            throw e;
//        } finally {
//            clearSecurityContext();
//        }
//    }
//
//    @Transactional(propagation = Propagation.REQUIRES_NEW)
//    private void testPropertyCreationLimits() {
//        logger.info("🔍 Testing Property Creation Limits...");
//        
//        try {
//            // 🆕 First, debug the current state
//            debugUserLimits();
//            
//            setupSecurityContext("test.regular@example.com");
//            User freeUser = userService.getAuthenticatedUser();
//            
//            // Get current count and limits
//            long initialCount = realEstateRepository.countActiveRealEstatesByUser(freeUser.getId());
//            UserLimitation limits = authService.getEffectiveLimitations(freeUser);
//            int maxAllowed = limits.getMaxListings();
//            
//            logger.info("🎯 Starting test - User has {}/{} properties", initialCount, maxAllowed);
//            
//            // Create properties up to the limit
//            int propertiesToCreate = maxAllowed - (int) initialCount;
//            for (int i = 1; i <= propertiesToCreate; i++) {
//                RealEstateCreateDTO property = createTestProperty("Free User Property " + i);
//                realEstateService.createRealEstate(property, null);
//                logger.info("✅ Created property {}/{}", i, propertiesToCreate);
//            }
//            
//            // 🆕 Verify we're at the limit
//            long currentCount = realEstateRepository.countActiveRealEstatesByUser(freeUser.getId());
//            logger.info("📊 After creating {} properties - now at {}/{}", propertiesToCreate, currentCount, maxAllowed);
//            
//            // Now try to create one more (should fail)
//            try {
//                RealEstateCreateDTO extraProperty = createTestProperty("Extra Property Beyond Limit");
//                realEstateService.createRealEstate(extraProperty, null);
//                logger.error("❌ Free user limit test: FAILED - Should have thrown exception");
//                throw new AssertionError("Free user limit not enforced");
//            } catch (LimitationExceededException e) {
//                logger.info("✅ Free user property limit enforcement: PASSED - Exception thrown correctly");
//                // 🆕 IMPORTANT: Don't rethrow the expected exception
//            }
//            
//        } catch (Exception e) {
//            logger.error("❌ Property creation limit test failed: {}", e.getMessage());
//            throw e; // Only rethrow unexpected exceptions
//        } finally {
//            clearSecurityContext();
//        }
//    }
//
//    private void testAgencyPropertyManagement() {
//        logger.info("🔍 Testing Agency Property Management...");
//        
//        try {
//            setupSecurityContext("test.agency@example.com");
//            User agencyUser = userService.getAuthenticatedUser();
//            AgencyDTO agency = agencyService.getAgencyByAdminId(agencyUser.getId());
//            
//            // Create agency property
//            RealEstateCreateDTO agencyProperty = createTestProperty("Agency Property");
//            agencyProperty.setAgencyId(agency.getId());
//            agencyProperty.setAgentName("Test Agent");
//            agencyProperty.setAgentPhone("+1234567890");
//            agencyProperty.setAgentLicense("AGENT-LICENSE-001");
//            
//            RealEstateResponseDTO created = realEstateService.createRealEstate(agencyProperty, null);
//            assert created.getAgencyId().equals(agency.getId());
//            assert created.getAgentName().equals("Test Agent");
//            logger.info("✅ Agency property creation: PASSED");
//            
//            // Test agency properties retrieval
//            List<RealEstateResponseDTO> agencyProperties = realEstateService.getAgencyProperties(agency.getId());
//            assert !agencyProperties.isEmpty();
//            logger.info("✅ Agency properties retrieval: PASSED");
//        } finally {
//            clearSecurityContext();
//        }
//    }
//
//    private void testInvestorFlows() {
//        logger.info("🔍 Testing Investor Flows...");
//        
//        try {
//            // Create investor user
//            UserDTO.Create investorUser = new UserDTO.Create();
//            investorUser.setEmail("test.investor@example.com");
//            investorUser.setPassword("password123");
//            investorUser.setTier(UserTier.FREE_INVESTOR);
//            
//            UserDTO investorResult = userService.registerUser(investorUser, false);
//            assert investorResult.getTier() == UserTier.FREE_INVESTOR;
//            logger.info("✅ Investor registration: PASSED");
//            
//            // Test investor property creation
//            setupSecurityContext("test.investor@example.com");
//            User investor = userService.getAuthenticatedUser();
//            RealEstateCreateDTO investmentProperty = createTestProperty("Investment Property");
//            
//            RealEstateResponseDTO created = realEstateService.createRealEstate(investmentProperty, null);
//            assert created.getOwnerId().equals(investor.getId());
//            logger.info("✅ Investor property creation: PASSED");
//            
//            // Test investor portfolio stats
//            Map<String, Object> portfolioStats = realEstateService.getInvestmentPortfolioStats(investor.getId());
//            assert portfolioStats.containsKey("totalProperties");
//            assert portfolioStats.containsKey("totalPortfolioValue");
//            logger.info("✅ Investor portfolio stats: PASSED");
//        } finally {
//            clearSecurityContext();
//        }
//    }
//
//    private RealEstateCreateDTO createTestProperty(String title) {
//        RealEstateCreateDTO property = new RealEstateCreateDTO();
//        
//        // Basic info
//        property.setTitle(title);
//        property.setDescription("Test property description");
//        property.setPropertyType(PropertyType.APARTMENT);
//        property.setListingType(ListingType.FOR_SALE);
//        property.setPrice(new BigDecimal("150000"));
//        
//        // Location
//        property.setAddress("Test Address 123");
//        property.setCity("Belgrade");
//        property.setState("Serbia");
//        property.setZipCode("11000");
//        property.setMunicipality("Stari Grad");
//        
//        // Property details - using correct data types from your DTO
//        property.setSizeInSqMt(new BigDecimal("75.0")); // Changed from String to BigDecimal
//        property.setRoomCount(new BigDecimal("3.0"));
//        property.setFloor(2);
//        property.setTotalFloors(5);
//        property.setConstructionYear(2010);
//        property.setPropertyCondition(PropertyCondition.GOOD);
//        property.setHeatingType(HeatingType.CENTRAL);
//        
//        // Features
//        property.setFeatures(List.of("Parking", "Elevator", "Balcony"));
//        
//        // Status flags (typically required)
//        property.setIsActive(true);
//        property.setIsFeatured(false);
//        property.setFeaturedAt(LocalDateTime.now());
//        
//        // Coordinates (optional but good to have)
//        property.setLatitude(new BigDecimal("44.8125"));
//        property.setLongitude(new BigDecimal("20.4612"));
//        
//        return property;
//    }
//    
//    private void setupSecurityContext(String email) {
//        User user = userRepository.findByEmail(email)
//                .orElseThrow(() -> new UserNotFoundException("User not found: " + email));
//        
//        // Initialize roles within transactional context
//        initializeRoles(user);
//        
//        CustomUserDetails userDetails = new CustomUserDetails(user);
//        Authentication auth = new UsernamePasswordAuthenticationToken(
//            userDetails, null, userDetails.getAuthorities()
//        );
//        SecurityContextHolder.getContext().setAuthentication(auth);
//    }
//
//    private void clearSecurityContext() {
//        SecurityContextHolder.clearContext();
//    }
//
//    private void initializeRoles(User user) {
//        // Force initialization of lazy collection within transactional context
//        if (user.getRoles() != null) {
//            user.getRoles().size(); // This forces Hibernate to initialize the collection
//        }
//    }
//    
//    private void debugUserLimits() {
//        logger.info("🔍 DEBUG: Checking User Limits...");
//        
//        try {
//            setupSecurityContext("test.regular@example.com");
//            User freeUser = userService.getAuthenticatedUser();
//            
//            UserLimitation limits = authService.getEffectiveLimitations(freeUser);
//            long currentCount = realEstateRepository.countActiveRealEstatesByUser(freeUser.getId());
//            
//            logger.info("👤 User: {} (Tier: {})", freeUser.getEmail(), freeUser.getTier());
//            logger.info("📊 Current properties: {}", currentCount);
//            logger.info("📈 Max properties allowed: {}", limits.getMaxListings()); // ✅ FIXED: getMaxListings()
//            logger.info("✅ Can create more: {}", authService.canCreateRealEstate(freeUser.getId()));
//            
//            // 🆕 Also check the database values
//            logger.info("🔍 UserLimitation details:");
//            logger.info("   - maxListings: {}", limits.getMaxListings());
//            logger.info("   - maxImages: {}", limits.getMaxImages());
//            logger.info("   - maxImagesPerListing: {}", limits.getMaxImagesPerListing());
//            logger.info("   - canFeatureListings: {}", limits.getCanFeatureListings());
//            logger.info("   - maxFeaturedListings: {}", limits.getMaxFeaturedListings());
//            
//        } catch (Exception e) {
//            logger.error("❌ Debug failed: {}", e.getMessage());
//        } finally {
//            clearSecurityContext();
//        }
//    }
//}