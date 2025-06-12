package com.corevent.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.corevent.repository.*;
import com.corevent.api.EventApiClient;
import com.corevent.dto.event.CreateEventRequest;
import com.corevent.entity.Attendance;
import com.corevent.entity.Event;
import com.corevent.entity.Participant;
import com.corevent.entity.ParticipantInfo;
import com.corevent.entity.Ticket;

@Service
@Transactional
public class EventService {
  
  @Autowired
  private EventRepository eventRepository;
  
  @Autowired
  private TicketRepository ticketRepository;
  
  @Autowired
  private AttendanceRepository attendanceRepository;
  
  @Autowired
  private EventApiClient eventApiClient;
  
  @Autowired
  private SyncService syncService;
  
  public Event save(Event event) {
    Event savedEvent = eventRepository.save(event);
    
    // Try to sync with server
    CompletableFuture.runAsync(() -> {
      try {
        eventApiClient.createEvent(toCreateEventRequest(event)).execute();
      } catch (Exception e) {
        // Queue for later sync
        syncService.queueEventSync(savedEvent);
      }
    });
    
    return savedEvent;
  }
  
  public Event update(Event event) {
    return eventRepository.save(event);
  }
  
  public Event findById(String eventId) {
    return eventRepository.findById(eventId).orElse(null);
  }
  
  public List<Event> findAll() {
    return eventRepository.findAll();
  }
  
  public List<Event> findUpcomingEvents() {
    return eventRepository.findByDateAfter(LocalDateTime.now());
  }
  
  public List<Event> getCommitteeEvents(String committeeId) {
    // Try to get from server first
    try {
      List<Event> serverEvents = eventApiClient.getCommitteeEvents(committeeId)
              .execute().body();
      if (serverEvents != null) {
        // Update local cache
        serverEvents.forEach(this::updateLocalCache);
        return serverEvents;
      }
    } catch (Exception e) {
      // Fall back to local data
    }
    
    return eventRepository.findByCommitteeId(committeeId);
  }
  
  public List<Participant> getEventParticipants(String eventId) {
    List<Ticket> tickets = ticketRepository.findByEventId(eventId);
    return tickets.stream()
            .map(Ticket::getParticipant)
            .distinct()
            .collect(Collectors.toList());
  }
  
  public List<ParticipantInfo> getParticipantInfoList(String eventId) {
    List<Ticket> tickets = ticketRepository.findByEventId(eventId);
    
    return tickets.stream().map(ticket -> {
      Participant p = ticket.getParticipant();
      Attendance attendance = attendanceRepository
              .findByEventIdAndParticipantId(eventId, p.getUserId())
              .orElse(null);
      
      return new ParticipantInfo(
        p.getUserId(),
        p.getFullName(),
        p.getEmail(),
        p.getPhoneNumber(),
        p.getInstitution(),
        ticket.getStatus().toString(),
        attendance != null ? attendance.getStatus().toString() : "NOT_CHECKED_IN",
        ticket.getPurchaseDate()
      );
    }).collect(Collectors.toList());
  }
  
  public boolean checkLocationAvailability(String location, LocalDateTime date) {
    List<Event> conflictingEvents = eventRepository
            .findByLocationAndDate(location, date);
    return conflictingEvents.isEmpty();
  }
  
  public EventStatistics getEventStatistics(String eventId) {
    Event event = findById(eventId);
    if (event == null) return null;
    
    int totalTickets = ticketRepository.countByEventId(eventId);
    int checkedIn = attendanceRepository.countPresentByEventId(eventId);
    int evaluations = evaluationRepository.countByEventId(eventId);
    Double avgRating = evaluationRepository.getAverageRatingByEventId(eventId);
    
    return new EventStatistics(
        event.getEventName(),
        totalTickets,
        checkedIn,
        evaluations,
        avgRating != null ? avgRating : 0.0
    );
  }
  
  private void updateLocalCache(Event event) {
    eventRepository.save(event);
  }
  
  private CreateEventRequest toCreateEventRequest(Event event) {
    CreateEventRequest request = new CreateEventRequest();
    request.setEventName(event.getEventName());
    request.setDate(event.getDate());
    request.setLocation(event.getLocation());
    request.setQuota(event.getQuota());
    request.setEventType(event.getEventType());
    request.setTicketPrice(event.getTicketPrice());
    request.setSchedule(event.getSchedule());
    request.setTermsAndConditions(event.getTermsAndConditions());
    return request;
  }
  
  // Inner class for statistics
  public static class EventStatistics {
    public final String eventName;
    public final int totalParticipants;
    public final int checkedIn;
    public final int evaluations;
    public final double averageRating;
    
    public EventStatistics(String eventName, int totalParticipants, 
                          int checkedIn, int evaluations, double averageRating) {
      this.eventName = eventName;
      this.totalParticipants = totalParticipants;
      this.checkedIn = checkedIn;
      this.evaluations = evaluations;
      this.averageRating = averageRating;
    }
  }
}