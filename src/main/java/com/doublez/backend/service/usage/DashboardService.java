package com.doublez.backend.service.usage;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.doublez.backend.dto.analytics.MarketInsightsDTO;
import com.doublez.backend.dto.analytics.TeamProductivityDTO;
import com.doublez.backend.dto.warning.SystemWarningDTO;
import com.doublez.backend.entity.user.User;
import com.doublez.backend.service.agency.TeamAnalyticsService;
import com.doublez.backend.service.agency.WarningService;
import com.doublez.backend.service.credit.CreditService;

@Service
public class DashboardService {
    
    @Autowired
    private TeamAnalyticsService teamAnalyticsService;
    
    @Autowired
    private WarningService warningService;
    
    @Autowired
    private UsageStatsService usageStatsService; 
    
    @Autowired
    private CreditService creditService;
    
    public ComprehensiveDashboardDTO getAgencyDashboard(Long agencyId, User user) {
        ComprehensiveDashboardDTO dashboard = new ComprehensiveDashboardDTO();
        
        // 1. Use your existing UserUsageDashboard
        UserUsageDashboard userDashboard = getUserUsageDashboard(user);
        dashboard.setUserUsageDashboard(userDashboard);
        
        // 2. Add team analytics
        TierAnalyticsDTO tierAnalytics = teamAnalyticsService.getTierAnalytics(agencyId);
        dashboard.setTierAnalytics(tierAnalytics);
        
        // 3. Add team productivity (last 30 days)
        LocalDateTime monthAgo = LocalDateTime.now().minusDays(30);
        TeamProductivityDTO teamProductivity = teamAnalyticsService.getTeamProductivity(
            agencyId, monthAgo, LocalDateTime.now());
        dashboard.setTeamProductivity(teamProductivity);
        
        // 4. Add warnings
        List<SystemWarningDTO> warnings = warningService.getActiveWarningsForAgency(agencyId);
        dashboard.setActiveWarnings(warnings);
        
        // 5. Add market insights
        MarketInsightsDTO marketInsights = teamAnalyticsService.getMarketInsights(agencyId);
        dashboard.setMarketInsights(marketInsights);
        
        return dashboard;
    }
}