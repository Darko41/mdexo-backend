package com.doublez.backend.aspect;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.doublez.backend.annotation.RequiresPermission;
import com.doublez.backend.service.usage.PermissionService;

@Aspect
@Component
public class PermissionAspect {
    
    @Autowired
    private PermissionService permissionService;
    
    @Before("@annotation(com.doublez.backend.annotation.RequiresPermission)")
    public void checkPermission(JoinPoint joinPoint) {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();
        RequiresPermission annotation = method.getAnnotation(RequiresPermission.class);
        
        Object[] args = joinPoint.getArgs();
        Parameter[] parameters = method.getParameters();
        
        // Extract parameters based on annotation
        Long agencyId = extractParameter(annotation.agencyIdParam(), parameters, args, Long.class);
        Long listingId = extractParameter(annotation.listingIdParam(), parameters, args, Long.class);
        Long agentId = extractParameter(annotation.agentIdParam(), parameters, args, Long.class);
        
        // Get current user (you'll need to implement this)
        // User currentUser = SecurityUtils.getCurrentUser();
        
        // Based on permission type, check access
        switch (annotation.value()) {
            case VIEW_TEAM:
                // permissionService.canViewTeam(currentUser, agencyId);
                break;
            case MANAGE_TEAM:
                // permissionService.canManageTeam(currentUser, agencyId);
                break;
            case VIEW_LISTING:
                // permissionService.canViewListing(currentUser, listingId);
                break;
            case EDIT_LISTING:
                // permissionService.canEditListing(currentUser, listingId);
                break;
            // Add other permission types
        }
        
        // If permission check fails, throw PermissionException
        // throw new PermissionException("Access denied", annotation.value().name(), resourceId);
    }
    
    private <T> T extractParameter(String paramName, Parameter[] parameters, Object[] args, Class<T> type) {
        for (int i = 0; i < parameters.length; i++) {
            if (parameters[i].getName().equals(paramName)) {
                return type.cast(args[i]);
            }
        }
        return null;
    }
}
