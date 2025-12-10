package com.doublez.backend.mapper;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.doublez.backend.dto.team.TeamDashboardDTO;
import com.doublez.backend.entity.agency.Agency;
import com.doublez.backend.entity.agency.Agent;
import com.doublez.backend.entity.agency.Invitation;
import com.doublez.backend.enums.agency.AgentRole;

@Component
public class TeamDashboardMapper {
    
    @Autowired
    private AgentMapper agentMapper;
    
    @Autowired
    private InvitationMapper invitationMapper;
    
    public TeamDashboardDTO toDashboardDTO(Agency agency, 
                                          List<Agent> agents,
                                          List<Invitation> pendingInvitations,
                                          int maxAgentsAllowed,
                                          int totalActiveListings) {
        
        TeamDashboardDTO dashboard = new TeamDashboardDTO();
        
        // Basic agency info
        dashboard.setAgencyId(agency.getId());
        dashboard.setAgencyName(agency.getName());
        
        // Agent statistics
        int activeAgentCount = agents.size();
        dashboard.setTotalAgents(activeAgentCount);
        dashboard.setActiveAgents(activeAgentCount);
        
        // Calculate super agent count
        long superAgentCount = agents.stream()
                .filter(agent -> agent.getRole() == AgentRole.SUPER_AGENT)
                .count();
        dashboard.setSuperAgentCount((int) superAgentCount);
        
        // Invitation statistics
        int pendingInvitationCount = pendingInvitations.size();
        dashboard.setPendingInvitations(pendingInvitationCount);
        
        // Tier limits
        dashboard.setMaxAgentsAllowed(maxAgentsAllowed);
        dashboard.setRemainingAgentSlots(Math.max(0, maxAgentsAllowed - activeAgentCount));
        dashboard.setCanAddMoreAgents(activeAgentCount < maxAgentsAllowed);
        
        // Map agents to DTOs
        dashboard.setAgents(agentMapper.toResponseDTOList(agents));
        
        // Map invitations to DTOs
        dashboard.setPendingInvitationsList(invitationMapper.toResponseDTOList(pendingInvitations));
        
        // Performance metrics
        dashboard.setTotalActiveListings(totalActiveListings);
        
        // Calculate team performance metrics
        calculateTeamPerformance(agents, dashboard);
        
        return dashboard;
    }
    
    private void calculateTeamPerformance(List<Agent> agents, TeamDashboardDTO dashboard) {
        // Calculate total leads this month (placeholder - implement with actual data)
        int totalLeadsThisMonth = agents.stream()
                .mapToInt(agent -> agent.getLeadsGenerated() != null ? agent.getLeadsGenerated() : 0)
                .sum();
        dashboard.setTotalLeadsThisMonth(totalLeadsThisMonth);
        
        // Calculate total deals closed this month (placeholder)
        int totalDealsClosedThisMonth = agents.stream()
                .mapToInt(agent -> agent.getDealsClosed() != null ? agent.getDealsClosed() : 0)
                .sum();
        dashboard.setTotalDealsClosedThisMonth(totalDealsClosedThisMonth);
        
        // Calculate team conversion rate
        double teamConversionRate = 0.0;
        if (totalLeadsThisMonth > 0) {
            teamConversionRate = (double) totalDealsClosedThisMonth / totalLeadsThisMonth * 100;
        }
        // The conversion rate is calculated in the getter, but we can set it if needed
    }
    
    /**
     * Create a simplified dashboard for quick overview
     */
    public TeamDashboardDTO toQuickOverviewDTO(Agency agency, 
                                              List<Agent> agents,
                                              int maxAgentsAllowed) {
        
        TeamDashboardDTO dashboard = new TeamDashboardDTO();
        dashboard.setAgencyId(agency.getId());
        dashboard.setAgencyName(agency.getName());
        
        int activeAgentCount = agents.size();
        dashboard.setTotalAgents(activeAgentCount);
        dashboard.setActiveAgents(activeAgentCount);
        
        // Calculate super agent count
        long superAgentCount = agents.stream()
                .filter(agent -> agent.getRole() == AgentRole.SUPER_AGENT)
                .count();
        dashboard.setSuperAgentCount((int) superAgentCount);
        
        // Tier limits
        dashboard.setMaxAgentsAllowed(maxAgentsAllowed);
        dashboard.setRemainingAgentSlots(Math.max(0, maxAgentsAllowed - activeAgentCount));
        dashboard.setCanAddMoreAgents(activeAgentCount < maxAgentsAllowed);
        
        // Quick performance metrics
        int totalActiveListings = agents.stream()
                .mapToInt(agent -> agent.getActiveListingsCount() != null ? agent.getActiveListingsCount() : 0)
                .sum();
        dashboard.setTotalActiveListings(totalActiveListings);
        
        return dashboard;
    }
}