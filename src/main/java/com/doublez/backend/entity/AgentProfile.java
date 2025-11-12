package com.doublez.backend.entity;

import java.time.LocalDateTime;

import com.doublez.backend.entity.user.User;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.MapsId;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "agent_profiles")
public class AgentProfile {
    @Id
    private Long id;
    
    @OneToOne
    @MapsId
    @JoinColumn(name = "id")
    private User user;
    
    private String companyName;
    private String licenseNumber;
    private String specialties; // JSON array stored as string
    private String yearsExperience;
    private String website;
    private String bio;
    private boolean isVerified = false;
    private Double rating;
    private Integer reviewCount;
    
    @Column(name = "created_at")
    private LocalDateTime createdAt;
}