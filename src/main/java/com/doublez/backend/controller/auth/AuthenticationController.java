package com.doublez.backend.controller.auth;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.doublez.backend.config.security.JwtTokenUtil;
import com.doublez.backend.dto.AuthenticationRequest;
import com.doublez.backend.dto.AuthenticationResponse;
import com.doublez.backend.entity.User;
import com.doublez.backend.repository.UserRepository;

@RestController
@RequestMapping("/api")
public class AuthenticationController {
	
	private final AuthenticationManager authenticationManager;
	private final JwtTokenUtil jwtTokenUtil;
	private final UserRepository userRepository;
	
	public AuthenticationController(AuthenticationManager authenticationManager, JwtTokenUtil jwtTokenUtil, UserRepository userRepository) {
		this.authenticationManager = authenticationManager;
		this.jwtTokenUtil = jwtTokenUtil;
		this.userRepository = userRepository;
	}
	
	@PostMapping("/authenticate")
	public ResponseEntity<?> authenticateUser(@RequestBody AuthenticationRequest authenticationRequest) {
		// Authenticate user based on provided credentials
		
		try {
			authenticationManager.authenticate(
					new UsernamePasswordAuthenticationToken(
							authenticationRequest.getUsername(),
							authenticationRequest.getPassword()
							)
					);
		} catch (BadCredentialsException e) {
			return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid username or password");
		}
		
		// Fetch the user from database
		User user = userRepository.findByUsername(authenticationRequest.getUsername())
				.orElseThrow(() -> new UsernameNotFoundException("User not found"));
		
		// Generate JWT token for the user
		final String jwt = jwtTokenUtil.generateToken(user.getUsername());
		
		// Return the token to the client
		return ResponseEntity.ok(new AuthenticationResponse(jwt));
		
	}

}
