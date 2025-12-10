package com.doublez.backend.dto.team;

import java.util.List;

import com.doublez.backend.dto.agent.AgentResponseDTO;
import com.doublez.backend.dto.invitation.InvitationResponseDTO;

public class TeamDashboardDTO {
    
    private Long agencyId;
    private String agencyName;
    private int totalAgents;
    private int activeAgents;
    private int pendingInvitations;
    private int superAgentCount;
    private int maxAgentsAllowed; // Based on tier
    private int remainingAgentSlots;
    private boolean canAddMoreAgents;
    
    private List<AgentResponseDTO> agents;
    private List<InvitationResponseDTO> pendingInvitationsList;
    
    // Performance overview
    private int totalActiveListings;
    private int totalLeadsThisMonth;
    private int totalDealsClosedThisMonth;
    private double teamConversionRate;
    
    // Getters and setters
    public Long getAgencyId() { return agencyId; }
    public void setAgencyId(Long agencyId) { this.agencyId = agencyId; }
    
    public String getAgencyName() { return agencyName; }
    public void setAgencyName(String agencyName) { this.agencyName = agencyName; }
    
    public int getTotalAgents() { return totalAgents; }
    public void setTotalAgents(int totalAgents) { this.totalAgents = totalAgents; }
    
    public int getActiveAgents() { return activeAgents; }
    public void setActiveAgents(int activeAgents) { this.activeAgents = activeAgents; }
    
    public int getPendingInvitations() { return pendingInvitations; }
    public void setPendingInvitations(int pendingInvitations) { this.pendingInvitations = pendingInvitations; }
    
    public int getSuperAgentCount() { return superAgentCount; }
    public void setSuperAgentCount(int superAgentCount) { this.superAgentCount = superAgentCount; }
    
    public int getMaxAgentsAllowed() { return maxAgentsAllowed; }
    public void setMaxAgentsAllowed(int maxAgentsAllowed) { this.maxAgentsAllowed = maxAgentsAllowed; }
    
    public int getRemainingAgentSlots() { return remainingAgentSlots; }
    public void setRemainingAgentSlots(int remainingAgentSlots) { this.remainingAgentSlots = remainingAgentSlots; }
    
    public boolean isCanAddMoreAgents() { return canAddMoreAgents; }
    public void setCanAddMoreAgents(boolean canAddMoreAgents) { this.canAddMoreAgents = canAddMoreAgents; }
    
    public List<AgentResponseDTO> getAgents() { return agents; }
    public void setAgents(List<AgentResponseDTO> agents) { this.agents = agents; }
    
    public List<InvitationResponseDTO> getPendingInvitationsList() { return pendingInvitationsList; }
    public void setPendingInvitationsList(List<InvitationResponseDTO> pendingInvitationsList) { this.pendingInvitationsList = pendingInvitationsList; }
    
    public int getTotalActiveListings() { return totalActiveListings; }
    public void setTotalActiveListings(int totalActiveListings) { this.totalActiveListings = totalActiveListings; }
    
    public int getTotalLeadsThisMonth() { return totalLeadsThisMonth; }
    public void setTotalLeadsThisMonth(int totalLeadsThisMonth) { this.totalLeadsThisMonth = totalLeadsThisMonth; }
    
    public int getTotalDealsClosedThisMonth() { return totalDealsClosedThisMonth; }
    public void setTotalDealsClosedThisMonth(int totalDealsClosedThisMonth) { this.totalDealsClosedThisMonth = totalDealsClosedThisMonth; }
    
    public double getTeamConversionRate() {
        if (totalLeadsThisMonth > 0) {
            return (double) totalDealsClosedThisMonth / totalLeadsThisMonth * 100;
        }
        return 0.0;
    }
    
    public void setTeamConversionRate(double teamConversionRate) {
        // Read-only property
    }
}