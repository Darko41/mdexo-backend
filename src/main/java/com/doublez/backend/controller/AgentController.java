package com.doublez.backend.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.doublez.backend.dto.agent.AgentCreateDTO;
import com.doublez.backend.dto.agent.AgentResponseDTO;
import com.doublez.backend.dto.agent.AgentUpdateDTO;
import com.doublez.backend.dto.team.TeamDashboardDTO;
import com.doublez.backend.entity.user.User;
import com.doublez.backend.service.agency.TeamService;
import com.doublez.backend.utils.SecurityUtils;

import io.swagger.v3.oas.annotations.parameters.RequestBody;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/v1/agents")
public class AgentController {
    
    @Autowired
    private TeamService teamService;
    
    @Autowired
    private SecurityUtils securityUtils;
    
    /**
     * Add new agent to agency
     */
    @PostMapping
    @PreAuthorize("hasRole('AGENCY')")
    public ResponseEntity<AgentResponseDTO> addAgent(@Valid @RequestBody AgentCreateDTO agentCreateDTO) {
        User currentUser = getCurrentUser();
        AgentResponseDTO agent = teamService.addAgent(agentCreateDTO, currentUser);
        return ResponseEntity.status(HttpStatus.CREATED).body(agent);
    }
    
    /**
     * Update agent
     */
    @PutMapping("/{agentId}")
    @PreAuthorize("hasRole('AGENCY')")
    public ResponseEntity<AgentResponseDTO> updateAgent(
            @PathVariable Long agentId,
            @Valid @RequestBody AgentUpdateDTO agentUpdateDTO) {
        agentUpdateDTO.setId(agentId);
        User currentUser = getCurrentUser();
        AgentResponseDTO updatedAgent = teamService.updateAgent(agentUpdateDTO, currentUser);
        return ResponseEntity.ok(updatedAgent);
    }
    
    /**
     * Remove agent from agency
     */
    @DeleteMapping("/{agentId}")
    @PreAuthorize("hasRole('AGENCY')")
    public ResponseEntity<Void> removeAgent(@PathVariable Long agentId) {
        User currentUser = getCurrentUser();
        teamService.removeAgent(agentId, currentUser);
        return ResponseEntity.noContent().build();
    }
    
    /**
     * Get agent by ID
     */
    @GetMapping("/{agentId}")
    @PreAuthorize("hasRole('AGENCY')")
    public ResponseEntity<AgentResponseDTO> getAgent(@PathVariable Long agentId) {
        User currentUser = getCurrentUser();
        AgentResponseDTO agent = teamService.getAgentById(agentId, currentUser);
        return ResponseEntity.ok(agent);
    }
    
    /**
     * Get all agents for current user's agency
     */
    @GetMapping("/agency/{agencyId}")
    @PreAuthorize("hasRole('AGENCY')")
    public ResponseEntity<List<AgentResponseDTO>> getAgencyAgents(@PathVariable Long agencyId) {
        User currentUser = getCurrentUser();
        List<AgentResponseDTO> agents = teamService.getAgencyAgents(agencyId, currentUser);
        return ResponseEntity.ok(agents);
    }
    
    /**
     * Get team dashboard for agency
     */
    @GetMapping("/agency/{agencyId}/dashboard")
    @PreAuthorize("hasRole('AGENCY')")
    public ResponseEntity<TeamDashboardDTO> getTeamDashboard(@PathVariable Long agencyId) {
        User currentUser = getCurrentUser();
        TeamDashboardDTO dashboard = teamService.getTeamDashboard(agencyId, currentUser);
        return ResponseEntity.ok(dashboard);
    }
    
    /**
     * Get current user's agent profile for an agency
     */
    @GetMapping("/my-agent/{agencyId}")
    @PreAuthorize("hasRole('AGENCY')")
    public ResponseEntity<AgentResponseDTO> getMyAgentProfile(@PathVariable Long agencyId) {
        User currentUser = getCurrentUser();
        return teamService.getCurrentUserAgentForAgency(agencyId, currentUser)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
    
    private User getCurrentUser() {
        // This should be implemented to get the authenticated user
        // For now, returning a placeholder
        throw new UnsupportedOperationException("Implement getCurrentUser() based on your authentication");
    }
}