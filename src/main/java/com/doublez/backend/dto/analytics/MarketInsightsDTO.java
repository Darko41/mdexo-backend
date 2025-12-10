package com.doublez.backend.dto.analytics;

import java.math.BigDecimal;
import java.util.Map;

public class MarketInsightsDTO {
    private Long agencyId;
    private String city;
    private BigDecimal agencyAveragePrice;
    private BigDecimal marketAveragePrice;
    private Double priceVsMarket; // percentage
    private Double averageDaysOnMarket;
    private Map<String, Long> listingsByType;
    
    
	public Long getAgencyId() {
		return agencyId;
	}
	public void setAgencyId(Long agencyId) {
		this.agencyId = agencyId;
	}
	public String getCity() {
		return city;
	}
	public void setCity(String city) {
		this.city = city;
	}
	public BigDecimal getAgencyAveragePrice() {
		return agencyAveragePrice;
	}
	public void setAgencyAveragePrice(BigDecimal agencyAveragePrice) {
		this.agencyAveragePrice = agencyAveragePrice;
	}
	public BigDecimal getMarketAveragePrice() {
		return marketAveragePrice;
	}
	public void setMarketAveragePrice(BigDecimal marketAveragePrice) {
		this.marketAveragePrice = marketAveragePrice;
	}
	public Double getPriceVsMarket() {
		return priceVsMarket;
	}
	public void setPriceVsMarket(Double priceVsMarket) {
		this.priceVsMarket = priceVsMarket;
	}
	public Double getAverageDaysOnMarket() {
		return averageDaysOnMarket;
	}
	public void setAverageDaysOnMarket(Double averageDaysOnMarket) {
		this.averageDaysOnMarket = averageDaysOnMarket;
	}
	public Map<String, Long> getListingsByType() {
		return listingsByType;
	}
	public void setListingsByType(Map<String, Long> listingsByType) {
		this.listingsByType = listingsByType;
	}

}
