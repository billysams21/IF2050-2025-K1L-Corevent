package com.corevent.controller;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;

import com.corevent.entity.Event;
import com.corevent.entity.Event.EventType;
import com.corevent.entity.Participant;
import com.corevent.entity.User;
import com.corevent.service.EventService;
import com.corevent.service.TicketService;
import com.corevent.util.NavigationManager;
import com.corevent.util.SessionManager;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Controller
public class BrowseEventsController {

    @FXML private TextField searchField;
    @FXML private ComboBox<EventType> eventTypeFilter;
    @FXML private ComboBox<String> dateFilter;
    @FXML private Button searchButton;
    @FXML private Button backButton;
    @FXML private Button prevPageButton;
    @FXML private Button nextPageButton;
    @FXML private Label pageInfoLabel;

    @FXML private TableView<Event> eventsTable;
    @FXML private TableColumn<Event, String> eventNameColumn;
    @FXML private TableColumn<Event, LocalDateTime> dateColumn;
    @FXML private TableColumn<Event, String> locationColumn;
    @FXML private TableColumn<Event, EventType> typeColumn;
    @FXML private TableColumn<Event, Double> priceColumn;
    @FXML private TableColumn<Event, Integer> availableSlotsColumn;
    @FXML private TableColumn<Event, Void> actionsColumn;

    @Autowired private EventService eventService;
    @Autowired private TicketService ticketService;
    @Autowired private NavigationManager navigationManager;

    private static final int ITEMS_PER_PAGE = 10;
    private int currentPage = 0;
    private List<Event> allEvents;
    private ObservableList<Event> displayedEvents;
    private Participant currentParticipant;

    @FXML
    public void initialize() {
        setupUserInfo();
        setupFilters();
        setupTableColumns();
        loadEvents();
    }

    private void setupUserInfo() {
        User currentUser = SessionManager.getInstance().getCurrentUser();
        if (currentUser instanceof Participant) {
            currentParticipant = (Participant) currentUser;
        } else {
            showError("You must be logged in as a participant to browse events");
            try {
                navigationManager.navigateToLogin();
            } catch (IOException e) {
                log.error("Failed to navigate to login", e);
            }
        }
    }

    private void setupFilters() {
        // Setup event type filter
        eventTypeFilter.setItems(FXCollections.observableArrayList(EventType.values()));
        eventTypeFilter.getItems().add(0, null); // Add null for "All Types"
        eventTypeFilter.setValue(null);

        // Setup date filter
        dateFilter.setItems(FXCollections.observableArrayList(
            "All Dates",
            "Today",
            "This Week",
            "This Month",
            "Next 3 Months"
        ));
        dateFilter.setValue("All Dates");
    }

