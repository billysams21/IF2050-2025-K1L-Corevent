package com.corevent.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;

import com.corevent.entity.Committee;
import com.corevent.entity.Participant;
import com.corevent.entity.User;
import com.corevent.service.AuthService;
import com.corevent.util.NavigationManager;

import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.TextField;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Controller
public class RegisterController {

    @FXML
    private TextField usernameField;
    @FXML
    private TextField fullNameField;
    @FXML
    private TextField emailField;
    @FXML
    private PasswordField passwordField;
    @FXML
    private PasswordField confirmPasswordField;
    @FXML
    private ComboBox<String> roleComboBox;
    @FXML
    private Button registerButton;
    @FXML
    private Label errorLabel;
    @FXML
    private ProgressIndicator loadingIndicator;
    @FXML
    private Hyperlink loginLink;

    @Autowired
    private AuthService authService;

    @Autowired
    private NavigationManager navigationManager;

    @FXML
    public void initialize() {
        // Initialize role combo box
        roleComboBox.getItems().addAll("COMMITTEE", "PARTICIPANT");
        roleComboBox.setValue("COMMITTEE");

        // Set up event handlers
        registerButton.setOnAction(e -> handleRegister());
        loginLink.setOnAction(e -> navigateToLogin());

        // Initialize error box
        errorLabel.setVisible(false);

        // Initialize loading indicator
        loadingIndicator.setVisible(false);
    }

    @FXML
    private void handleRegister() {
        String username = usernameField.getText().trim();
        String fullName = fullNameField.getText().trim();
        String email = emailField.getText().trim();
        String password = passwordField.getText();
        String confirmPassword = confirmPasswordField.getText();
        String role = roleComboBox.getValue();

        // Validate input
        if (username.isEmpty() || fullName.isEmpty() || email.isEmpty() ||
                password.isEmpty() || confirmPassword.isEmpty()) {
            showError("Please fill in all fields");
            return;
        }

        if (!password.equals(confirmPassword)) {
            showError("Passwords do not match");
            return;
        }

        if (!email.matches("^[A-Za-z0-9+_.-]+@(.+)$")) {
            showError("Invalid email format");
            return;
        }

        // Disable form during registration
        setFormDisabled(true);
        loadingIndicator.setVisible(true);
        errorLabel.setVisible(false);

        // Create registration task
        Task<Boolean> registerTask = new Task<>() {
            @Override
            protected Boolean call() throws Exception {
                User user;
                if (role.equals("COMMITTEE")) {
                    Committee committee = new Committee();
                    committee.setFullName(fullName);
                    user = committee;
                } else {
                    Participant participant = new Participant();
                    participant.setFullName(fullName);
                    user = participant;
                }

                user.setUsername(username);
                user.setEmail(email);
                user.setPassword(password);
                user.setRole(User.UserRole.valueOf(role));
                user.setStatus(User.AccountStatus.ACTIVE);

                return authService.register(user);
            }
        };

        registerTask.setOnSucceeded(e -> {
            loadingIndicator.setVisible(false);
            if (registerTask.getValue()) {
                showSuccess("Registration successful! Please wait for admin approval.");
                navigateToLogin();
            } else {
                showError("Registration failed. Username or email may already exist.");
                setFormDisabled(false);
            }
        });

        registerTask.setOnFailed(e -> {
            log.error("Registration failed", e.getSource().getException());
            Platform.runLater(() -> {
                showError("Registration failed. Please try again.");
                setFormDisabled(false);
                loadingIndicator.setVisible(false);
            });
        });

        new Thread(registerTask).start();
    }

    @FXML
    private void navigateToLogin() {
        try {
            navigationManager.navigateToLogin();
        } catch (Exception e) {
            log.error("Failed to navigate to login page", e);
            showError("Error navigating to login page");
        }
    }

    private void setFormDisabled(boolean disabled) {
        usernameField.setDisable(disabled);
        fullNameField.setDisable(disabled);
        emailField.setDisable(disabled);
        passwordField.setDisable(disabled);
        confirmPasswordField.setDisable(disabled);
        roleComboBox.setDisable(disabled);
        registerButton.setDisable(disabled);
        loginLink.setDisable(disabled);
    }

    private void showError(String message) {
        errorLabel.setText(message);
        errorLabel.setVisible(true);
        errorLabel.setStyle("-fx-text-fill: red;");
    }

    private void showSuccess(String message) {
        errorLabel.setText(message);
        errorLabel.setVisible(true);
        errorLabel.setStyle("-fx-text-fill: green;");
    }
}
