package com.doublez.backend.service.usage;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import com.doublez.backend.entity.agency.Agency;
import com.doublez.backend.entity.user.User;
import com.doublez.backend.exception.UserNotFoundException;
import com.doublez.backend.repository.AgencyRepository;
import com.doublez.backend.repository.UserRepository;
import com.doublez.backend.service.email.ResendEmailService;

import jakarta.transaction.Transactional;

@Service
@Transactional
public class TrialService {

    private static final Logger logger = LoggerFactory.getLogger(TrialService.class);
    
    private final UserRepository userRepository;
    private final ResendEmailService resendEmailService;
    private final int TRIAL_MONTHS = 6;
    private final AgencyRepository agencyRepository;

    public TrialService(UserRepository userRepository,
                        ResendEmailService resendEmailService, AgencyRepository agencyRepository) {
        this.userRepository = userRepository;
        this.resendEmailService = resendEmailService;
        this.agencyRepository = agencyRepository;
    }

    public void startTrial(User user) {
        logger.info("Starting 45-day trial for user: {}", user.getEmail());
        
        user.startTrial(45); // Use entity method
        
        // Set appropriate tier based on role
        if (user.isAgencyAdmin()) {
            // Agency users get PRO features during trial
            // The effective tier calculation happens in Agency entity
        } else if (user.isInvestor()) {
            // Investor specific logic if needed
        }
        // Regular users don't need tier changes

        userRepository.save(user);

        try {
            resendEmailService.sendTrialStartedEmail(
                user.getEmail(), 
                getUserDisplayName(user), 
                45 // 45 days trial
            );
        } catch (Exception e) {
            logger.warn("Failed to send trial started email: {}", e.getMessage());
        }

        logger.info("Trial started for user {} until {}", user.getEmail(), user.getTrialEndDate());
    }

    public boolean isInTrial(User user) { return user.isInTrialPeriod(); }
    public boolean isTrialExpired(User user) { return user.isTrialExpired(); }
    public long getTrialDaysRemaining(User user) { return user.getTrialDaysRemaining(); }

    public int getTrialProgressPercentage(User user) {
        if (!user.getTrialUsed() || user.getTrialStartDate() == null || user.getTrialEndDate() == null) {
            return 0;
        }
        long totalDays = ChronoUnit.DAYS.between(user.getTrialStartDate(), user.getTrialEndDate());
        long daysPassed = ChronoUnit.DAYS.between(user.getTrialStartDate(), LocalDateTime.now());
        if (totalDays <= 0) return 100;
        int percentage = (int) ((daysPassed * 100) / totalDays);
        return Math.min(Math.max(percentage, 0), 100);
    }

    public void expireTrial(Long userId) {
        User user = userRepository.findById(userId).orElseThrow(() -> new UserNotFoundException(userId));
        user.setTrialEndDate(LocalDateTime.now().minusDays(1));
        userRepository.save(user);
        handleTrialExpiration(user);
    }

    public void extendTrial(Long userId, int additionalMonths) {
        User user = userRepository.findById(userId).orElseThrow(() -> new UserNotFoundException(userId));
        if (user.getTrialEndDate() == null) {
            user.setTrialEndDate(LocalDateTime.now().plusMonths(additionalMonths));
        } else {
            user.setTrialEndDate(user.getTrialEndDate().plusMonths(additionalMonths));
        }
        userRepository.save(user);
        try {
            resendEmailService.sendTrialExtendedEmail(user.getEmail(), getUserDisplayName(user), additionalMonths);
        } catch (Exception e) {
            logger.warn("Failed to send trial extended email: {}", e.getMessage());
        }
    }

    @Scheduled(cron = "0 0 9 * * ?") // Daily at 9 AM
    public void checkExpiringTrials() {
        LocalDateTime now = LocalDateTime.now();
        
        // Notifications at 15, 7, 3, and 1 days before expiry
        sendTrialNotifications(now.plusDays(15), 15);
        sendTrialNotifications(now.plusDays(7), 7);
        sendTrialNotifications(now.plusDays(3), 3);
        sendTrialNotifications(now.plusDays(1), 1);
        
        // Handle expired trials
        handleExpiredTrials(now);
    }
    
    private void sendTrialNotifications(LocalDateTime targetDate, int daysRemaining) {
        List<User> users = userRepository.findUsersWithTrialEndingBetween(
            targetDate.minusHours(1), targetDate.plusHours(1)
        );
        
        users.stream()
            .filter(this::isInTrial)
            .forEach(user -> sendTrialExpirationWarning(user, daysRemaining));
    }

    private void handleExpiredTrials(LocalDateTime now) {
        List<User> expiredUsers = userRepository.findByTrialEndDateBefore(now);
        
        expiredUsers.stream()
            .filter(this::isTrialExpired)
            .forEach(user -> {
                // Auto-downgrade logic
                if (user.isAgencyAdmin() && user.getOwnedAgency().isPresent()) {
                    handleAgencyTrialExpiration(user, user.getOwnedAgency().get());
                } else {
                    handleTrialExpiration(user);
                }
                
                // Send final expiration notice - FIXED: Use the method we just created
                sendTrialExpiredNotification(user);
            });
    }
    
 // 🆕 TRIAL SUNSET MECHANISM (remove trial option after 2-3 months)
    public boolean isTrialAvailable() {
        // After 3 months, disable trial registrations
        LocalDateTime cutoffDate = LocalDateTime.now().minusMonths(3);
        long agenciesCreatedAfterCutoff = agencyRepository.countByCreatedAtAfter(cutoffDate);
        
        // Only allow trials for first 100 agencies or first 3 months
        return agenciesCreatedAfterCutoff < 100;
    }

