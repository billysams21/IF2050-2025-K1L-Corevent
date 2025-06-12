package com.corevent.config;

import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import com.corevent.entity.Committee;
import com.corevent.entity.User;
import com.corevent.repository.UserRepository;

@Component
public class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public DataInitializer(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {
        // Create default admin user
        if (!userRepository.existsByUsername("admin")) {
            Committee admin = new Committee();
            admin.setUsername("admin");
            admin.setPassword(passwordEncoder.encode("admin"));
            admin.setFullName("Amin");
            admin.setEmail("admin@corevent.com");
            admin.setRole(User.UserRole.COMMITTEE);
            admin.setStatus(User.AccountStatus.ACTIVE);
            userRepository.save(admin);
        }
    }
} 