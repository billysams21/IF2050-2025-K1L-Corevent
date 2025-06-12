package com.corevent.controller;

import java.io.IOException;
import java.time.format.DateTimeFormatter;
import java.util.List;

import org.springframework.stereotype.Controller;

import com.corevent.entity.Event;
import com.corevent.entity.User;
import com.corevent.service.EventService;
import com.corevent.util.NavigationManager;
import com.corevent.util.SessionManager;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Controller
@RequiredArgsConstructor
public class CommitteeDashboardController {
    
    private final EventService eventService;
    private final NavigationManager navigationManager;
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd MMM yyyy HH:mm");
    
    @FXML private Label welcomeLabel;
    @FXML private Label roleLabel;
    @FXML private Label totalEventsLabel;
    @FXML private Label upcomingEventsLabel;
    @FXML private Label totalParticipantsLabel;
    @FXML private TableView<Event> eventsTable;
    @FXML private TableColumn<Event, String> nameColumn;
    @FXML private TableColumn<Event, String> dateColumn;
    @FXML private TableColumn<Event, String> locationColumn;
    @FXML private TableColumn<Event, Integer> participantsColumn;
    @FXML private TableColumn<Event, Void> actionsColumn;
    
    @FXML private Button createEventButton;
    @FXML private Button manageParticipantsButton;
    @FXML private Button manageAttendanceButton;
    @FXML private Button profileButton;
    
    @FXML
    public void initialize() {
        setupUserInfo();
        setupEventHandlers();
        setupTableColumns();
        loadDashboardData();
    }
    
    private void setupUserInfo() {
        User currentUser = SessionManager.getInstance().getCurrentUser();
        welcomeLabel.setText("Welcome, " + currentUser.getUsername());
        roleLabel.setText("Committee Dashboard");
    }
    
    private void setupEventHandlers() {
        createEventButton.setOnAction(e -> handleCreateEvent());
        manageParticipantsButton.setOnAction(e -> handleManageParticipants());
        manageAttendanceButton.setOnAction(e -> handleManageAttendance());
        profileButton.setOnAction(e -> handleProfile());
    }
    
    private void setupTableColumns() {
        nameColumn.setCellValueFactory(new PropertyValueFactory<>("eventName"));
        dateColumn.setCellValueFactory(new PropertyValueFactory<>("date"));
        locationColumn.setCellValueFactory(new PropertyValueFactory<>("location"));
        participantsColumn.setCellValueFactory(new PropertyValueFactory<>("currentParticipants"));
        
        // Set up the actions column with buttons
        actionsColumn.setCellFactory(col -> new TableCell<>() {
            private final HBox buttons = new HBox(5);
            private final Button editButton = new Button("Edit");
            private final Button deleteButton = new Button("Delete");
            
            {
                buttons.setAlignment(javafx.geometry.Pos.CENTER);
                editButton.getStyleClass().add("button-secondary");
                deleteButton.getStyleClass().add("button-danger");
                
                editButton.setOnAction(e -> handleEditEvent(getTableRow().getItem()));
                deleteButton.setOnAction(e -> handleDeleteEvent(getTableRow().getItem()));
                
                buttons.getChildren().addAll(editButton, deleteButton);
            }
            
            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : buttons);
            }
        });
    }
    
    private void loadDashboardData() {
        Platform.runLater(() -> {
            try {
                List<Event> events = eventService.findAll();
                ObservableList<Event> eventList = FXCollections.observableArrayList(events);
                eventsTable.setItems(eventList);
                
                totalEventsLabel.setText(String.valueOf(events.size()));
                upcomingEventsLabel.setText(String.valueOf(
                    events.stream().filter(Event::isAvailable).count()
                ));
                totalParticipantsLabel.setText(String.valueOf(
                    events.stream().mapToInt(Event::getCurrentParticipants).sum()
                ));
            } catch (Exception e) {
                log.error("Failed to load dashboard data", e);
                showError("Failed to load dashboard data: " + e.getMessage());
            }
        });
    }
    
    private void handleCreateEvent() {
        try {
            navigationManager.navigateToCreateEvent();
        } catch (IOException e) {
            log.error("Failed to navigate to create event", e);
            showError("Failed to open create event page");
        }
    }
    
    @FXML
    private void handleManageParticipants() {
        try {
            Event selectedEvent = eventsTable.getSelectionModel().getSelectedItem();
            if (selectedEvent == null) {
                showError("Please select an event first");
                return;
            }
            navigationManager.navigateToParticipantManagement(selectedEvent.getEventId());
        } catch (IOException e) {
            log.error("Failed to navigate to participant management", e);
            showError("Failed to open participant management page");
        }
    }
    
    @FXML
    private void handleManageAttendance() {
        try {
            navigationManager.navigateToManageAttendance();
        } catch (IOException e) {
            log.error("Failed to navigate to manage attendance", e);
            showError("Failed to open attendance management page");
        }
    }
    
    @FXML
    private void handleProfile() {
        try {
            navigationManager.navigateToProfile();
        } catch (Exception e) {
            log.error("Failed to navigate to profile", e);
            showError("Failed to open profile page");
        }
    }
    
    @FXML
    private void handleMyProfile() {
        try {
            navigationManager.navigateToProfile();
        } catch (Exception e) {
            log.error("Failed to navigate to profile", e);
            showError("Failed to open profile page");
        }
    }
    
    @FXML
    private void handleLogout() {
        try {
            navigationManager.navigateToLogin();
        } catch (IOException e) {
            log.error("Failed to navigate to login", e);
            showError("Failed to logout");
        }
    }
    
    @FXML
    private void handleRefresh() {
        loadDashboardData();
    }
    
    @FXML
    private void handleViewEvent(Event event) {
        try {
            navigationManager.navigateToEventDetails(event.getEventId());
        } catch (IOException e) {
            log.error("Failed to navigate to event details", e);
            showError("Failed to open event details");
        }
    }
    
    @FXML
    private void handleEditEvent(Event event) {
        try {
            navigationManager.navigateToManageEvent(event.getEventId());
        } catch (IOException e) {
            log.error("Failed to navigate to manage event", e);
            showError("Failed to open event management page");
        }
    }
    
    @FXML
    private void handleDeleteEvent(Event event) {
        Alert confirmDialog = new Alert(Alert.AlertType.CONFIRMATION);
        confirmDialog.setTitle("Confirm Delete");
        confirmDialog.setHeaderText(null);
        confirmDialog.setContentText("Are you sure you want to delete this event?");
        
        confirmDialog.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                try {
                    eventService.deleteEvent(event.getEventId());
                    loadDashboardData();
                    showSuccess("Event deleted successfully");
                } catch (Exception e) {
                    log.error("Failed to delete event", e);
                    showError("Failed to delete event: " + e.getMessage());
                }
            }
        });
    }
    
    private void showAlert(String title, String content) {
        Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle(title);
            alert.setHeaderText(null);
            alert.setContentText(content);
            alert.showAndWait();
        });
    }
    
    private void showError(String message) {
        Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Error");
            alert.setHeaderText(null);
            alert.setContentText(message);
            alert.showAndWait();
        });
    }

    private void showSuccess(String message) {
        Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Success");
            alert.setHeaderText(null);
            alert.setContentText(message);
            alert.showAndWait();
        });
    }
}