package com.doublez.backend.service.realestate;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.doublez.backend.entity.RealEstate;
import com.doublez.backend.entity.agency.Agency;
import com.doublez.backend.entity.agency.Agent;
import com.doublez.backend.entity.user.User;
import com.doublez.backend.repository.AgentRepository;
import com.doublez.backend.repository.RealEstateRepository;
import com.doublez.backend.service.usage.PermissionService;

@Service
public class ListingVisibilityService {
    
    @Autowired
    private RealEstateRepository realEstateRepository;
    
    @Autowired
    private AgentRepository agentRepository;
    
    @Autowired
    private PermissionService permissionService;
    
    public Page<RealEstate> getVisibleListings(User user, Pageable pageable) {
        if (user == null) {
            return realEstateRepository.findByMultipleCriteria(
                null, null, null, null, null, null, null, null, null, null, pageable
            );
        }
        
        if (!user.isAgencyAdmin()) {
            return realEstateRepository.findVisibleToUser(user.getId(), pageable);
        }
        
        return getAgencyVisibleListings(user, pageable);
    }
    
    private Page<RealEstate> getAgencyVisibleListings(User user, Pageable pageable) {
        List<Agent> userAgents = agentRepository.findByUserAndIsActive(user, true);
        List<Long> agencyIds = userAgents.stream()
                .map(agent -> agent.getAgency().getId())
                .toList();
        
        if (agencyIds.isEmpty()) {
            return realEstateRepository.findByMultipleCriteria(
                null, null, null, null, null, null, null, null, null, null, pageable
            );
        }
        
        return realEstateRepository.findVisibleToAgencies(agencyIds, pageable);
    }
    
    public List<RealEstate> getAgencyListingsForAgent(User user, Long agencyId) {
        Agency agency = new Agency();
        agency.setId(agencyId);
        return permissionService.getVisibleListingsForAgent(user, agency);
    }
    
    public boolean isListingVisibleInSearch(RealEstate listing, User currentUser) {
        if (listing.getIsActive()) {
            return true;
        }
        
        if (currentUser == null) {
            return false;
        }
        
        if (listing.getOwner().getId().equals(currentUser.getId())) {
            return true;
        }
        
        if (listing.getAgency() != null) {
            return agentRepository.findByUserIdAndAgencyId(currentUser.getId(), listing.getAgency().getId())
                    .map(Agent::getIsActive)
                    .orElse(false);
        }
        
        return false;
    }
}