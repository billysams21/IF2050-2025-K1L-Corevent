package com.corevent.controller;

import java.io.IOException;
import java.util.List;

import org.springframework.stereotype.Controller;

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
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
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
    @FXML private Button refreshButton;
    
    @FXML private TableView<Event> eventsTable;
    @FXML private TableColumn<Event, String> eventNameColumn;
    @FXML private TableColumn<Event, String> dateColumn;
    @FXML private TableColumn<Event, String> locationColumn;
    @FXML private TableColumn<Event, String> ticketStatusColumn;
    @FXML private TableColumn<Event, String> attendanceStatusColumn;
    @FXML private TableColumn<Event, Void> actionsColumn;
    
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
        setupTableColumns();
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
    
    private void setupTableColumns() {
        eventNameColumn.setCellValueFactory(new PropertyValueFactory<>("eventName"));
        dateColumn.setCellValueFactory(new PropertyValueFactory<>("date"));
        locationColumn.setCellValueFactory(new PropertyValueFactory<>("location"));
        
        // TODO: Set up ticket status and attendance status columns
        // These will need custom cell factories to show the status from tickets and attendance
        
        // Set up actions column with View button only
        actionsColumn.setCellFactory(col -> new TableCell<>() {
            private final Button viewButton = new Button("View");
            
            {
                viewButton.getStyleClass().add("button-secondary");
                viewButton.setOnAction(e -> handleViewEvent(getTableRow().getItem()));
            }
            
            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : viewButton);
            }
        });
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
    
    @FXML
    private void handleBrowseEvents() {
        try {
            navigationManager.navigateToBrowseEvents();
        } catch (IOException e) {
            log.error("Failed to navigate to browse events", e);
            showAlert("Error", "Failed to open browse events");
        }
    }
    
    @FXML
    private void handleMyTickets() {
        try {
            navigationManager.navigateToMyTickets();
        } catch (IOException e) {
            log.error("Failed to navigate to my tickets", e);
            showAlert("Error", "Failed to open my tickets");
        }
    }
    
    @FXML
    private void handleMyEvaluations() {
        try {
            navigationManager.navigateToMyEvaluations();
        } catch (IOException e) {
            log.error("Failed to navigate to my evaluations", e);
            showAlert("Error", "Failed to open my evaluations");
        }
    }
    
    @FXML
    private void handleProfile() {
        try {
            navigationManager.navigateToProfile();
        } catch (IOException e) {
            log.error("Failed to navigate to profile", e);
            showAlert("Error", "Failed to open profile");
        }
    }
    
    @FXML
    private void handleLogout() {
        SessionManager.getInstance().clearSession();
        try {
            navigationManager.navigateToLogin();
        } catch (IOException e) {
            log.error("Failed to navigate to login", e);
            showAlert("Error", "Failed to logout");
        }
    }
    
    @FXML
    private void handleRefresh() {
        loadDashboardData();
    }
    
    private void handleViewEvent(Event event) {
        if (event != null) {
            try {
                navigationManager.navigateToEventDetails(event.getEventId());
            } catch (IOException e) {
                log.error("Failed to navigate to event details", e);
                showAlert("Error", "Failed to open event details");
            }
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