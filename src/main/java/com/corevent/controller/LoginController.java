package com.corevent.boundary;

import com.corevent.service.AuthenticationService;
import com.corevent.dto.LoginResponse;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class LoginController {
    
    @FXML private TextField usernameField;
    @FXML private PasswordField passwordField;
    @FXML private Button loginButton;
    @FXML private Label errorLabel;
    @FXML private ProgressIndicator progressIndicator;
    @FXML private CheckBox rememberMeCheckbox;
    
    @Autowired
    private AuthenticationService authService;
    
    @FXML
    public void initialize() {
        progressIndicator.setVisible(false);
        errorLabel.setVisible(false);
        
        // Enable login on Enter key
        passwordField.setOnAction(e -> handleLogin());
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
                return authService.authenticate(username, password).get();
            }
        };
        
        loginTask.setOnSucceeded(e -> {
            LoginResponse response = loginTask.getValue();
            Platform.runLater(() -> handleLoginResponse(response));
        });
        
        loginTask.setOnFailed(e -> {
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
            e.printStackTrace();
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