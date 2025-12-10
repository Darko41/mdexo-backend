package com.doublez.backend.service.agency;

import java.util.List;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.doublez.backend.dto.agent.AgentCreateDTO;
import com.doublez.backend.dto.agent.AgentResponseDTO;
import com.doublez.backend.dto.agent.AgentUpdateDTO;
import com.doublez.backend.dto.team.TeamDashboardDTO;
import com.doublez.backend.entity.agency.Agency;
import com.doublez.backend.entity.agency.Agent;
import com.doublez.backend.entity.agency.Invitation;
import com.doublez.backend.entity.user.User;
import com.doublez.backend.enums.agency.AgentRole;
import com.doublez.backend.exception.BusinessRuleException;
import com.doublez.backend.exception.ResourceNotFoundException;
import com.doublez.backend.mapper.AgentMapper;
import com.doublez.backend.mapper.TeamDashboardMapper;
import com.doublez.backend.repository.AgencyRepository;
import com.doublez.backend.repository.AgentRepository;
import com.doublez.backend.repository.UserRepository;
import com.doublez.backend.service.usage.PermissionService;
import com.doublez.backend.service.usage.TierLimitationService;

@Service
@Transactional
public class TeamService {
    
    private static final Logger logger = LoggerFactory.getLogger(TeamService.class);
    
    @Autowired
    private AgentRepository agentRepository;
    
    @Autowired
    private AgencyRepository agencyRepository;
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private AgentMapper agentMapper;
    
    @Autowired
    private TeamDashboardMapper teamDashboardMapper;
    
    @Autowired
    private InvitationService invitationService;
    
    @Autowired
    private TierLimitationService tierLimitationService;
    
    @Autowired
    private PermissionService permissionService;
    