    private void setupTableColumns() {
        eventNameColumn.setCellValueFactory(new PropertyValueFactory<>("eventName"));
        dateColumn.setCellValueFactory(new PropertyValueFactory<>("date"));
        locationColumn.setCellValueFactory(new PropertyValueFactory<>("location"));
        typeColumn.setCellValueFactory(new PropertyValueFactory<>("eventType"));
        priceColumn.setCellValueFactory(new PropertyValueFactory<>("ticketPrice"));
        
        // Custom cell factory for available slots
        availableSlotsColumn.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(Integer item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || getTableRow() == null || getTableRow().getItem() == null) {
                    setText(null);
                    return;
                }
                Event event = getTableRow().getItem();
                int available = event.getQuota() - event.getCurrentParticipants();
                setText(String.valueOf(available));
            }
        });

        // Custom cell factory for actions
        actionsColumn.setCellFactory(col -> new TableCell<>() {
            private final Button buyButton = new Button("Buy Ticket");
            
            {
                buyButton.getStyleClass().add("button-primary");
                buyButton.setOnAction(e -> handleBuyTicket(getTableRow().getItem()));
            }
            
            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || getTableRow() == null || getTableRow().getItem() == null) {
                    setGraphic(null);
                    return;
                }
                
                Event event = getTableRow().getItem();
                boolean hasTicket = false;
                
                if (currentParticipant != null) {
                    hasTicket = ticketService.findByParticipantId(currentParticipant.getUserId()).stream()
                        .anyMatch(ticket -> ticket.getEvent().getEventId().equals(event.getEventId()));
                }
                
                if (hasTicket) {
                    setGraphic(null); // Hide the button if participant already has a ticket
                } else {
                    HBox buttons = new HBox(8, buyButton);
                    setGraphic(buttons);
                }
            }
        });
    }

    private void loadEvents() {
        allEvents = eventService.findAll().stream()
            .filter(Event::isAvailable)
            .collect(Collectors.toList());
        
        displayedEvents = FXCollections.observableArrayList();
        updateDisplayedEvents();
    }

    private void updateDisplayedEvents() {
        int startIndex = currentPage * ITEMS_PER_PAGE;
        int endIndex = Math.min(startIndex + ITEMS_PER_PAGE, allEvents.size());
        
        displayedEvents.clear();
        if (startIndex < allEvents.size()) {
            displayedEvents.addAll(allEvents.subList(startIndex, endIndex));
        }
        
        eventsTable.setItems(displayedEvents);
        updatePaginationControls();
    }

    private void updatePaginationControls() {
        int totalPages = (int) Math.ceil((double) allEvents.size() / ITEMS_PER_PAGE);
        pageInfoLabel.setText(String.format("Page %d of %d", currentPage + 1, totalPages));
        
        prevPageButton.setDisable(currentPage == 0);
        nextPageButton.setDisable(currentPage >= totalPages - 1);
    }

    @FXML
    private void handleSearch() {
        String searchText = searchField.getText().toLowerCase();
        EventType selectedType = eventTypeFilter.getValue();
        String selectedDate = dateFilter.getValue();
        
        allEvents = eventService.findAll().stream()
            .filter(Event::isAvailable)
            .filter(event -> {
                // Search text filter
                if (!searchText.isEmpty() && 
                    !event.getEventName().toLowerCase().contains(searchText) &&
                    !event.getLocation().toLowerCase().contains(searchText)) {
                    return false;
                }
                
                // Event type filter
                if (selectedType != null && event.getEventType() != selectedType) {
                    return false;
                }
                
                // Date filter
                if (selectedDate != null && !selectedDate.equals("All Dates")) {
                    LocalDateTime now = LocalDateTime.now();
                    LocalDateTime eventDate = event.getDate();
                    
                    switch (selectedDate) {
                        case "Today":
                            if (!eventDate.toLocalDate().equals(now.toLocalDate())) {
                                return false;
                            }
                            break;
                        case "This Week":
                            if (eventDate.isBefore(now) || 
                                eventDate.isAfter(now.plusWeeks(1))) {
                                return false;
                            }
                            break;
                        case "This Month":
                            if (eventDate.getMonth() != now.getMonth() || 
                                eventDate.getYear() != now.getYear()) {
                                return false;
                            }
                            break;
                        case "Next 3 Months":
                            if (eventDate.isBefore(now) || 
                                eventDate.isAfter(now.plusMonths(3))) {
                                return false;
                            }
                            break;
                    }
                }
                
                return true;
            })
            .collect(Collectors.toList());
        
        currentPage = 0;
        updateDisplayedEvents();
    }

    @FXML
    private void handlePreviousPage() {
        if (currentPage > 0) {
            currentPage--;
            updateDisplayedEvents();
        }
    }

    @FXML
    private void handleNextPage() {
        int totalPages = (int) Math.ceil((double) allEvents.size() / ITEMS_PER_PAGE);
        if (currentPage < totalPages - 1) {
            currentPage++;
            updateDisplayedEvents();
        }
    }

    @FXML
    private void handleBack() {
        try {
            navigationManager.goBack();
        } catch (IOException e) {
            log.error("Failed to navigate back to dashboard", e);
            showError("Failed to navigate back to dashboard");
        }
    }

    private void handleBuyTicket(Event event) {
        if (event != null) {
            try {
                navigationManager.navigateToBuyTicket(event.getEventId());
            } catch (IOException e) {
                log.error("Failed to navigate to buy ticket", e);
                showError("Failed to open buy ticket page");
            }
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
} 