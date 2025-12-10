package com.doublez.backend.service.agency;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.doublez.backend.dto.analytics.AgentProductivityDTO;
import com.doublez.backend.dto.analytics.MarketInsightsDTO;
import com.doublez.backend.dto.analytics.TeamProductivityDTO;
import com.doublez.backend.dto.analytics.TierAnalyticsDTO;
import com.doublez.backend.dto.analytics.TierRecommendationDTO;
import com.doublez.backend.entity.RealEstate;
import com.doublez.backend.entity.agency.Agency;
import com.doublez.backend.entity.agency.Agent;
import com.doublez.backend.enums.agency.AgentRole;
import com.doublez.backend.exception.ResourceNotFoundException;
import com.doublez.backend.repository.AgencyRepository;
import com.doublez.backend.repository.AgentRepository;
import com.doublez.backend.repository.InvitationRepository;
import com.doublez.backend.repository.RealEstateRepository;
import com.doublez.backend.service.usage.TierLimitationService;

@Service
public class TeamAnalyticsService {
    
    @Autowired
    private AgentRepository agentRepository;
    
    @Autowired
    private RealEstateRepository realEstateRepository;
    
    @Autowired
    private InvitationRepository invitationRepository;
    
    @Autowired
    private TierLimitationService tierLimitationService;
    
    @Autowired
    private AgencyRepository agencyRepository;
    
    // ========================
    // AGENT PRODUCTIVITY METRICS
    // ========================
    
    public AgentProductivityDTO getAgentProductivity(Long agentId, LocalDateTime startDate, LocalDateTime endDate) {
        Agent agent = agentRepository.findById(agentId)
                .orElseThrow(() -> new ResourceNotFoundException("Agent not found"));
        
        AgentProductivityDTO productivity = new AgentProductivityDTO();
        productivity.setAgentId(agentId);
        productivity.setAgentName(agent.getUser().getFirstName() + " " + agent.getUser().getLastName());
        productivity.setPeriodStart(startDate);
        productivity.setPeriodEnd(endDate);
        
        // Get listings created in period
        List<RealEstate> listings = getAgentListingsInPeriod(agent, startDate, endDate);
        productivity.setListingsCreated(listings.size());
        
        // Calculate average price of listings
        BigDecimal avgPrice = BigDecimal.ZERO;
        if (!listings.isEmpty()) {
            avgPrice = listings.stream()
                    .map(RealEstate::getPrice)
                    .reduce(BigDecimal.ZERO, BigDecimal::add)
                    .divide(BigDecimal.valueOf(listings.size()), 2, RoundingMode.HALF_UP);
        }
        productivity.setAverageListingPrice(avgPrice);
        
        // Get leads generated
        int leads = getLeadsForAgent(agentId, startDate, endDate);
        productivity.setLeadsGenerated(leads);
        
        // Get deals closed
        int deals = getDealsForAgent(agentId, startDate, endDate);
        productivity.setDealsClosed(deals);
        
        // Calculate conversion rate
        if (leads > 0) {
            double conversionRate = (double) deals / leads * 100;
            productivity.setConversionRate(conversionRate);
        } else {
            productivity.setConversionRate(0.0);
        }
        
        // Response time
        productivity.setAverageResponseTime(agent.getAverageResponseTimeMinutes());
        
        // Performance rating
        productivity.setPerformanceRating(calculatePerformanceRating(productivity));
        
        return productivity;
    }
    
