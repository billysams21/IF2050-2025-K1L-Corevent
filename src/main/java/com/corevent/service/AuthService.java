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

import com.corevent.entity.Committee;
import com.corevent.entity.Participant;
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