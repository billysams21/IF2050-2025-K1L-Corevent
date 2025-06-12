package com.corevent.controller;

import java.util.List;

import org.springframework.stereotype.Controller;

import com.corevent.dto.EvaluationData;
import com.corevent.dto.SubmitResult;
import com.corevent.entity.Evaluation;
import com.corevent.entity.Event;
import com.corevent.entity.Participant;
import com.corevent.entity.User;
import com.corevent.service.EvaluationService;
import com.corevent.service.EventService;
import com.corevent.util.NavigationManager;
import com.corevent.util.SessionManager;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.ToggleGroup;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Controller
public class EvaluationController {
    
    // Form Elements
    @FXML private Label eventNameLabel;
    @FXML private Label eventDateLabel;
    @FXML private Label eventLocationLabel;
    @FXML private TextArea feedbackTextArea;
    @FXML private Button submitButton;
    @FXML private Button cancelButton;
    
    // Rating Elements
    @FXML private HBox ratingContainer;
    private ToggleGroup ratingGroup;
    private RadioButton[] ratingButtons;
    
    // Results Elements (untuk committee)
    @FXML private ComboBox<Event> eventComboBox;
    @FXML private TableView<Evaluation> evaluationsTable;
    @FXML private TableColumn<Evaluation, String> participantNameColumn;
    @FXML private TableColumn<Evaluation, Integer> scoreColumn;
    @FXML private TableColumn<Evaluation, String> feedbackColumn;
    @FXML private TableColumn<Evaluation, String> submittedAtColumn;
    
    // Statistics Elements
    @FXML private Label averageScoreLabel;
    @FXML private Label totalEvaluationsLabel;
    @FXML private Label minScoreLabel;
    @FXML private Label maxScoreLabel;
    
    // Services
    private final EvaluationService evaluationService;
    private final EventService eventService;
    private final NavigationManager navigationManager;
    
    // Current state
    private Event currentEvent;
    private Participant currentParticipant;
    
    public EvaluationController(EvaluationService evaluationService,
                              EventService eventService,
                              NavigationManager navigationManager) {
        this.evaluationService = evaluationService;
        this.eventService = eventService;
        this.navigationManager = navigationManager;
    }
    
    @FXML
    public void initialize() {
        setupRatingButtons();
        setupResultsTable();
        loadCurrentUser();
    }
    
    /**
     * Initialize evaluation form for specific event
     */
    public void initializeForEvent(Event event) {
        this.currentEvent = event;
        setupEventInfo();
        checkIfAlreadySubmitted();
    }
    
    /**
     * Initialize evaluation results view for committee
     */
    public void initializeForResults() {
        loadEventsForComboBox();
        setupEventComboBoxListener();
    }
    
    private void setupRatingButtons() {
        if (ratingContainer != null) {
            ratingGroup = new ToggleGroup();
            ratingButtons = new RadioButton[5];
            
            for (int i = 0; i < 5; i++) {
                RadioButton radioButton = new RadioButton();
                radioButton.setText(String.valueOf(i + 1) + " ⭐");
                radioButton.setToggleGroup(ratingGroup);
                radioButton.setUserData(i + 1);
                ratingButtons[i] = radioButton;
                ratingContainer.getChildren().add(radioButton);
            }
        }
    }
    
    private void setupResultsTable() {
        if (evaluationsTable != null) {
            participantNameColumn.setCellValueFactory(new PropertyValueFactory<>("participantName"));
            scoreColumn.setCellValueFactory(new PropertyValueFactory<>("score"));
            feedbackColumn.setCellValueFactory(new PropertyValueFactory<>("feedback"));
            submittedAtColumn.setCellValueFactory(new PropertyValueFactory<>("submittedAt"));
            
            // Custom cell factory for score to show stars
            scoreColumn.setCellFactory(col -> new TableCell<Evaluation, Integer>() {
                @Override
                protected void updateItem(Integer score, boolean empty) {
                    super.updateItem(score, empty);
                    if (empty || score == null) {
                        setText("");
                    } else {
                        setText(score + " ⭐");
                    }
                }
            });
        }
    }
    
    private void loadCurrentUser() {
        User currentUser = SessionManager.getInstance().getCurrentUser();
        if (currentUser instanceof Participant) {
            this.currentParticipant = (Participant) currentUser;
        }
    }
    
    private void setupEventInfo() {
        if (currentEvent != null) {
            eventNameLabel.setText(currentEvent.getEventName());
            eventDateLabel.setText(currentEvent.getDate().toString());
            eventLocationLabel.setText(currentEvent.getLocation());
        }
    }
    
    private void checkIfAlreadySubmitted() {
        if (currentEvent != null && currentParticipant != null) {
            boolean hasSubmitted = evaluationService.hasSubmittedEvaluation(
                currentParticipant.getId(), currentEvent.getEventId());
            
            if (hasSubmitted) {
                // Load existing evaluation and disable form
                evaluationService.getEvaluation(currentEvent.getEventId(), currentParticipant.getId())
                    .ifPresent(this::displayExistingEvaluation);
            }
        }
    }
    
