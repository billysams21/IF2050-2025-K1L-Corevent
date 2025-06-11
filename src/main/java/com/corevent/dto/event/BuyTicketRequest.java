package com.corevent.dto.ticket;

import com.corevent.entity.Payment;

public class BuyTicketRequest {
  private String eventId;
  private Payment.PaymentMethod paymentMethod;
  private String paymentDetails;
  
  // Getters and Setters
  public String getEventId() { return eventId; }
  public void setEventId(String eventId) { this.eventId = eventId; }
  
  public Payment.PaymentMethod getPaymentMethod() { return paymentMethod; }
  public void setPaymentMethod(Payment.PaymentMethod paymentMethod) { 
    this.paymentMethod = paymentMethod; 
  }
  
  public String getPaymentDetails() { return paymentDetails; }
  public void setPaymentDetails(String paymentDetails) { this.paymentDetails = paymentDetails; }
}