package com.corevent.controller;

import java.io.IOException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;

import com.corevent.entity.User;
import com.corevent.service.AuthService;
import com.corevent.util.NavigationManager;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import lombok.extern.slf4j.Slf4j;
@Slf4j
@Controller
public class ProfileController {

    @FXML private TextField usernameField;
    @FXML private TextField nameField;
    @FXML private TextField emailField;
    @FXML private TextField phoneField;
    @FXML private Button editButton;
    @FXML private Button saveButton;
    @FXML private Button cancelButton;
    @FXML private Button backButton;

    @Autowired
    private AuthService authService;

    @Autowired
    private NavigationManager navigationManager;

    private User currentUser;

    @FXML
    public void initialize() {
        currentUser = authService.getCurrentUser();
        if (currentUser != null) {
            usernameField.setText(currentUser.getUsername());
            nameField.setText(currentUser.getFullName());
            emailField.setText(currentUser.getEmail());
            phoneField.setText(currentUser.getPhoneNumber());
        }
        setEditable(false);
    }

    private void setEditable(boolean editable) {
        nameField.setEditable(editable);
        phoneField.setEditable(editable);
        // Email is usually not editable
        emailField.setEditable(false);
        saveButton.setVisible(editable);
        cancelButton.setVisible(editable);
        editButton.setVisible(!editable);
    }

    @FXML
    private void handleEdit() {
        setEditable(true);
    }

    @FXML
    private void handleSave() {
        if (currentUser != null) {
            currentUser.setFullName(nameField.getText());
            currentUser.setPhoneNumber(phoneField.getText());
            authService.updateProfile(currentUser);
            User refreshed = authService.findByUsername(currentUser.getUsername());
            authService.setCurrentUser(refreshed);
            nameField.setText(refreshed.getFullName());
            phoneField.setText(refreshed.getPhoneNumber());
            setEditable(false);
        }
    }

    @FXML
    private void handleCancel() {
        if (currentUser != null) {
            nameField.setText(currentUser.getFullName());
            phoneField.setText(currentUser.getPhoneNumber());
            setEditable(false);
        }
    }

    @FXML
    private void handleBack() {
        if (currentUser != null) {
            navigationManager.navigateToDashboard(currentUser.getRole());
        }
    }

    @FXML
    private void handleLogout() {
        try {
            navigationManager.navigateToLogin();
        } catch (IOException e) {
            log.error("Failed to navigate to login", e);
            showAlert("Error", "Failed to logout");
        }
    }
    
    @FXML
    private void showAlert(String title, String content) {
        Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle(title);
            alert.setHeaderText(null);
            alert.setContentText(content);
            alert.showAndWait();
        });
    }
    
    public static void showErrorPopup(String title, String message) {
        Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle(title);
            alert.setHeaderText(null);
            alert.setContentText(message);
            alert.getDialogPane().getStyleClass().add("custom-alert");
            alert.getDialogPane().getStylesheets().add(ProfileController.class.getResource("/css/style.css").toExternalForm());
            alert.showAndWait();
        });
    }
}