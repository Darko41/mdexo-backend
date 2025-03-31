package com.doublez.backend.service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import com.doublez.backend.entity.ListingType;
import com.doublez.backend.entity.PropertyType;
import com.doublez.backend.entity.RealEstate;
import com.doublez.backend.repository.RealEstateRepository;

@Service
public class RealEstateService {

    @Autowired
    private RealEstateRepository realEstateRepository;

    public long getRealEstateCount() {
        return realEstateRepository.count();
    }

    public List<RealEstate> getAllRealEstates() {
        return realEstateRepository.findAll();
    }
    
    public Page<RealEstate> getRealEstates(Specification<RealEstate> spec, Pageable pageable) {
        return realEstateRepository.findAll(spec, pageable);
    }

    public RealEstate getRealEstateById(Long propertyId) {
        return realEstateRepository.findById(propertyId)
                .orElseThrow(() -> new RuntimeException("Real estate not found with id: " + propertyId));
    }

    public RealEstate createRealEstate(RealEstate realEstate) {
        return realEstateRepository.save(realEstate);
    }

    public RealEstate updateRealEstate(Long propertyId, Map<String, Object> updates) {
        RealEstate realEstate = realEstateRepository.findById(propertyId)
                .orElseThrow(() -> new RuntimeException("Real estate not found with id: " + propertyId));

        // Apply updates
        if (updates.containsKey("title")) {
            realEstate.setTitle((String) updates.get("title"));
        }
        if (updates.containsKey("description")) {
            realEstate.setDescription((String) updates.get("description"));
        }
        if (updates.containsKey("propertyType")) {
            realEstate.setPropertyType(PropertyType.valueOf((String) updates.get("propertyType")));
        }
        if (updates.containsKey("listingType")) {
            realEstate.setListingType(ListingType.valueOf((String) updates.get("listingType")));
        }
        if (updates.containsKey("price")) {
            realEstate.setPrice(new BigDecimal((String) updates.get("price")));
        }
        if (updates.containsKey("address")) {
            realEstate.setAddress((String) updates.get("address"));
        }
        if (updates.containsKey("city")) {
            realEstate.setCity((String) updates.get("city"));
        }
        if (updates.containsKey("state")) {
            realEstate.setState((String) updates.get("state"));
        }
        if (updates.containsKey("zipCode")) {
            realEstate.setZipCode((String) updates.get("zipCode"));
        }
        if (updates.containsKey("sizeInSqMt")) {
            realEstate.setSizeInSqMt((String) updates.get("sizeInSqMt"));
        }
        if (updates.containsKey("features")) {
            realEstate.setFeatures((List<String>) updates.get("features"));
        }

        // Update the `updatedAt` timestamp
        realEstate.setUpdatedAt(LocalDate.now());

        // Save the updated entity
        return realEstateRepository.save(realEstate);
    }

    public void deleteRealEstate(Long propertyId) {
        realEstateRepository.deleteById(propertyId);
    }
}