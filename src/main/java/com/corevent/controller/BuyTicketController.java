package com.corevent.controller;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;

import com.corevent.entity.Event;
import com.corevent.entity.Participant;
import com.corevent.entity.Payment;
import com.corevent.entity.Ticket;
import com.corevent.repository.EventRepository;
import com.corevent.repository.PaymentRepository;
import com.corevent.repository.TicketRepository;
import com.corevent.util.NavigationManager;

import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.stage.FileChooser;

@Controller
public class BuyTicketController {

    @FXML private Label eventNameLabel;
    @FXML private Label eventDateLabel;
    @FXML private Label eventLocationLabel;
    @FXML private Label eventPriceLabel;
    @FXML private Label availableSlotsLabel;
    @FXML private ComboBox<Payment.PaymentMethod> paymentMethodCombo;
    @FXML private TextField transactionRefField;
    @FXML private TextField paymentProofField;
    @FXML private Button uploadButton;
    @FXML private Button buyButton;
    @FXML private Button cancelButton;

    @Autowired private EventRepository eventRepository;
    @Autowired private PaymentRepository paymentRepository;
    @Autowired private TicketRepository ticketRepository;
    @Autowired private NavigationManager navigationManager;

    private Event event;
    private Participant participant;
    private File paymentProofFile;

    @FXML
    public void initialize() {
        // Initialize payment method combo box
        paymentMethodCombo.getItems().addAll(Payment.PaymentMethod.values());
        paymentMethodCombo.setValue(Payment.PaymentMethod.BANK_TRANSFER);
    }

    public void setEvent(Event event) {
        this.event = event;
        updateEventDetails();
    }

    public void setParticipant(Participant participant) {
        this.participant = participant;
    }

    private void updateEventDetails() {
        if (event != null) {
            eventNameLabel.setText(event.getEventName());
            eventDateLabel.setText(event.getDate().toString());
            eventLocationLabel.setText(event.getLocation());
            eventPriceLabel.setText(String.format("Rp %.2f", event.getTicketPrice()));
            availableSlotsLabel.setText(String.valueOf(event.getQuota() - event.getCurrentParticipants()));
        }
    }

    @FXML
    private void handleUploadProof() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Select Payment Proof");
        fileChooser.getExtensionFilters().addAll(
            new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.jpeg"),
            new FileChooser.ExtensionFilter("PDF Files", "*.pdf")
        );

        File file = fileChooser.showOpenDialog(uploadButton.getScene().getWindow());
        if (file != null) {
            paymentProofFile = file;
            paymentProofField.setText(file.getName());
        }
    }

    @FXML
    private void handleBuyTicket() {
        if (!validateInputs()) {
            return;
        }

        try {
            // Create payment
            Payment payment = new Payment();
            payment.setParticipant(participant);
            payment.setEvent(event);
            payment.setAmount(event.getTicketPrice());
            payment.setPaymentMethod(paymentMethodCombo.getValue());
            payment.setTransactionReference(transactionRefField.getText());
            payment.setTimestamp(LocalDateTime.now());
            payment.setStatus(Payment.PaymentStatus.WAITING);

            // Save payment proof
            if (paymentProofFile != null) {
                String proofPath = savePaymentProof(paymentProofFile);
                if (proofPath == null) {
                    showError("Failed to save payment proof.");
                    return;
                }
                payment.setPaymentProof(proofPath);
            }

            payment = paymentRepository.save(payment);

            // Create ticket
            Ticket ticket = new Ticket();
            ticket.setEvent(event);
            ticket.setParticipant(participant);
            ticket.setPayment(payment);
            ticket.setStatus(Ticket.TicketStatus.ACTIVE);
            ticket.generateQRCode();

            ticket = ticketRepository.save(ticket);

            // Update event capacity
            event.incrementParticipants();
            eventRepository.save(event);

            showSuccess("Ticket purchased successfully!");
            navigationManager.navigateToParticipantDashboard();

        } catch (Exception e) {
            showError("Failed to purchase ticket: " + e.getMessage());
        }
    }

    private boolean validateInputs() {
        if (paymentMethodCombo.getValue() == null) {
            showError("Please select a payment method");
            return false;
        }

        if (transactionRefField.getText().trim().isEmpty()) {
            showError("Please enter transaction reference");
            return false;
        }

        if (paymentProofFile == null) {
            showError("Please upload payment proof");
            return false;
        }

        return true;
    }

    private String savePaymentProof(File file) {
        try {
            String uploadDir = "uploads/payment_proofs";
            Path uploadPath = Paths.get(uploadDir);
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }

            String fileName = System.currentTimeMillis() + "_" + file.getName();
            Path targetPath = uploadPath.resolve(fileName);
            Files.copy(file.toPath(), targetPath);

            return targetPath.toString();
        } catch (IOException e) {
            return null;
        }
    }

    @FXML
    private void handleCancel() {
        try {
            navigationManager.navigateToParticipantDashboard();
        } catch (IOException e) {
            showError("Failed to cancel");
        }
    }

    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void showSuccess(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Success");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
} 