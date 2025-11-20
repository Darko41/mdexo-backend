package com.doublez.backend.service.investor;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.doublez.backend.dto.InvestorProfileDTO;
import com.doublez.backend.dto.realestate.RealEstateResponseDTO;
import com.doublez.backend.dto.user.UserDTO;
import com.doublez.backend.entity.InvestorProfile;
import com.doublez.backend.entity.user.User;
import com.doublez.backend.enums.PropertyType;
import com.doublez.backend.exception.IllegalOperationException;
import com.doublez.backend.exception.ResourceNotFoundException;
import com.doublez.backend.exception.UserNotFoundException;
import com.doublez.backend.mapper.UserMapper;
import com.doublez.backend.repository.UserRepository;
import com.doublez.backend.service.realestate.RealEstateService;

import jakarta.transaction.Transactional;

@Service
@Transactional
public class InvestorProfileService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final RealEstateService realEstateService;
    
    private static final Logger logger = LoggerFactory.getLogger(InvestorProfileService.class);

    public InvestorProfileService(
            UserRepository userRepository,
            UserMapper userMapper,
            RealEstateService realEstateService) {
        this.userRepository = userRepository;
        this.userMapper = userMapper;
        this.realEstateService = realEstateService;
    }

    /**
     * Get investor profile for a user
     */
    public InvestorProfileDTO getInvestorProfile(Long userId) {
        logger.info("🔍 Fetching investor profile for user ID: {}", userId);
        
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException(userId));

        validateUserIsInvestor(user);

        if (user.getInvestorProfile() == null) {
            logger.warn("⚠️ No investor profile found for user: {}", userId);
            throw new ResourceNotFoundException("Investor profile not found");
        }

        return userMapper.toInvestorProfileDTO(user.getInvestorProfile());
    }

    /**
     * Create or update investor profile
     */
    public UserDTO createOrUpdateInvestorProfile(Long userId, InvestorProfileDTO profileDto) {
        logger.info("💼 Creating/updating investor profile for user ID: {}", userId);
        
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException(userId));

        validateUserIsInvestor(user);

        InvestorProfile investorProfile = user.getOrCreateInvestorProfile();
        userMapper.updateInvestorProfileFromDTO(profileDto, investorProfile);
        user.setInvestorProfile(investorProfile);

        User updatedUser = userRepository.save(user);
        logger.info("✅ Investor profile saved successfully for user: {}", userId);
        
        return userMapper.toDTO(updatedUser);
    }

    /**
     * Create new investor profile (fails if already exists)
     */
    public UserDTO createInvestorProfile(Long userId, InvestorProfileDTO profileDto) {
        logger.info("🆕 Creating new investor profile for user ID: {}", userId);
        
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException(userId));

        validateUserIsInvestor(user);

        if (user.getInvestorProfile() != null) {
            logger.warn("🚫 Investor profile already exists for user: {}", userId);
            throw new IllegalOperationException("Investor profile already exists");
        }

        InvestorProfile investorProfile = new InvestorProfile();
        userMapper.updateInvestorProfileFromDTO(profileDto, investorProfile);
        user.setInvestorProfile(investorProfile);

        User updatedUser = userRepository.save(user);
        logger.info("✅ New investor profile created successfully for user: {}", userId);
        
        return userMapper.toDTO(updatedUser);
    }

    /**
     * Delete investor profile
     */
    public void deleteInvestorProfile(Long userId) {
        logger.info("🗑️ Deleting investor profile for user ID: {}", userId);
        
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException(userId));

        if (user.getInvestorProfile() == null) {
            logger.warn("⚠️ No investor profile to delete for user: {}", userId);
            throw new ResourceNotFoundException("No investor profile found to delete");
        }

        user.setInvestorProfile(null);
        userRepository.save(user);
        logger.info("✅ Investor profile deleted successfully for user: {}", userId);
    }

    /**
     * Get investor dashboard with portfolio statistics
     */
    public Map<String, Object> getInvestorDashboard(Long userId) {
        logger.info("📊 Generating investor dashboard for user ID: {}", userId);
        
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException(userId));

        validateUserIsInvestor(user);

        Map<String, Object> dashboard = new HashMap<>();
        dashboard.put("user", userMapper.toDTO(user));
        
        // Add investor profile if exists
        if (user.getInvestorProfile() != null) {
            dashboard.put("investorProfile", userMapper.toInvestorProfileDTO(user.getInvestorProfile()));
        }
        
        // Add portfolio statistics
        dashboard.put("portfolioStats", getPortfolioStatistics(userId));
        
        // Add recent activity or other investor-specific data
        dashboard.put("recentActivity", getRecentActivity(userId));
        
        logger.info("✅ Dashboard generated successfully for investor: {}", userId);
        return dashboard;
    }

    /**
     * Get comprehensive portfolio statistics for investor
     */
    public Map<String, Object> getPortfolioStatistics(Long investorId) {
        logger.info("📈 Calculating portfolio statistics for investor: {}", investorId);
        
        try {
            // Get investor's properties
            List<RealEstateResponseDTO> properties = realEstateService.getInvestorProperties(investorId);
            
            long totalProperties = properties.size();
            long activeProperties = properties.stream()
                    .filter(property -> property.getIsActive() != null && property.getIsActive())
                    .count();
                    
            BigDecimal totalPortfolioValue = properties.stream()
                    .map(RealEstateResponseDTO::getPrice)
                    .filter(Objects::nonNull)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            // Group by property type
            Map<PropertyType, Long> propertiesByType = properties.stream()
                    .collect(Collectors.groupingBy(RealEstateResponseDTO::getPropertyType, Collectors.counting()));

            // Group by city
            Map<String, Long> propertiesByCity = properties.stream()
                    .collect(Collectors.groupingBy(RealEstateResponseDTO::getCity, Collectors.counting()));

            Map<String, Object> stats = new HashMap<>();
            stats.put("totalProperties", totalProperties);
            stats.put("activeProperties", activeProperties);
            stats.put("inactiveProperties", totalProperties - activeProperties);
            stats.put("totalPortfolioValue", totalPortfolioValue);
            stats.put("averagePropertyValue", totalProperties > 0 ? 
                    totalPortfolioValue.divide(BigDecimal.valueOf(totalProperties), 2, RoundingMode.HALF_UP) : BigDecimal.ZERO);
            stats.put("propertiesByType", propertiesByType);
            stats.put("propertiesByCity", propertiesByCity);
            stats.put("portfolioHealth", calculatePortfolioHealth(properties));
            stats.put("activationRate", totalProperties > 0 ? (double) activeProperties / totalProperties * 100 : 0.0);

            logger.info("📊 Portfolio stats calculated - {} properties, ${} total value", 
                    totalProperties, totalPortfolioValue);
            
            return stats;
            
        } catch (Exception e) {
            logger.error("❌ Failed to calculate portfolio statistics for investor: {}", investorId, e);
            throw new RuntimeException("Failed to calculate portfolio statistics", e);
        }
    }

    /**
     * Validate that user is actually an investor
     */
    private void validateUserIsInvestor(User user) {
        if (!user.isInvestor()) {
            logger.warn("🚫 User {} is not registered as an investor", user.getId());
            throw new IllegalOperationException("User is not registered as an investor");
        }
    }

    /**
     * Calculate portfolio health based on various factors
     */
    private String calculatePortfolioHealth(List<RealEstateResponseDTO> properties) {
        if (properties.isEmpty()) return "EMPTY";
        
        long activeCount = properties.stream()
                .filter(property -> property.getIsActive() != null && property.getIsActive())
                .count();
        
        double activeRatio = (double) activeCount / properties.size();
        
        if (activeRatio >= 0.8) return "EXCELLENT";
        if (activeRatio >= 0.6) return "GOOD";
        if (activeRatio >= 0.4) return "FAIR";
        return "NEEDS_ATTENTION";
    }

    /**
     * Get recent activity for investor
     */
    private List<Map<String, Object>> getRecentActivity(Long investorId) {
        // Implement based on your business logic
        // This could include recent property views, inquiries, etc.
        List<Map<String, Object>> activity = new ArrayList<>();
        
        // Example activity entries
        Map<String, Object> activity1 = new HashMap<>();
        activity1.put("type", "PROPERTY_VIEW");
        activity1.put("description", "Viewed property listing");
        activity1.put("timestamp", LocalDateTime.now().minusHours(2));
        activity.add(activity1);
        
        Map<String, Object> activity2 = new HashMap<>();
        activity2.put("type", "PORTFOLIO_UPDATE");
        activity2.put("description", "Updated portfolio preferences");
        activity2.put("timestamp", LocalDateTime.now().minusDays(1));
        activity.add(activity2);
        
        return activity;
    }

    /**
     * Check if investor profile exists
     */
    public boolean hasInvestorProfile(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException(userId));
        return user.getInvestorProfile() != null;
    }
}