    public TeamProductivityDTO getTeamProductivity(Long agencyId, LocalDateTime startDate, LocalDateTime endDate) {
        // Fixed: Get agency properly
        Agency agency = agencyRepository.findById(agencyId)
                .orElseThrow(() -> new ResourceNotFoundException("Agency not found"));
        
        List<Agent> agents = agentRepository.findByAgencyAndIsActive(agency, true);
        
        TeamProductivityDTO teamProductivity = new TeamProductivityDTO();
        teamProductivity.setAgencyId(agencyId);
        teamProductivity.setPeriodStart(startDate);
        teamProductivity.setPeriodEnd(endDate);
        
        // Individual agent productivity
        List<AgentProductivityDTO> agentProductivities = new ArrayList<>();
        int totalListings = 0;
        int totalLeads = 0;
        int totalDeals = 0;
        BigDecimal totalRevenue = BigDecimal.ZERO;
        
        for (Agent agent : agents) {
            AgentProductivityDTO agentProductivity = getAgentProductivity(agent.getId(), startDate, endDate);
            agentProductivities.add(agentProductivity);
            
            totalListings += agentProductivity.getListingsCreated();
            totalLeads += agentProductivity.getLeadsGenerated();
            totalDeals += agentProductivity.getDealsClosed();
            totalRevenue = totalRevenue.add(agentProductivity.getAverageListingPrice()
                    .multiply(BigDecimal.valueOf(agentProductivity.getDealsClosed())));
        }
        
        teamProductivity.setAgentProductivities(agentProductivities);
        teamProductivity.setTotalListingsCreated(totalListings);
        teamProductivity.setTotalLeadsGenerated(totalLeads);
        teamProductivity.setTotalDealsClosed(totalDeals);
        teamProductivity.setTotalRevenue(totalRevenue);
        
        // Team conversion rate
        if (totalLeads > 0) {
            double teamConversionRate = (double) totalDeals / totalLeads * 100;
            teamProductivity.setTeamConversionRate(teamConversionRate);
        }
        
        // Top performers
        teamProductivity.setTopPerformers(getTopPerformers(agentProductivities, 3));
        
        return teamProductivity;
    }
    
    private List<AgentProductivityDTO> getTopPerformers(List<AgentProductivityDTO> productivities, int limit) {
        return productivities.stream()
                .sorted((a, b) -> Integer.compare(b.getDealsClosed(), a.getDealsClosed()))
                .limit(limit)
                .collect(Collectors.toList());
    }
    
    private String calculatePerformanceRating(AgentProductivityDTO productivity) {
        if (productivity.getListingsCreated() == 0) {
            return "Bez oglasa";
        }
        
        if (productivity.getDealsClosed() == 0) {
            return "Nema poslova";
        }
        
        double dealsPerListing = (double) productivity.getDealsClosed() / productivity.getListingsCreated();
        
        if (dealsPerListing > 0.3) return "Odlično";
        if (dealsPerListing > 0.2) return "Dobro";
        if (dealsPerListing > 0.1) return "Prosečno";
        return "Potrebno poboljšanje";
    }
    
    // ========================
    // TIER-BASED ANALYTICS
    // ========================
    
    public TierAnalyticsDTO getTierAnalytics(Long agencyId) {
        Agency agency = agencyRepository.findById(agencyId)
                .orElseThrow(() -> new ResourceNotFoundException("Agency not found"));
        
        TierAnalyticsDTO analytics = new TierAnalyticsDTO();
        analytics.setAgencyId(agencyId);
        analytics.setCurrentTier(agency.getEffectiveTier());
        
        // Current usage - Fixed constructor issue
        List<Agent> agents = agentRepository.findByAgencyAndIsActive(agency, true);
        analytics.setCurrentAgentCount(agents.size());
        analytics.setMaxAgentsAllowed(tierLimitationService.getMaxAgentsForAgency(agency));
        
        // Listing usage
        Long activeListings = realEstateRepository.countActiveRealEstatesByAgency(agencyId);
        analytics.setCurrentListings(activeListings != null ? activeListings.intValue() : 0);
        analytics.setMaxListingsAllowed(agency.getEffectiveTier().getMaxListingsSafe());
        
        // Image usage
        Long totalImages = realEstateRepository.getTotalImageCountByAgencyId(agencyId);
        analytics.setCurrentImages(totalImages != null ? totalImages.intValue() : 0);
        analytics.setMaxImagesAllowed(agency.getEffectiveTier().getMaxImagesSafe());
        
        // Super agent usage
        long superAgentCount = agentRepository.countByAgencyAndRole(agency, AgentRole.SUPER_AGENT);
        analytics.setCurrentSuperAgents((int) superAgentCount);
        analytics.setMaxSuperAgentsAllowed(tierLimitationService.getMaxSuperAgentsForAgency(agency));
        
        // Calculate utilization percentages
        analytics.setAgentUtilization(calculateUtilization(
                analytics.getCurrentAgentCount(), analytics.getMaxAgentsAllowed()));
        analytics.setListingUtilization(calculateUtilization(
                analytics.getCurrentListings(), analytics.getMaxListingsAllowed()));
        analytics.setImageUtilization(calculateUtilization(
                analytics.getCurrentImages(), analytics.getMaxImagesAllowed()));
        
        // Tier recommendations - Fixed constructor
        analytics.setTierRecommendations(getTierRecommendations(analytics));
        
        return analytics;
    }
    
