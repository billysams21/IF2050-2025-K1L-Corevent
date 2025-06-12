package com.corevent.controller;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Controller;

import com.corevent.entity.Evaluation;
import com.corevent.entity.Event;
import com.corevent.entity.Participant;
import com.corevent.entity.User;
import com.corevent.service.EvaluationService;
import com.corevent.service.EventService;
import com.corevent.service.TicketService;
import com.corevent.util.NavigationManager;
import com.corevent.util.SessionManager;

import javafx.application.Platform;
import javafx.collections.FXCollections;
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
public class MyEvaluationsController {

    // Statistics Labels
    @FXML private Label completedEventsLabel;
    @FXML private Label submittedEvaluationsLabel;
    @FXML private Label pendingEvaluationsLabel;

    // Available Events Table (can be evaluated)
    @FXML private TableView<Event> availableEventsTable;
    @FXML private TableColumn<Event, String> availableEventNameColumn;
    @FXML private TableColumn<Event, String> availableDateColumn;
    @FXML private TableColumn<Event, String> availableLocationColumn;
    @FXML private TableColumn<Event, String> availableStatusColumn;
    @FXML private TableColumn<Event, Void> availableActionsColumn;

    // Submitted Evaluations Table
    @FXML private TableView<Evaluation> submittedEvaluationsTable;
    @FXML private TableColumn<Evaluation, String> submittedEventNameColumn;
    @FXML private TableColumn<Evaluation, Integer> submittedScoreColumn;
    @FXML private TableColumn<Evaluation, String> submittedFeedbackColumn;
    @FXML private TableColumn<Evaluation, String> submittedDateColumn;
    @FXML private TableColumn<Evaluation, Void> submittedActionsColumn;

    // Services
    private final EvaluationService evaluationService;
    private final EventService eventService;
    private final TicketService ticketService;
    private final NavigationManager navigationManager;

    // Current state
    private Participant currentParticipant;

    public MyEvaluationsController(EvaluationService evaluationService,
                                   EventService eventService,
                                   TicketService ticketService,
                                   NavigationManager navigationManager) {
        this.evaluationService = evaluationService;
        this.eventService = eventService;
        this.ticketService = ticketService;
        this.navigationManager = navigationManager;
    }

    @FXML
    public void initialize() {
        loadCurrentParticipant();
        setupTableColumns();
        loadEvaluationData();
    }

    private void loadCurrentParticipant() {
        User currentUser = SessionManager.getInstance().getCurrentUser();
        if (currentUser instanceof Participant) {
            this.currentParticipant = (Participant) currentUser;
        } else {
            log.error("Current user is not a participant");
            showAlert("Error", "You must be logged in as a participant to access this page");
            handleBackToDashboard();
        }
    }