    /**
     * Add a new agent to an agency
     */
    public AgentResponseDTO addAgent(AgentCreateDTO agentCreateDTO, User currentUser) {
        logger.info("Adding new agent with email: {}", agentCreateDTO.getEmail());
        
        // Find agency
        Agency agency = agencyRepository.findById(agentCreateDTO.getAgencyId())
                .orElseThrow(() -> new ResourceNotFoundException("Agency not found with id: " + agentCreateDTO.getAgencyId()));
        
        // Verify current user has permission to add agents
        verifyCanManageTeam(currentUser, agency, agentCreateDTO.getRole());
        
        // Check agency tier limits
        verifyAgencyCanAddAgent(agency, agentCreateDTO.getRole());
        
        // Find or create user
        User user;
        if (agentCreateDTO.getUserId() != null) {
            user = userRepository.findById(agentCreateDTO.getUserId())
                    .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + agentCreateDTO.getUserId()));
        } else {
            user = userRepository.findByEmail(agentCreateDTO.getEmail())
                    .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + agentCreateDTO.getEmail()));
        }
        
        // Check if user is already an agent in this agency
        if (agentRepository.findByUserIdAndAgencyId(user.getId(), agency.getId()).isPresent()) {
            throw new BusinessRuleException("User is already an agent in this agency");
        }
        
        // Check if user has ROLE_AGENCY or can be converted
        if (!user.isAgencyAdmin() && !user.hasRole("ROLE_USER")) {
            // For now, allow any user to become an agent
            // In future, we might need to update user roles
        }
        
        // Create agent
        Agent agent = agentMapper.toEntity(agentCreateDTO, user, agency, currentUser);
        Agent savedAgent = agentRepository.save(agent);
        
        logger.info("Agent added successfully: {} to agency: {}", user.getEmail(), agency.getName());
        
        return agentMapper.toResponseDTO(savedAgent);
    }
    
    /**
     * Update an existing agent
     */
    public AgentResponseDTO updateAgent(AgentUpdateDTO agentUpdateDTO, User currentUser) {
        logger.info("Updating agent with id: {}", agentUpdateDTO.getId());
        
        Agent agent = agentRepository.findById(agentUpdateDTO.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Agent not found with id: " + agentUpdateDTO.getId()));
        
        // Verify current user has permission to update this agent
        verifyCanManageAgent(currentUser, agent);
        
        // Update agent
        agentMapper.updateEntityFromDTO(agentUpdateDTO, agent);
        
        // If role is being updated, adjust permissions
        if (agentUpdateDTO.getRole() != null) {
            agent.setRole(agentUpdateDTO.getRole());
        }
        
        Agent updatedAgent = agentRepository.save(agent);
        logger.info("Agent updated successfully: {}", updatedAgent.getId());
        
        return agentMapper.toResponseDTO(updatedAgent);
    }
    
    /**
     * Remove an agent from agency (soft delete)
     */
    public void removeAgent(Long agentId, User currentUser) {
        logger.info("Removing agent with id: {}", agentId);
        
        Agent agent = agentRepository.findById(agentId)
                .orElseThrow(() -> new ResourceNotFoundException("Agent not found with id: " + agentId));
        
        // Verify current user has permission to remove this agent
        verifyCanManageAgent(currentUser, agent);
        
        // Don't allow removing agency owner
        if (agent.getRole() == AgentRole.OWNER) {
            throw new BusinessRuleException("Cannot remove agency owner");
        }
        
        // Deactivate agent
        agent.setIsActive(false);
        agentRepository.save(agent);
        
        logger.info("Agent removed successfully: {}", agentId);
    }
    
    /**
     * Get all agents for an agency
     */
    @Transactional(readOnly = true)
    public List<AgentResponseDTO> getAgencyAgents(Long agencyId, User currentUser) {
        Agency agency = agencyRepository.findById(agencyId)
                .orElseThrow(() -> new ResourceNotFoundException("Agency not found with id: " + agencyId));
        
        // Verify current user has access to view agents
        verifyCanViewTeam(currentUser, agency);
        
        List<Agent> agents = agentRepository.findByAgencyAndIsActive(agency, true);
        return agentMapper.toResponseDTOList(agents);
    }
    
    /**
     * Get agent by ID
     */
    @Transactional(readOnly = true)
    public AgentResponseDTO getAgentById(Long agentId, User currentUser) {
        Agent agent = agentRepository.findById(agentId)
                .orElseThrow(() -> new ResourceNotFoundException("Agent not found with id: " + agentId));
        
        // Verify current user has access to view this agent
        verifyCanViewAgent(currentUser, agent);
        
        return agentMapper.toResponseDTO(agent);
    }
    
    /**
     * Get team dashboard for an agency
     */
    @Transactional(readOnly = true)
    public TeamDashboardDTO getTeamDashboard(Long agencyId, User currentUser) {
        Agency agency = agencyRepository.findById(agencyId)
                .orElseThrow(() -> new ResourceNotFoundException("Agency not found with id: " + agencyId));
        
        // Verify current user has access to view dashboard
        verifyCanViewTeam(currentUser, agency);
        
        // Get active agents
        List<Agent> activeAgents = agentRepository.findByAgencyAndIsActive(agency, true);
        
        // Get pending invitations
        List<Invitation> pendingInvitations = invitationService.getPendingInvitationsForAgency(agency);
        
        // Calculate tier-based limits
        int maxAgentsAllowed = tierLimitationService.getMaxAgentsForAgency(agency);
        
        // Calculate total active listings
        int totalActiveListings = activeAgents.stream()
                .mapToInt(agent -> agent.getActiveListingsCount() != null ? agent.getActiveListingsCount() : 0)
                .sum();
        
        // Use dashboard mapper
        return teamDashboardMapper.toDashboardDTO(
            agency,
            activeAgents,
            pendingInvitations,
            maxAgentsAllowed,
            totalActiveListings
        );
    }
    
    /**
     * Verify if user can add/remove agents with specific role
     */
    private void verifyCanManageTeam(User currentUser, Agency agency, AgentRole roleToAssign) {
        if (!permissionService.canManageTeam(currentUser, agency)) {
            throw new BusinessRuleException("You don't have permission to manage team members");
        }
        
        if (!permissionService.canAssignRole(currentUser, agency, roleToAssign)) {
            throw new BusinessRuleException("You don't have permission to assign the role: " + roleToAssign.getDisplayName());
        }
    }
    
    /**
     * Verify if user can manage a specific agent
     */
    private void verifyCanManageAgent(User currentUser, Agent agentToManage) {
        Agent currentAgent = agentRepository.findByUserIdAndAgencyId(currentUser.getId(), agentToManage.getAgency().getId())
                .orElseThrow(() -> new BusinessRuleException("You are not a member of this agency"));
        
        if (!currentAgent.getCanInviteAgents()) {
            throw new BusinessRuleException("You don't have permission to manage agents");
        }
        
        if (!currentAgent.getRole().canManage(agentToManage.getRole())) {
            throw new BusinessRuleException("You don't have permission to manage this agent's role");
        }
    }
    
    /**
     * Verify if user can view team information
     */
    private void verifyCanViewTeam(User currentUser, Agency agency) {
        if (!permissionService.canViewTeam(currentUser, agency)) {
            throw new BusinessRuleException("You don't have permission to view team information");
        }
    }
    
    /**
     * Verify if user can view a specific agent
     */
    private void verifyCanViewAgent(User currentUser, Agent agent) {
        // Allow agents to view themselves
        if (currentUser.getId().equals(agent.getUser().getId())) {
            return;
        }
        
        // Otherwise check team viewing permissions
        verifyCanViewTeam(currentUser, agent.getAgency());
    }
    
    /**
     * Verify agency can add another agent based on tier limits
     */
    private void verifyAgencyCanAddAgent(Agency agency, AgentRole role) {
        // Check total agent limit
        long currentAgentCount = agentRepository.countByAgencyAndIsActive(agency, true);
        int maxAgents = tierLimitationService.getMaxAgentsForAgency(agency);
        
        if (currentAgentCount >= maxAgents) {
            throw new BusinessRuleException("Agency has reached maximum agent limit (" + maxAgents + ")");
        }
        
        // Check super agent limit if assigning SUPER_AGENT role
        if (role == AgentRole.SUPER_AGENT) {
            long currentSuperAgentCount = agentRepository.countByAgencyAndRole(agency, AgentRole.SUPER_AGENT);
            int maxSuperAgents = tierLimitationService.getMaxSuperAgentsForAgency(agency);
            
            if (currentSuperAgentCount >= maxSuperAgents) {
                throw new BusinessRuleException("Agency has reached maximum super agent limit (" + maxSuperAgents + ")");
            }
        }
    }
    
    /**
     * Update agent's last active date
     */
    public void updateAgentLastActive(Long agentId) {
        agentRepository.findById(agentId).ifPresent(agent -> {
            agent.setLastActiveDate(java.time.LocalDateTime.now());
            agentRepository.save(agent);
        });
    }
    
    /**
     * Get agent for current user in a specific agency
     */
    @Transactional(readOnly = true)
    public Optional<AgentResponseDTO> getCurrentUserAgentForAgency(Long agencyId, User currentUser) {
        return agentRepository.findByUserIdAndAgencyId(currentUser.getId(), agencyId)
                .map(agentMapper::toResponseDTO);
    }
    
    /**
     * Check if user is agent in agency
     */
    @Transactional(readOnly = true)
    public boolean isUserAgentInAgency(User user, Agency agency) {
        return agentRepository.findByUserIdAndAgencyId(user.getId(), agency.getId())
                .map(Agent::getIsActive)
                .orElse(false);
    }
}