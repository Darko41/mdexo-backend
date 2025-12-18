package com.doublez.backend.bootstrap;

import org.springframework.stereotype.Component;

import com.doublez.backend.entity.realestate.PropertyFeature;
import com.doublez.backend.enums.property.FeatureCategory;
import com.doublez.backend.repository.realestate.PropertyFeatureRepository;

import jakarta.annotation.PostConstruct;
import jakarta.transaction.Transactional;

@Component
public class FeatureSeeder {

    private final PropertyFeatureRepository featureRepository;
    
    public FeatureSeeder(PropertyFeatureRepository featureRepository) {
        this.featureRepository = featureRepository;
    }

    @PostConstruct
    @Transactional
    public void seed() {
        // ===== LEGAL CATEGORY (Most important first) =====
        seedIfMissing("REGISTERED", "Uknjižen", FeatureCategory.LEGAL, 100,
                     "Nekretnina je uknjižena u katastru");
        seedIfMissing("VAT_REFUND", "Povraćaj PDV-a", FeatureCategory.LEGAL, 200);
        seedIfMissing("UNDER_MORTGAGE", "Pod hipotekom", FeatureCategory.LEGAL, 300);
        
        // ===== ADDITIONAL CATEGORY =====
        seedIfMissing("IMMEDIATELY_MOVABLE", "Odmah useljiv", FeatureCategory.ADDITIONAL, 100);
        seedIfMissing("URGENT_SALE", "Hitna prodaja", FeatureCategory.ADDITIONAL, 200);
        
        // ===== STRUCTURE CATEGORY =====
        seedIfMissing("PENTHOUSE", "Penthouse", FeatureCategory.STRUCTURE, 100);
        seedIfMissing("DUPLEX", "Duplex", FeatureCategory.STRUCTURE, 200);
        
        // ===== ENERGY CATEGORY =====
        seedIfMissing("ENERGY_PASSPORT", "Energetski pasoš", FeatureCategory.ENERGY, 100);
        
        // ===== AMENITIES CATEGORY =====
        seedIfMissing("TERRACE", "Terasa", FeatureCategory.AMENITIES, 100);
        seedIfMissing("FRENCH_BALCONY", "Francuski balkon", FeatureCategory.AMENITIES, 200);
        seedIfMissing("LOGGIA", "Lođa", FeatureCategory.AMENITIES, 300);
        
        // ===== COMFORT CATEGORY (Most important amenities) =====
        seedIfMissing("ELEVATOR", "Lift", FeatureCategory.COMFORT, 100);
        seedIfMissing("AIR_CONDITIONING", "Klima", FeatureCategory.COMFORT, 200);
        seedIfMissing("PARKING", "Parking", FeatureCategory.COMFORT, 300);
        seedIfMissing("GARAGE", "Garaža", FeatureCategory.COMFORT, 400);
        seedIfMissing("WITH_GARDEN", "Sa baštom", FeatureCategory.COMFORT, 500);
        
        // ===== SECURITY CATEGORY =====
        seedIfMissing("CCTV", "Video nadzor", FeatureCategory.SECURITY, 100);
    }

    private void seedIfMissing(String code, String name, FeatureCategory category, Integer displayOrder) {
        seedIfMissing(code, name, category, displayOrder, null);
    }

    private void seedIfMissing(String code, String name, FeatureCategory category, 
                               Integer displayOrder, String description) {
        featureRepository
            .findByCode(code)
            .orElseGet(() ->
                featureRepository.save(
                    PropertyFeature.systemFeature(code, name, category, displayOrder, description)
                )
            );
    }
}