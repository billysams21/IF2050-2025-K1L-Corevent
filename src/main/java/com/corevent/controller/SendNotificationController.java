package com.corevent.controller;

import java.util.List;
import java.util.ArrayList;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;

import com.corevent.dto.notification.NotificationResult;
import com.corevent.entity.Event;
import com.corevent.entity.Participant;
import com.corevent.service.NotificationService;

import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.CheckBoxTableCell;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import lombok.extern.slf4j.Slf4j;
import lombok.RequiredArgsConstructor;

import java.net.URL;
import java.util.ResourceBundle;

@Slf4j
@Controller
@RequiredArgsConstructor
public class SendNotificationController implements Initializable {
    
    @FXML
    private ComboBox<Event> eventComboBox;
    
    @FXML
    private TextField titleField;
    
    @FXML
    private TextArea messageArea;
    
    @FXML
    private TableView<ParticipantSelectionModel> participantTable;
    
    @FXML
    private TableColumn<ParticipantSelectionModel, Boolean> selectColumn;
    
    @FXML
    private TableColumn<ParticipantSelectionModel, String> nameColumn;
    
    @FXML
    private TableColumn<ParticipantSelectionModel, String> emailColumn;
    
    @FXML
    private Button sendButton;
    
    @FXML
    private Button selectAllButton;
    
    @FXML
    private Button deselectAllButton;
    
    @FXML
    private Label statusLabel;
    
    @FXML
    private ProgressIndicator loadingIndicator;
    
    private final NotificationService notificationService;
    
