// package com.corevent.service;

// import com.corevent.entity.User;
// import com.corevent.security.JwtTokenUtil;
// import com.corevent.api.AuthApiClient;
// import com.corevent.dto.LoginRequest;
// import com.corevent.dto.LoginResponse;
// import org.springframework.beans.factory.annotation.Autowired;
// import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
// import org.springframework.stereotype.Service;
// import java.util.Optional;
// import java.util.concurrent.CompletableFuture;

// @Service
// public class AuthenticationService {
    
//     @Autowired
//     private AuthApiClient authApiClient;
    
//     @Autowired
//     private UserService userService;
    
//     @Autowired
//     private JwtTokenUtil jwtTokenUtil;
    
//     private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
    
//     /**
//      * Authenticate user with backend API
//      * Falls back to local authentication if offline
//      */ 
//     public CompletableFuture<LoginResponse> authenticate(String username, String password) {
//         LoginRequest request = new LoginRequest(username, password);
        
//         return CompletableFuture.supplyAsync(() -> {
//             try {
//                 // Try online authentication first
//                 LoginResponse response = authApiClient.login(request).execute().body();
//                 if (response != null && response.isSuccess()) {
//                     // Cache user data locally
//                     cacheUserData(response.getUser(), response.getToken());
//                     return response;
//                 }
//             } catch (Exception e) {
//                 // Fall back to offline authentication
//                 return authenticateOffline(username, password);
//             }
            
//             return new LoginResponse(false, "Invalid credentials", null, null);
//         });
//     }
    
//     /**
//      * Offline authentication using cached credentials
//      */
//     private LoginResponse authenticateOffline(String username, String password) {
//         Optional<User> cachedUser = userService.findByUsername(username);
        
//         if (cachedUser.isPresent() && 
//             passwordEncoder.matches(password, cachedUser.get().getPassword())) {
            
//             String token = jwtTokenUtil.generateToken(cachedUser.get());
//             return new LoginResponse(true, "Offline authentication", 
//                                    cachedUser.get(), token);
//         }
        
//         return new LoginResponse(false, "Invalid credentials (offline mode)", null, null);
//     }
    
//     /**
//      * Cache user data for offline access
//      */
//     private void cacheUserData(User user, String token) {
//         user.setLastToken(token);
//         user.setLastLoginTime(System.currentTimeMillis());
//         userService.saveOrUpdate(user);
//     }
    
//     /**
//      * Logout user and clear cached credentials
//      */
//     public void logout() {
//         // Clear current session
//         SessionManager.getInstance().clearSession();
        
//         // Optional: Notify backend about logout
//         CompletableFuture.runAsync(() -> {
//             try {
//                 authApiClient.logout().execute();
//             } catch (Exception e) {
//                 // Ignore errors, just log
//             }
//         });
//     }
    
//     /**
//      * Check if user has specific role
//      */
//     public boolean hasRole(String role) {
//         User currentUser = SessionManager.getInstance().getCurrentUser();
//         return currentUser != null && currentUser.getRoles().contains(role);
//     }
// }