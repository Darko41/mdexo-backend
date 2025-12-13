package com.doublez.backend.service.agency;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import com.doublez.backend.dto.warning.SystemWarningDTO;
import com.doublez.backend.entity.agency.Agency;
import com.doublez.backend.entity.agency.Agent;
import com.doublez.backend.entity.realestate.RealEstate;
import com.doublez.backend.entity.user.User;
import com.doublez.backend.repository.AgencyRepository;
import com.doublez.backend.repository.AgentRepository;
import com.doublez.backend.repository.InvitationRepository;
import com.doublez.backend.repository.RealEstateRepository;
import com.doublez.backend.service.email.ResendEmailService;

@Service
public class WarningService {
	
	private final Logger logger = LoggerFactory.getLogger(WarningService.class);
    
    @Autowired
    private AgentRepository agentRepository;
    
    @Autowired
    private AgencyRepository agencyRepository;
    
    @Autowired
    private RealEstateRepository realEstateRepository;
    
    @Autowired
    private InvitationRepository invitationRepository;
    
    @Autowired
    private ResendEmailService emailService;
    
    @Value("${app.warnings.enabled:true}")
    private boolean warningsEnabled;
    
    @Value("${app.warnings.check-interval:3600}") // 1 hour in seconds
    private int checkInterval;
    
    // ========================
    // WARNING GENERATION
    // ========================
    
    @Scheduled(fixedDelayString = "${app.warnings.check-interval:3600000}")
    public void generateWarnings() {
        if (!warningsEnabled) return;
        
        logger.info("Generating system warnings...");
        
        // Check for various warning conditions
        checkInactiveAgents();
        checkListingLimits();
        checkExpiringTrials();
        checkLowPerformance();
        checkUnusedCredits();
        
        logger.info("Warning generation completed");
    }
    
    public List<SystemWarningDTO> getActiveWarningsForAgency(Long agencyId) {
        List<SystemWarningDTO> warnings = new ArrayList<>();
        
        // Add various warnings
        warnings.addAll(getInactiveAgentWarnings(agencyId));
        warnings.addAll(getListingLimitWarnings(agencyId));
        warnings.addAll(getTrialExpiryWarnings(agencyId));
        warnings.addAll(getPerformanceWarnings(agencyId));
        
        return warnings.stream()
                .sorted(Comparator.comparing(SystemWarningDTO::getSeverity).reversed())
                .collect(Collectors.toList());
    }
    
    // ========================
    // SPECIFIC WARNING CHECKS
    // ========================
    
    private void checkInactiveAgents() {
        LocalDateTime threshold = LocalDateTime.now().minusDays(30);
        
        // Get all active agents from all agencies
        List<Agent> allAgents = agentRepository.findAll().stream()
                .filter(Agent::getIsActive)
                .collect(Collectors.toList());
        
        for (Agent agent : allAgents) {
            if (agent.getLastActiveDate() != null && 
                    agent.getLastActiveDate().isBefore(threshold)) {
                createWarning(
                    agent.getAgency().getId(),
                    "INACTIVE_AGENT",
                    "Agent " + agent.getUser().getEmail() + " je neaktivan više od 30 dana",
                    "Razmotrite deaktivaciju agenta ili kontakt",
                    "MEDIUM",
                    agent.getId()
                );
                
                sendInactiveAgentWarning(agent);
            }
        }
    }
    
    private void checkListingLimits() {
        // Get all agencies
        List<Agency> agencies = agencyRepository.findAll();
        
        for (Agency agency : agencies) {
            Long activeListings = realEstateRepository.countActiveRealEstatesByAgency(agency.getId());
            int maxListings = agency.getEffectiveTier().getMaxListingsSafe();
            
            if (activeListings != null) {
                int utilization = (int) ((double) activeListings / maxListings * 100);
                
                if (utilization >= 90) {
                    createWarning(
                        agency.getId(),
                        "LISTING_LIMIT_CRITICAL",
                        "Dostignuto 90% limita za oglase (" + activeListings + "/" + maxListings + ")",
                        "Nadogradite tier ili arhivirajte stare oglase",
                        "HIGH",
                        null
                    );
                } else if (utilization >= 75) {
                    createWarning(
                        agency.getId(),
                        "LISTING_LIMIT_WARNING",
                        "Dostignuto 75% limita za oglase (" + activeListings + "/" + maxListings + ")",
                        "Razmislite o nadogradnji",
                        "MEDIUM",
                        null
                    );
                }
            }
        }
    }
    