    private ObservableList<Event> events = FXCollections.observableArrayList();
    private ObservableList<ParticipantSelectionModel> participants = FXCollections.observableArrayList();
    
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        setupEventComboBox();
        setupParticipantTable();
        setupForm();
        loadEvents();
    }
    
    private void setupEventComboBox() {
        eventComboBox.setItems(events);
        eventComboBox.setPromptText("-- Pilih Acara --");
        
        // Converter untuk menampilkan nama event
        eventComboBox.setConverter(new javafx.util.StringConverter<Event>() {
            @Override
            public String toString(Event event) {
                return event != null ? event.getEventName() : "";
            }
            
            @Override
            public Event fromString(String string) {
                return events.stream()
                    .filter(event -> event.getEventName().equals(string))
                    .findFirst()
                    .orElse(null);
            }
        });
        
        // Event handler untuk load participants
        eventComboBox.setOnAction(e -> {
            Event selectedEvent = eventComboBox.getValue();
            if (selectedEvent != null) {
                loadParticipants(selectedEvent.getEventId());
            }
        });
    }
    
    private void setupParticipantTable() {
        // Setup columns
        selectColumn.setCellValueFactory(new PropertyValueFactory<>("selected"));
        selectColumn.setCellFactory(CheckBoxTableCell.forTableColumn(selectColumn));
        selectColumn.setEditable(true);
        
        nameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
        emailColumn.setCellValueFactory(new PropertyValueFactory<>("email"));
        
        participantTable.setItems(participants);
        participantTable.setEditable(true);
    }
    
    private void setupForm() {
        titleField.setPromptText("Contoh: Pengingat Pembayaran");
        messageArea.setPromptText("Tulis isi pesan yang akan dikirim...");
        messageArea.setWrapText(true);
        
        loadingIndicator.setVisible(false);
        statusLabel.setText("");
        
        // Disable send button initially
        sendButton.setDisable(true);
        
        // Enable send button when all required fields are filled
        titleField.textProperty().addListener((obs, oldVal, newVal) -> validateForm());
        messageArea.textProperty().addListener((obs, oldVal, newVal) -> validateForm());
        eventComboBox.valueProperty().addListener((obs, oldVal, newVal) -> validateForm());
    }
    
    private void validateForm() {
        boolean isValid = !titleField.getText().trim().isEmpty() && 
                         !messageArea.getText().trim().isEmpty() && 
                         eventComboBox.getValue() != null &&
                         participants.stream().anyMatch(ParticipantSelectionModel::isSelected);
        
        sendButton.setDisable(!isValid);
    }
    
    private void loadEvents() {
        Task<List<Event>> loadEventsTask = new Task<>() {
            @Override
            protected List<Event> call() throws Exception {
                return notificationService.getAvailableEventsForNotification();
            }
        };
        
        loadEventsTask.setOnSucceeded(e -> {
            events.clear();
            events.addAll(loadEventsTask.getValue());
        });
        
        loadEventsTask.setOnFailed(e -> {
            log.error("Failed to load events", e.getSource().getException());
            showError("Gagal memuat daftar acara");
        });
        
        new Thread(loadEventsTask).start();
    }
    
    private void loadParticipants(String eventID) {
        participants.clear();
        
        Task<List<Participant>> loadParticipantsTask = new Task<>() {
            @Override
            protected List<Participant> call() throws Exception {
                return notificationService.getEventParticipants(eventID);
            }
        };
        
        loadParticipantsTask.setOnSucceeded(e -> {
            List<Participant> participantList = loadParticipantsTask.getValue();
            for (Participant participant : participantList) {
                participants.add(new ParticipantSelectionModel(
                    participant.getUserId(),
                    participant.getFullName(),
                    participant.getEmail(),
                    false
                ));
            }
            validateForm();
        });
        
        loadParticipantsTask.setOnFailed(e -> {
            log.error("Failed to load participants", e.getSource().getException());
            showError("Gagal memuat daftar peserta");
        });
        
        new Thread(loadParticipantsTask).start();
    }
    
    @FXML
    private void handleSelectAll() {
        participants.forEach(p -> p.setSelected(true));
        participantTable.refresh();
        validateForm();
    }
    
    @FXML
    private void handleDeselectAll() {
        participants.forEach(p -> p.setSelected(false));
        participantTable.refresh();
        validateForm();
    }
    
    @FXML
    private void handleSendNotification() {
        Event selectedEvent = eventComboBox.getValue();
        String title = titleField.getText().trim();
        String message = messageArea.getText().trim();
        
        // Get selected participants
        List<String> selectedParticipantIds = participants.stream()
            .filter(ParticipantSelectionModel::isSelected)
            .map(ParticipantSelectionModel::getId)
            .collect(Collectors.toList());
        
        if (selectedParticipantIds.isEmpty()) {
            showError("Pilih setidaknya satu peserta");
            return;
        }
        
        // Disable form during sending
        setFormDisabled(true);
        loadingIndicator.setVisible(true);
        showStatus("Mengirim notifikasi...");
        
        // Send notifications in background
        Task<NotificationResult> sendTask = new Task<>() {
            @Override
            protected NotificationResult call() throws Exception {
                return notificationService.sendNotificationToParticipants(
                    selectedEvent.getEventId(), 
                    title, 
                    message, 
                    selectedParticipantIds
                );
            }
        };
        
        sendTask.setOnSucceeded(e -> {
            NotificationResult result = sendTask.getValue();
            Platform.runLater(() -> {
                loadingIndicator.setVisible(false);
                setFormDisabled(false);
                
                if (result.isSuccess()) {
                    showSuccess(result.getMessage());
                    clearForm();
                    log.info("Notification sent successfully: {}", result.getMessage());
                } else {
                    showError(result.getMessage());
                }
            });
        });
        
        sendTask.setOnFailed(e -> {
            log.error("Failed to send notifications", e.getSource().getException());
            Platform.runLater(() -> {
                loadingIndicator.setVisible(false);
                setFormDisabled(false);
                showError("Gagal mengirim notifikasi: " + e.getSource().getException().getMessage());
            });
        });
        
        new Thread(sendTask).start();
    }
    
    private void setFormDisabled(boolean disabled) {
        eventComboBox.setDisable(disabled);
        titleField.setDisable(disabled);
        messageArea.setDisable(disabled);
        participantTable.setDisable(disabled);
        sendButton.setDisable(disabled);
        selectAllButton.setDisable(disabled);
        deselectAllButton.setDisable(disabled);
    }
    
    private void clearForm() {
        titleField.clear();
        messageArea.clear();
        eventComboBox.setValue(null);
        participants.clear();
        statusLabel.setText("");
    }
    
    private void showStatus(String message) {
        statusLabel.setText(message);
        statusLabel.setStyle("-fx-text-fill: #333;");
    }
    
    private void showSuccess(String message) {
        statusLabel.setText(message);
        statusLabel.setStyle("-fx-text-fill: #27ae60;");
    }
    
    private void showError(String message) {
        statusLabel.setText(message);
        statusLabel.setStyle("-fx-text-fill: #e74c3c;");
    }
    
    // Model class untuk participant selection
    public static class ParticipantSelectionModel {
        private String id;
        private String name;
        private String email;
        private boolean selected;
        
        public ParticipantSelectionModel(String id, String name, String email, boolean selected) {
            this.id = id;
            this.name = name;
            this.email = email;
            this.selected = selected;
        }
        
        // Getters and Setters
        public String getId() { return id; }
        public void setId(String id) { this.id = id; }
        
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        
        public String getEmail() { return email; }
        public void setEmail(String email) { this.email = email; }
        
        public boolean isSelected() { return selected; }
        public void setSelected(boolean selected) { this.selected = selected; }
    }
} 