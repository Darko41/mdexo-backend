package com.doublez.backend.service.agency;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.doublez.backend.entity.agency.Agency;
import com.doublez.backend.enums.agency.AgentRole;
import com.doublez.backend.repository.AgentRepository;
import com.doublez.backend.service.usage.TierLimitationService;

@Service
public class SuperAgentSeatService {
    
    @Autowired
    private AgentRepository agentRepository;
    
    @Autowired
    private TierLimitationService tierLimitationService;
    
    public boolean canAddSuperAgent(Agency agency) {
        long currentSuperAgentCount = agentRepository.countByAgencyAndRole(agency, AgentRole.SUPER_AGENT);
        int maxSuperAgents = tierLimitationService.getMaxSuperAgentsForAgency(agency);
        return currentSuperAgentCount < maxSuperAgents;
    }
    
    public int getRemainingSuperAgentSeats(Agency agency) {
        long currentSuperAgentCount = agentRepository.countByAgencyAndRole(agency, AgentRole.SUPER_AGENT);
        int maxSuperAgents = tierLimitationService.getMaxSuperAgentsForAgency(agency);
        return Math.max(0, maxSuperAgents - (int) currentSuperAgentCount);
    }
    
    public boolean upgradeToSuperAgent(Long agentId, Agency agency) {
        if (!canAddSuperAgent(agency)) {
            return false;
        }
        
        return agentRepository.findById(agentId)
                .map(agent -> {
                    if (agent.getRole() != AgentRole.SUPER_AGENT) {
                        agent.setRole(AgentRole.SUPER_AGENT);
                        agent.setCanViewAnalytics(true);
                        agent.setCanInviteAgents(true);
                        agentRepository.save(agent);
                        return true;
                    }
                    return false;
                })
                .orElse(false);
    }
    
    public boolean downgradeToAgent(Long agentId) {
        return agentRepository.findById(agentId)
                .map(agent -> {
                    if (agent.getRole() == AgentRole.SUPER_AGENT) {
                        agent.setRole(AgentRole.AGENT);
                        agent.setCanViewAnalytics(false);
                        agent.setCanInviteAgents(false);
                        agentRepository.save(agent);
                        return true;
                    }
                    return false;
                })
                .orElse(false);
    }
}