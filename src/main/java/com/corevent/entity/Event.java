package main.java.com.corevent.entity;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.*;

@Entity
@Table(name = "events")
public class Event {
  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  private String eventId;
  
  @Column(nullable = false)
  private String eventName;
  
  @Column(nullable = false)
  private LocalDateTime date;
  
  @Column(columnDefinition = "TEXT")
  private String schedule;
  
  @Column(nullable = false)
  private String location;
  
  @Column(nullable = false)
  private Integer quota;
  
  @Column(nullable = false)
  private Integer currentParticipants = 0;
  
  @Enumerated(EnumType.STRING)
  private EventType eventType = EventType.FREE;
  
  private Double ticketPrice = 0.0;
  
  @Column(columnDefinition = "TEXT")
  private String termsAndConditions;
  
  // @OneToMany(mappedBy = "event", cascade = CascadeType.ALL)
  // private List<Ticket> tickets = new ArrayList<>();
  
  // @OneToMany(mappedBy = "event", cascade = CascadeType.ALL)
  // private List<Question> questions = new ArrayList<>();
  
  // @ManyToMany
  // @JoinTable(
  //     name = "event_committee",
  //     joinColumns = @JoinColumn(name = "event_id"),
  //     inverseJoinColumns = @JoinColumn(name = "committee_id")
  // )
  private List<Committee> committees = new ArrayList<>();
  
  // Business Logic Methods
  public boolean isFull() {
    return currentParticipants >= quota;
  }
  
  public boolean isAvailable() {
    return LocalDateTime.now().isBefore(date) && !isFull();
  }
  
  public void incrementParticipants() {
    if (!isFull()) {
      currentParticipants++;
    }
  }
  
  // Getters and Setters
  public String getEventId() { return eventId; }
  public void setEventId(String eventId) { this.eventId = eventId; }
  
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
  
  public EventType getEventType() { return eventType; }
  public void setEventType(EventType eventType) { this.eventType = eventType; }
  
  public Double getTicketPrice() { return ticketPrice; }
  public void setTicketPrice(Double ticketPrice) { this.ticketPrice = ticketPrice; }
  
  // Enum for Event Type
  public enum EventType {
    FREE, PAID
  }
}