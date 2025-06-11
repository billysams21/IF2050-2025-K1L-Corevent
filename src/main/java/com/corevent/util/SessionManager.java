package com.corevent.util;

import java.time.LocalDateTime;
import java.util.prefs.Preferences;

import com.corevent.entity.User;

// Singleton class for user session
public class SessionManager {
  
  private static SessionManager instance;
  private User currentUser;
  private String authToken;
  private LocalDateTime loginTime;
  private boolean rememberMe;
  
  // Preferences for storing persistent data
  private final Preferences prefs;
  
  private SessionManager() {
    this.prefs = Preferences.userNodeForPackage(SessionManager.class);
  }
  
  public static synchronized SessionManager getInstance() {
    if (instance == null) {
      instance = new SessionManager();
    }
    return instance;
  }
  
  // Set session data after successful login
  public void setSession(User user, String token, boolean rememberMe) {
    this.currentUser = user;
    this.authToken = token;
    this.loginTime = LocalDateTime.now();
    this.rememberMe = rememberMe;
    
    if (rememberMe) {
      prefs.put("username", user.getUsername());
      prefs.put("token", token);
      prefs.put("user_id", user.getUserId());
      prefs.put("user_role", user.getRole());
    }
  }
  
  public void clearSession() {
    this.currentUser = null;
    this.authToken = null;
    this.loginTime = null;
    this.rememberMe = false;
    
    // Clear preferences
    prefs.remove("username");
    prefs.remove("token");
    prefs.remove("user_id");
    prefs.remove("user_role");
  }
  
  public boolean isLoggedIn() {
    return currentUser != null && authToken != null;
  }
  
  public boolean hasSavedSession() {
    return prefs.get("username", null) != null && 
          prefs.get("token", null) != null;
  }
  
  // Restore session from saved preferences
  public SavedSession getSavedSession() {
    if (!hasSavedSession()) {
      return null;
    }
    
    return new SavedSession(
      prefs.get("username", null),
      prefs.get("token", null),
      prefs.get("user_id", null),
      prefs.get("user_role", null)
    );
  }
  
  public boolean hasRole(String role) {
    return currentUser != null && 
          currentUser.getRole() != null && 
          currentUser.getRole().equalsIgnoreCase(role);
  }
  
  // Getters
  public User getCurrentUser() { return currentUser; }
  public String getAuthToken() { return authToken; }
  public LocalDateTime getLoginTime() { return loginTime; }
  public boolean isRememberMe() { return rememberMe; }
  
  public static class SavedSession {
    public final String username;
    public final String token;
    public final String userId;
    public final String role;
    
    public SavedSession(String username, String token, String userId, String role) {
      this.username = username;
      this.token = token;
      this.userId = userId;
      this.role = role;
    }
  }
}