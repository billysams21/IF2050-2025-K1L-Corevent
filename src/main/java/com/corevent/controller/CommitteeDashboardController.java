// CommitteeDashboardController.java
package com.corevent.boundary;

import com.corevent.controller.EventController;
import com.corevent.entity.Event;
import com.corevent.entity.ParticipantInfo;
import com.corevent.service.EventService;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Component
public class CommitteeDashboardController {
    
    @FXML private TableView<Event> eventsTable;
    @FXML private TableColumn<Event, String> eventNameColumn;
    @FXML private TableColumn<Event, String> dateColumn;
    @FXML private TableColumn<Event, String> locationColumn;
    @FXML private TableColumn<Event, String> participantsColumn;
    @FXML private TableColumn<Event, Void> actionsColumn;
    
    @FXML private Button createEventButton;
    @FXML private Button refreshButton;
    @FXML private Label welcomeLabel;
    @FXML private Label totalEventsLabel;
    @FXML private Label upcomingEventsLabel;
    @FXML private Label totalParticipantsLabel;
    
    @Autowired
    private EventService eventService;
    
    @Autowired
    private EventController eventController;
    
    private ObservableList<Event> eventsList = FXCollections.observableArrayList();
    
    @FXML
    public void initialize() {
        setupTableColumns();
        loadDashboardData();
        
        // Set welcome message
        welcomeLabel.setText("Welcome, " + SessionManager.getInstance().getCurrentUser().getUsername());
    }
    
    private void setupTableColumns() {
        eventNameColumn.setCellValueFactory(new PropertyValueFactory<>("eventName"));
        
        dateColumn.setCellValueFactory(cellData -> 
            new SimpleStringProperty(cellData.getValue().getDate()
                .format(DateTimeFormatter.ofPattern("dd MMM yyyy HH:mm")))
        );
        
        locationColumn.setCellValueFactory(new PropertyValueFactory<>("location"));
        
        participantsColumn.setCellValueFactory(cellData -> {
            Event event = cellData.getValue();
            return new SimpleStringProperty(event.getCurrentParticipants() + "/" + event.getQuota());
        });
        
        // Add action buttons
        actionsColumn.setCellFactory(param -> new TableCell<>() {
            private final Button viewButton = new Button("View");
            private final Button editButton = new Button("Edit");
            private final Button participantsButton = new Button("Participants");
            
            {
                viewButton.getStyleClass().add("button-primary-small");
                editButton.getStyleClass().add("button-secondary-small");
                participantsButton.getStyleClass().add("button-info-small");
                
                viewButton.setOnAction(e -> viewEvent(getTableView().getItems().get(getIndex())));
                editButton.setOnAction(e -> editEvent(getTableView().getItems().get(getIndex())));
                participantsButton.setOnAction(e -> viewParticipants(getTableView().getItems().get(getIndex())));
            }
            
            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    HBox buttons = new HBox(5);
                    buttons.getChildren().addAll(viewButton, editButton, participantsButton);
                    setGraphic(buttons);
                }
            }
        });
        
        eventsTable.setItems(eventsList);
    }
    
    @FXML
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
            
            // Refresh events list after creation
            loadDashboardData();
            
        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Error", "Failed xto open create event dialog");
        }
    }
    
    @FXML
    private void handleRefresh() {
        loadDashboardData();
    }
    
    @FXML
    private void handleLogout() {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Logout Confirmation");
        alert.setHeaderText("Are you sure you want to logout?");
        
        if (alert.showAndWait().orElse(ButtonType.CANCEL) == ButtonType.OK) {
            SessionManager.getInstance().clearSession();
            navigateToLogin();
        }
    }
    
    private void loadDashboardData() {
        refreshButton.setDisable(true);
        
        Task<DashboardData> loadTask = new Task<>() {
            @Override
            protected DashboardData call() throws Exception {
                List<Event> events = eventService.getCommitteeEvents(
                    SessionManager.getInstance().getCurrentUser().getUserId()
                );
                
                int totalEvents = events.size();
                int upcomingEvents = (int) events.stream()
                    .filter(Event::isAvailable)
                    .count();
                int totalParticipants = events.stream()
                    .mapToInt(Event::getCurrentParticipants)
                    .sum();
                
                return new DashboardData(events, totalEvents, upcomingEvents, totalParticipants);
            }
        };
        
        loadTask.setOnSucceeded(e -> {
            DashboardData data = loadTask.getValue();
            eventsList.setAll(data.events);
            totalEventsLabel.setText(String.valueOf(data.totalEvents));
            upcomingEventsLabel.setText(String.valueOf(data.upcomingEvents));
            totalParticipantsLabel.setText(String.valueOf(data.totalParticipants));
            refreshButton.setDisable(false);
        });
        
        loadTask.setOnFailed(e -> {
            showAlert("Error", "Failed to load dashboard data");
            refreshButton.setDisable(false);
        });
        
        new Thread(loadTask).start();
    }
    
    private void viewParticipants(Event event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/participants-list.fxml"));
            loader.setControllerFactory(SpringContext::getBean);
            Parent root = loader.load();
            
            ParticipantsListController controller = loader.getController();
            controller.setEvent(event);
            
            Stage stage = new Stage();
            stage.setTitle("Participants - " + event.getEventName());
            stage.setScene(new Scene(root));
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.show();
            
        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Error", "Failed to load participants list");
        }
    }
    
    private void viewEvent(Event event) {
        // Implement view event details
    }
    
    private void editEvent(Event event) {
        // Implement edit event
    }
    
    private void navigateToLogin() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/login.fxml"));
            loader.setControllerFactory(SpringContext::getBean);
            Parent root = loader.load();
            
            Stage stage = (Stage) createEventButton.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("Corevent - Login");
            stage.centerOnScreen();
            
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    private void showAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setContentText(content);
        alert.showAndWait();
    }
    
    // Inner class for dashboard data
    private static class DashboardData {
        final List<Event> events;
        final int totalEvents;
        final int upcomingEvents;
        final int totalParticipants;
        
        DashboardData(List<Event> events, int totalEvents, int upcomingEvents, int totalParticipants) {
            this.events = events;
            this.totalEvents = totalEvents;
            this.upcomingEvents = upcomingEvents;
            this.totalParticipants = totalParticipants;
        }
    }
}