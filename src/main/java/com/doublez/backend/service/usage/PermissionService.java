package com.doublez.backend.service.usage;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.doublez.backend.entity.agency.Agency;
import com.doublez.backend.entity.agency.Agent;
import com.doublez.backend.entity.realestate.RealEstate;
import com.doublez.backend.entity.user.User;
import com.doublez.backend.enums.agency.AgentRole;
import com.doublez.backend.repository.AgentRepository;
import com.doublez.backend.repository.realestate.RealEstateRepository;
import com.doublez.backend.service.agency.TeamService;

@Service
public class PermissionService {
    
    @Autowired
    private AgentRepository agentRepository;
    
    @Autowired
    private RealEstateRepository realEstateRepository;
    
    @Autowired
    private TeamService teamService;
    
    // ========================
    // AGENCY & TEAM PERMISSIONS
    // ========================
    
    public boolean canViewTeam(User user, Agency agency) {
        if (user.isAgencyAdmin() && user.getOwnedAgency().isPresent() 
            && user.getOwnedAgency().get().getId().equals(agency.getId())) {
            return true;
        }
        
        return agentRepository.findByUserIdAndAgencyId(user.getId(), agency.getId())
                .map(agent -> agent.getIsActive() && 
                     (agent.getCanViewAnalytics() || agent.getRole() == AgentRole.OWNER))
                .orElse(false);
    }
    
    public boolean canManageTeam(User user, Agency agency) {
        if (user.isAgencyAdmin() && user.getOwnedAgency().isPresent() 
            && user.getOwnedAgency().get().getId().equals(agency.getId())) {
            return true;
        }
        
        return agentRepository.findByUserIdAndAgencyId(user.getId(), agency.getId())
                .map(agent -> agent.getIsActive() && agent.getCanInviteAgents())
                .orElse(false);
    }
    
    public boolean canManageAgent(User user, Agent targetAgent) {
        if (user.getId().equals(targetAgent.getUser().getId())) {
            return false;
        }
        
        Agent userAgent = agentRepository.findByUserIdAndAgencyId(user.getId(), targetAgent.getAgency().getId())
                .orElse(null);
        
        if (userAgent == null || !userAgent.getIsActive()) {
            return false;
        }
        
        return userAgent.getRole().canManage(targetAgent.getRole());
    }
    
    // ========================
    // LISTING PERMISSIONS
    // ========================
    
    public boolean canCreateListing(User user, Agency agency) {
        if (agency == null) {
            return user.isOwner() || user.isAgencyAdmin();
        }
        
        return agentRepository.findByUserIdAndAgencyId(user.getId(), agency.getId())
                .map(agent -> agent.getIsActive() && agent.getCanManageListings())
                .orElse(false);
    }
    
    public boolean canViewListing(User user, RealEstate listing) {
        if (listing.getIsActive()) {
            return true;
        }
        
        if (listing.getOwner().getId().equals(user.getId())) {
            return true;
        }
        
        if (listing.getAgency() != null) {
            return agentRepository.findByUserIdAndAgencyId(user.getId(), listing.getAgency().getId())
                    .map(agent -> agent.getIsActive() && 
                         (agent.getCanManageListings() || agent.getRole() == AgentRole.OWNER))
                    .orElse(false);
        }
        
        return false;
    }
    
    public boolean canEditListing(User user, RealEstate listing) {
        if (listing.getOwner().getId().equals(user.getId())) {
            return true;
        }
        
        if (listing.getAgency() != null) {
            return agentRepository.findByUserIdAndAgencyId(user.getId(), listing.getAgency().getId())
                    .map(agent -> {
                        if (!agent.getIsActive() || !agent.getCanManageListings()) {
                            return false;
                        }
                        
                        if (agent.getRole() == AgentRole.OWNER || agent.getRole() == AgentRole.SUPER_AGENT) {
                            return true;
                        }
                        
                        return listing.getListingAgent() != null && 
                               listing.getListingAgent().getId().equals(agent.getId());
                    })
                    .orElse(false);
        }
        
        return false;
    }
    
