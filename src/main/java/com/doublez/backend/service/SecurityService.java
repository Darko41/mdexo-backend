package com.doublez.backend.service;

import java.util.Set;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Service
public class SecurityService {
	
	public boolean hasRealEstateCreateAccess() {
		Authentication auth = SecurityContextHolder.getContext().getAuthentication();
		return auth.getAuthorities().stream()
				.anyMatch(a -> Set.of("ROLE_ADMIN", "ROLE_AGENT", "ROLE_USER")
						.contains(a.getAuthority()));
	}

}
