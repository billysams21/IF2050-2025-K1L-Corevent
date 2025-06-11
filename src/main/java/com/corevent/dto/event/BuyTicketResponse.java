package com.corevent.dto.event;

public class BuyTicketResponse {
  private boolean success;
  private String message;
  private String ticketId;
  private byte[] qrCodeImage;
  private String paymentReference;
  
  public BuyTicketResponse() {}
  
  public BuyTicketResponse(boolean success, String message) {
    this.success = success;
    this.message = message;
  }
  
  public BuyTicketResponse(boolean success, String message, String ticketId, byte[] qrCodeImage) {
    this.success = success;
    this.message = message;
    this.ticketId = ticketId;
    this.qrCodeImage = qrCodeImage;
  }
  
  // Getters and Setters
  public boolean isSuccess() { return success; }
  public void setSuccess(boolean success) { this.success = success; }
  
  public String getMessage() { return message; }
  public void setMessage(String message) { this.message = message; }
  
  public String getTicketId() { return ticketId; }
  public void setTicketId(String ticketId) { this.ticketId = ticketId; }
  
  public byte[] getQrCodeImage() { return qrCodeImage; }
  public void setQrCodeImage(byte[] qrCodeImage) { this.qrCodeImage = qrCodeImage; }
}