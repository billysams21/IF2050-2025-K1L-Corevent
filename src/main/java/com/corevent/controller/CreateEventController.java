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
public class CreateEventController {
    private final EventService eventService;
    private final NavigationManager navigationManager;

    @FXML private TextField eventNameField;
    @FXML private DatePicker datePicker;
    @FXML private TextField timeField;
    @FXML private TextField endTimeField;
    @FXML private TextField locationField;
    @FXML private TextField quotaField;
    @FXML private ComboBox<String> eventTypeCombo;
    @FXML private TextField ticketPriceField;
    @FXML private TextArea termsField;
    @FXML private TextArea descriptionField;
    @FXML private Button saveButton;
    @FXML private Button cancelButton;

    public CreateEventController(EventService eventService, NavigationManager navigationManager) {
        this.eventService = eventService;
        this.navigationManager = navigationManager;
    }

    @FXML
    private void initialize() {
        try {
            log.info("Initializing CreateEventController");
            ObservableList<String> eventTypes = FXCollections.observableArrayList("FREE", "PAID");
            eventTypeCombo.setItems(eventTypes);
            eventTypeCombo.getSelectionModel().selectFirst();
            log.info("ComboBox initialized successfully");
        } catch (Exception e) {
            log.error("Error initializing CreateEventController", e);
        }
    }

    @FXML
    private void handleSave(ActionEvent event) {
        try {
            // Validate required fields
            if (!validateFields()) {
                return;
            }

            // Create new event
            Event newEvent = new Event();
            newEvent.setEventName(eventNameField.getText().trim());
            
            // Combine date and time
            LocalDate date = datePicker.getValue();
            LocalTime time = LocalTime.parse(timeField.getText().trim(), DateTimeFormatter.ofPattern("HH:mm"));
            LocalTime endTime = LocalTime.parse(endTimeField.getText().trim(), DateTimeFormatter.ofPattern("HH:mm"));
            newEvent.setDate(LocalDateTime.of(date, time));
            newEvent.setEndTime(LocalDateTime.of(date, endTime));
            
            newEvent.setLocation(locationField.getText().trim());
            newEvent.setQuota(Integer.parseInt(quotaField.getText().trim()));
            newEvent.setEventType(Event.EventType.valueOf(eventTypeCombo.getValue()));
            
            if (newEvent.getEventType() == Event.EventType.PAID) {
                newEvent.setTicketPrice(Double.parseDouble(ticketPriceField.getText().trim()));
            }
            
            newEvent.setTermsAndConditions(termsField.getText().trim());
            newEvent.setDescription(descriptionField.getText().trim());

            // Save event
            Event savedEvent = eventService.save(newEvent);
            log.info("Event saved successfully with ID: {}", savedEvent.getEventId());

            // Show success message
            Alert alert = new Alert(AlertType.INFORMATION);
            alert.setTitle("Success");
            alert.setHeaderText(null);
            alert.setContentText("Event created successfully!");
            alert.showAndWait();

            // Navigate back to committee dashboard
            navigateBack();

        } catch (DateTimeParseException e) {
            showError("Invalid time format. Please use HH:mm format (e.g., 14:30)");
        } catch (NumberFormatException e) {
            showError("Invalid number format for quota or ticket price");
        } catch (Exception e) {
            log.error("Error creating event", e);
            showError("Failed to create event: " + e.getMessage());
        }
    }

    @FXML
    private void handleCancel(ActionEvent event) {
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
            showError("Start time is required");
            return false;
        }
        if (endTimeField.getText().trim().isEmpty()) {
            showError("End time is required");
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
        if (termsField.getText().trim().isEmpty()) {
            showError("Terms and conditions are required");
            return false;
        }
        if (descriptionField.getText().trim().isEmpty()) {
            showError("Description is required");
            return false;
        }

        // Validate time format
        try {
            LocalTime.parse(timeField.getText().trim(), DateTimeFormatter.ofPattern("HH:mm"));
            LocalTime.parse(endTimeField.getText().trim(), DateTimeFormatter.ofPattern("HH:mm"));
        } catch (DateTimeParseException e) {
            showError("Invalid time format. Please use HH:mm format (e.g., 14:30)");
            return false;
        }

        // Validate end time is after start time
        LocalTime startTime = LocalTime.parse(timeField.getText().trim(), DateTimeFormatter.ofPattern("HH:mm"));
        LocalTime endTime = LocalTime.parse(endTimeField.getText().trim(), DateTimeFormatter.ofPattern("HH:mm"));
        if (!endTime.isAfter(startTime)) {
            showError("End time must be after start time");
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

    private void navigateBack() {
        try {
            navigationManager.navigateToCommitteeDashboard();
        } catch (IOException e) {
            log.error("Failed to navigate to committee dashboard", e);
            showError("Event created but failed to return to dashboard. Please try again.");
        }
    }
}
