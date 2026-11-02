package com.doublez.backend.aspect;

import com.doublez.backend.annotation.RequiresPermission;
import com.doublez.backend.entity.Role;
import com.doublez.backend.entity.agency.Agency;
import com.doublez.backend.entity.agency.Agent;
import com.doublez.backend.entity.realestate.RealEstate;
import com.doublez.backend.entity.user.User;
import com.doublez.backend.exception.PermissionException;
import com.doublez.backend.exception.ResourceNotFoundException;
import com.doublez.backend.repository.AgencyRepository;
import com.doublez.backend.repository.AgentRepository;
import com.doublez.backend.repository.realestate.RealEstateRepository;
import com.doublez.backend.service.agency.PermissionService;
import com.doublez.backend.utils.SecurityUtils;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Aspect
@Component
public class PermissionAspect {

    private static final Logger log = LoggerFactory.getLogger(PermissionAspect.class);

    private final PermissionService permissionService;
    private final SecurityUtils securityUtils;
    private final AgencyRepository agencyRepository;
    private final RealEstateRepository realEstateRepository;
    private final AgentRepository agentRepository;

    public PermissionAspect(PermissionService permissionService,
                            SecurityUtils securityUtils,
                            AgencyRepository agencyRepository,
                            RealEstateRepository realEstateRepository,
                            AgentRepository agentRepository) {
        this.permissionService = permissionService;
        this.securityUtils = securityUtils;
        this.agencyRepository = agencyRepository;
        this.realEstateRepository = realEstateRepository;
        this.agentRepository = agentRepository;
    }

    @Before("@annotation(requiresPermission)")
    public void checkPermission(JoinPoint joinPoint, RequiresPermission requiresPermission) {
        User currentUser = securityUtils.getCurrentUser();
        Object[] args = joinPoint.getArgs();

        Long agencyId = extractLong(args, requiresPermission.agencyIdIndex());
        Long listingId = extractLong(args, requiresPermission.listingIdIndex());
        Long agentId = extractLong(args, requiresPermission.agentIdIndex());

        // Load resources first — throws 404 for missing resources, even for admins
        Agency agency = agencyId != null ? loadAgency(agencyId) : null;
        RealEstate listing = listingId != null ? loadListing(listingId) : null;
        Agent agent = agentId != null ? loadAgent(agentId) : null;

        // Platform admins bypass the permission decision (but resources must exist)
        if (currentUser.isAdmin()) {
            return;
        }

        boolean granted = switch (requiresPermission.value()) {
            case VIEW_TEAM -> permissionService.canViewTeam(currentUser, agency);
            case MANAGE_TEAM, INVITE_AGENTS -> permissionService.canManageTeam(currentUser, agency);
            case CREATE_LISTING -> permissionService.canCreateListing(currentUser, agency);
            case VIEW_LISTING -> permissionService.canViewListing(currentUser, listing);
            case EDIT_LISTING -> permissionService.canEditListing(currentUser, listing);
            case DELETE_LISTING, MANAGE_LISTINGS -> permissionService.canDeleteListing(currentUser, listing);
            case VIEW_BILLING -> permissionService.canViewBilling(currentUser, agency);
            case MANAGE_BILLING, PURCHASE_CREDITS -> permissionService.canManageBilling(currentUser, agency);
            case VIEW_ANALYTICS -> permissionService.canViewAnalytics(currentUser, agency);
            case VIEW_AGENT_PERFORMANCE -> permissionService.canViewAgentPerformance(currentUser, agent);
            case MANAGE_AGENCY_SETTINGS -> permissionService.canManageAgencySettings(currentUser, agency);
            case VERIFY_AGENTS -> currentUser.isAdmin() || permissionService.canManageTeam(currentUser, agency);
            case VIEW_AGENCY_LISTINGS -> permissionService.canViewAgencyListings(currentUser, agency);
        };

        if (!granted) {
            Long resourceId = listingId != null ? listingId
                    : agentId != null ? agentId
                    : agencyId;
            String resourceDesc = resourceId != null ? " (resource: " + resourceId + ")" : "";
            log.warn("Permission denied: userId={} permission={} resource={}",
                    currentUser.getId(), requiresPermission.value(), resourceId);
            throw new PermissionException(
                    "Access denied to " + requiresPermission.value().name() + resourceDesc,
                    requiresPermission.value().name(),
                    resourceId != null ? resourceId.toString() : null
            );
        }
    }

    private Long extractLong(Object[] args, int index) {
        if (index < 0 || index >= args.length) return null;
        Object arg = args[index];
        if (arg == null) return null;
        if (arg instanceof Long l) return l;
        if (arg instanceof Integer i) return i.longValue();
        throw new IllegalArgumentException(
                "RequiresPermission: expected Long/Integer at index " + index + ", got " + arg.getClass().getName());
    }

    private Agency loadAgency(Long agencyId) {
        if (agencyId == null) {
            throw new IllegalArgumentException("RequiresPermission: agencyIdIndex not set or arg missing");
        }
        return agencyRepository.findById(agencyId)
                .orElseThrow(() -> new ResourceNotFoundException("Agency not found: " + agencyId));
    }

    private RealEstate loadListing(Long listingId) {
        if (listingId == null) {
            throw new IllegalArgumentException("RequiresPermission: listingIdIndex not set or arg missing");
        }
        return realEstateRepository.findById(listingId)
                .orElseThrow(() -> new ResourceNotFoundException("Listing not found: " + listingId));
    }

    private Agent loadAgent(Long agentId) {
        if (agentId == null) {
            throw new IllegalArgumentException("RequiresPermission: agentIdIndex not set or arg missing");
        }
        return agentRepository.findById(agentId)
                .orElseThrow(() -> new ResourceNotFoundException("Agent not found: " + agentId));
    }
}
