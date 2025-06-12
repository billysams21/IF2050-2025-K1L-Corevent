package com.corevent.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.corevent.config.SpringContext;
import com.corevent.dto.auth.LoginResponse;
import com.corevent.service.AuthService;
import com.corevent.util.PreferencesManager;

import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class LoginController {
    
    @FXML private TextField usernameField;
    @FXML private PasswordField passwordField;
    @FXML private Button loginButton;
    @FXML private Label errorLabel;
    @FXML private ProgressIndicator progressIndicator;
    @FXML private CheckBox rememberMeCheckbox;
    
    @Autowired
    private AuthService authService;
    
    @FXML
    public void initialize() {
        progressIndicator.setVisible(false);
        errorLabel.setVisible(false);
        
        // Enable login on Enter key
        passwordField.setOnAction(e -> handleLogin());
        
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
            showError("Please enter username and password");
            return;
        }
        
        // Disable form during login
        setFormDisabled(true);
        progressIndicator.setVisible(true);
        errorLabel.setVisible(false);
        
        // Create login task
        Task<LoginResponse> loginTask = new Task<>() {
            @Override
            protected LoginResponse call() throws Exception {
                return authService.authenticate(username, password, rememberMeCheckbox.isSelected()).get();
            }
        };
        
        loginTask.setOnSucceeded(e -> {
            LoginResponse response = loginTask.getValue();
            Platform.runLater(() -> handleLoginResponse(response));
        });
        
        loginTask.setOnFailed(e -> {
            log.error("Login failed", e.getSource().getException());
            Platform.runLater(() -> {
                showError("Connection error. Please try again.");
                setFormDisabled(false);
                progressIndicator.setVisible(false);
            });
        });
        
        new Thread(loginTask).start();
    }
    
    private void handleLoginResponse(LoginResponse response) {
        progressIndicator.setVisible(false);
        
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
    
    private void navigateToDashboard(String role) {
        try {
            String fxmlFile = role.equals("COMMITTEE") ? 
                            "/fxml/committee-dashboard.fxml" : 
                            "/fxml/participant-dashboard.fxml";
            
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlFile));
            loader.setControllerFactory(SpringContext::getBean);
            Parent root = loader.load();
            
            Stage stage = (Stage) loginButton.getScene().getWindow();
            Scene scene = new Scene(root);
            scene.getStylesheets().add(getClass().getResource("/css/style.css").toExternalForm());
            
            stage.setScene(scene);
            stage.setTitle("Corevent - Dashboard");
            stage.centerOnScreen();
            
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
    }
    
    private void showError(String message) {
        errorLabel.setText(message);
        errorLabel.setVisible(true);
    }
}