    // 🆕 GET TRIAL AVAILABILITY STATUS
    public Map<String, Object> getTrialAvailability() {
        LocalDateTime cutoffDate = LocalDateTime.now().minusMonths(3);
        long recentAgencies = agencyRepository.countByCreatedAtAfter(cutoffDate);
        
        Map<String, Object> availability = new HashMap<>();
        availability.put("trialAvailable", recentAgencies < 100);
        availability.put("agenciesCreatedInPeriod", recentAgencies);
        availability.put("maxAgenciesForTrial", 100);
        availability.put("cutoffDate", cutoffDate);
        
        return availability;
    }

    private void sendTrialExpirationWarning(User user, int daysRemaining) {
        try {
            resendEmailService.sendTrialExpiringEmail(user.getEmail(), getUserDisplayName(user), daysRemaining);
        } catch (Exception e) {
            logger.warn("Failed to send expiration warning to {}: {}", user.getEmail(), e.getMessage());
        }
    }

    private void handleTrialExpiration(User user) {
        // For regular users, just mark trial as expired
        // No tier changes needed as per business model
        logger.info("Trial expired for user: {}", user.getEmail());
        
        try {
            resendEmailService.sendTrialExpiredEmail(
                user.getEmail(), 
                getUserDisplayName(user), 
                null // No tier info needed
            );
        } catch (Exception e) {
            logger.warn("Failed to send trial expired email: {}", e.getMessage());
        }
    }

    private String getUserDisplayName(User user) {
        if (user.getFirstName() != null && user.getLastName() != null) {
            return user.getFirstName() + " " + user.getLastName();
        } else if (user.getFirstName() != null) {
            return user.getFirstName();
        }
        return user.getEmail();
    }

    // stats method (same as earlier)
    public Map<String,Object> getTrialStatistics() {
        List<User> all = userRepository.findAll();
        long total = all.size();
        long inTrial = all.stream().filter(this::isInTrial).count();
        long expired = all.stream().filter(this::isTrialExpired).count();
        long never = all.stream().filter(u -> !u.getTrialUsed()).count();
        Map<String,Object> stats = new HashMap<>();
        stats.put("totalUsers", total);
        stats.put("usersInTrial", inTrial);
        stats.put("trialExpired", expired);
        stats.put("neverUsedTrial", never);
        stats.put("trialUtilizationRate", total > 0 ? (inTrial * 100.0 / total) : 0);
        return stats;
    }
    
    // Agency trial logic
    public void startAgencyTrial(User user, Agency agency) {
        logger.info("Starting {} month agency trial for agency: {}", TRIAL_MONTHS, agency.getName());
        
        user.startTrial(TRIAL_MONTHS * 30); // Convert months to days
        
        userRepository.save(user);

        try {
            resendEmailService.sendTrialStartedEmail(
                user.getEmail(), 
                getUserDisplayName(user),
                TRIAL_MONTHS
            );
        } catch (Exception e) {
            logger.warn("Failed to send agency trial started email: {}", e.getMessage());
        }

        logger.info("Agency trial started for {} (agency: {}) until {}", 
            user.getEmail(), agency.getName(), user.getTrialEndDate());
    }
    
    // Check if agency is in trial
    public boolean isAgencyInTrial(Agency agency) {
        User admin = agency.getAdmin();
        return isInTrial(admin); // Trial is based on dates, not tier
    }

    // Agency trial expiration handler
    private void handleAgencyTrialExpiration(User user, Agency agency) {
        // After trial, agency keeps their chosen tier but loses PRO features
        // The effective tier calculation in Agency entity handles this
        logger.info("Agency trial expired for user: {}, agency: {}", user.getEmail(), agency.getName());
        
        try {
            resendEmailService.sendTrialExpiredEmail(
                user.getEmail(),
                getUserDisplayName(user),
                agency.getTier() // Send their base tier
            );
        } catch (Exception e) {
            logger.warn("Failed to send agency trial expired email: {}", e.getMessage());
        }
    }
    
    // Enhanced trial statistics
    public Map<String, Object> getEnhancedTrialStatistics() {
        List<User> allUsers = userRepository.findAll();
        long total = allUsers.size();
        long inTrial = allUsers.stream().filter(this::isInTrial).count();
        long expired = allUsers.stream().filter(this::isTrialExpired).count();
        long never = allUsers.stream().filter(u -> !u.getTrialUsed()).count();
        
        // 🆕 Agency-specific stats
        long agencyUsers = allUsers.stream().filter(User::isAgencyAdmin).count();
        long agencyInTrial = allUsers.stream()
                .filter(User::isAgencyAdmin)
                .filter(this::isInTrial)
                .count();
        
        Map<String, Object> stats = new HashMap<>();
        stats.put("totalUsers", total);
        stats.put("usersInTrial", inTrial);
        stats.put("trialExpired", expired);
        stats.put("neverUsedTrial", never);
        stats.put("trialUtilizationRate", total > 0 ? (inTrial * 100.0 / total) : 0);
        
        // 🆕 Agency stats
        stats.put("agencyUsers", agencyUsers);
        stats.put("agencyUsersInTrial", agencyInTrial);
        stats.put("agencyTrialRate", agencyUsers > 0 ? (agencyInTrial * 100.0 / agencyUsers) : 0);
        
        return stats;
    }
    
    private void sendTrialExpiredNotification(User user) {
        try {
            resendEmailService.sendTrialExpiredEmail(
                user.getEmail(),
                getUserDisplayName(user),
                null // No tier info needed for basic users
            );
        } catch (Exception e) {
            logger.warn("Failed to send trial expired notification to {}: {}", user.getEmail(), e.getMessage());
        }
    }
}