    private void displayExistingEvaluation(Evaluation evaluation) {
        // Select the appropriate rating
        for (int i = 0; i < ratingButtons.length; i++) {
            if ((Integer) ratingButtons[i].getUserData() == evaluation.getScore()) {
                ratingButtons[i].setSelected(true);
                break;
            }
        }
        
        // Set feedback
        feedbackTextArea.setText(evaluation.getFeedback());
        
        // Disable form elements
        for (RadioButton button : ratingButtons) {
            button.setDisable(true);
        }
        feedbackTextArea.setDisable(true);
        submitButton.setDisable(true);
        submitButton.setText("Sudah Disubmit");
    }
    
    private void loadEventsForComboBox() {
        Task<List<Event>> loadTask = new Task<>() {
            @Override
            protected List<Event> call() throws Exception {
                return eventService.findAll();
            }
        };
        
        loadTask.setOnSucceeded(e -> {
            List<Event> events = loadTask.getValue();
            eventComboBox.setItems(FXCollections.observableArrayList(events));
            eventComboBox.setCellFactory(param -> new ListCell<Event>() {
                @Override
                protected void updateItem(Event event, boolean empty) {
                    super.updateItem(event, empty);
                    if (empty || event == null) {
                        setText(null);
                    } else {
                        setText(event.getEventName());
                    }
                }
            });
        });
        
        loadTask.setOnFailed(e -> {
            log.error("Failed to load events for combo box", loadTask.getException());
            showAlert("Error", "Gagal memuat daftar event");
        });
        
        new Thread(loadTask).start();
    }
    
    private void setupEventComboBoxListener() {
        if (eventComboBox != null) {
            eventComboBox.setOnAction(e -> {
                Event selectedEvent = eventComboBox.getSelectionModel().getSelectedItem();
                if (selectedEvent != null) {
                    loadEvaluationResults(selectedEvent.getEventId());
                }
            });
        }
    }
    
    private void loadEvaluationResults(String eventId) {
        Task<List<Evaluation>> loadTask = new Task<>() {
            @Override
            protected List<Evaluation> call() throws Exception {
                return evaluationService.getEventEvaluations(eventId);
            }
        };
        
        loadTask.setOnSucceeded(e -> {
            List<Evaluation> evaluations = loadTask.getValue();
            evaluationsTable.setItems(FXCollections.observableArrayList(evaluations));
            updateStatistics(eventId);
        });
        
        loadTask.setOnFailed(e -> {
            log.error("Failed to load evaluation results", loadTask.getException());
            showAlert("Error", "Gagal memuat hasil evaluasi");
        });
        
        new Thread(loadTask).start();
    }
    
    private void updateStatistics(String eventId) {
        Task<EvaluationService.EvaluationStatistics> statsTask = new Task<>() {
            @Override
            protected EvaluationService.EvaluationStatistics call() throws Exception {
                return evaluationService.getEvaluationStatistics(eventId);
            }
        };
        
        statsTask.setOnSucceeded(e -> {
            EvaluationService.EvaluationStatistics stats = statsTask.getValue();
            Platform.runLater(() -> {
                averageScoreLabel.setText(String.format("%.2f ⭐", stats.averageScore()));
                totalEvaluationsLabel.setText(String.valueOf(stats.totalEvaluations()));
                minScoreLabel.setText(String.valueOf(stats.minScore()));
                maxScoreLabel.setText(String.valueOf(stats.maxScore()));
            });
        });
        
        new Thread(statsTask).start();
    }
    
    @FXML
    private void handleSubmitEvaluation() {
        if (!validateForm()) {
            return;
        }
        
        RadioButton selectedRating = (RadioButton) ratingGroup.getSelectedToggle();
        Integer score = (Integer) selectedRating.getUserData();
        String feedback = feedbackTextArea.getText().trim();
        
        EvaluationData data = new EvaluationData(
            currentEvent.getEventId(),
            currentParticipant.getId(), 
            score,
            feedback
        );
        
        submitButton.setDisable(true);
        submitButton.setText("Mengirim...");
        
        Task<SubmitResult> submitTask = new Task<>() {
            @Override
            protected SubmitResult call() throws Exception {
                return evaluationService.submitEvaluation(data);
            }
        };
        
        submitTask.setOnSucceeded(e -> {
            SubmitResult result = submitTask.getValue();
            Platform.runLater(() -> {
                if (result.isSuccess()) {
                    showAlert("Sukses", "Evaluasi berhasil dikirim!");
                    handleCancel(); // Go back to previous view
                } else {
                    showAlert("Error", result.getMessage());
                    submitButton.setDisable(false);
                    submitButton.setText("Kirim Evaluasi");
                }
            });
        });
        
        submitTask.setOnFailed(e -> {
            log.error("Failed to submit evaluation", submitTask.getException());
            Platform.runLater(() -> {
                showAlert("Error", "Terjadi kesalahan saat mengirim evaluasi");
                submitButton.setDisable(false);
                submitButton.setText("Kirim Evaluasi");
            });
        });
        
        new Thread(submitTask).start();
    }
    
    @FXML
    private void handleCancel() {
        try {
            navigationManager.goBack();
        } catch (Exception e) {
            log.error("Failed to navigate back", e);
        }
    }
    
    private boolean validateForm() {
        if (ratingGroup.getSelectedToggle() == null) {
            showAlert("Validasi", "Silakan pilih rating untuk event ini");
            return false;
        }
        
        String feedback = feedbackTextArea.getText().trim();
        if (feedback.length() > 500) {
            showAlert("Validasi", "Feedback tidak boleh lebih dari 500 karakter");
            return false;
        }
        
        return true;
    }
    
    private void showAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
} 