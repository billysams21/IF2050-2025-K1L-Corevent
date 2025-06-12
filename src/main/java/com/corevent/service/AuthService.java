package com.corevent.service;

import java.time.LocalDateTime;
import java.util.concurrent.CompletableFuture;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.corevent.dto.auth.LoginResponse;
import com.corevent.entity.User;
import com.corevent.repository.UserRepository;
import com.corevent.security.JwtTokenUtil;
import com.corevent.util.SessionManager;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtTokenUtil jwtTokenUtil;

    public CompletableFuture<LoginResponse> authenticate(String username, String password, boolean rememberMe) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(username, password)
                );
                
                SecurityContextHolder.getContext().setAuthentication(authentication);
                User user = (User) authentication.getPrincipal();
                
                if (user.getStatus() != User.AccountStatus.ACTIVE) {
                    return new LoginResponse(false, "Account is not active", null, null, null, 0);
                }
                
                String token = jwtTokenUtil.generateToken(user);
                String refreshToken = rememberMe ? jwtTokenUtil.generateRefreshToken(user) : null;
                
                handleSuccessfulLogin(user, token, rememberMe);
                
                return new LoginResponse(true, "Login successful", 
                    user, token, refreshToken, jwtTokenUtil.getExpirationTime());
                
            } catch (Exception e) {
                log.error("Authentication failed", e);
                return new LoginResponse(false, "Invalid username or password", null, null, null, 0);
            }
        });
    }

    @Transactional
    public boolean register(User user) {
        try {
            // Check if username or email already exists
            if (userRepository.existsByUsername(user.getUsername()) || 
                userRepository.existsByEmail(user.getEmail())) {
                return false;
            }
            
            // Encode password
            user.setPassword(passwordEncoder.encode(user.getPassword()));
            
            // Set default values
            user.setEnabled(true);
            user.setCreatedAt(LocalDateTime.now());
            user.setStatus(User.AccountStatus.ACTIVE);
            
            // Save user
            userRepository.save(user);
            return true;
        } catch (Exception e) {
            log.error("Registration failed", e);
            return false;
        }
    }

    private void handleSuccessfulLogin(User user, String token, boolean rememberMe) {
        user.setLastLogin(LocalDateTime.now());
        
        if (rememberMe) {
            user.setRememberMeToken(token);
            user.setRememberMeExpiry(LocalDateTime.now().plusDays(30));
        }
        
        userRepository.save(user);
        SessionManager.getInstance().setSession(user, token, rememberMe);
    }

    public void logout() {
        User currentUser = SessionManager.getInstance().getCurrentUser();
        if (currentUser != null) {
            currentUser.setRememberMeToken(null);
            currentUser.setRememberMeExpiry(null);
            userRepository.save(currentUser);
        }
        
        SecurityContextHolder.clearContext();
        SessionManager.getInstance().clearSession();
    }

    public User getCurrentUser() {
        return SessionManager.getInstance().getCurrentUser();
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