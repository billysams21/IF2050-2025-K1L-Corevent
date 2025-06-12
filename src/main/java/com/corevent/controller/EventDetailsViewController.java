package com.corevent.controller;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;

import com.corevent.entity.Attendance;
import com.corevent.entity.Event;
import com.corevent.entity.Participant;
import com.corevent.entity.User;
import com.corevent.service.AttendanceService;
import com.corevent.service.EventService;
import com.corevent.util.NavigationManager;
import com.corevent.util.SessionManager;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Controller
public class EventDetailsViewController {

    @FXML private Label eventNameLabel;
    @FXML private Label eventDateLabel;
    @FXML private Label eventEndTimeLabel;
    @FXML private Label eventLocationLabel;
    @FXML private Label eventTypeLabel;
    @FXML private Label eventPriceLabel;
    @FXML private Label availableSlotsLabel;
    @FXML private TextArea descriptionTextArea;
    @FXML private TextArea termsTextArea;
    @FXML private Button checkInButton;

    @Autowired private EventService eventService;
    @Autowired private AttendanceService attendanceService;
    @Autowired private NavigationManager navigationManager;

    private Event event;
    private Participant participant;
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd MMM yyyy HH:mm");

    @FXML
    public void initialize() {
        // Get current participant from session
        User currentUser = SessionManager.getInstance().getCurrentUser();
        if (currentUser instanceof Participant) {
            this.participant = (Participant) currentUser;
        } else {
            showError("You must be logged in as a participant to view event details");
            try {
                navigationManager.navigateToLogin();
            } catch (IOException e) {
                log.error("Failed to navigate to login", e);
            }
        }
    }

    public void setEvent(Event event) {
        this.event = event;
        updateEventDetails();
        checkEventTime();
    }

    private void updateEventDetails() {
        if (event != null) {
            eventNameLabel.setText(event.getEventName());
            eventDateLabel.setText(event.getDate().format(DATE_FORMATTER));
            eventEndTimeLabel.setText(event.getEndTime().format(DATE_FORMATTER));
            eventLocationLabel.setText(event.getLocation());
            eventTypeLabel.setText(event.getEventType().name());
            eventPriceLabel.setText(String.format("Rp %.2f", event.getTicketPrice()));
            availableSlotsLabel.setText(String.valueOf(event.getQuota() - event.getCurrentParticipants()));
            descriptionTextArea.setText(event.getDescription());
            termsTextArea.setText(event.getTermsAndConditions());
        }
    }

    private void checkEventTime() {
        if (event != null) {
            LocalDateTime now = LocalDateTime.now();
            LocalDateTime eventTime = event.getDate();
            LocalDateTime eventEndTime = event.getEndTime();
            
            // Show check-in button if it's time for the event (within 1 hour before and after)
            boolean isEventTime = now.isAfter(eventTime.minusHours(1)) && now.isBefore(eventEndTime.plusHours(1));
            checkInButton.setVisible(isEventTime);
            checkInButton.setManaged(isEventTime);
        }
    }

    @FXML
    private void handleCheckIn() {
        if (event != null && participant != null) {
            try {
                Attendance attendance = attendanceService.checkIn(participant, event);
                if (attendance != null) {
                    showSuccess("Check-in successful!");
                    checkInButton.setVisible(false);
                    checkInButton.setManaged(false);
                } else {
                    showError("Failed to check in. Please try again.");
                }
            } catch (Exception e) {
                log.error("Error during check-in", e);
                showError("An error occurred during check-in: " + e.getMessage());
            }
        }
    }

    @FXML
    private void handleBack() {
        try {
            navigationManager.goBack();
        } catch (IOException e) {
            log.error("Failed to navigate back", e);
            showError("Failed to navigate back");
        }
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