    private void checkExpiringTrials() {
        List<Agency> trialAgencies = agencyRepository.findAll().stream()
                .filter(agency -> agency.isInTrialPeriod())
                .collect(Collectors.toList());
        
        for (Agency agency : trialAgencies) {
            long daysRemaining = agency.getTrialDaysRemaining();
            
            if (daysRemaining <= 3) {
                createWarning(
                    agency.getId(),
                    "TRIAL_EXPIRING_CRITICAL",
                    "Probni period ističe za " + daysRemaining + " dan(a)",
                    "Odaberite paket pre isteka probnog perioda",
                    "HIGH",
                    null
                );
            } else if (daysRemaining <= 7) {
                createWarning(
                    agency.getId(),
                    "TRIAL_EXPIRING_WARNING",
                    "Probni period ističe za " + daysRemaining + " dan(a)",
                    "Odaberite paket uskoro",
                    "MEDIUM",
                    null
                );
            }
        }
    }
    
    private void checkLowPerformance() {
        LocalDateTime monthAgo = LocalDateTime.now().minusMonths(1);
        
        List<Agent> agents = agentRepository.findAll().stream()
                .filter(Agent::getIsActive)
                .collect(Collectors.toList());
        
        for (Agent agent : agents) {
            // Check deals in last month
            int dealsLastMonth = getDealsForAgent(agent.getId(), monthAgo, LocalDateTime.now());
            
            if (dealsLastMonth == 0) {
                // Check if agent has listings
                List<RealEstate> agentListings = realEstateRepository.findByAgencyAndListingAgent(
                    agent.getAgency(), agent);
                
                if (!agentListings.isEmpty()) {
                    createWarning(
                        agent.getAgency().getId(),
                        "LOW_PERFORMANCE",
                        "Agent " + agent.getUser().getEmail() + " nije zatvorio nijedan posao u poslednjih 30 dana",
                        "Razmotrite dodatnu obuku ili podršku",
                        "LOW",
                        agent.getId()
                    );
                }
            }
        }
    }
    
    private void checkUnusedCredits() {
        // Implement with your credit system
        // Check agencies with unused credits for more than 60 days
    }
    
    // ========================
    // WARNING GETTERS
    // ========================
    
    private List<SystemWarningDTO> getInactiveAgentWarnings(Long agencyId) {
        LocalDateTime threshold = LocalDateTime.now().minusDays(30);
        
        Agency agency = new Agency();
        agency.setId(agencyId);
        
        return agentRepository.findByAgencyAndIsActive(agency, true).stream()
                .filter(agent -> agent.getLastActiveDate() != null && 
                        agent.getLastActiveDate().isBefore(threshold))
                .map(agent -> {
                    SystemWarningDTO warning = new SystemWarningDTO();
                    warning.setCode("INACTIVE_AGENT");
                    warning.setTitle("Neaktivan agent: " + agent.getUser().getEmail());
                    warning.setDescription("Poslednja aktivnost: " + 
                            agent.getLastActiveDate().format(DateTimeFormatter.ofPattern("dd.MM.yyyy.")));
                    warning.setSeverity("MEDIUM");
                    warning.setGeneratedAt(LocalDateTime.now());
                    warning.setRelatedId(agent.getId());
                    return warning;
                })
                .collect(Collectors.toList());
    }
    
    private List<SystemWarningDTO> getListingLimitWarnings(Long agencyId) {
        List<SystemWarningDTO> warnings = new ArrayList<>();
        
        Agency agency = agencyRepository.findById(agencyId).orElse(null);
        if (agency == null) return warnings;
        
        Long activeListings = realEstateRepository.countActiveRealEstatesByAgency(agencyId);
        int maxListings = agency.getEffectiveTier().getMaxListingsSafe();
        
        if (activeListings != null) {
            int utilization = (int) ((double) activeListings / maxListings * 100);
            
            if (utilization >= 90) {
                warnings.add(new SystemWarningDTO(
                    "LISTING_LIMIT_CRITICAL",
                    "Dostignuto 90% limita za oglase",
                    "Trenutno: " + activeListings + ", Limit: " + maxListings,
                    "HIGH",
                    LocalDateTime.now(),
                    null
                ));
            } else if (utilization >= 75) {
                warnings.add(new SystemWarningDTO(
                    "LISTING_LIMIT_WARNING",
                    "Dostignuto 75% limita za oglase",
                    "Trenutno: " + activeListings + ", Limit: " + maxListings,
                    "MEDIUM",
                    LocalDateTime.now(),
                    null
                ));
            }
        }
        
        return warnings;
    }
    
