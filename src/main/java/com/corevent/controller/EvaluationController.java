package com.corevent.controller;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
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
import com.itextpdf.text.Document;
import com.itextpdf.text.Element;
import com.itextpdf.text.Font;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;

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
import javafx.stage.FileChooser;
import javafx.stage.Stage;
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
    
    // Results Elements (for committee)
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
    
    // Export Elements
    @FXML private Button exportButton;
    @FXML private Button refreshButton;
    @FXML private Button downloadReportButton;
    
    // Services
    private final EvaluationService evaluationService;
    private final EventService eventService;
    private final NavigationManager navigationManager;
    
    // Current state
    private Event currentEvent;
    private Participant currentParticipant;
    
    // Primary stage
    private Stage primaryStage;
    
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
        
        // Set up export button
        if (exportButton != null) {
            exportButton.setOnAction(e -> handleExportData());
        }
        
        // Set up refresh button
        if (refreshButton != null) {
            refreshButton.setOnAction(e -> {
                Event selectedEvent = eventComboBox.getSelectionModel().getSelectedItem();
                if (selectedEvent != null) {
                    loadEvaluationResults(selectedEvent.getEventId());
                }
            });
        }
        
        // Set up download report button
        if (downloadReportButton != null) {
            downloadReportButton.setOnAction(e -> handleDownloadReport());
        }
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
        submitButton.setText("Already Submitted");
    }
    
    private void loadEventsForComboBox() {
        log.info("Loading events for combo box...");
        Task<List<Event>> loadTask = new Task<>() {
            @Override
            protected List<Event> call() throws Exception {
                List<Event> events = eventService.findAll();
                log.info("Found {} events", events.size());
                return events;
            }
        };
        
        loadTask.setOnSucceeded(e -> {
            List<Event> events = loadTask.getValue();
            log.info("Setting {} events in combo box", events.size());
            eventComboBox.setItems(FXCollections.observableArrayList(events));
            
            // Set up cell factory for the ComboBox
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
            
            // Set up button cell factory for the ComboBox
            eventComboBox.setButtonCell(new ListCell<Event>() {
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
            showAlert("Error", "Failed to load events");
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
            showAlert("Error", "Failed to load evaluation results");
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
                minScoreLabel.setText(stats.minScore() + " ⭐");
                maxScoreLabel.setText(stats.maxScore() + " ⭐");
            });
        });
        
        statsTask.setOnFailed(e -> {
            log.error("Failed to load statistics", statsTask.getException());
            Platform.runLater(() -> {
                showAlert("Error", "Failed to load evaluation statistics");
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
        submitButton.setText("Submitting...");
        
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
                    showAlert("Success", "Evaluation submitted successfully!");
                    handleCancel(); // Go back to previous view
                } else {
                    showAlert("Error", result.getMessage());
                    submitButton.setDisable(false);
                    submitButton.setText("Submit Evaluation");
                }
            });
        });
        
        submitTask.setOnFailed(e -> {
            log.error("Failed to submit evaluation", submitTask.getException());
            Platform.runLater(() -> {
                showAlert("Error", "An error occurred while submitting evaluation");
                submitButton.setDisable(false);
                submitButton.setText("Submit Evaluation");
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
    
    @FXML
    private void handleBack() {
        try {
            navigationManager.goBack();
        } catch (IOException e) {
            log.error("Failed to navigate back to committee dashboard", e);
        }
    }
    
    private boolean validateForm() {
        if (ratingGroup.getSelectedToggle() == null) {
            showAlert("Validation", "Please select a rating for this event");
            return false;
        }
        
        String feedback = feedbackTextArea.getText().trim();
        if (feedback.length() > 500) {
            showAlert("Validation", "Feedback must not exceed 500 characters");
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
    
    private void handleExportData() {
        Event selectedEvent = eventComboBox.getSelectionModel().getSelectedItem();
        if (selectedEvent == null) {
            showAlert("Error", "Please select an event first");
            return;
        }

        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Export Evaluation Data");
        fileChooser.getExtensionFilters().add(
            new FileChooser.ExtensionFilter("CSV Files", "*.csv")
        );
        fileChooser.setInitialFileName(selectedEvent.getEventName() + "_evaluations.csv");

        File file = fileChooser.showSaveDialog(primaryStage);
        if (file != null) {
            Task<Void> exportTask = new Task<>() {
                @Override
                protected Void call() throws Exception {
                    try (FileWriter writer = new FileWriter(file)) {
                        // Write header
                        writer.write("Participant Name,Rating,Comment,Submitted At\n");
                        
                        // Write data
                        for (Evaluation eval : evaluationsTable.getItems()) {
                            writer.write(String.format("%s,%d,%s,%s\n",
                                eval.getParticipant().getFullName(),
                                eval.getScore(),
                                eval.getFeedback().replace(",", ";"), // Replace commas to avoid CSV issues
                                eval.getSubmittedAt()
                            ));
                        }
                    }
                    return null;
                }
            };

            exportTask.setOnSucceeded(e -> {
                showAlert("Success", "Data exported successfully to " + file.getAbsolutePath());
            });

            exportTask.setOnFailed(e -> {
                log.error("Failed to export data", exportTask.getException());
                showAlert("Error", "Failed to export data: " + exportTask.getException().getMessage());
            });

            new Thread(exportTask).start();
        }
    }

    private void handleDownloadReport() {
        Event selectedEvent = eventComboBox.getSelectionModel().getSelectedItem();
        if (selectedEvent == null) {
            showAlert("Error", "Please select an event first");
            return;
        }

        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Download Evaluation Report");
        fileChooser.getExtensionFilters().add(
            new FileChooser.ExtensionFilter("PDF Files", "*.pdf")
        );
        fileChooser.setInitialFileName(selectedEvent.getEventName() + "_evaluation_report.pdf");

        File file = fileChooser.showSaveDialog(primaryStage);
        if (file != null) {
            Task<Void> reportTask = new Task<>() {
                @Override
                protected Void call() throws Exception {
                    try {
                        Document document = new Document();
                        PdfWriter.getInstance(document, new FileOutputStream(file));
                        document.open();

                        // Add title
                        Font titleFont = new Font(Font.FontFamily.HELVETICA, 18, Font.BOLD);
                        Paragraph title = new Paragraph(selectedEvent.getEventName() + " - Evaluation Report", titleFont);
                        title.setAlignment(Element.ALIGN_CENTER);
                        title.setSpacingAfter(20);
                        document.add(title);

                        // Add statistics
                        Font headerFont = new Font(Font.FontFamily.HELVETICA, 14, Font.BOLD);
                        Paragraph statsHeader = new Paragraph("Evaluation Statistics", headerFont);
                        statsHeader.setSpacingBefore(20);
                        statsHeader.setSpacingAfter(10);
                        document.add(statsHeader);

                        PdfPTable statsTable = new PdfPTable(2);
                        statsTable.setWidthPercentage(100);
                        statsTable.addCell("Average Rating");
                        statsTable.addCell(String.format("%.2f ⭐", Double.parseDouble(averageScoreLabel.getText().replace(" ⭐", ""))));
                        statsTable.addCell("Total Evaluations");
                        statsTable.addCell(totalEvaluationsLabel.getText());
                        statsTable.addCell("Lowest Rating");
                        statsTable.addCell(minScoreLabel.getText() + " ⭐");
                        statsTable.addCell("Highest Rating");
                        statsTable.addCell(maxScoreLabel.getText() + " ⭐");
                        document.add(statsTable);

                        // Add evaluations
                        Paragraph evalHeader = new Paragraph("Individual Evaluations", headerFont);
                        evalHeader.setSpacingBefore(20);
                        evalHeader.setSpacingAfter(10);
                        document.add(evalHeader);

                        PdfPTable evalTable = new PdfPTable(4);
                        evalTable.setWidthPercentage(100);
                        float[] columnWidths = {2f, 1f, 3f, 2f};
                        evalTable.setWidths(columnWidths);

                        // Add headers
                        evalTable.addCell("Participant");
                        evalTable.addCell("Rating");
                        evalTable.addCell("Comment");
                        evalTable.addCell("Submitted At");

                        // Add data
                        for (Evaluation eval : evaluationsTable.getItems()) {
                            evalTable.addCell(eval.getParticipant().getFullName());
                            evalTable.addCell(eval.getScore() + " ⭐");
                            evalTable.addCell(eval.getFeedback());
                            evalTable.addCell(eval.getSubmittedAt().toString());
                        }
                        document.add(evalTable);

                        document.close();
                    } catch (Exception e) {
                        throw new RuntimeException("Failed to generate PDF report", e);
                    }
                    return null;
                }
            };

            reportTask.setOnSucceeded(e -> {
                showAlert("Success", "Report downloaded successfully to " + file.getAbsolutePath());
            });

            reportTask.setOnFailed(e -> {
                log.error("Failed to generate report", reportTask.getException());
                showAlert("Error", "Failed to generate report: " + reportTask.getException().getMessage());
            });

            new Thread(reportTask).start();
        }
    }
}