    private int calculateUtilization(int current, int max) {
        if (max <= 0) return 0;
        if (current <= 0) return 0;
        return Math.min(100, (int) ((double) current / max * 100));
    }
    
    private List<TierRecommendationDTO> getTierRecommendations(TierAnalyticsDTO analytics) {
        List<TierRecommendationDTO> recommendations = new ArrayList<>();
        
        // Agent limit warning
        if (analytics.getAgentUtilization() >= 80) {
            TierRecommendationDTO rec = new TierRecommendationDTO();
            rec.setCode("AGENT_LIMIT");
            rec.setTitle("Dostignut je limit za agente");
            rec.setDescription("Razmislite o nadogradnji na viši tier za više agenata");
            rec.setSeverity("WARNING");
            recommendations.add(rec);
        }
        
        // Listing limit warning
        if (analytics.getListingUtilization() >= 90) {
            TierRecommendationDTO rec = new TierRecommendationDTO();
            rec.setCode("LISTING_LIMIT");
            rec.setTitle("Dostignut je limit za oglase");
            rec.setDescription("Nadogradite tier za više oglasa");
            rec.setSeverity("CRITICAL");
            recommendations.add(rec);
        } else if (analytics.getListingUtilization() >= 75) {
            TierRecommendationDTO rec = new TierRecommendationDTO();
            rec.setCode("LISTING_LIMIT_WARNING");
            rec.setTitle("Dostignuto 75% limita za oglase");
            rec.setDescription("Razmislite o nadogradnji");
            rec.setSeverity("WARNING");
            recommendations.add(rec);
        }
        
        // Image limit warning
        if (analytics.getImageUtilization() >= 85) {
            TierRecommendationDTO rec = new TierRecommendationDTO();
            rec.setCode("IMAGE_LIMIT");
            rec.setTitle("Dostignut je limit za slike");
            rec.setDescription("Nadogradite tier za više slika");
            rec.setSeverity("WARNING");
            recommendations.add(rec);
        }
        
        // Super agent limit warning
        if (analytics.getCurrentSuperAgents() >= analytics.getMaxSuperAgentsAllowed()) {
            TierRecommendationDTO rec = new TierRecommendationDTO();
            rec.setCode("SUPER_AGENT_LIMIT");
            rec.setTitle("Dostignut je limit za super agente");
            rec.setDescription("Nadogradite tier za više super agenata");
            rec.setSeverity("WARNING");
            recommendations.add(rec);
        }
        
        // Trial period warning (if applicable)
        Agency agency = agencyRepository.findById(analytics.getAgencyId()).orElse(null);
        if (agency != null && agency.isInTrialPeriod()) {
            long daysRemaining = agency.getTrialDaysRemaining();
            if (daysRemaining <= 7) {
                TierRecommendationDTO rec = new TierRecommendationDTO();
                rec.setCode("TRIAL_EXPIRING");
                rec.setTitle("Probni period ističe za " + daysRemaining + " dan(a)");
                rec.setDescription("Odaberite paket pre isteka probnog perioda");
                rec.setSeverity(daysRemaining <= 3 ? "CRITICAL" : "WARNING");
                recommendations.add(rec);
            }
        }
        
        return recommendations;
    }
    
