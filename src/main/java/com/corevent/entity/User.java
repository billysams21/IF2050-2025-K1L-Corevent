package com.corevent.entity;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Inheritance;
import jakarta.persistence.InheritanceType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.Data;

@Data
@Entity
@Table(name = "users")
@Inheritance(strategy = InheritanceType.JOINED)
public abstract class User {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;
  
  @Column(unique = true, nullable = false)
  private String username;
  
  @Column(nullable = false)
  private String password;
  
  @Column(nullable = false)
  private String email;
  
  @Column(nullable = false)
  @Enumerated(EnumType.STRING)
  private UserRole role;
  
  @Column(name = "account_status")
  @Enumerated(EnumType.STRING)
  private AccountStatus status = AccountStatus.ACTIVE;
  
  @Column(nullable = false)
  private boolean enabled = true;
  
  @Column(name = "last_login")
  private LocalDateTime lastLogin;
  
  private String rememberMeToken;
  private LocalDateTime rememberMeExpiry;
  
  @ElementCollection(fetch = FetchType.EAGER)
  @CollectionTable(name = "user_roles", joinColumns = @JoinColumn(name = "user_id"))
  @Column(name = "role")
  private Set<String> roles = new HashSet<>();
  
  @Column(name = "created_at")
  private LocalDateTime createdAt;
  
  @PrePersist
  protected void onCreate() {
    createdAt = LocalDateTime.now();
  }
  
  // Getters and Setters
  public String getUserId() { return id.toString(); }
  public void setUserId(String userId) { this.id = Long.parseLong(userId); }
  
  public String getUsername() { return username; }
  public void setUsername(String username) { this.username = username; }
  
  public String getEmail() { return email; }
  public void setEmail(String email) { this.email = email; }
  
  public String getPassword() { return password; }
  public void setPassword(String password) { this.password = password; }
  
  public Set<String> getRoles() { return roles; }
  public void setRoles(Set<String> roles) { this.roles = roles; }
  
  public LocalDateTime getCreatedAt() { return createdAt; }
  public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
  
  public String getRole() {
    return role != null ? role.name() : null;
  }
  
  public boolean isEnabled() {
    return status == AccountStatus.ACTIVE;
  }
  
  public void setEnabled(boolean enabled) {
    this.status = enabled ? AccountStatus.ACTIVE : AccountStatus.DISABLED;
  }
  
  public boolean isActive() {
    return status == AccountStatus.ACTIVE;
  }
  
  public void setActive(boolean active) {
    this.status = active ? AccountStatus.ACTIVE : AccountStatus.DISABLED;
  }

  public enum UserRole {
    COMMITTEE,
    PARTICIPANT
  }
  
  public enum AccountStatus {
    ACTIVE,
    DISABLED,
    SUSPENDED,
    PENDING_ACTIVATION
  }
}