package com.doublez.backend.repository.realestate;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.doublez.backend.entity.realestate.PropertyFeature;
import com.doublez.backend.enums.property.FeatureCategory;

public interface PropertyFeatureRepository extends JpaRepository<PropertyFeature, Long> {

    Optional<PropertyFeature> findByCode(String code);
    
    Optional<PropertyFeature> findByNameAndCategory(
        String name,
        FeatureCategory category
    );

    List<PropertyFeature> findBySystemDefinedTrue();
    
    List<PropertyFeature> findBySystemDefinedTrueOrderByCategoryAscDisplayOrderAsc();
    
    List<PropertyFeature> findBySystemDefinedFalseOrderByDisplayOrderAsc();

    List<PropertyFeature> findByCategory(FeatureCategory category);
    
    List<PropertyFeature> findByCategoryOrderByDisplayOrderAsc(FeatureCategory category);
    
    List<PropertyFeature> findByCategoryIn(List<FeatureCategory> categories);
    
    List<PropertyFeature> findByCategoryInOrderByCategoryAscDisplayOrderAsc(List<FeatureCategory> categories);
    
    List<PropertyFeature> findByCodeIn(List<String> codes);
    
    List<PropertyFeature> findAllByOrderByCategoryAscDisplayOrderAsc();
}
