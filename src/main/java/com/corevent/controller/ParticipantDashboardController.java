package com.corevent.controller;

import java.util.List;

import org.springframework.stereotype.Controller;

import com.corevent.config.SpringContext;
import com.corevent.entity.Event;
import com.corevent.entity.Participant;
import com.corevent.entity.User;
import com.corevent.service.EventService;
import com.corevent.service.TicketService;
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
    private final EventService eventService;
    private final TicketService ticketService;
    
    public ParticipantDashboardController(NavigationManager navigationManager, 
                                        EventService eventService,
                                        TicketService ticketService) {
        this.navigationManager = navigationManager;
        this.eventService = eventService;
        this.ticketService = ticketService;
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
        Task<DashboardData> loadTask = new Task<>() {
            @Override
            protected DashboardData call() throws Exception {
                User currentUser = SessionManager.getInstance().getCurrentUser();
                if (!(currentUser instanceof Participant)) {
                    throw new IllegalStateException("Current user is not a participant");
                }
                
                Participant participant = (Participant) currentUser;
                List<Event> participantEvents = eventService.findAll().stream()
                    .filter(event -> ticketService.findByParticipantId(participant.getUserId())
                        .stream()
                        .anyMatch(ticket -> ticket.getEvent().getEventId().equals(event.getEventId())))
                    .toList();
                
                int registeredEvents = participantEvents.size();
                int upcomingEvents = (int) participantEvents.stream()
                    .filter(Event::isAvailable)
                    .count();
                int completedEvents = (int) participantEvents.stream()
                    .filter(event -> !event.isAvailable())
                    .count();
                
                return new DashboardData(registeredEvents, upcomingEvents, completedEvents);
            }
        };
        
        loadTask.setOnSucceeded(event -> {
            DashboardData data = loadTask.getValue();
            registeredEventsLabel.setText(String.valueOf(data.registeredEvents()));
            upcomingEventsLabel.setText(String.valueOf(data.upcomingEvents()));
            completedEventsLabel.setText(String.valueOf(data.completedEvents()));
        });
        
        loadTask.setOnFailed(event -> {
            log.error("Failed to load dashboard data", loadTask.getException());
            showAlert("Error", "Failed to load dashboard data");
        });
        
        new Thread(loadTask).start();
    }
    
    private void handleBrowseEvents() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/browse-events.fxml"));
            loader.setControllerFactory(SpringContext::getBean);
            Parent root = loader.load();
            
            Stage stage = new Stage();
            stage.setTitle("Browse Events");
            stage.setScene(new Scene(root));
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.initOwner(browseEventsButton.getScene().getWindow());
            
            stage.showAndWait();
            
            // Refresh dashboard data after browsing
            loadDashboardData();
            
        } catch (Exception e) {
            log.error("Failed to open browse events", e);
            showAlert("Error", "Failed to open browse events");
        }
    }
    
    private void handleMyTickets() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/my-tickets.fxml"));
            loader.setControllerFactory(SpringContext::getBean);
            Parent root = loader.load();
            
            Stage stage = new Stage();
            stage.setTitle("My Tickets");
            stage.setScene(new Scene(root));
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.initOwner(myTicketsButton.getScene().getWindow());
            
            stage.showAndWait();
            
        } catch (Exception e) {
            log.error("Failed to open my tickets", e);
            showAlert("Error", "Failed to open my tickets");
        }
    }
    
    private void handleMyEvaluations() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/my-evaluations.fxml"));
            loader.setControllerFactory(SpringContext::getBean);
            Parent root = loader.load();
            
            Stage stage = new Stage();
            stage.setTitle("My Evaluations");
            stage.setScene(new Scene(root));
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.initOwner(myEvaluationsButton.getScene().getWindow());
            
            stage.showAndWait();
            
        } catch (Exception e) {
            log.error("Failed to open my evaluations", e);
            showAlert("Error", "Failed to open my evaluations");
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
    
    private record DashboardData(int registeredEvents, int upcomingEvents, int completedEvents) {}
}