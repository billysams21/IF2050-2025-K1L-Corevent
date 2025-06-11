package com.corevent.dto.event;

public class CreateEventResponse {
  private boolean success;
  private String message;
  private String eventId;
  
  public CreateEventResponse() {}
  
  public CreateEventResponse(boolean success, String message) {
    this.success = success;
    this.message = message;
  }
  
  public CreateEventResponse(boolean success, String message, String eventId) {
    this.success = success;
    this.message = message;
    this.eventId = eventId;
  }
  
  // Getters and Setters
  public boolean isSuccess() { return success; }
  public void setSuccess(boolean success) { this.success = success; }
  
  public String getMessage() { return message; }
  public void setMessage(String message) { this.message = message; }
  
  public String getEventId() { return eventId; }
  public void setEventId(String eventId) { this.eventId = eventId; }
}
