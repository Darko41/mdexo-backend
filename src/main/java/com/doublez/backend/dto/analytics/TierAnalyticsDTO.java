package com.doublez.backend.dto.analytics;

import java.util.List;

import com.doublez.backend.entity.user.UserTier;

public class TierAnalyticsDTO {
    private Long agencyId;
    private UserTier currentTier;
    private Integer currentAgentCount;
    private Integer maxAgentsAllowed;
    private Integer currentListings;
    private Integer maxListingsAllowed;
    private Integer currentImages;
    private Integer maxImagesAllowed;
    private Integer currentSuperAgents;
    private Integer maxSuperAgentsAllowed;
    private Integer agentUtilization;
    private Integer listingUtilization;
    private Integer imageUtilization;
    private List<TierRecommendationDTO> tierRecommendations;
    
	public Long getAgencyId() {
		return agencyId;
	}
	public void setAgencyId(Long agencyId) {
		this.agencyId = agencyId;
	}
	public UserTier getCurrentTier() {
		return currentTier;
	}
	public void setCurrentTier(UserTier currentTier) {
		this.currentTier = currentTier;
	}
	public Integer getCurrentAgentCount() {
		return currentAgentCount;
	}
	public void setCurrentAgentCount(Integer currentAgentCount) {
		this.currentAgentCount = currentAgentCount;
	}
	public Integer getMaxAgentsAllowed() {
		return maxAgentsAllowed;
	}
	public void setMaxAgentsAllowed(Integer maxAgentsAllowed) {
		this.maxAgentsAllowed = maxAgentsAllowed;
	}
	public Integer getCurrentListings() {
		return currentListings;
	}
	public void setCurrentListings(Integer currentListings) {
		this.currentListings = currentListings;
	}
	public Integer getMaxListingsAllowed() {
		return maxListingsAllowed;
	}
	public void setMaxListingsAllowed(Integer maxListingsAllowed) {
		this.maxListingsAllowed = maxListingsAllowed;
	}
	public Integer getCurrentImages() {
		return currentImages;
	}
	public void setCurrentImages(Integer currentImages) {
		this.currentImages = currentImages;
	}
	public Integer getMaxImagesAllowed() {
		return maxImagesAllowed;
	}
	public void setMaxImagesAllowed(Integer maxImagesAllowed) {
		this.maxImagesAllowed = maxImagesAllowed;
	}
	public Integer getCurrentSuperAgents() {
		return currentSuperAgents;
	}
	public void setCurrentSuperAgents(Integer currentSuperAgents) {
		this.currentSuperAgents = currentSuperAgents;
	}
	public Integer getMaxSuperAgentsAllowed() {
		return maxSuperAgentsAllowed;
	}
	public void setMaxSuperAgentsAllowed(Integer maxSuperAgentsAllowed) {
		this.maxSuperAgentsAllowed = maxSuperAgentsAllowed;
	}
	public Integer getAgentUtilization() {
		return agentUtilization;
	}
	public void setAgentUtilization(Integer agentUtilization) {
		this.agentUtilization = agentUtilization;
	}
	public Integer getListingUtilization() {
		return listingUtilization;
	}
	public void setListingUtilization(Integer listingUtilization) {
		this.listingUtilization = listingUtilization;
	}
	public Integer getImageUtilization() {
		return imageUtilization;
	}
	public void setImageUtilization(Integer imageUtilization) {
		this.imageUtilization = imageUtilization;
	}
	public List<TierRecommendationDTO> getTierRecommendations() {
		return tierRecommendations;
	}
	public void setTierRecommendations(List<TierRecommendationDTO> tierRecommendations) {
		this.tierRecommendations = tierRecommendations;
	}

}
