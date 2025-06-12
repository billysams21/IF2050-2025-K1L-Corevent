package com.corevent.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import org.springframework.stereotype.Service;

import com.corevent.api.EventApiClient;
import com.corevent.dto.event.CreateEventRequest;
import com.corevent.entity.Event;
import com.corevent.repository.AttendanceRepository;
import com.corevent.repository.EventRepository;
import com.corevent.repository.TicketRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class EventService {
  
    private final EventRepository eventRepository;
    private final TicketRepository ticketRepository;
    private final AttendanceRepository attendanceRepository;
    private final EventApiClient eventApiClient;
    private final SyncService syncService;
  
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
        return eventRepository.findByCommitteesUserId(committeeId);
    }
    
    public List<Event> findByCommitteesUserId(String userId) {
        return eventRepository.findByCommitteesUserId(userId);
    }
    
    public void deleteEvent(String eventId) {
        Event event = findById(eventId);
        if (event != null) {
            // Delete from local database
            eventRepository.deleteById(eventId);
            
            // Try to sync with server
            CompletableFuture.runAsync(() -> {
                try {
                    eventApiClient.deleteEvent(eventId).execute();
                } catch (Exception e) {
                    // Queue for later sync
                    syncService.queueEventSync(event);
                }
            });
        }
    }
    
    private CreateEventRequest toCreateEventRequest(Event event) {
        return new CreateEventRequest(
            event.getEventName(),
            event.getDate(),
            event.getLocation(),
            event.getQuota(),
            event.getEventType().name(),
            event.getTicketPrice(),
            event.getTermsAndConditions()
        );
    }
}