    // ========================
    // MARKET INSIGHTS
    // ========================
    
    public MarketInsightsDTO getMarketInsights(Long agencyId) {
        Agency agency = agencyRepository.findById(agencyId)
                .orElseThrow(() -> new ResourceNotFoundException("Agency not found"));
        
        MarketInsightsDTO insights = new MarketInsightsDTO();
        insights.setAgencyId(agencyId);
        insights.setCity(agency.getCity());
        
        // Get agency's active listings
        List<RealEstate> agencyListings = realEstateRepository.findActiveRealEstatesByAgency(agencyId);
        
        // Calculate average price for agency
        BigDecimal agencyAvgPrice = calculateAveragePrice(agencyListings);
        insights.setAgencyAveragePrice(agencyAvgPrice);
        
        // Get all listings in same city for comparison
        Page<RealEstate> cityListings = realEstateRepository.findByMultipleCriteria(
                null, null, agency.getCity(), null, null, null, null, null, null, null, Pageable.unpaged()
        );
        
        BigDecimal cityAvgPrice = calculateAveragePrice(cityListings.getContent());
        insights.setMarketAveragePrice(cityAvgPrice);
        
        // Price comparison
        if (cityAvgPrice.compareTo(BigDecimal.ZERO) > 0) {
            double priceDifference = agencyAvgPrice.subtract(cityAvgPrice)
                    .divide(cityAvgPrice, 4, RoundingMode.HALF_UP)
                    .multiply(BigDecimal.valueOf(100))
                    .doubleValue();
            insights.setPriceVsMarket(priceDifference);
        }
        
        // Days on market (average)
        double avgDaysOnMarket = agencyListings.stream()
                .mapToDouble(listing -> ChronoUnit.DAYS.between(listing.getCreatedAt(), LocalDateTime.now()))
                .average()
                .orElse(0);
        insights.setAverageDaysOnMarket(avgDaysOnMarket);
        
        // Listing performance by type
        Map<String, Long> listingsByType = agencyListings.stream()
                .collect(Collectors.groupingBy(
                    listing -> listing.getPropertyType().name(),
                    Collectors.counting()
                ));
        insights.setListingsByType(listingsByType);
        
        return insights;
    }
    
    private BigDecimal calculateAveragePrice(List<RealEstate> listings) {
        if (listings.isEmpty()) return BigDecimal.ZERO;
        
        return listings.stream()
                .map(RealEstate::getPrice)
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .divide(BigDecimal.valueOf(listings.size()), 2, RoundingMode.HALF_UP);
    }
    
    // ========================
    // HELPER METHODS (Placeholders - TODO: implement with your systems)
    // ========================
    
    private List<RealEstate> getAgentListingsInPeriod(Agent agent, LocalDateTime start, LocalDateTime end) {
        List<RealEstate> agentListings = realEstateRepository.findByAgencyAndListingAgent(agent.getAgency(), agent);
        
        return agentListings.stream()
                .filter(listing -> !listing.getCreatedAt().isBefore(start) && !listing.getCreatedAt().isAfter(end))
                .collect(Collectors.toList());
    }
    
    // TODO: Implement this
    // Placeholder methods - implement with your systems
    private int getLeadsForAgent(Long agentId, LocalDateTime start, LocalDateTime end) {
        // Implement with your lead tracking system
        return 0;
    }
    
    private int getDealsForAgent(Long agentId, LocalDateTime start, LocalDateTime end) {
        // Implement with your deal tracking system
        return 0;
    }
}
