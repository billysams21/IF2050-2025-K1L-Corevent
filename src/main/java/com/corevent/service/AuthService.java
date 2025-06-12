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

import com.corevent.api.AuthApiClient;
import com.corevent.dto.auth.LoginRequest;
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
    private final AuthApiClient authApiClient;
    private final JwtTokenUtil jwtTokenUtil;

    public CompletableFuture<LoginResponse> authenticate(String username, String password, boolean rememberMe) {
        LoginRequest request = new LoginRequest(username, password, rememberMe);
        
        return CompletableFuture.supplyAsync(() -> {
            try {
                // Try online authentication first
                LoginResponse response = authApiClient.login(request).execute().body();
                if (response != null && response.isSuccess()) {
                    handleSuccessfulLogin(response.getUser(), response.getToken(), rememberMe);
                    return response;
                }
            } catch (Exception e) {
                log.warn("Online authentication failed, falling back to offline mode", e);
                return authenticateOffline(username, password, rememberMe);
            }
            
            return new LoginResponse(false, "Invalid credentials", null, null, null, 0);
        });
    }

    @Transactional
    private LoginResponse authenticateOffline(String username, String password, boolean rememberMe) {
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
            
            return new LoginResponse(true, "Offline authentication successful", 
                user, token, refreshToken, jwtTokenUtil.getExpirationTime());
            
        } catch (Exception e) {
            log.error("Offline authentication failed", e);
            return new LoginResponse(false, "Invalid credentials (offline mode)", null, null, null, 0);
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
        
        // Notify backend about logout
        CompletableFuture.runAsync(() -> {
            try {
                authApiClient.logout().execute();
            } catch (Exception e) {
                log.warn("Failed to notify backend about logout", e);
            }
        });
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