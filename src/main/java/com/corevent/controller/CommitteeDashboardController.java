package com.corevent.controller;

import java.util.List;

import org.springframework.stereotype.Controller;

import com.corevent.config.SpringContext;
import com.corevent.entity.Committee;
import com.corevent.entity.Event;
import com.corevent.entity.User;
import com.corevent.service.AttendanceService;
import com.corevent.service.EventService;
import com.corevent.util.NavigationManager;
import com.corevent.util.SessionManager;

import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.stage.Modality;
import javafx.stage.Stage;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Controller
public class CommitteeDashboardController {
    
    @FXML private Label welcomeLabel;
    @FXML private Label roleLabel;
    @FXML private Label totalEventsLabel;
    @FXML private Label upcomingEventsLabel;
    @FXML private Label totalParticipantsLabel;
    
    @FXML private Button createEventButton;
    @FXML private Button manageEventsButton;
    @FXML private Button manageAttendanceButton;
    @FXML private Button profileButton;
    
    @FXML private MenuItem logoutMenuItem;
    
    private final NavigationManager navigationManager;
    private final EventService eventService;
    private final AttendanceService attendanceService;
    
    public CommitteeDashboardController(NavigationManager navigationManager, 
                                      EventService eventService,
                                      AttendanceService attendanceService) {
        this.navigationManager = navigationManager;
        this.eventService = eventService;
        this.attendanceService = attendanceService;
    }
    
    @FXML
    public void initialize() {
        setupUserInfo();
        setupEventHandlers();
        loadDashboardData();
    }
    
    private void setupUserInfo() {
        User currentUser = SessionManager.getInstance().getCurrentUser();
        if (currentUser instanceof Committee) {
            Committee committee = (Committee) currentUser;
            welcomeLabel.setText("Welcome, " + committee.getFullName());
            roleLabel.setText("Committee Dashboard");
        }
    }
    
    private void setupEventHandlers() {
        createEventButton.setOnAction(event -> handleCreateEvent());
        manageEventsButton.setOnAction(event -> handleManageEvents());
        manageAttendanceButton.setOnAction(event -> handleManageAttendance());
        profileButton.setOnAction(event -> handleProfile());
        
        logoutMenuItem.setOnAction(event -> handleLogout());
    }
    
    private void loadDashboardData() {
        Task<DashboardData> loadTask = new Task<>() {
            @Override
            protected DashboardData call() throws Exception {
                User currentUser = SessionManager.getInstance().getCurrentUser();
                if (!(currentUser instanceof Committee)) {
                    throw new IllegalStateException("Current user is not a committee member");
                }
                
                Committee committee = (Committee) currentUser;
                List<Event> committeeEvents = eventService.getCommitteeEvents(committee.getUserId());
                
                int totalEvents = committeeEvents.size();
                int upcomingEvents = (int) committeeEvents.stream()
                    .filter(Event::isAvailable)
                    .count();
                int totalParticipants = committeeEvents.stream()
                    .mapToInt(Event::getCurrentParticipants)
                    .sum();
                
                return new DashboardData(totalEvents, upcomingEvents, totalParticipants);
            }
        };
        
        loadTask.setOnSucceeded(event -> {
            DashboardData data = loadTask.getValue();
            totalEventsLabel.setText(String.valueOf(data.totalEvents()));
            upcomingEventsLabel.setText(String.valueOf(data.upcomingEvents()));
            totalParticipantsLabel.setText(String.valueOf(data.totalParticipants()));
        });
        
        loadTask.setOnFailed(event -> {
            log.error("Failed to load dashboard data", loadTask.getException());
            showAlert("Error", "Failed to load dashboard data");
        });
        
        new Thread(loadTask).start();
    }
    
    private void handleCreateEvent() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/create-event.fxml"));
            loader.setControllerFactory(SpringContext::getBean);
            Parent root = loader.load();
            
            Stage stage = new Stage();
            stage.setTitle("Create New Event");
            stage.setScene(new Scene(root));
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.initOwner(createEventButton.getScene().getWindow());
            
            stage.showAndWait();
            
            // Refresh dashboard data after creating event
            loadDashboardData();
            
        } catch (Exception e) {
            log.error("Failed to open create event", e);
            showAlert("Error", "Failed to open create event");
        }
    }
    
    private void handleManageEvents() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/manage-events.fxml"));
            loader.setControllerFactory(SpringContext::getBean);
            Parent root = loader.load();
            
            Stage stage = new Stage();
            stage.setTitle("Manage Events");
            stage.setScene(new Scene(root));
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.initOwner(manageEventsButton.getScene().getWindow());
            
            stage.showAndWait();
            
            // Refresh dashboard data after managing events
            loadDashboardData();
            
        } catch (Exception e) {
            log.error("Failed to open manage events", e);
            showAlert("Error", "Failed to open manage events");
        }
    }
    
    private void handleManageAttendance() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/manage-attendance.fxml"));
            loader.setControllerFactory(SpringContext::getBean);
            Parent root = loader.load();
            
            Stage stage = new Stage();
            stage.setTitle("Manage Attendance");
            stage.setScene(new Scene(root));
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.initOwner(manageAttendanceButton.getScene().getWindow());
            
            stage.showAndWait();
            
        } catch (Exception e) {
            log.error("Failed to open manage attendance", e);
            showAlert("Error", "Failed to open manage attendance");
        }
    }
    
    private void handleProfile() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/profile.fxml"));
            loader.setControllerFactory(SpringContext::getBean);
            Parent root = loader.load();
            
            Stage stage = new Stage();
            stage.setTitle("My Profile");
            stage.setScene(new Scene(root));
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.initOwner(profileButton.getScene().getWindow());
            
            stage.showAndWait();
            
        } catch (Exception e) {
            log.error("Failed to open profile", e);
            showAlert("Error", "Failed to open profile");
        }
    }
    
    private void handleLogout() {
        SessionManager.getInstance().clearSession();
        try {
            navigationManager.navigateToLogin();
        } catch (Exception e) {
            log.error("Failed to navigate to login", e);
            showAlert("Error", "Failed to logout");
        }
    }
    
    private void showAlert(String title, String content) {
        Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle(title);
            alert.setContentText(content);
            alert.showAndWait();
        });
    }
    
    private record DashboardData(int totalEvents, int upcomingEvents, int totalParticipants) {}
}