package com.doublez.backend.dto.feature;

import com.doublez.backend.entity.realestate.PropertyFeature;
import com.doublez.backend.enums.property.FeatureCategory;

// Used when returning feature data
public class PropertyFeatureResponseDTO {
	private Long id;
    private String code;
    private String name;
    private FeatureCategory category;
    private String categoryDisplayName;
    private Integer displayOrder;
    private boolean systemDefined;
    private String description;

	// Constructors
	public PropertyFeatureResponseDTO() {
	}

	public PropertyFeatureResponseDTO(PropertyFeature feature) {
        this.id = feature.getId();
        this.code = feature.getCode();
        this.name = feature.getName();
        this.category = feature.getCategory();
        this.categoryDisplayName = feature.getCategory().getDisplayName();
        this.displayOrder = feature.getDisplayOrder();
        this.systemDefined = feature.isSystemDefined();
        this.description = feature.getDescription();
    }

	// Getters and Setters
	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getCode() {
		return code;
	}

	public void setCode(String code) {
		this.code = code;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public FeatureCategory getCategory() {
		return category;
	}

	public void setCategory(FeatureCategory category) {
		this.category = category;
	}

	public String getCategoryDisplayName() {
		return categoryDisplayName;
	}

	public void setCategoryDisplayName(String categoryDisplayName) {
		this.categoryDisplayName = categoryDisplayName;
	}

	public boolean isSystemDefined() {
		return systemDefined;
	}

	public void setSystemDefined(boolean systemDefined) {
		this.systemDefined = systemDefined;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public Integer getDisplayOrder() {
		return displayOrder;
	}

	public void setDisplayOrder(Integer displayOrder) {
		this.displayOrder = displayOrder;
	}
}
