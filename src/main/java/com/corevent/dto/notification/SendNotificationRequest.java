package com.corevent.dto.notification;

import java.util.List;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;

public class SendNotificationRequest {
    
    @NotBlank(message = "Event ID is required")
    private String eventID;
    
    @NotBlank(message = "Title is required")
    @Size(max = 255, message = "Title must not exceed 255 characters")
    private String title;
    
    @NotBlank(message = "Message is required")
    @Size(max = 1000, message = "Message must not exceed 1000 characters")
    private String message;
    
    @NotEmpty(message = "Participant list cannot be empty")
    private List<String> participantList;
    
    public SendNotificationRequest() {}
    
    public SendNotificationRequest(String eventID, String title, String message, List<String> participantList) {
        this.eventID = eventID;
        this.title = title;
        this.message = message;
        this.participantList = participantList;
    }
    
    // Getters and Setters
    public String getEventID() {
        return eventID;
    }
    
    public void setEventID(String eventID) {
        this.eventID = eventID;
    }
    
    public String getTitle() {
        return title;
    }
    
    public void setTitle(String title) {
        this.title = title;
    }
    
    public String getMessage() {
        return message;
    }
    
    public void setMessage(String message) {
        this.message = message;
    }
    
    public List<String> getParticipantList() {
        return participantList;
    }
    
    public void setParticipantList(List<String> participantList) {
        this.participantList = participantList;
    }
} 