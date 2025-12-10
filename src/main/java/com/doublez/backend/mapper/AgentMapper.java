package com.doublez.backend.mapper;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;

import com.doublez.backend.dto.agent.AgentCreateDTO;
import com.doublez.backend.dto.agent.AgentResponseDTO;
import com.doublez.backend.dto.agent.AgentUpdateDTO;
import com.doublez.backend.entity.agency.Agency;
import com.doublez.backend.entity.agency.Agent;
import com.doublez.backend.entity.agency.Invitation;
import com.doublez.backend.entity.user.User;

@Component
public class AgentMapper {
    
    // ===== AGENT MAPPING =====
    
    public AgentResponseDTO toResponseDTO(Agent agent) {
        if (agent == null) return null;
        
        AgentResponseDTO dto = new AgentResponseDTO();
        dto.setId(agent.getId());
        
        // Map user info
        if (agent.getUser() != null) {
            dto.setUserId(agent.getUser().getId());
            dto.setUserEmail(agent.getUser().getEmail());
            dto.setUserFirstName(agent.getUser().getFirstName());
            dto.setUserLastName(agent.getUser().getLastName());
            dto.setUserPhone(agent.getUser().getPhone());
            // Add profile image if you have it
        }
        
        // Map agency info
        if (agent.getAgency() != null) {
            dto.setAgencyId(agent.getAgency().getId());
            dto.setAgencyName(agent.getAgency().getName());
            dto.setAgencyLogo(agent.getAgency().getLogo());
        }
        
        // Map agent properties
        dto.setRole(agent.getRole());
        dto.setRoleDisplayName(agent.getRole() != null ? agent.getRole().getDisplayName() : null);
        dto.setIsActive(agent.getIsActive());
        dto.setJoinDate(agent.getJoinDate());
        dto.setLastActiveDate(agent.getLastActiveDate());
        dto.setMaxListings(agent.getMaxListings());
        dto.setCanManageListings(agent.getCanManageListings());
        dto.setCanViewAnalytics(agent.getCanViewAnalytics());
        dto.setCanManageBilling(agent.getCanManageBilling());
        dto.setCanInviteAgents(agent.getCanInviteAgents());
        dto.setCommissionRate(agent.getCommissionRate());
        dto.setTotalListingsCreated(agent.getTotalListingsCreated());
        dto.setActiveListingsCount(agent.getActiveListingsCount());
        dto.setLeadsGenerated(agent.getLeadsGenerated());
        dto.setDealsClosed(agent.getDealsClosed());
        dto.setAverageResponseTimeMinutes(agent.getAverageResponseTimeMinutes());
        dto.setCreatedAt(agent.getCreatedAt());
        dto.setUpdatedAt(agent.getUpdatedAt());
        
        // Calculate derived properties (these are calculated in AgentResponseDTO getters)
        // No need to set them here as they're calculated on the fly
        
        return dto;
    }
    
    public List<AgentResponseDTO> toResponseDTOList(List<Agent> agents) {
        if (agents == null) return null;
        return agents.stream()
                .map(this::toResponseDTO)
                .collect(Collectors.toList());
    }
    
    public Agent toEntity(AgentCreateDTO createDto, User user, Agency agency, User invitedBy) {
        if (createDto == null) return null;
        
        Agent agent = new Agent(user, agency, createDto.getRole(), invitedBy);
        
        // Set additional properties from DTO
        if (createDto.getMaxListings() != null) {
            agent.setMaxListings(createDto.getMaxListings());
        }
        
        if (createDto.getCommissionRate() != null) {
            agent.setCommissionRate(createDto.getCommissionRate());
        }
        
        // Set custom permissions if provided
        if (createDto.getCustomPermissions() != null) {
            // You'll need to parse the JSON and set individual permissions
            // For now, we'll handle this in the service layer
        }
        
        return agent;
    }
    
    public void updateEntityFromDTO(AgentUpdateDTO updateDto, Agent agent) {
        if (updateDto == null || agent == null) return;
        
        // Update role
        if (updateDto.getRole() != null) {
            agent.setRole(updateDto.getRole());
        }
        
        // Update other properties
        if (updateDto.getMaxListings() != null) {
            agent.setMaxListings(updateDto.getMaxListings());
        }
        
        if (updateDto.getCommissionRate() != null) {
            agent.setCommissionRate(updateDto.getCommissionRate());
        }
        
        if (updateDto.getCanManageListings() != null) {
            agent.setCanManageListings(updateDto.getCanManageListings());
        }
        
        if (updateDto.getCanViewAnalytics() != null) {
            agent.setCanViewAnalytics(updateDto.getCanViewAnalytics());
        }
        
        if (updateDto.getCanManageBilling() != null) {
            agent.setCanManageBilling(updateDto.getCanManageBilling());
        }
        
        if (updateDto.getCanInviteAgents() != null) {
            agent.setCanInviteAgents(updateDto.getCanInviteAgents());
        }
        
        if (updateDto.getIsActive() != null) {
            agent.setIsActive(updateDto.getIsActive());
        }
        
        if (updateDto.getCustomPermissions() != null) {
            // Parse and apply custom permissions
            // Handle in service layer
        }
        
        // Update timestamp
        agent.preUpdate();
    }
    
    // Helper method to create basic agent from invitation
    public Agent toEntityFromInvitation(Invitation invitation, User user) {
        if (invitation == null || user == null) return null;
        
        Agent agent = new Agent(
            user,
            invitation.getAgency(),
            invitation.getRole(),
            invitation.getInvitedBy()
        );
        
        return agent;
    }
}