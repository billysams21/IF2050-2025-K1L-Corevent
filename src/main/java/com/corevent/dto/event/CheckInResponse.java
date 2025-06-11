package com.corevent.dto.event;

public class CheckInResponse {
  private boolean success;
  private String message;
  private String participantName;
  private String eventName;
  private String checkInTime;
  
  public CheckInResponse() {}
  
  public CheckInResponse(boolean success, String message) {
    this.success = success;
    this.message = message;
  }
  
  public CheckInResponse(boolean success, String message, String participantName, String eventName) {
    this.success = success;
    this.message = message;
    this.participantName = participantName;
    this.eventName = eventName;
  }
  
  // Getters and Setters
  public boolean isSuccess() { return success; }
  public void setSuccess(boolean success) { this.success = success; }
  
  public String getMessage() { return message; }
  public void setMessage(String message) { this.message = message; }
  
  public String getParticipantName() { return participantName; }
  public void setParticipantName(String participantName) { this.participantName = participantName; }
  
  public String getEventName() { return eventName; }
  public void setEventName(String eventName) { this.eventName = eventName; }
}