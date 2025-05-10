package com.doublez.backend.utils;

import java.util.Arrays;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

public final class RoleUtils {
	
	public static final String ROLE_PREFIX = "ROLE_";

	public static boolean hasAnyRole(Authentication auth, String... roles) {
		return auth.getAuthorities().stream()
				.anyMatch(a -> Arrays.stream(roles).anyMatch(role -> (ROLE_PREFIX + role).equals(a.getAuthority())));
	}

	public static boolean hasRole(Authentication auth, String role) {
		return auth.getAuthorities().stream().anyMatch(a -> (ROLE_PREFIX + role).equals(a.getAuthority()));
	}
	
	public static String prefixRole(String role) {
		return role.startsWith(ROLE_PREFIX) ? role : ROLE_PREFIX + role;
	}

}
