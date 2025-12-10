package com.doublez.backend.service.user;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.doublez.backend.dto.auth.CustomUserDetails;
import com.doublez.backend.entity.user.User;
import com.doublez.backend.repository.UserRepository;

// Manages the user authentication part. It loads user details based on the username, handles roles, etc

// TODO:
/* Make sure the authentication success handler calls:
 * After successful authentication
 * userRepository.save(user);
 * 
 * */

@Service
public class CustomUserDetailsService implements UserDetailsService {
    
    private final UserRepository userRepository;
    
    public CustomUserDetailsService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    @Transactional
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with email: " + email));
        
        // Update last login attempt time
        user.recordFailedLogin(); // This records the attempt (will be cleared on success)
        
        // Use the PRIMARY constructor that takes the full User entity
        // This allows CustomUserDetails to access all security fields and methods
        return new CustomUserDetails(user);
    }
}
