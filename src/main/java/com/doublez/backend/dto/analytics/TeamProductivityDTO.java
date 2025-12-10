package com.doublez.backend.dto.analytics;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public class TeamProductivityDTO {
    private Long agencyId;
    private LocalDateTime periodStart;
    private LocalDateTime periodEnd;
    private List<AgentProductivityDTO> agentProductivities;
    private Integer totalListingsCreated;
    private Integer totalLeadsGenerated;
    private Integer totalDealsClosed;
    private BigDecimal totalRevenue;
    private Double teamConversionRate;
    private List<AgentProductivityDTO> topPerformers;
    
	public Long getAgencyId() {
		return agencyId;
	}
	public void setAgencyId(Long agencyId) {
		this.agencyId = agencyId;
	}
	public LocalDateTime getPeriodStart() {
		return periodStart;
	}
	public void setPeriodStart(LocalDateTime periodStart) {
		this.periodStart = periodStart;
	}
	public LocalDateTime getPeriodEnd() {
		return periodEnd;
	}
	public void setPeriodEnd(LocalDateTime periodEnd) {
		this.periodEnd = periodEnd;
	}
	public List<AgentProductivityDTO> getAgentProductivities() {
		return agentProductivities;
	}
	public void setAgentProductivities(List<AgentProductivityDTO> agentProductivities) {
		this.agentProductivities = agentProductivities;
	}
	public Integer getTotalListingsCreated() {
		return totalListingsCreated;
	}
	public void setTotalListingsCreated(Integer totalListingsCreated) {
		this.totalListingsCreated = totalListingsCreated;
	}
	public Integer getTotalLeadsGenerated() {
		return totalLeadsGenerated;
	}
	public void setTotalLeadsGenerated(Integer totalLeadsGenerated) {
		this.totalLeadsGenerated = totalLeadsGenerated;
	}
	public Integer getTotalDealsClosed() {
		return totalDealsClosed;
	}
	public void setTotalDealsClosed(Integer totalDealsClosed) {
		this.totalDealsClosed = totalDealsClosed;
	}
	public BigDecimal getTotalRevenue() {
		return totalRevenue;
	}
	public void setTotalRevenue(BigDecimal totalRevenue) {
		this.totalRevenue = totalRevenue;
	}
	public Double getTeamConversionRate() {
		return teamConversionRate;
	}
	public void setTeamConversionRate(Double teamConversionRate) {
		this.teamConversionRate = teamConversionRate;
	}
	public List<AgentProductivityDTO> getTopPerformers() {
		return topPerformers;
	}
	public void setTopPerformers(List<AgentProductivityDTO> topPerformers) {
		this.topPerformers = topPerformers;
	}
    
    

}
