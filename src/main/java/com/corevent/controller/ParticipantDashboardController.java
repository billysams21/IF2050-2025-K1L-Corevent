package com.corevent.controller;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;

import com.corevent.entity.Participant;
import com.corevent.entity.Ticket;
import com.corevent.entity.Ticket.TicketStatus;
import com.corevent.entity.User;
import com.corevent.service.EventService;
import com.corevent.service.TicketService;
import com.corevent.util.NavigationManager;
import com.corevent.util.SessionManager;

import javafx.application.Platform;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
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
    
    @FXML private TableView<Ticket> eventsTable;
    @FXML private TableColumn<Ticket, String> eventNameColumn;
    @FXML private TableColumn<Ticket, LocalDateTime> dateColumn;
    @FXML private TableColumn<Ticket, String> locationColumn;
    @FXML private TableColumn<Ticket, TicketStatus> ticketStatusColumn;
    @FXML private TableColumn<Ticket, String> attendanceStatusColumn;
    @FXML private TableColumn<Ticket, Void> actionsColumn;
    
    @Autowired
    private EventService eventService;
    
    @Autowired
    private NavigationManager navigationManager;
    
    @Autowired
    private TicketService ticketService;
    
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
        eventNameColumn.setCellValueFactory(cellData -> 
            new SimpleStringProperty(cellData.getValue().getEvent().getEventName()));
        dateColumn.setCellValueFactory(cellData -> 
            new SimpleObjectProperty<>(cellData.getValue().getEvent().getDate()));
        locationColumn.setCellValueFactory(cellData -> 
            new SimpleStringProperty(cellData.getValue().getEvent().getLocation()));
        ticketStatusColumn.setCellValueFactory(new PropertyValueFactory<>("status"));
        
        // Set up attendance status column
        attendanceStatusColumn.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || getTableRow() == null || getTableRow().getItem() == null) {
                    setText(null);
                    return;
                }
                
                Ticket ticket = getTableRow().getItem();
                // TODO: Implement attendance status check
                setText("Not Checked In");
            }
        });
        
        // Set up actions column with Download QR buttons
        actionsColumn.setCellFactory(col -> new TableCell<>() {
            private final Button downloadButton = new Button("Download QR");
            
            {
                downloadButton.getStyleClass().add("button-secondary");
                downloadButton.setOnAction(e -> handleDownloadQR(getTableRow().getItem()));
            }
            
            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || getTableRow() == null || getTableRow().getItem() == null) {
                    setGraphic(null);
                    return;
                }
                
                HBox buttons = new HBox(8, downloadButton);
                setGraphic(buttons);
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
                List<Ticket> participantTickets = ticketService.findByParticipantId(participant.getUserId());
                
                int registeredEvents = participantTickets.size();
                int upcomingEvents = (int) participantTickets.stream()
                    .filter(ticket -> ticket.getEvent().isAvailable())
                    .count();
                int completedEvents = (int) participantTickets.stream()
                    .filter(ticket -> !ticket.getEvent().isAvailable())
                    .count();
                
                return new DashboardData(registeredEvents, upcomingEvents, completedEvents);
            }
        };
        
        loadTask.setOnSucceeded(event -> {
            DashboardData data = loadTask.getValue();
            registeredEventsLabel.setText(String.valueOf(data.registeredEvents()));
            upcomingEventsLabel.setText(String.valueOf(data.upcomingEvents()));
            completedEventsLabel.setText(String.valueOf(data.completedEvents()));
            
            // Load tickets into table
            User currentUser = SessionManager.getInstance().getCurrentUser();
            if (currentUser instanceof Participant) {
                Participant participant = (Participant) currentUser;
                List<Ticket> tickets = ticketService.findByParticipantId(participant.getUserId());
                eventsTable.getItems().setAll(tickets);
            }
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
            // Show list of events that can be evaluated
            navigationManager.navigateToMyEvaluations();
        } catch (IOException e) {
            log.error("Failed to navigate to my evaluations", e);
            showAlert("Error", "Failed to open my evaluations");
        }
    }
    
    @FXML
    private void handleProfile() {
        navigationManager.navigateToProfile();
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
    
    @FXML
    private void handleMyProfile() {
        try {
            navigationManager.navigateToProfile();
        } catch (Exception e) {
            log.error("Failed to navigate to profile", e);
            showError("Error", "Error loading profile page");
        }
    }
    
    private void handleViewEvent(Ticket ticket) {
        if (ticket != null) {
            try {
                navigationManager.navigateToEventDetails(ticket.getEvent().getEventId());
            } catch (IOException e) {
                log.error("Failed to navigate to event details", e);
                showAlert("Error", "Failed to open event details");
            }
        }
    }
    
    private void handleDownloadQR(Ticket ticket) {
        if (ticket != null && ticket.getQrCode() != null) {
            // TODO: Implement QR code download functionality
            showAlert("Info", "QR code download not implemented yet");
        }
    }
    
    private void showAlert(String title, String content) {
        Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle(title);
            alert.setHeaderText(null);
            alert.setContentText(content);
            alert.showAndWait();
        });
    }
    
    private void showError(String title, String message) {
        Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle(title);
            alert.setHeaderText(null);
            alert.setContentText(message);
            alert.showAndWait();
        });
    }
    
    private record DashboardData(int registeredEvents, int upcomingEvents, int completedEvents) {}
}