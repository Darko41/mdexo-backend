package com.doublez.backend.entity.realestate;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.doublez.backend.enums.LocationDataSource;
import com.doublez.backend.utils.JsonUtils;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import jakarta.persistence.Version;
import jakarta.persistence.Index;

@Entity
@Table(name = "real_estate_location_metadata",
       indexes = {
           @Index(name = "idx_location_property", columnList = "property_id", unique = true),
           @Index(name = "idx_walk_score", columnList = "walk_score"),
           @Index(name = "idx_transit_score", columnList = "transit_score")
       })
public class LocationMetadata {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    // === PROPERTY REFERENCE ===
    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "property_id", unique = true)
    private RealEstate property;
    
    // === SCORES (0-100) ===
    @Column(name = "walk_score")
    private Integer walkScore; // Walkability score
    
    @Column(name = "transit_score")
    private Integer transitScore; // Public transport accessibility
    
    @Column(name = "bike_score")
    private Integer bikeScore; // Bike-friendliness
    
    @Column(name = "safety_score")
    private Integer safetyScore; // Perceived safety
    
    @Column(name = "overall_location_score")
    private Integer overallLocationScore; // Composite score
    
    // === AMENITY COUNTS ===
    @Column(name = "restaurants_500m")
    private Integer restaurants500m;
    
    @Column(name = "restaurants_1000m")
    private Integer restaurants1000m;
    
    @Column(name = "cafes_500m")
    private Integer cafes500m;
    
    @Column(name = "cafes_1000m")
    private Integer cafes1000m;
    
    @Column(name = "supermarkets_500m")
    private Integer supermarkets500m;
    
    @Column(name = "supermarkets_1000m")
    private Integer supermarkets1000m;
    
    @Column(name = "parks_500m")
    private Integer parks500m;
    
    @Column(name = "parks_1000m")
    private Integer parks1000m;
    
    @Column(name = "pharmacies_1000m")
    private Integer pharmacies1000m;
    
    @Column(name = "total_amenities_1000m")
    private Integer totalAmenities1000m;
    
    // === COMMUTE TIMES (minutes) ===
    @Column(name = "commute_city_center_car_min")
    private Integer commuteCityCenterCarMin;
    
    @Column(name = "commute_city_center_transit_min")
    private Integer commuteCityCenterTransitMin;
    
    @Column(name = "commute_city_center_walk_min")
    private Integer commuteCityCenterWalkMin;
    
    @Column(name = "commute_airport_car_min")
    private Integer commuteAirportCarMin;
    
    @Column(name = "commute_main_station_car_min")
    private Integer commuteMainStationCarMin;
    
    // === DISTANCES (meters) ===
    @Column(name = "nearest_bus_stop_m")
    private Integer nearestBusStopM;
    
    @Column(name = "nearest_tram_stop_m")
    private Integer nearestTramStopM;
    
    @Column(name = "nearest_train_station_m")
    private Integer nearestTrainStationM;
    
    @Column(name = "nearest_school_m")
    private Integer nearestSchoolM;
    
    @Column(name = "nearest_park_m")
    private Integer nearestParkM;
    
    // === NAMES ===
    @Column(name = "nearest_bus_stop_name", length = 150)
    private String nearestBusStopName;
    
    @Column(name = "nearest_school_name", length = 200)
    private String nearestSchoolName;
    
    @Column(name = "nearest_park_name", length = 150)
    private String nearestParkName;
    
    // === SCHOOLS ===
    @Column(name = "schools_within_500m")
    private Integer schoolsWithin500m;
    
    @Column(name = "schools_within_1000m")
    private Integer schoolsWithin1000m;
    
    @Column(name = "schools_within_2000m")
    private Integer schoolsWithin2000m;
    
    // === TRANSPORT ===
    @Column(name = "bus_lines_nearby", length = 500)
    private String busLinesNearby; // "65,73,83" comma-separated
    
    @Column(name = "has_bike_sharing")
    private Boolean hasBikeSharing;
    
    @Column(name = "has_car_sharing")
    private Boolean hasCarSharing;
    
    // === DATA SOURCE ===
    @Enumerated(EnumType.STRING)
    @Column(name = "data_source", length = 20)
    private LocationDataSource dataSource = LocationDataSource.OSM;
    
    // === CACHE & DETAILED DATA ===
    @Column(name = "detailed_data_json", columnDefinition = "JSON")
    private String detailedDataJson; // All other detailed info
    
    @Column(name = "raw_osm_data_json", columnDefinition = "JSON")
    private String rawOsmDataJson; // Raw OSM response cache
    
    @Column(name = "raw_commute_data_json", columnDefinition = "JSON")
    private String rawCommuteDataJson; // Raw commute API cache
    
    // === DATA QUALITY ===
    @Column(name = "data_quality_score")
    private Integer dataQualityScore; // 0-100 confidence score
    
    @Column(name = "coverage_completeness")
    private Integer coverageCompleteness; // % of expected data found
    
    @Column(name = "is_data_complete")
    private Boolean isDataComplete = false;
    
    // === UPDATE TRACKING ===
    @Column(name = "last_updated", nullable = false)
    private LocalDateTime lastUpdated;
    
    @Column(name = "next_update_scheduled")
    private LocalDateTime nextUpdateScheduled;
    
    @Column(name = "update_attempts")
    private Integer updateAttempts = 0;
    
    @Column(name = "last_successful_update")
    private LocalDateTime lastSuccessfulUpdate;
    
    // === AUDIT FIELDS ===
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @Version
    private Integer version;
    
    // === LIFECYCLE ===
    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        if (this.lastUpdated == null) {
            this.lastUpdated = LocalDateTime.now();
        }
    }
    
    // === HELPER METHODS ===
    
    /**
     * Get detailed data as map (convenience method)
     */
    @Transient
    public Map<String, Object> getDetailedData() {
        return JsonUtils.parseStringObjectMap(detailedDataJson);
    }

    /**
     * Set detailed data from map (convenience method)
     */
    public void setDetailedData(Map<String, Object> data) {
        this.detailedDataJson = JsonUtils.toJson(data);
    }

    /**
     * Get raw OSM data as map
     */
    @Transient
    public Map<String, Object> getRawOsmData() {
        return JsonUtils.parseStringObjectMap(rawOsmDataJson);
    }

    /**
     * Set raw OSM data from map
     */
    public void setRawOsmData(Map<String, Object> data) {
        this.rawOsmDataJson = JsonUtils.toJson(data);
    }

    /**
     * Get raw commute data as map
     */
    @Transient
    public Map<String, Object> getRawCommuteData() {
        return JsonUtils.parseStringObjectMap(rawCommuteDataJson);
    }

    /**
     * Set raw commute data from map
     */
    public void setRawCommuteData(Map<String, Object> data) {
        this.rawCommuteDataJson = JsonUtils.toJson(data);
    }

    /**
     * Calculate overall score based on available data
     */
    public void calculateOverallScore() {
        int total = 0;
        int count = 0;
        
        if (walkScore != null) { total += walkScore; count++; }
        if (transitScore != null) { total += transitScore; count++; }
        if (bikeScore != null) { total += bikeScore; count++; }
        if (safetyScore != null) { total += safetyScore; count++; }
        
        this.overallLocationScore = count > 0 ? total / count : null;
    }

    /**
     * Check if location data needs update (older than 30 days)
     */
    @Transient
    public boolean needsUpdate() {
        if (lastUpdated == null) return true;
        return Duration.between(lastUpdated, LocalDateTime.now()).toDays() > 30;
    }

    /**
     * Get amenity summary for display
     */
    @Transient
    public String getAmenitySummary() {
        List<String> parts = new ArrayList<>();
        if (restaurants1000m != null) parts.add(restaurants1000m + " restaurants");
        if (cafes1000m != null) parts.add(cafes1000m + " cafes");
        if (parks1000m != null) parts.add(parks1000m + " parks");
        if (supermarkets1000m != null) parts.add(supermarkets1000m + " supermarkets");
        
        return String.join(", ", parts);
    }

    /**
     * Get commute summary for display
     */
    @Transient
    public String getCommuteSummary() {
        if (commuteCityCenterCarMin != null) {
            return commuteCityCenterCarMin + " min to city center";
        }
        return "Commute data not available";
    }

    /**
     * Check if data is considered good quality
     */
    @Transient
    public boolean isGoodQuality() {
        return dataQualityScore != null && dataQualityScore >= 70;
    }

    /**
     * Get bus lines as list
     */
    @Transient
    public List<String> getBusLinesList() {
        if (busLinesNearby == null || busLinesNearby.trim().isEmpty()) {
            return new ArrayList<>();
        }
        return Arrays.stream(busLinesNearby.split(","))
                     .map(String::trim)
                     .filter(s -> !s.isEmpty())
                     .collect(Collectors.toList());
    }

    /**
     * Set bus lines from list
     */
    public void setBusLinesList(List<String> busLines) {
        if (busLines == null || busLines.isEmpty()) {
            this.busLinesNearby = null;
        } else {
            this.busLinesNearby = String.join(",", busLines);
        }
    }
    
    
    
    // Getters and setters...

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public RealEstate getProperty() {
		return property;
	}

	public void setProperty(RealEstate property) {
		this.property = property;
	}

	public Integer getWalkScore() {
		return walkScore;
	}

	public void setWalkScore(Integer walkScore) {
		this.walkScore = walkScore;
	}

	public Integer getTransitScore() {
		return transitScore;
	}

	public void setTransitScore(Integer transitScore) {
		this.transitScore = transitScore;
	}

	public Integer getBikeScore() {
		return bikeScore;
	}

	public void setBikeScore(Integer bikeScore) {
		this.bikeScore = bikeScore;
	}

	public Integer getSafetyScore() {
		return safetyScore;
	}

	public void setSafetyScore(Integer safetyScore) {
		this.safetyScore = safetyScore;
	}

	public Integer getOverallLocationScore() {
		return overallLocationScore;
	}

	public void setOverallLocationScore(Integer overallLocationScore) {
		this.overallLocationScore = overallLocationScore;
	}

	public Integer getRestaurants500m() {
		return restaurants500m;
	}

	public void setRestaurants500m(Integer restaurants500m) {
		this.restaurants500m = restaurants500m;
	}

	public Integer getRestaurants1000m() {
		return restaurants1000m;
	}

	public void setRestaurants1000m(Integer restaurants1000m) {
		this.restaurants1000m = restaurants1000m;
	}

	public Integer getCafes500m() {
		return cafes500m;
	}

	public void setCafes500m(Integer cafes500m) {
		this.cafes500m = cafes500m;
	}

	public Integer getCafes1000m() {
		return cafes1000m;
	}

	public void setCafes1000m(Integer cafes1000m) {
		this.cafes1000m = cafes1000m;
	}

	public Integer getSupermarkets500m() {
		return supermarkets500m;
	}

	public void setSupermarkets500m(Integer supermarkets500m) {
		this.supermarkets500m = supermarkets500m;
	}

	public Integer getSupermarkets1000m() {
		return supermarkets1000m;
	}

	public void setSupermarkets1000m(Integer supermarkets1000m) {
		this.supermarkets1000m = supermarkets1000m;
	}

	public Integer getParks500m() {
		return parks500m;
	}

	public void setParks500m(Integer parks500m) {
		this.parks500m = parks500m;
	}

	public Integer getParks1000m() {
		return parks1000m;
	}

	public void setParks1000m(Integer parks1000m) {
		this.parks1000m = parks1000m;
	}

	public Integer getPharmacies1000m() {
		return pharmacies1000m;
	}

	public void setPharmacies1000m(Integer pharmacies1000m) {
		this.pharmacies1000m = pharmacies1000m;
	}

	public Integer getTotalAmenities1000m() {
		return totalAmenities1000m;
	}

	public void setTotalAmenities1000m(Integer totalAmenities1000m) {
		this.totalAmenities1000m = totalAmenities1000m;
	}

	public Integer getCommuteCityCenterCarMin() {
		return commuteCityCenterCarMin;
	}

	public void setCommuteCityCenterCarMin(Integer commuteCityCenterCarMin) {
		this.commuteCityCenterCarMin = commuteCityCenterCarMin;
	}

	public Integer getCommuteCityCenterTransitMin() {
		return commuteCityCenterTransitMin;
	}

	public void setCommuteCityCenterTransitMin(Integer commuteCityCenterTransitMin) {
		this.commuteCityCenterTransitMin = commuteCityCenterTransitMin;
	}

	public Integer getCommuteCityCenterWalkMin() {
		return commuteCityCenterWalkMin;
	}

	public void setCommuteCityCenterWalkMin(Integer commuteCityCenterWalkMin) {
		this.commuteCityCenterWalkMin = commuteCityCenterWalkMin;
	}

	public Integer getCommuteAirportCarMin() {
		return commuteAirportCarMin;
	}

	public void setCommuteAirportCarMin(Integer commuteAirportCarMin) {
		this.commuteAirportCarMin = commuteAirportCarMin;
	}

	public Integer getCommuteMainStationCarMin() {
		return commuteMainStationCarMin;
	}

	public void setCommuteMainStationCarMin(Integer commuteMainStationCarMin) {
		this.commuteMainStationCarMin = commuteMainStationCarMin;
	}

	public Integer getNearestBusStopM() {
		return nearestBusStopM;
	}

	public void setNearestBusStopM(Integer nearestBusStopM) {
		this.nearestBusStopM = nearestBusStopM;
	}

	public Integer getNearestTramStopM() {
		return nearestTramStopM;
	}

	public void setNearestTramStopM(Integer nearestTramStopM) {
		this.nearestTramStopM = nearestTramStopM;
	}

	public Integer getNearestTrainStationM() {
		return nearestTrainStationM;
	}

	public void setNearestTrainStationM(Integer nearestTrainStationM) {
		this.nearestTrainStationM = nearestTrainStationM;
	}

	public Integer getNearestSchoolM() {
		return nearestSchoolM;
	}

	public void setNearestSchoolM(Integer nearestSchoolM) {
		this.nearestSchoolM = nearestSchoolM;
	}

	public Integer getNearestParkM() {
		return nearestParkM;
	}

	public void setNearestParkM(Integer nearestParkM) {
		this.nearestParkM = nearestParkM;
	}

	public String getNearestBusStopName() {
		return nearestBusStopName;
	}

	public void setNearestBusStopName(String nearestBusStopName) {
		this.nearestBusStopName = nearestBusStopName;
	}

	public String getNearestSchoolName() {
		return nearestSchoolName;
	}

	public void setNearestSchoolName(String nearestSchoolName) {
		this.nearestSchoolName = nearestSchoolName;
	}

	public String getNearestParkName() {
		return nearestParkName;
	}

	public void setNearestParkName(String nearestParkName) {
		this.nearestParkName = nearestParkName;
	}

	public Integer getSchoolsWithin500m() {
		return schoolsWithin500m;
	}

	public void setSchoolsWithin500m(Integer schoolsWithin500m) {
		this.schoolsWithin500m = schoolsWithin500m;
	}

	public Integer getSchoolsWithin1000m() {
		return schoolsWithin1000m;
	}

	public void setSchoolsWithin1000m(Integer schoolsWithin1000m) {
		this.schoolsWithin1000m = schoolsWithin1000m;
	}

	public Integer getSchoolsWithin2000m() {
		return schoolsWithin2000m;
	}

	public void setSchoolsWithin2000m(Integer schoolsWithin2000m) {
		this.schoolsWithin2000m = schoolsWithin2000m;
	}

	public String getBusLinesNearby() {
		return busLinesNearby;
	}

	public void setBusLinesNearby(String busLinesNearby) {
		this.busLinesNearby = busLinesNearby;
	}

	public Boolean getHasBikeSharing() {
		return hasBikeSharing;
	}

	public void setHasBikeSharing(Boolean hasBikeSharing) {
		this.hasBikeSharing = hasBikeSharing;
	}

	public Boolean getHasCarSharing() {
		return hasCarSharing;
	}

	public void setHasCarSharing(Boolean hasCarSharing) {
		this.hasCarSharing = hasCarSharing;
	}

	public LocationDataSource getDataSource() {
		return dataSource;
	}

	public void setDataSource(LocationDataSource dataSource) {
		this.dataSource = dataSource;
	}

	public String getDetailedDataJson() {
		return detailedDataJson;
	}

	public void setDetailedDataJson(String detailedDataJson) {
		this.detailedDataJson = detailedDataJson;
	}

	public String getRawOsmDataJson() {
		return rawOsmDataJson;
	}

	public void setRawOsmDataJson(String rawOsmDataJson) {
		this.rawOsmDataJson = rawOsmDataJson;
	}

	public String getRawCommuteDataJson() {
		return rawCommuteDataJson;
	}

	public void setRawCommuteDataJson(String rawCommuteDataJson) {
		this.rawCommuteDataJson = rawCommuteDataJson;
	}

	public Integer getDataQualityScore() {
		return dataQualityScore;
	}

	public void setDataQualityScore(Integer dataQualityScore) {
		this.dataQualityScore = dataQualityScore;
	}

	public Integer getCoverageCompleteness() {
		return coverageCompleteness;
	}

	public void setCoverageCompleteness(Integer coverageCompleteness) {
		this.coverageCompleteness = coverageCompleteness;
	}

	public Boolean getIsDataComplete() {
		return isDataComplete;
	}

	public void setIsDataComplete(Boolean isDataComplete) {
		this.isDataComplete = isDataComplete;
	}

	public LocalDateTime getLastUpdated() {
		return lastUpdated;
	}

	public void setLastUpdated(LocalDateTime lastUpdated) {
		this.lastUpdated = lastUpdated;
	}

	public LocalDateTime getNextUpdateScheduled() {
		return nextUpdateScheduled;
	}

	public void setNextUpdateScheduled(LocalDateTime nextUpdateScheduled) {
		this.nextUpdateScheduled = nextUpdateScheduled;
	}

	public Integer getUpdateAttempts() {
		return updateAttempts;
	}

	public void setUpdateAttempts(Integer updateAttempts) {
		this.updateAttempts = updateAttempts;
	}

	public LocalDateTime getLastSuccessfulUpdate() {
		return lastSuccessfulUpdate;
	}

	public void setLastSuccessfulUpdate(LocalDateTime lastSuccessfulUpdate) {
		this.lastSuccessfulUpdate = lastSuccessfulUpdate;
	}

	public LocalDateTime getCreatedAt() {
		return createdAt;
	}

	public void setCreatedAt(LocalDateTime createdAt) {
		this.createdAt = createdAt;
	}

	public Integer getVersion() {
		return version;
	}

	public void setVersion(Integer version) {
		this.version = version;
	}
    
}