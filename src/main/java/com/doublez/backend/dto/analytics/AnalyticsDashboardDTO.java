package com.doublez.backend.dto.analytics;

import java.util.List;

import com.doublez.backend.dto.warnings.SystemWarningDTO;

public class AnalyticsDashboardDTO {
    private TierAnalyticsDTO tierAnalytics;
    private MarketInsightsDTO marketInsights;
    private TeamProductivityDTO teamProductivity;
    private List<SystemWarningDTO> activeWarnings;
    
	public TierAnalyticsDTO getTierAnalytics() {
		return tierAnalytics;
	}
	public void setTierAnalytics(TierAnalyticsDTO tierAnalytics) {
		this.tierAnalytics = tierAnalytics;
	}
	public MarketInsightsDTO getMarketInsights() {
		return marketInsights;
	}
	public void setMarketInsights(MarketInsightsDTO marketInsights) {
		this.marketInsights = marketInsights;
	}
	public TeamProductivityDTO getTeamProductivity() {
		return teamProductivity;
	}
	public void setTeamProductivity(TeamProductivityDTO teamProductivity) {
		this.teamProductivity = teamProductivity;
	}
	public List<SystemWarningDTO> getActiveWarnings() {
		return activeWarnings;
	}
	public void setActiveWarnings(List<SystemWarningDTO> activeWarnings) {
		this.activeWarnings = activeWarnings;
	}
    
    

}
