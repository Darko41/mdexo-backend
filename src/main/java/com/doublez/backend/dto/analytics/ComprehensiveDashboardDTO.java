package com.doublez.backend.dto.analytics;

import java.util.List;

import com.doublez.backend.dto.warning.SystemWarningDTO;

public class ComprehensiveDashboardDTO {
	
	private UserUsageDashboard userUsageDashboard;
    private TierAnalyticsDTO tierAnalytics;
    private TeamProductivityDTO teamProductivity;
    private MarketInsightsDTO marketInsights;
    private List<SystemWarningDTO> activeWarnings;
    
    // Getters and setters
	public UserUsageDashboard getUserUsageDashboard() {
		return userUsageDashboard;
	}
	public void setUserUsageDashboard(UserUsageDashboard userUsageDashboard) {
		this.userUsageDashboard = userUsageDashboard;
	}
	public TierAnalyticsDTO getTierAnalytics() {
		return tierAnalytics;
	}
	public void setTierAnalytics(TierAnalyticsDTO tierAnalytics) {
		this.tierAnalytics = tierAnalytics;
	}
	public TeamProductivityDTO getTeamProductivity() {
		return teamProductivity;
	}
	public void setTeamProductivity(TeamProductivityDTO teamProductivity) {
		this.teamProductivity = teamProductivity;
	}
	public MarketInsightsDTO getMarketInsights() {
		return marketInsights;
	}
	public void setMarketInsights(MarketInsightsDTO marketInsights) {
		this.marketInsights = marketInsights;
	}
	public List<SystemWarningDTO> getActiveWarnings() {
		return activeWarnings;
	}
	public void setActiveWarnings(List<SystemWarningDTO> activeWarnings) {
		this.activeWarnings = activeWarnings;
	}
    
    

}
