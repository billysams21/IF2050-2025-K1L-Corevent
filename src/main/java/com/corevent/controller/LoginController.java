package com.corevent.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;

import com.corevent.dto.auth.LoginResponse;
import com.corevent.service.AuthService;
import com.corevent.util.NavigationManager;
import com.corevent.util.PreferencesManager;

import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.TextField;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Controller
public class LoginController {
    
    @FXML private TextField usernameField;
    @FXML private PasswordField passwordField;
    @FXML private Button loginButton;
    @FXML private Label errorLabel;
    @FXML private ProgressIndicator loadingIndicator;
    @FXML private CheckBox rememberMeCheckbox;
    @FXML private Hyperlink registerLink;
    
    @Autowired
    private AuthService authService;
    
    @Autowired
    private NavigationManager navigationManager;
    
    @FXML
    public void initialize() {
        loadingIndicator.setVisible(false);
        errorLabel.setVisible(false);
        
        // Enable login on Enter key
        passwordField.setOnAction(e -> handleLogin());
        
        // Set up button handlers
        loginButton.setOnAction(e -> handleLogin());
        registerLink.setOnAction(e -> navigateToRegister());
        
        // Load saved credentials if available
        String savedUsername = PreferencesManager.getSavedUsername();
        if (savedUsername != null) {
            usernameField.setText(savedUsername);
            rememberMeCheckbox.setSelected(true);
        }
    }
    
    @FXML
    private void handleLogin() {
        String username = usernameField.getText().trim();
        String password = passwordField.getText();
        
        if (username.isEmpty() || password.isEmpty()) {
            showError("Please enter both username and password");
            return;
        }
        
        // Disable form during login
        setFormDisabled(true);
        loadingIndicator.setVisible(true);
        errorLabel.setVisible(false);
        
        // Create login task
        Task<LoginResponse> loginTask = new Task<>() {
            @Override
            protected LoginResponse call() throws Exception {
                return authService.authenticate(username, password, rememberMeCheckbox.isSelected()).get();
            }
        };
        
        // Handle task completion
        loginTask.setOnSucceeded(e -> {
            LoginResponse response = loginTask.getValue();
            Platform.runLater(() -> handleLoginResponse(response));
        });
        
        loginTask.setOnFailed(e -> {
            log.error("Login failed", e.getSource().getException());
            Platform.runLater(() -> {
                showError("Connection error. Please try again.");
                setFormDisabled(false);
                loadingIndicator.setVisible(false);
            });
        });
        
        // Start task
        new Thread(loginTask).start();
    }
    
    private void handleLoginResponse(LoginResponse response) {
        loadingIndicator.setVisible(false);
        
        if (response.isSuccess()) {
            // Save credentials if remember me is checked
            if (rememberMeCheckbox.isSelected()) {
                PreferencesManager.saveCredentials(usernameField.getText());
            } else {
                PreferencesManager.clearCredentials();
            }
            
            // Navigate to appropriate dashboard
            navigateToDashboard(response.getUser().getRole());
        } else {
            showError(response.getMessage());
            setFormDisabled(false);
        }
    }
    
    private void navigateToRegister() {
        try {
            navigationManager.navigateToRegister();
        } catch (Exception e) {
            log.error("Failed to load register page", e);
            showError("Error loading register page");
        }
    }
    
    private void navigateToDashboard(String role) {
        try {
            if (role != null && role.equals("COMMITTEE")) {
                navigationManager.navigateToCommitteeDashboard();
            } else if (role != null && role.equals("PARTICIPANT")) {
                navigationManager.navigateToParticipantDashboard();
            } else {
                log.error("Unknown role: {}", role);
                showError("Invalid user role");
            }
        } catch (Exception e) {
            log.error("Failed to load dashboard", e);
            showError("Error loading dashboard");
        }
    }
    
    private void setFormDisabled(boolean disabled) {
        usernameField.setDisable(disabled);
        passwordField.setDisable(disabled);
        loginButton.setDisable(disabled);
        rememberMeCheckbox.setDisable(disabled);
        registerLink.setDisable(disabled);
    }
    
    private void showError(String message) {
        errorLabel.setText(message);
        errorLabel.setVisible(true);
    }
}