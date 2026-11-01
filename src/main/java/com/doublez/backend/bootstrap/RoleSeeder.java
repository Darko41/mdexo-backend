package com.doublez.backend.bootstrap;

import com.doublez.backend.entity.Role;
import com.doublez.backend.entity.user.UserRole;
import com.doublez.backend.repository.RoleRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.stream.Stream;

@Component
public class RoleSeeder implements CommandLineRunner {
    private final RoleRepository roleRepository;
    public RoleSeeder(RoleRepository roleRepository) { this.roleRepository = roleRepository; }

    @Override
    @Transactional
    public void run(String... args) {
        Stream.of(UserRole.values())
                .map(r -> "ROLE_" + r.name())
                .forEach(name -> createIfMissing(name));
        createIfMissing("ROLE_ADMIN");
    }

    private void createIfMissing(String name) {
        if (roleRepository.findByName(name).isEmpty()) {
            Role role = new Role();
            role.setName(name);
            roleRepository.save(role);
        }
    }
}
