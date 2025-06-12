package com.corevent.controller;

import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

import org.springframework.stereotype.Controller;

import com.corevent.entity.ParticipantInfo;
import com.corevent.entity.Ticket.TicketStatus;
import com.corevent.service.ExportService;
import com.corevent.service.ParticipantManagementService;
import com.corevent.util.NavigationManager;

import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.stage.FileChooser;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Controller
@RequiredArgsConstructor
public class ParticipantManagementViewController {
    
    @FXML private TableView<ParticipantInfo> participantsTable;
    @FXML private TableColumn<ParticipantInfo, String> participantIdColumn;
    @FXML private TableColumn<ParticipantInfo, String> fullNameColumn;
    @FXML private TableColumn<ParticipantInfo, String> emailColumn;
    @FXML private TableColumn<ParticipantInfo, String> phoneColumn;
    @FXML private TableColumn<ParticipantInfo, String> institutionColumn;
    @FXML private TableColumn<ParticipantInfo, String> ticketStatusColumn;
    @FXML private TableColumn<ParticipantInfo, String> attendanceStatusColumn;
    @FXML private TableColumn<ParticipantInfo, String> registrationDateColumn;
    @FXML private TableColumn<ParticipantInfo, String> paymentStatusColumn;
    @FXML private TableColumn<ParticipantInfo, String> amountPaidColumn;
    
    @FXML private ComboBox<TicketStatus> statusFilter;
    @FXML private Button refreshButton;
    @FXML private Button exportButton;
    
    @FXML private Label totalParticipantsLabel;
    @FXML private Label presentParticipantsLabel;
    @FXML private Label absentParticipantsLabel;
    
    private final ParticipantManagementService participantManagementService;
    private final ExportService exportService;
    private final NavigationManager navigationManager;
    
    private String currentEventId;
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
    
    @FXML
    public void initialize() {
        setupTableColumns();
        setupStatusFilter();
        setupEventHandlers();
    }
    
    private void setupTableColumns() {
        participantIdColumn.setCellValueFactory(cellData -> 
            new SimpleStringProperty(cellData.getValue().getParticipantId()));
        fullNameColumn.setCellValueFactory(cellData -> 
            new SimpleStringProperty(cellData.getValue().getFullName()));
        emailColumn.setCellValueFactory(cellData -> 
            new SimpleStringProperty(cellData.getValue().getEmail()));
        phoneColumn.setCellValueFactory(cellData -> 
            new SimpleStringProperty(cellData.getValue().getPhoneNumber()));
        institutionColumn.setCellValueFactory(cellData -> 
            new SimpleStringProperty(cellData.getValue().getInstitution()));
        ticketStatusColumn.setCellValueFactory(cellData -> 
            new SimpleStringProperty(cellData.getValue().getTicketStatus()));
        attendanceStatusColumn.setCellValueFactory(cellData -> 
            new SimpleStringProperty(cellData.getValue().getAttendanceStatus()));
        registrationDateColumn.setCellValueFactory(cellData -> {
            LocalDateTime date = cellData.getValue().getRegistrationDate();
            return new SimpleStringProperty(date != null ? date.format(DATE_FORMATTER) : "");
        });
        paymentStatusColumn.setCellValueFactory(cellData -> 
            new SimpleStringProperty(cellData.getValue().getPaymentStatus()));
        amountPaidColumn.setCellValueFactory(cellData -> {
            Double amount = cellData.getValue().getAmountPaid();
            return new SimpleStringProperty(amount != null ? String.format("%.2f", amount) : "0.00");
        });
    }
    
    private void setupStatusFilter() {
        statusFilter.setItems(FXCollections.observableArrayList(TicketStatus.values()));
        statusFilter.getItems().add(0, null); // Add null for "All" option
        statusFilter.setValue(null);
    }
    
    private void setupEventHandlers() {
        statusFilter.setOnAction(event -> loadParticipants());
        refreshButton.setOnAction(event -> loadParticipants());
        exportButton.setOnAction(event -> handleExport());
    }
    
    public void setEventId(String eventId) {
        this.currentEventId = eventId;
        loadParticipants();
    }
    
    @FXML
    public void loadParticipants() {
        if (currentEventId == null) return;
        
        List<ParticipantInfo> participants;
        if (statusFilter.getValue() != null) {
            participants = participantManagementService.getEventParticipantsByStatus(
                currentEventId, statusFilter.getValue());
        } else {
            participants = participantManagementService.getEventParticipants(currentEventId);
        }
        
        Platform.runLater(() -> {
            participantsTable.setItems(FXCollections.observableArrayList(participants));
            updateStatistics(participants);
        });
    }
    
    private void updateStatistics(List<ParticipantInfo> participants) {
        int total = participants.size();
        int present = (int) participants.stream()
            .filter(p -> "PRESENT".equals(p.getAttendanceStatus()))
            .count();
        int absent = (int) participants.stream()
            .filter(p -> "ABSENT".equals(p.getAttendanceStatus()))
            .count();
        
        totalParticipantsLabel.setText("Total Participants: " + total);
        presentParticipantsLabel.setText("Present: " + present);
        absentParticipantsLabel.setText("Absent: " + absent);
    }
    
    @FXML
    private void handleExport() {
        if (currentEventId == null) return;
        
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Export Participants");
        
        // Add file extension filters
        FileChooser.ExtensionFilter csvFilter = new FileChooser.ExtensionFilter("CSV Files", "*.csv");
        FileChooser.ExtensionFilter excelFilter = new FileChooser.ExtensionFilter("Excel Files", "*.xlsx");
        FileChooser.ExtensionFilter pdfFilter = new FileChooser.ExtensionFilter("PDF Files", "*.pdf");
        fileChooser.getExtensionFilters().addAll(csvFilter, excelFilter, pdfFilter);
        
        File file = fileChooser.showSaveDialog(participantsTable.getScene().getWindow());
        if (file == null) return;
        
        try {
            byte[] data;
            ExportService.ExportFormat format;
            
            String extension = file.getName().substring(file.getName().lastIndexOf('.') + 1).toLowerCase();
            switch (extension) {
                case "csv":
                    format = ExportService.ExportFormat.CSV;
                    break;
                case "xlsx":
                    format = ExportService.ExportFormat.EXCEL;
                    break;
                case "pdf":
                    format = ExportService.ExportFormat.PDF;
                    break;
                default:
                    throw new IllegalArgumentException("Unsupported file format: " + extension);
            }
            
            List<ParticipantInfo> participants = participantManagementService.getEventParticipants(currentEventId);
            
            switch (format) {
                case CSV:
                    data = exportService.exportToCSV(participants);
                    break;
                case EXCEL:
                    data = exportService.exportToExcel(participants);
                    break;
                case PDF:
                    data = exportService.exportToPDF(participants);
                    break;
                default:
                    throw new IllegalArgumentException("Unsupported export format: " + format);
            }
            
            // Write data to file
            java.nio.file.Files.write(file.toPath(), data);
            
            // Show success message
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Export Successful");
            alert.setHeaderText(null);
            alert.setContentText("Participants data has been exported successfully.");
            alert.showAndWait();
            
        } catch (Exception e) {
            log.error("Error exporting participants data", e);
            
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Export Failed");
            alert.setHeaderText(null);
            alert.setContentText("Failed to export participants data: " + e.getMessage());
            alert.showAndWait();
        }
    }
    
    @FXML
    private void handleBack() {
        try {
            navigationManager.goBack();
        } catch (IOException e) {
            log.error("Failed to navigate back to manage event", e);
        }
    }
} 