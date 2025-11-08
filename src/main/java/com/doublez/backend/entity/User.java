package com.doublez.backend.entity;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import com.doublez.backend.entity.agency.Agency;
import com.doublez.backend.entity.agency.AgencyMembership;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;

@Entity
@Table(name = "users")
public class User {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @NotNull(message = "Email cannot be null")
    @Email(message = "Please provide a valid email address")
    @Column(unique = true, nullable = false)
    private String email;
    
    @NotNull(message = "Password cannot be null")
    @Column(nullable = false)
    private String password;
    
    @ManyToMany(fetch = FetchType.LAZY, cascade = CascadeType.PERSIST)
    @JoinTable(
            name = "user_roles",
            joinColumns = @JoinColumn(name = "user_id"),
            inverseJoinColumns = @JoinColumn(name = "role_id"))
    private List<Role> roles;
    
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDate createdAt;
    
    @Column(name = "updated_at")
    private LocalDate updatedAt;
    
    @OneToOne(mappedBy = "user", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private UserProfile userProfile;
    
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<AgencyMembership> agencyMemberships = new ArrayList<>();

    public List<AgencyMembership> getAgencyMemberships() {
		return agencyMemberships;
	}

	public void setAgencyMemberships(List<AgencyMembership> agencyMemberships) {
		this.agencyMemberships = agencyMemberships;
	}

	public List<Agency> getOwnedAgencies() {
		return ownedAgencies;
	}

	public void setOwnedAgencies(List<Agency> ownedAgencies) {
		this.ownedAgencies = ownedAgencies;
	}

	@OneToMany(mappedBy = "admin", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Agency> ownedAgencies = new ArrayList<>();

    // No constructors - using default constructor only

    @PrePersist
    public void prePersist() {
        this.createdAt = LocalDate.now();
    }

    public void preUpdate() {
        this.updatedAt = LocalDate.now();
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public List<Role> getRoles() {
        return roles;
    }

    public void setRoles(List<Role> roles) {
        this.roles = roles;
    }

    public LocalDate getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDate createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDate getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDate updatedAt) {
        this.updatedAt = updatedAt;
    }

    public UserProfile getUserProfile() {
        return userProfile;
    }

    public void setUserProfile(UserProfile userProfile) {
        this.userProfile = userProfile;
    }

    // Helper method to ensure profile exists
    public UserProfile getOrCreateProfile() {
        if (this.userProfile == null) {
            this.userProfile = new UserProfile(this);
        }
        return this.userProfile;
    }

    public boolean isAdmin() {
        return this.roles.stream()
                .anyMatch(role -> role.getName().equals("ROLE_ADMIN"));
    }
    
    public boolean hasAnyRole(String... roleNames) {
        return this.roles.stream()
                .map(Role::getName)
                .anyMatch(role -> Arrays.asList(roleNames).contains(role));
    }
    
    public boolean hasRole(String roleName) {
        return this.roles.stream()
                .anyMatch(role -> role.getName().equals(roleName));
    }
    
    
    // Helper methods for agency functionality
    public boolean isAgencyAdmin() {
        return this.roles.stream()
                .anyMatch(role -> role.getName().equals("ROLE_AGENCY_ADMIN"));
    }

    public boolean isAgent() {
        return this.roles.stream()
                .anyMatch(role -> role.getName().equals("ROLE_AGENT"));
    }

    public Optional<Agency> getOwnedAgency() {
        return this.ownedAgencies.stream().findFirst();
    }

    public List<Agency> getAgentAgencies() {
        return this.agencyMemberships.stream()
                .filter(m -> m.getStatus() == AgencyMembership.MembershipStatus.ACTIVE)
                .map(AgencyMembership::getAgency)
                .collect(Collectors.toList());
    }
}
