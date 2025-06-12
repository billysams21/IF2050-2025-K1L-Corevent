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
@Table(name = "payments")
public class Payment {
  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  private String paymentId;
  
  @ManyToOne
  @JoinColumn(name = "participant_id", nullable = false)
  private Participant participant;
  
  @ManyToOne
  @JoinColumn(name = "event_id", nullable = false)
  private Event event;
  
  @Column(nullable = false)
  private Double amount;
  
  @Enumerated(EnumType.STRING)
  private PaymentMethod paymentMethod;
  
  @Enumerated(EnumType.STRING)
  private PaymentStatus status = PaymentStatus.WAITING;
  
  @Column(nullable = false)
  private LocalDateTime timestamp;
  
  private String transactionReference;
  
  private String paymentProof;
  
  private LocalDateTime confirmedAt;
  
  private String confirmedBy;
  
  // Business Logic Methods
  public void makePayment() {
    this.timestamp = LocalDateTime.now();
    this.status = PaymentStatus.WAITING;
  }
  
  public void confirmPayment(String confirmedBy) {
    if (this.status == PaymentStatus.WAITING) {
      this.status = PaymentStatus.PAID;
      this.confirmedAt = LocalDateTime.now();
      this.confirmedBy = confirmedBy;
    }
  }
  
  public boolean checkPaymentStatus() {
    return this.status == PaymentStatus.PAID;
  }
  
  public PaymentDetails getPaymentDetails() {
    return new PaymentDetails(
      paymentId, 
      amount, 
      paymentMethod, 
      status, 
      timestamp,
      transactionReference
    );
  }
  
  // Getters and Setters
  public String getPaymentId() { return paymentId; }
  public void setPaymentId(String paymentId) { this.paymentId = paymentId; }
  
  public Participant getParticipant() { return participant; }
  public void setParticipant(Participant participant) { this.participant = participant; }
  
  public Event getEvent() { return event; }
  public void setEvent(Event event) { this.event = event; }
  
  public Double getAmount() { return amount; }
  public void setAmount(Double amount) { this.amount = amount; }
  
  public PaymentMethod getPaymentMethod() { return paymentMethod; }
  public void setPaymentMethod(PaymentMethod paymentMethod) { this.paymentMethod = paymentMethod; }
  
  public PaymentStatus getStatus() { return status; }
  public void setStatus(PaymentStatus status) { this.status = status; }
  
  public LocalDateTime getTimestamp() { return timestamp; }
  public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }
  
  public String getTransactionReference() { return transactionReference; }
  public void setTransactionReference(String transactionReference) { this.transactionReference = transactionReference; }
  
  // Enums
  public enum PaymentMethod {
    BANK_TRANSFER, E_WALLET, CREDIT_CARD, CASH
  }
  
  public enum PaymentStatus {
    WAITING, PAID, FAILED, CANCELLED, REFUNDED
  }
  
  // Inner class for payment details
  public static class PaymentDetails {
    public final String paymentId;
    public final Double amount;
    public final PaymentMethod method;
    public final PaymentStatus status;
    public final LocalDateTime timestamp;
    public final String reference;
    
    public PaymentDetails(String paymentId, Double amount, PaymentMethod method,
                        PaymentStatus status, LocalDateTime timestamp, String reference) {
      this.paymentId = paymentId;
      this.amount = amount;
      this.method = method;
      this.status = status;
      this.timestamp = timestamp;
      this.reference = reference;
    }
  }
}