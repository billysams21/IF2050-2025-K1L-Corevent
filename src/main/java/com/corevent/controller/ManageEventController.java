package com.corevent.controller;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

import org.springframework.stereotype.Controller;

import com.corevent.entity.Event;
import com.corevent.service.EventService;
import com.corevent.util.NavigationManager;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Controller
public class ManageEventController {
    private final EventService eventService;
    private final NavigationManager navigationManager;
    private String eventId;

    @FXML private TextField eventNameField;
    @FXML private DatePicker datePicker;
    @FXML private TextField timeField;
    @FXML private TextField locationField;
    @FXML private TextField quotaField;
    @FXML private ComboBox<String> eventTypeCombo;
    @FXML private TextField ticketPriceField;
    @FXML private TextArea termsField;
    @FXML private TextArea descriptionField;
    @FXML private Button saveButton;
    @FXML private Button cancelButton;

    public ManageEventController(EventService eventService, NavigationManager navigationManager) {
        this.eventService = eventService;
        this.navigationManager = navigationManager;
    }

    public void setEventId(String eventId) {
        this.eventId = eventId;
        loadEventData();
    }

    @FXML
    private void initialize() {
        try {
            log.info("Initializing ManageEventController");
            ObservableList<String> eventTypes = FXCollections.observableArrayList("FREE", "PAID");
            eventTypeCombo.setItems(eventTypes);
            log.info("ComboBox initialized successfully");
        } catch (Exception e) {
            log.error("Error initializing ManageEventController", e);
        }
    }

    private void loadEventData() {
        try {
            Event event = eventService.findById(eventId);
            if (event != null) {
                eventNameField.setText(event.getEventName());
                datePicker.setValue(event.getDate().toLocalDate());
                timeField.setText(event.getDate().toLocalTime().format(DateTimeFormatter.ofPattern("HH:mm")));
                locationField.setText(event.getLocation());
                quotaField.setText(String.valueOf(event.getQuota()));
                eventTypeCombo.setValue(event.getEventType().name());
                ticketPriceField.setText(String.valueOf(event.getTicketPrice()));
                termsField.setText(event.getTermsAndConditions());
                descriptionField.setText(event.getDescription());
            } else {
                showError("Event not found");
                navigateBack();
            }
        } catch (Exception e) {
            log.error("Error loading event data", e);
            showError("Failed to load event data: " + e.getMessage());
            navigateBack();
        }
    }

    @FXML
    private void handleSave(ActionEvent event) {
        try {
            // Validate required fields
            if (!validateFields()) {
                return;
            }

            // Get existing event
            Event existingEvent = eventService.findById(eventId);
            if (existingEvent == null) {
                showError("Event not found");
                return;
            }

            // Update event data
            existingEvent.setEventName(eventNameField.getText().trim());
            
            // Combine date and time
            LocalDate date = datePicker.getValue();
            LocalTime time = LocalTime.parse(timeField.getText().trim(), DateTimeFormatter.ofPattern("HH:mm"));
            existingEvent.setDate(LocalDateTime.of(date, time));
            
            existingEvent.setLocation(locationField.getText().trim());
            existingEvent.setQuota(Integer.parseInt(quotaField.getText().trim()));
            existingEvent.setEventType(Event.EventType.valueOf(eventTypeCombo.getValue()));
            
            if (existingEvent.getEventType() == Event.EventType.PAID) {
                existingEvent.setTicketPrice(Double.parseDouble(ticketPriceField.getText().trim()));
            } else {
                existingEvent.setTicketPrice(0.0);
            }
            
            existingEvent.setTermsAndConditions(termsField.getText().trim());
            existingEvent.setDescription(descriptionField.getText().trim());

            // Save updated event
            Event updatedEvent = eventService.update(existingEvent);
            log.info("Event updated successfully with ID: {}", updatedEvent.getEventId());

            // Show success message
            Alert alert = new Alert(AlertType.INFORMATION);
            alert.setTitle("Success");
            alert.setHeaderText(null);
            alert.setContentText("Event updated successfully!");
            alert.showAndWait();

            // Navigate back to committee dashboard
            navigateBack();

        } catch (DateTimeParseException e) {
            showError("Invalid time format. Please use HH:mm format (e.g., 14:30)");
        } catch (NumberFormatException e) {
            showError("Invalid number format for quota or ticket price");
        } catch (Exception e) {
            log.error("Error updating event", e);
            showError("Failed to update event: " + e.getMessage());
        }
    }

    @FXML
    private void handleCancel(ActionEvent event) {
        navigateBack();
    }

    private void navigateBack() {
        try {
            navigationManager.navigateToCommitteeDashboard();
        } catch (IOException e) {
            log.error("Failed to navigate to committee dashboard", e);
            showError("Failed to return to dashboard. Please try again.");
        }
    }

    private boolean validateFields() {
        if (eventNameField.getText().trim().isEmpty()) {
            showError("Event name is required");
            return false;
        }
        if (datePicker.getValue() == null) {
            showError("Date is required");
            return false;
        }
        if (timeField.getText().trim().isEmpty()) {
            showError("Time is required");
            return false;
        }
        if (locationField.getText().trim().isEmpty()) {
            showError("Location is required");
            return false;
        }
        if (quotaField.getText().trim().isEmpty()) {
            showError("Quota is required");
            return false;
        }
        if (eventTypeCombo.getValue() == null) {
            showError("Event type is required");
            return false;
        }
        if (eventTypeCombo.getValue().equals("PAID") && ticketPriceField.getText().trim().isEmpty()) {
            showError("Ticket price is required for paid events");
            return false;
        }
        return true;
    }

    private void showError(String message) {
        Alert alert = new Alert(AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
} 