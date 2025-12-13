package com.doublez.backend.dto.analytics;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class AgentProductivityDTO {
    private Long agentId;
    private String agentName;
    private Integer listingsCreated;
    private BigDecimal averageListingPrice;
    private Integer leadsGenerated;
    private Integer dealsClosed;
    private Double conversionRate;
    private Double averageResponseTime;
    private String performanceRating;
    private LocalDateTime periodStart;
    private LocalDateTime periodEnd;
    
    // Getters and setters
	public Long getAgentId() {
		return agentId;
	}
	public void setAgentId(Long agentId) {
		this.agentId = agentId;
	}
	public String getAgentName() {
		return agentName;
	}
	public void setAgentName(String agentName) {
		this.agentName = agentName;
	}
	public Integer getListingsCreated() {
		return listingsCreated;
	}
	public void setListingsCreated(Integer listingsCreated) {
		this.listingsCreated = listingsCreated;
	}
	public BigDecimal getAverageListingPrice() {
		return averageListingPrice;
	}
	public void setAverageListingPrice(BigDecimal averageListingPrice) {
		this.averageListingPrice = averageListingPrice;
	}
	public Integer getLeadsGenerated() {
		return leadsGenerated;
	}
	public void setLeadsGenerated(Integer leadsGenerated) {
		this.leadsGenerated = leadsGenerated;
	}
	public Integer getDealsClosed() {
		return dealsClosed;
	}
	public void setDealsClosed(Integer dealsClosed) {
		this.dealsClosed = dealsClosed;
	}
	public Double getConversionRate() {
		return conversionRate;
	}
	public void setConversionRate(Double conversionRate) {
		this.conversionRate = conversionRate;
	}
	public Double getAverageResponseTime() {
		return averageResponseTime;
	}
	public void setAverageResponseTime(Double averageResponseTime) {
		this.averageResponseTime = averageResponseTime;
	}
	public String getPerformanceRating() {
		return performanceRating;
	}
	public void setPerformanceRating(String performanceRating) {
		this.performanceRating = performanceRating;
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

}
