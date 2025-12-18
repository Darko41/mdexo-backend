package com.doublez.backend.controller;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.doublez.backend.dto.analytics.AgentProductivityDTO;
import com.doublez.backend.dto.analytics.MarketInsightsDTO;
import com.doublez.backend.dto.analytics.TeamProductivityDTO;
import com.doublez.backend.dto.analytics.TierAnalyticsDTO;
import com.doublez.backend.dto.warning.SystemWarningDTO;
import com.doublez.backend.entity.user.User;
import com.doublez.backend.service.agency.TeamAnalyticsService;
import com.doublez.backend.service.agency.WarningService;
import com.doublez.backend.utils.SecurityUtils;

@RestController
@RequestMapping("/api/v1/analytics")
public class TeamAnalyticsController {
    
    @Autowired
    private TeamAnalyticsService analyticsService;
    
    @Autowired
    private WarningService warningService;
    
    @Autowired
    private SecurityUtils securityUtils;
    
    @GetMapping("/agency/{agencyId}/productivity")
    @PreAuthorize("hasRole('AGENCY')")
    public ResponseEntity<TeamProductivityDTO> getTeamProductivity(
            @PathVariable Long agencyId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate) {
        
        User currentUser = securityUtils.getCurrentUser();
        
        // Default to last 30 days
        if (startDate == null) startDate = LocalDateTime.now().minusDays(30);
        if (endDate == null) endDate = LocalDateTime.now();
        
        TeamProductivityDTO productivity = analyticsService.getTeamProductivity(agencyId, startDate, endDate);
        return ResponseEntity.ok(productivity);
    }
    
    @GetMapping("/agent/{agentId}/productivity")
    @PreAuthorize("hasRole('AGENCY')")
    public ResponseEntity<AgentProductivityDTO> getAgentProductivity(
            @PathVariable Long agentId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate) {
        
        // Default to last 30 days
        if (startDate == null) startDate = LocalDateTime.now().minusDays(30);
        if (endDate == null) endDate = LocalDateTime.now();
        
        AgentProductivityDTO productivity = analyticsService.getAgentProductivity(agentId, startDate, endDate);
        return ResponseEntity.ok(productivity);
    }
    
    @GetMapping("/agency/{agencyId}/tier-analytics")
    @PreAuthorize("hasRole('AGENCY')")
    public ResponseEntity<TierAnalyticsDTO> getTierAnalytics(@PathVariable Long agencyId) {
        TierAnalyticsDTO analytics = analyticsService.getTierAnalytics(agencyId);
        return ResponseEntity.ok(analytics);
    }
    
    @GetMapping("/agency/{agencyId}/market-insights")
    @PreAuthorize("hasRole('AGENCY')")
    public ResponseEntity<MarketInsightsDTO> getMarketInsights(@PathVariable Long agencyId) {
        MarketInsightsDTO insights = analyticsService.getMarketInsights(agencyId);
        return ResponseEntity.ok(insights);
    }
    
    @GetMapping("/agency/{agencyId}/warnings")
    @PreAuthorize("hasRole('AGENCY')")
    public ResponseEntity<List<SystemWarningDTO>> getActiveWarnings(@PathVariable Long agencyId) {
        List<SystemWarningDTO> warnings = warningService.getActiveWarningsForAgency(agencyId);
        return ResponseEntity.ok(warnings);
    }
    
//    @GetMapping("/agency/{agencyId}/dashboard")
//    @PreAuthorize("hasRole('AGENCY')")
//    public ResponseEntity<AnalyticsDashboardDTO> getAnalyticsDashboard(@PathVariable Long agencyId) {
//        AnalyticsDashboardDTO dashboard = new AnalyticsDashboardDTO();
//        
//        // Get various analytics
//        dashboard.setTierAnalytics(analyticsService.getTierAnalytics(agencyId));
//        dashboard.setMarketInsights(analyticsService.getMarketInsights(agencyId));
//        
//        // Get productivity for last 30 days
//        LocalDateTime monthAgo = LocalDateTime.now().minusDays(30);
//        dashboard.setTeamProductivity(analyticsService.getTeamProductivity(agencyId, monthAgo, LocalDateTime.now()));
//        
//        // Get active warnings
//        dashboard.setActiveWarnings(warningService.getActiveWarningsForAgency(agencyId));
//        
//        return ResponseEntity.ok(dashboard);
//    }
}
