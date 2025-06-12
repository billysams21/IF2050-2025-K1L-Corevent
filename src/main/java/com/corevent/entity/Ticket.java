package com.corevent.entity;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "tickets")
public class Ticket {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String ticketId;
    
    @ManyToOne
    @JoinColumn(name = "event_id", nullable = false)
    private Event event;
    
    @ManyToOne
    @JoinColumn(name = "participant_id", nullable = false)
    private Participant participant;
    
    @Column(unique = true)
    private String qrCode;
    
    @Enumerated(EnumType.STRING)
    private TicketStatus status = TicketStatus.ACTIVE;
    
    private LocalDateTime purchaseDate;
    
    private LocalDateTime usedDate;
    
    // Business Logic
    public void generateQR() {
        // Generate unique QR code
        this.qrCode = "COREVENT-" + event.getEventId() + "-" + 
                      participant.getUserId() + "-" + System.currentTimeMillis();
    }
    
    public boolean validate() {
        return status == TicketStatus.ACTIVE && 
               event.isAvailable() &&
               qrCode != null && !qrCode.isEmpty();
    }
    
    public void markAsUsed() {
        if (status == TicketStatus.ACTIVE) {
            status = TicketStatus.USED;
            usedDate = LocalDateTime.now();
        }
    }
    
    // Getters and Setters
    public String getTicketId() { return ticketId; }
    public void setTicketId(String ticketId) { this.ticketId = ticketId; }
    
    public Event getEvent() { return event; }
    public void setEvent(Event event) { this.event = event; }
    
    public Participant getParticipant() { return participant; }
    public void setParticipant(Participant participant) { this.participant = participant; }
    
    public String getQrCode() { return qrCode; }
    public void setQrCode(String qrCode) { this.qrCode = qrCode; }
    
    public TicketStatus getStatus() { return status; }
    public void setStatus(TicketStatus status) { this.status = status; }
    
    public LocalDateTime getPurchaseDate() { return purchaseDate; }
    public void setPurchaseDate(LocalDateTime purchaseDate) { this.purchaseDate = purchaseDate; }
    
    // Enum for Ticket Status
    public enum TicketStatus {
        ACTIVE, USED, CANCELLED
    }
}