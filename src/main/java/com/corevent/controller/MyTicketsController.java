package com.corevent.controller;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;

import com.corevent.entity.Event;
import com.corevent.entity.Participant;
import com.corevent.entity.Ticket;
import com.corevent.entity.Ticket.TicketStatus;
import com.corevent.entity.User;
import com.corevent.service.TicketService;
import com.corevent.util.NavigationManager;
import com.corevent.util.SessionManager;

import javafx.application.Platform;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
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
public class MyTicketsController {

    @FXML private TextField searchField;
    @FXML private ComboBox<TicketStatus> statusFilter;
    @FXML private Button searchButton;
    @FXML private Button backButton;
    @FXML private Button prevPageButton;
    @FXML private Button nextPageButton;
    @FXML private Label pageInfoLabel;

    @FXML private TableView<Ticket> ticketsTable;
    @FXML private TableColumn<Ticket, String> eventNameColumn;
    @FXML private TableColumn<Ticket, LocalDateTime> dateColumn;
    @FXML private TableColumn<Ticket, String> locationColumn;
    @FXML private TableColumn<Ticket, TicketStatus> ticketStatusColumn;
    @FXML private TableColumn<Ticket, LocalDateTime> purchaseDateColumn;
    @FXML private TableColumn<Ticket, String> qrCodeColumn;
    @FXML private TableColumn<Ticket, Void> actionsColumn;

    @Autowired private TicketService ticketService;
    @Autowired private NavigationManager navigationManager;

    private static final int ITEMS_PER_PAGE = 10;
    private int currentPage = 0;
    private List<Ticket> allTickets;
    private ObservableList<Ticket> displayedTickets;
    private Participant participant;

    @FXML
    public void initialize() {
        setupFilters();
        setupTableColumns();
        loadParticipantTickets();
    }

    private void setupFilters() {
        // Setup status filter
        statusFilter.setItems(FXCollections.observableArrayList(TicketStatus.values()));
        statusFilter.getItems().add(0, null); // Add null for "All Statuses"
        statusFilter.setValue(null);
    }

    private void setupTableColumns() {
        eventNameColumn.setCellValueFactory(cellData -> 
            new SimpleStringProperty(cellData.getValue().getEvent().getEventName()));
        dateColumn.setCellValueFactory(cellData -> 
            new SimpleObjectProperty<>(cellData.getValue().getEvent().getDate()));
        locationColumn.setCellValueFactory(cellData -> 
            new SimpleStringProperty(cellData.getValue().getEvent().getLocation()));
        ticketStatusColumn.setCellValueFactory(new PropertyValueFactory<>("status"));
        purchaseDateColumn.setCellValueFactory(new PropertyValueFactory<>("purchaseDate"));
        qrCodeColumn.setCellValueFactory(new PropertyValueFactory<>("qrCode"));

        // Custom cell factory for actions
        actionsColumn.setCellFactory(col -> new TableCell<>() {
            private final Button downloadButton = new Button("Download QR");
            
            {
                downloadButton.getStyleClass().add("button-secondary");
                downloadButton.setOnAction(e -> handleDownloadQR(getTableRow().getItem()));
            }
            
            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || getTableRow() == null || getTableRow().getItem() == null) {
                    setGraphic(null);
                    return;
                }
                
                HBox buttons = new HBox(8, downloadButton);
                setGraphic(buttons);
            }
        });
    }

    private void loadParticipantTickets() {
        User currentUser = SessionManager.getInstance().getCurrentUser();
        if (currentUser instanceof Participant) {
            participant = (Participant) currentUser;
            allTickets = ticketService.findByParticipantId(participant.getUserId());
            displayedTickets = FXCollections.observableArrayList();
            updateDisplayedTickets();
        } else {
            showError("You must be logged in as a participant to view tickets");
            try {
                navigationManager.navigateToParticipantDashboard();
            } catch (IOException e) {
                showError("Failed to navigate back to dashboard");
            }
        }
    }

    private void updateDisplayedTickets() {
        int startIndex = currentPage * ITEMS_PER_PAGE;
        int endIndex = Math.min(startIndex + ITEMS_PER_PAGE, allTickets.size());
        
        displayedTickets.clear();
        if (startIndex < allTickets.size()) {
            displayedTickets.addAll(allTickets.subList(startIndex, endIndex));
        }
        
        ticketsTable.setItems(displayedTickets);
        updatePaginationControls();
    }

    private void updatePaginationControls() {
        int totalPages = (int) Math.ceil((double) allTickets.size() / ITEMS_PER_PAGE);
        pageInfoLabel.setText(String.format("Page %d of %d", currentPage + 1, totalPages));
        
        prevPageButton.setDisable(currentPage == 0);
        nextPageButton.setDisable(currentPage >= totalPages - 1);
    }

    @FXML
    private void handleSearch() {
        String searchText = searchField.getText().toLowerCase();
        TicketStatus selectedStatus = statusFilter.getValue();
        
        allTickets = ticketService.findByParticipantId(participant.getUserId()).stream()
            .filter(ticket -> {
                // Search text filter
                if (!searchText.isEmpty()) {
                    Event event = ticket.getEvent();
                    if (!event.getEventName().toLowerCase().contains(searchText) &&
                        !event.getLocation().toLowerCase().contains(searchText)) {
                        return false;
                    }
                }
                
                // Status filter
                if (selectedStatus != null && ticket.getStatus() != selectedStatus) {
                    return false;
                }
                
                return true;
            })
            .collect(Collectors.toList());
        
        currentPage = 0;
        updateDisplayedTickets();
    }

    @FXML
    private void handlePreviousPage() {
        if (currentPage > 0) {
            currentPage--;
            updateDisplayedTickets();
        }
    }

    @FXML
    private void handleNextPage() {
        int totalPages = (int) Math.ceil((double) allTickets.size() / ITEMS_PER_PAGE);
        if (currentPage < totalPages - 1) {
            currentPage++;
            updateDisplayedTickets();
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

    private void handleViewEvent(Ticket ticket) {
        if (ticket != null) {
            try {
                navigationManager.navigateToEventDetails(ticket.getEvent().getEventId());
            } catch (IOException e) {
                log.error("Failed to navigate to event details", e);
                showError("Failed to open event details");
            }
        }
    }

    private void handleDownloadQR(Ticket ticket) {
        if (ticket != null && ticket.getQrCode() != null) {
            // TODO: Implement QR code download functionality
            showError("QR code download not implemented yet");
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