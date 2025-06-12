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
import com.corevent.entity.Committee;
import com.corevent.entity.Participant;
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

    @Transactional
    public User registerCommittee(String username, String password, String email, 
                                 String fullName, String department, String position, String phoneNumber) {
        if (userRepository.existsByUsername(username)) {
            throw new RuntimeException("Username already exists");
        }
        if (userRepository.existsByEmail(email)) {
            throw new RuntimeException("Email already exists");
        }

        Committee committee = new Committee();
        committee.setUsername(username);
        committee.setPassword(passwordEncoder.encode(password));
        committee.setEmail(email);
        committee.setFullName(fullName);
        committee.setDepartment(department);
        committee.setPosition(position);
        committee.setPhoneNumber(phoneNumber);
        committee.setRole(User.UserRole.COMMITTEE);
        committee.setEnabled(true);
        committee.getRoles().add("COMMITTEE");

        return userRepository.save(committee);
    }

    @Transactional
    public User registerParticipant(String username, String password, String email, 
                                   String fullName, String phoneNumber, String institution) {
        if (userRepository.existsByUsername(username)) {
            throw new RuntimeException("Username already exists");
        }
        if (userRepository.existsByEmail(email)) {
            throw new RuntimeException("Email already exists");
        }

        Participant participant = new Participant();
        participant.setUsername(username);
        participant.setPassword(passwordEncoder.encode(password));
        participant.setEmail(email);
        participant.setFullName(fullName);
        participant.setPhoneNumber(phoneNumber);
        participant.setInstitution(institution);
        participant.setRole(User.UserRole.PARTICIPANT);
        participant.setEnabled(true);
        participant.getRoles().add("PARTICIPANT");

        return userRepository.save(participant);
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

    public boolean isUsernameAvailable(String username) {
        return !userRepository.existsByUsername(username);
    }

    public boolean isEmailAvailable(String email) {
        return !userRepository.existsByEmail(email);
    }

    @Transactional
    public void changePassword(String username, String oldPassword, String newPassword) {
        User user = userRepository.findByUsername(username)
            .orElseThrow(() -> new RuntimeException("User not found"));

        if (!passwordEncoder.matches(oldPassword, user.getPassword())) {
            throw new RuntimeException("Invalid old password");
        }

        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
    }

    @Transactional
    public void updateProfile(User user) {
        userRepository.save(user);
    }
}