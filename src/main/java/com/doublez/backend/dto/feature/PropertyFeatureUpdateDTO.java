package com.doublez.backend.dto.feature;

import com.doublez.backend.enums.property.FeatureCategory;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

// For updating custom features (admin only)
public class PropertyFeatureUpdateDTO {
	@NotBlank(message = "Name is required")
    @Size(max = 100, message = "Name cannot exceed 100 characters")
    private String name;
    
    @NotNull(message = "Category is required")
    private FeatureCategory category;
    
    @Min(value = 0, message = "Display order cannot be negative")
    private Integer displayOrder = 0;
    
    @Size(max = 255, message = "Description cannot exceed 255 characters")
    private String description;

	// Getters and Setters
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
