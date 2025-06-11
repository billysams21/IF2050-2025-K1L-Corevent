package com.corevent.dto.event;

import java.time.LocalDateTime;

import com.corevent.entity.Event;

public class CreateEventRequest {
  private String eventName;
  private LocalDateTime date;
  private String schedule;
  private String location;
  private Integer quota;
  private Event.EventType eventType;
  private Double ticketPrice;
  private String termsAndConditions;
  private String description;
  
  // Getters and Setters
  public String getEventName() { return eventName; }
  public void setEventName(String eventName) { this.eventName = eventName; }
  
  public LocalDateTime getDate() { return date; }
  public void setDate(LocalDateTime date) { this.date = date; }
  
  public String getSchedule() { return schedule; }
  public void setSchedule(String schedule) { this.schedule = schedule; }
  
  public String getLocation() { return location; }
  public void setLocation(String location) { this.location = location; }
  
  public Integer getQuota() { return quota; }
  public void setQuota(Integer quota) { this.quota = quota; }
  
  public Event.EventType getEventType() { return eventType; }
  public void setEventType(Event.EventType eventType) { this.eventType = eventType; }
  
  public Double getTicketPrice() { return ticketPrice; }
  public void setTicketPrice(Double ticketPrice) { this.ticketPrice = ticketPrice; }
  
  public String getTermsAndConditions() { return termsAndConditions; }
  public void setTermsAndConditions(String termsAndConditions) { 
    this.termsAndConditions = termsAndConditions; 
  }
  
  public String getDescription() { return description; }
  public void setDescription(String description) { this.description = description; }
}