    private List<SystemWarningDTO> getTrialExpiryWarnings(Long agencyId) {
        List<SystemWarningDTO> warnings = new ArrayList<>();
        
        Agency agency = agencyRepository.findById(agencyId).orElse(null);
        if (agency == null || !agency.isInTrialPeriod()) return warnings;
        
        long daysRemaining = agency.getTrialDaysRemaining();
        
        if (daysRemaining <= 3) {
            warnings.add(new SystemWarningDTO(
                "TRIAL_EXPIRING_CRITICAL",
                "Probni period ističe za " + daysRemaining + " dan(a)",
                "Odaberite paket pre isteka",
                "HIGH",
                LocalDateTime.now(),
                null
            ));
        } else if (daysRemaining <= 7) {
            warnings.add(new SystemWarningDTO(
                "TRIAL_EXPIRING_WARNING",
                "Probni period ističe za " + daysRemaining + " dan(a)",
                "Razmislite o odabiru paketa",
                "MEDIUM",
                LocalDateTime.now(),
                null
            ));
        }
        
        return warnings;
    }
    
    private List<SystemWarningDTO> getPerformanceWarnings(Long agencyId) {
        List<SystemWarningDTO> warnings = new ArrayList<>();
        LocalDateTime monthAgo = LocalDateTime.now().minusMonths(1);
        
        // FIXED: Create Agency object properly
        Agency agency = new Agency();
        agency.setId(agencyId);
        
        List<Agent> agents = agentRepository.findByAgencyAndIsActive(agency, true);
        
        for (Agent agent : agents) {
            int dealsLastMonth = getDealsForAgent(agent.getId(), monthAgo, LocalDateTime.now());
            List<RealEstate> agentListings = realEstateRepository.findByAgencyAndListingAgent(
                agent.getAgency(), agent);
            
            if (dealsLastMonth == 0 && !agentListings.isEmpty()) {
                SystemWarningDTO warning = new SystemWarningDTO();
                warning.setCode("LOW_PERFORMANCE");
                warning.setTitle("Agent " + agent.getUser().getEmail() + " bez poslova 30+ dana");
                warning.setDescription("Imate " + agentListings.size() + " aktivnih oglasa");
                warning.setSeverity("LOW");
                warning.setGeneratedAt(LocalDateTime.now());
                warning.setRelatedId(agent.getId());
                warnings.add(warning);
            }
        }
        
        return warnings;
    }
    
    // ========================
    // HELPER METHODS
    // ========================
    
    private void createWarning(Long agencyId, String code, String title, String description, 
                              String severity, Long relatedId) {
        // Save warning to database (implement Warning entity/repository)
        // For now, just log it
        logger.warn("Warning generated for agency {}: {} - {}", agencyId, title, description);
    }
    
    private void sendInactiveAgentWarning(Agent agent) {
        try {
            String subject = "Upozorenje: Neaktivan agent - " + agent.getAgency().getName();
            String message = "Agent " + agent.getUser().getEmail() + 
                           " je neaktivan više od 30 dana. Poslednja aktivnost: " + 
                           agent.getLastActiveDate().format(DateTimeFormatter.ofPattern("dd.MM.yyyy."));
            
            // Send to agency owner
            User owner = agent.getAgency().getAdmin();
            emailService.sendTextEmail(owner.getEmail(), subject, message);
            
        } catch (Exception e) {
            logger.error("Failed to send inactive agent warning email", e);
        }
    }
    
    private int getDealsForAgent(Long agentId, LocalDateTime start, LocalDateTime end) {
        // Implement with your deal tracking
        return 0;
    }
}
