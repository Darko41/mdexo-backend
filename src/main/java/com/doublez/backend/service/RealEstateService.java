package com.doublez.backend.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

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

}
