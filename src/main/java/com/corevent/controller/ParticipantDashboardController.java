package com.corevent.controller;

import org.springframework.stereotype.Controller;

import com.corevent.entity.Participant;
import com.corevent.entity.User;
import com.corevent.util.NavigationManager;
import com.corevent.util.SessionManager;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;

@Controller
public class ParticipantDashboardController {
    
    @FXML private Label welcomeLabel;
    @FXML private Label roleLabel;
    @FXML private Label registeredEventsLabel;
    @FXML private Label upcomingEventsLabel;
    @FXML private Label completedEventsLabel;
    
    @FXML private Button browseEventsButton;
    @FXML private Button myTicketsButton;
    @FXML private Button myEvaluationsButton;
    @FXML private Button profileButton;
    
    @FXML private MenuItem logoutMenuItem;
    
    private final NavigationManager navigationManager;
    
    public ParticipantDashboardController(NavigationManager navigationManager) {
        this.navigationManager = navigationManager;
    }
    
    @FXML
    public void initialize() {
        setupUserInfo();
        setupEventHandlers();
        loadDashboardData();
    }
    
    private void setupUserInfo() {
        User currentUser = SessionManager.getInstance().getCurrentUser();
        if (currentUser instanceof Participant) {
            Participant participant = (Participant) currentUser;
            welcomeLabel.setText("Welcome, " + participant.getFullName());
            roleLabel.setText("Participant Dashboard");
        }
    }
    
    private void setupEventHandlers() {
        browseEventsButton.setOnAction(event -> handleBrowseEvents());
        myTicketsButton.setOnAction(event -> handleMyTickets());
        myEvaluationsButton.setOnAction(event -> handleMyEvaluations());
        profileButton.setOnAction(event -> handleProfile());
        
        logoutMenuItem.setOnAction(event -> handleLogout());
    }
    
    private void loadDashboardData() {
        // For now, show placeholder data
        registeredEventsLabel.setText("0");
        upcomingEventsLabel.setText("0");
        completedEventsLabel.setText("0");
    }
    
    private void handleBrowseEvents() {
        // Will implement later
        System.out.println("Browse Events clicked");
    }
    
    private void handleMyTickets() {
        // Will implement later
        System.out.println("My Tickets clicked");
    }
    
    private void handleMyEvaluations() {
        // Will implement later
        System.out.println("My Evaluations clicked");
    }
    
    private void handleProfile() {
        // Will implement later
        System.out.println("Profile clicked");
    }
    
    private void handleLogout() {
        navigationManager.logout();
    }
}