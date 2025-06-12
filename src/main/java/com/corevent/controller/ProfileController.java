package com.corevent.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;

import com.corevent.entity.User;
import com.corevent.service.AuthService;
import com.corevent.util.NavigationManager;

import javafx.fxml.FXML;
import javafx.scene.control.TextField;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Controller
public class ProfileController {

    @FXML private TextField nameField;
    @FXML private TextField emailField;
    @FXML private TextField phoneField;

    @Autowired
    private AuthService authService;

    @Autowired
    private NavigationManager navigationManager;

    @FXML
    public void initialize() {
        User currentUser = authService.getCurrentUser();
        if (currentUser != null) {
            nameField.setText(currentUser.getFullName());
            emailField.setText(currentUser.getEmail());
            phoneField.setText(currentUser.getPhoneNumber());
        }
    }

    @FXML
    private void handleSave() {
        User currentUser = authService.getCurrentUser();
        if (currentUser != null) {
            currentUser.setFullName(nameField.getText());
            currentUser.setEmail(emailField.getText());
            currentUser.setPhoneNumber(phoneField.getText());
            authService.updateProfile(currentUser);
            navigationManager.navigateToDashboard(currentUser.getRole());
        }
    }

    @FXML
    private void handleCancel() {
        User currentUser = authService.getCurrentUser();
        if (currentUser != null) {
            navigationManager.navigateToDashboard(currentUser.getRole());
        }
    }
}