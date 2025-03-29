package com.doublez.backend.service;

import java.util.Collection;
import java.util.stream.Collectors;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.security.core.userdetails.User;

import com.doublez.backend.dto.UserDetailsDTO;
import com.doublez.backend.entity.Role;
import com.doublez.backend.repository.UserRepository;

// Manages the user authentication part. It loads user details based on the username, handles roles, etc

@Service
public class CustomUserDetailsService implements UserDetailsService{
	
	private final UserRepository userRepository;
	
	public CustomUserDetailsService(UserRepository userRepository) {
		this.userRepository = userRepository;
	}

	@Override
	@Transactional
	public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
		// Fetch the user entity from the database
		com.doublez.backend.entity.User user = userRepository.findByEmail(email)
				.orElseThrow(() -> new UsernameNotFoundException("User not found with this email: " + email));
		 
		// Convert the user to a UserDetailsDTO
		UserDetailsDTO userDTO = new UserDetailsDTO(user.getEmail(), user.getRoles().stream()
				.map(Role::getName)
				.collect(Collectors.toList()));
		
		// Map roles from DTO
		Collection<? extends GrantedAuthority> authorities = getAuthorities(userDTO);
		
		return new User(userDTO.getEmail(), user.getPassword(), authorities);
	}

	private Collection<? extends GrantedAuthority> getAuthorities(UserDetailsDTO userDTO) {
		// mapping roles/authorities based on needs
		return userDTO.getRoles().stream()
				.map(SimpleGrantedAuthority::new)
				.collect(Collectors.toList());
	}
}
