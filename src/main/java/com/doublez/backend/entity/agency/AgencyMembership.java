package com.doublez.backend.entity.agency;

import java.time.LocalDate;

import com.doublez.backend.entity.user.User;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;

@Entity
@Table(name = "agency_memberships")
public class AgencyMembership  {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user; // ROLE_AGENT

    @ManyToOne
    @JoinColumn(name = "agency_id", nullable = false)
    private Agency agency;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private MembershipStatus status = MembershipStatus.PENDING;

    private String position;
    
    @Column(name = "join_date")
    private LocalDate joinDate;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDate createdAt;

    public enum MembershipStatus {
        PENDING, ACTIVE, INACTIVE, REJECTED
    }

    @PrePersist
    public void prePersist() {
        this.createdAt = LocalDate.now();
        if (this.joinDate == null && this.status == MembershipStatus.ACTIVE) {
            this.joinDate = LocalDate.now();
        }
    }

    // Constructors, getters, setters
    
    
	public AgencyMembership() {
		super();
	}
	
	public AgencyMembership(Long id, User user, Agency agency, MembershipStatus status, String position,
			LocalDate joinDate, LocalDate createdAt) {
		super();
		this.id = id;
		this.user = user;
		this.agency = agency;
		this.status = status;
		this.position = position;
		this.joinDate = joinDate;
		this.createdAt = createdAt;
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public User getUser() {
		return user;
	}

	public void setUser(User user) {
		this.user = user;
	}

	public Agency getAgency() {
		return agency;
	}

	public void setAgency(Agency agency) {
		this.agency = agency;
	}

	public MembershipStatus getStatus() {
		return status;
	}

	public void setStatus(MembershipStatus status) {
		this.status = status;
	}

	public String getPosition() {
		return position;
	}

	public void setPosition(String position) {
		this.position = position;
	}

	public LocalDate getJoinDate() {
		return joinDate;
	}

	public void setJoinDate(LocalDate joinDate) {
		this.joinDate = joinDate;
	}

	public LocalDate getCreatedAt() {
		return createdAt;
	}

	public void setCreatedAt(LocalDate createdAt) {
		this.createdAt = createdAt;
	}

}
