package com.doublez.backend.specification;

import java.math.BigDecimal;

import org.springframework.data.jpa.domain.Specification;

import com.doublez.backend.entity.RealEstate;

public class RealEstateSpecifications {
	
	public static Specification<RealEstate> hasMinPrice(BigDecimal priceMin) {
		return (root, query, criteriaBulder) -> {
			if (priceMin == null) return null;
			return criteriaBulder.greaterThanOrEqualTo(root.get("price"), priceMin);
		};
	}
	
	public static Specification<RealEstate> hasMaxPrice(BigDecimal priceMax) {
		return (root, query, criteriaBulder) -> {
			if (priceMax == null) return null;
			return criteriaBulder.lessThanOrEqualTo(root.get("price"), priceMax);
		};
	}
	
	public static Specification<RealEstate> hasPropertyType(String propertyType) {
		return (root, query, criteriaBulder) -> {
			if (propertyType == null) return null;
			return criteriaBulder.lessThanOrEqualTo(root.get("propertyType"), propertyType);
		};
	}

}
