package com.corevent.entity;

import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;

@Entity
@Table(name = "committees")
public class Committee extends User {
  @Column(nullable = false)
  private String fullName;
  
  private String department;
  
  private String position;
  
  private String phoneNumber;
  
  @ManyToMany(mappedBy = "committees")
  private List<Event> managedEvents = new ArrayList<>();
  
  @OneToMany(mappedBy = "committee", cascade = CascadeType.ALL)
  private List<Notification> sentNotifications = new ArrayList<>();
  
  // Business Logic
  public boolean canManageEvent(String eventId) {
    return managedEvents.stream()
          .anyMatch(event -> event.getEventId().equals(eventId));
  }
    
  public int getTotalManagedEvents() {
    return managedEvents.size();
  }
  
  public int getActiveEvents() {
    return (int) managedEvents.stream()
          .filter(Event::isAvailable)
          .count();
  }
    
  // Getters and Setters
  public String getFullName() { return fullName; }
  public void setFullName(String fullName) { this.fullName = fullName; }
  
  public String getDepartment() { return department; }
  public void setDepartment(String department) { this.department = department; }
  
  public String getPosition() { return position; }
  public void setPosition(String position) { this.position = position; }
  
  public String getPhoneNumber() { return phoneNumber; }
  public void setPhoneNumber(String phoneNumber) { this.phoneNumber = phoneNumber; }
  
  public List<Event> getManagedEvents() { return managedEvents; }
  public void setManagedEvents(List<Event> managedEvents) { this.managedEvents = managedEvents; }

  public List<Notification> getSentNotifications() { return sentNotifications; }
  public void setSentNotifications(List<Notification> sentNotifications) { this.sentNotifications = sentNotifications; }
}