    public boolean canDeleteListing(User user, RealEstate listing) {
        if (listing.getOwner().getId().equals(user.getId())) {
            return true;
        }
        
        if (listing.getAgency() != null) {
            return agentRepository.findByUserIdAndAgencyId(user.getId(), listing.getAgency().getId())
                    .map(agent -> agent.getIsActive() && 
                         (agent.getRole() == AgentRole.OWNER || agent.getRole() == AgentRole.SUPER_AGENT))
                    .orElse(false);
        }
        
        return false;
    }
    
    // ========================
    // BILLING & FINANCIAL PERMISSIONS
    // ========================
    
    public boolean canViewBilling(User user, Agency agency) {
        if (user.isAgencyAdmin() && user.getOwnedAgency().isPresent() 
            && user.getOwnedAgency().get().getId().equals(agency.getId())) {
            return true;
        }
        
        return agentRepository.findByUserIdAndAgencyId(user.getId(), agency.getId())
                .map(agent -> agent.getIsActive() && agent.getCanManageBilling())
                .orElse(false);
    }
    
    public boolean canManageBilling(User user, Agency agency) {
        return agentRepository.findByUserIdAndAgencyId(user.getId(), agency.getId())
                .map(agent -> agent.getIsActive() && agent.getRole() == AgentRole.OWNER)
                .orElse(false);
    }
    
    // ========================
    // ANALYTICS PERMISSIONS
    // ========================
    
    public boolean canViewAnalytics(User user, Agency agency) {
        if (user.isAgencyAdmin() && user.getOwnedAgency().isPresent() 
            && user.getOwnedAgency().get().getId().equals(agency.getId())) {
            return true;
        }
        
        return agentRepository.findByUserIdAndAgencyId(user.getId(), agency.getId())
                .map(agent -> agent.getIsActive() && agent.getCanViewAnalytics())
                .orElse(false);
    }
    
    // ========================
    // HELPER METHODS
    // ========================
    
    public AgentRole getUserAgentRoleInAgency(User user, Agency agency) {
        return agentRepository.findByUserIdAndAgencyId(user.getId(), agency.getId())
                .map(Agent::getRole)
                .orElse(null);
    }
    
    public List<RealEstate> getVisibleListingsForAgent(User user, Agency agency) {
        Agent agent = agentRepository.findByUserIdAndAgencyId(user.getId(), agency.getId())
                .orElse(null);
        
        if (agent == null || !agent.getIsActive()) {
            return new ArrayList<>();
        }
        
        if (agent.getRole() == AgentRole.OWNER || agent.getRole() == AgentRole.SUPER_AGENT) {
            return realEstateRepository.findByAgency(agency);
        } else {
            return realEstateRepository.findByAgencyAndListingAgent(agency, agent);
        }
    }
    
    public boolean canAssignRole(User user, Agency agency, AgentRole roleToAssign) {
        Agent userAgent = agentRepository.findByUserIdAndAgencyId(user.getId(), agency.getId())
                .orElse(null);
        
        if (userAgent == null || !userAgent.getIsActive()) {
            return false;
        }
        
        return userAgent.getRole().canManage(roleToAssign);
    }
    
    public boolean canViewAgentPerformance(User user, Agent targetAgent) {
        if (user.getId().equals(targetAgent.getUser().getId())) {
            return true;
        }
        
        Agent userAgent = agentRepository.findByUserIdAndAgencyId(user.getId(), targetAgent.getAgency().getId())
                .orElse(null);
        
        if (userAgent == null || !userAgent.getIsActive()) {
            return false;
        }
        
        return userAgent.getRole() == AgentRole.OWNER || userAgent.getRole() == AgentRole.SUPER_AGENT;
    }
}