    private void setupTableColumns() {
        // Available Events Table
        availableEventNameColumn.setCellValueFactory(new PropertyValueFactory<>("eventName"));
        availableDateColumn.setCellValueFactory(new PropertyValueFactory<>("date"));
        availableLocationColumn.setCellValueFactory(new PropertyValueFactory<>("location"));

        // Status column
        availableStatusColumn.setCellFactory(col -> new TableCell<Event, String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || getTableRow().getItem() == null) {
                    setText("");
                } else {
                    Event event = getTableRow().getItem();
                    boolean isCompleted = LocalDateTime.now().isAfter(event.getDate());
                    setText(isCompleted ? "Completed" : "Not Yet Completed");

                    getStyleClass().removeAll("status-completed", "status-pending");
                    if (isCompleted) {
                        getStyleClass().add("status-completed");
                    } else {
                        getStyleClass().add("status-pending");
                    }
                }
            }
        });

        // Actions column for available events
        availableActionsColumn.setCellFactory(col -> new TableCell<>() {
            private final Button evaluateButton = new Button("Evaluate");

            {
                evaluateButton.getStyleClass().add("button-primary");
                evaluateButton.setOnAction(e -> {
                    Event event = getTableRow().getItem();
                    if (event != null) {
                        handleEvaluateEvent(event);
                    }
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || getTableRow().getItem() == null) {
                    setGraphic(null);
                } else {
                    Event event = getTableRow().getItem();
                    boolean isCompleted = LocalDateTime.now().isAfter(event.getDate());

                    evaluateButton.setDisable(!isCompleted);
                    evaluateButton.setText(isCompleted ? "Evaluate" : "Not Yet Available");

                    setGraphic(evaluateButton);
                }
            }
        });

        // Submitted Evaluations Table
        submittedEventNameColumn.setCellValueFactory(new PropertyValueFactory<>("eventName"));
        submittedScoreColumn.setCellValueFactory(new PropertyValueFactory<>("score"));
        submittedFeedbackColumn.setCellValueFactory(new PropertyValueFactory<>("feedback"));
        submittedDateColumn.setCellValueFactory(new PropertyValueFactory<>("submittedAt"));

        // Score column with stars
        submittedScoreColumn.setCellFactory(col -> new TableCell<Evaluation, Integer>() {
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

        // Actions column for submitted evaluations
        submittedActionsColumn.setCellFactory(col -> new TableCell<>() {
            private final Button viewButton = new Button("View");

            {
                viewButton.getStyleClass().add("button-secondary");
                viewButton.setOnAction(e -> {
                    Evaluation evaluation = getTableRow().getItem();
                    if (evaluation != null) {
                        handleViewEvaluation(evaluation);
                    }
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : viewButton);
            }
        });
    }

    private void loadEvaluationData() {
        if (currentParticipant == null) return;

        Task<EvaluationData> loadTask = new Task<>() {
            @Override
            protected EvaluationData call() {
                List<Event> participantEvents = eventService.findAll().stream()
                        .filter(event -> ticketService.findByParticipantId(currentParticipant.getUserId())
                                .stream()
                                .anyMatch(ticket -> ticket.getEvent().getEventId().equals(event.getEventId())))
                        .collect(Collectors.toList());

                List<Evaluation> submittedEvaluations = evaluationService.getParticipantEvaluations(currentParticipant.getId());

                List<Event> availableForEvaluation = participantEvents.stream()
                        .filter(event -> {
                            boolean isCompleted = LocalDateTime.now().isAfter(event.getDate());
                            boolean hasEvaluated = submittedEvaluations.stream()
                                    .anyMatch(eval -> eval.getEvent().getEventId().equals(event.getEventId()));
                            return isCompleted && !hasEvaluated;
                        })
                        .collect(Collectors.toList());

                int completedEvents = (int) participantEvents.stream()
                        .filter(event -> LocalDateTime.now().isAfter(event.getDate()))
                        .count();
                int submittedCount = submittedEvaluations.size();
                int pendingCount = availableForEvaluation.size();

                return new EvaluationData(availableForEvaluation, submittedEvaluations,
                        completedEvents, submittedCount, pendingCount);
            }
        };

        loadTask.setOnSucceeded(e -> {
            EvaluationData data = loadTask.getValue();
            Platform.runLater(() -> {
                completedEventsLabel.setText(String.valueOf(data.completedEventsCount));
                submittedEvaluationsLabel.setText(String.valueOf(data.submittedEvaluationsCount));
                pendingEvaluationsLabel.setText(String.valueOf(data.pendingEvaluationsCount));

                availableEventsTable.setItems(FXCollections.observableArrayList(data.availableEvents));
                submittedEvaluationsTable.setItems(FXCollections.observableArrayList(data.submittedEvaluations));
            });
        });

        loadTask.setOnFailed(e -> {
            log.error("Failed to load evaluation data", loadTask.getException());
            showAlert("Error", "Failed to load evaluation data");
        });

        new Thread(loadTask).start();
    }

    @FXML
    private void handleRefreshAvailable() {
        loadEvaluationData();
    }

    @FXML
    private void handleRefreshSubmitted() {
        loadEvaluationData();
    }

    private void handleEvaluateEvent(Event event) {
        try {
            if (LocalDateTime.now().isBefore(event.getDate())) {
                showAlert("Warning", "Event is not yet completed. Evaluation is not available.");
                return;
            }

            if (evaluationService.hasSubmittedEvaluation(currentParticipant.getId(), event.getEventId())) {
                showAlert("Warning", "You have already submitted an evaluation for this event.");
                return;
            }

            navigationManager.navigateToEvaluationForm(event.getEventId());

        } catch (Exception e) {
            log.error("Failed to navigate to evaluation form", e);
            showAlert("Error", "Failed to open evaluation form");
        }
    }

    private void handleViewEvaluation(Evaluation evaluation) {
        showEvaluationDetails(evaluation);
    }

    private void showEvaluationDetails(Evaluation evaluation) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Evaluation Details");
        alert.setHeaderText("Evaluation for: " + evaluation.getEventName());

        String content = String.format(
                "Rating: %d ⭐\n\nFeedback:\n%s\n\nSubmitted at: %s",
                evaluation.getScore(),
                evaluation.getFeedback() != null ? evaluation.getFeedback() : "No feedback provided",
                evaluation.getSubmittedAt().toString()
        );

        alert.setContentText(content);
        alert.showAndWait();
    }

    @FXML
    private void handleBackToDashboard() {
        try {
            navigationManager.navigateToParticipantDashboard();
        } catch (Exception e) {
            log.error("Failed to navigate back to dashboard", e);
            showAlert("Error", "Failed to return to dashboard");
        }
    }

    private void showAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

    // Data class for loading task result
    private record EvaluationData(
            List<Event> availableEvents,
            List<Evaluation> submittedEvaluations,
            int completedEventsCount,
            int submittedEvaluationsCount,
            int pendingEvaluationsCount
    ) {}
}
