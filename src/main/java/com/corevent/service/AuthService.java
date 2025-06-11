package com.corevent.service;

import java.time.LocalDateTime;
import java.util.UUID;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.corevent.entity.User;
import com.corevent.repository.UserRepository;

@Service
public class AuthService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;

    public AuthService(UserRepository userRepository, 
                      PasswordEncoder passwordEncoder,
                      AuthenticationManager authenticationManager) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.authenticationManager = authenticationManager;
    }

    @Transactional
    public User login(String username, String password, boolean rememberMe) {
        Authentication authentication = authenticationManager.authenticate(
            new UsernamePasswordAuthenticationToken(username, password)
        );
        
        SecurityContextHolder.getContext().setAuthentication(authentication);
        User user = (User) authentication.getPrincipal();
        
        user.setLastLogin(LocalDateTime.now());
        
        if (rememberMe) {
            String token = UUID.randomUUID().toString();
            user.setRememberMeToken(token);
            user.setRememberMeExpiry(LocalDateTime.now().plusDays(30));
        }
        
        return userRepository.save(user);
    }

    public void logout() {
        SecurityContextHolder.clearContext();
    }

    public User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated()) {
            return (User) authentication.getPrincipal();
        }
        return null;
    }

    public boolean validateRememberMeToken(String token) {
        User user = userRepository.findByRememberMeToken(token)
            .orElse(null);
            
        if (user != null && user.getRememberMeExpiry() != null && 
            user.getRememberMeExpiry().isAfter(LocalDateTime.now())) {
            return true;
        }
        return false;
    }
} 