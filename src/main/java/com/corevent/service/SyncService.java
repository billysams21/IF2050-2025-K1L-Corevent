package com.corevent.service;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import com.corevent.api.EventApiClient;
import com.corevent.api.TicketApiClient;
import com.corevent.dto.event.CreateEventRequest;
import com.corevent.dto.event.UpdateEventRequest;
import com.corevent.entity.Event;
import com.corevent.entity.SyncQueue;
import com.corevent.repository.SyncQueueRepository;
import com.corevent.util.JsonUtil;
import com.corevent.util.NetworkUtil;
import com.corevent.util.PreferencesManager;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class SyncService {
  
    private final SyncQueueRepository syncQueueRepository;
    private final EventApiClient eventApiClient;
    private final TicketApiClient ticketApiClient;
  
    public void queueEventSync(Event event) {
        SyncQueue syncItem = new SyncQueue();
        syncItem.setEntityType("EVENT");
        syncItem.setEntityId(event.getEventId());
        syncItem.setOperation("CREATE");
        syncItem.setData(JsonUtil.toJson(event));
        syncItem.setCreatedAt(LocalDateTime.now());
        syncItem.setStatus(SyncQueue.SyncStatus.PENDING);
        
        syncQueueRepository.save(syncItem);
    }
    
    @Scheduled(fixedDelay = 300000) // Run every 5 minutes
    public void processSyncQueue() {
        if (!NetworkUtil.isOnline()) {
            return;
        }
        
        List<SyncQueue> pendingItems = syncQueueRepository
                .findByStatusOrderByCreatedAt(SyncQueue.SyncStatus.PENDING);
        
        for (SyncQueue item : pendingItems) {
            try {
                boolean success = syncItem(item);
                
                if (success) {
                    item.setStatus(SyncQueue.SyncStatus.COMPLETED);
                    item.setSyncedAt(LocalDateTime.now());
                } else {
                    item.setStatus(SyncQueue.SyncStatus.FAILED);
                    item.setRetryCount(item.getRetryCount() + 1);
                }
                
                syncQueueRepository.save(item);
                
            } catch (Exception e) {
                item.setStatus(SyncQueue.SyncStatus.FAILED);
                item.setErrorMessage(e.getMessage());
                item.setRetryCount(item.getRetryCount() + 1);
                syncQueueRepository.save(item);
            }
        }
        
        PreferencesManager.setLastSyncTime(System.currentTimeMillis());
    }
    
    private boolean syncItem(SyncQueue item) {
        switch (item.getEntityType()) {
            case "EVENT":
                return syncEvent(item);
            case "TICKET":
                return syncTicket(item);
            case "ATTENDANCE":
                return syncAttendance(item);
            default:
                return false;
        }
    }
    
    private boolean syncEvent(SyncQueue item) {
        try {
            Event event = JsonUtil.fromJson(item.getData(), Event.class);
            
            switch (item.getOperation()) {
                case "CREATE":
                    return eventApiClient.createEvent(toCreateEventRequest(event))
                                .execute().isSuccessful();
                case "UPDATE":
                    return eventApiClient.updateEvent(event.getEventId(), toUpdateEventRequest(event))
                                .execute().isSuccessful();
                case "DELETE":
                    return eventApiClient.deleteEvent(event.getEventId())
                                .execute().isSuccessful();
                default:
                    return false;
            }
        } catch (Exception e) {
            log.error("Error syncing event: {}", e.getMessage());
            return false;
        }
    }
    
    private boolean syncTicket(SyncQueue item) {
        // TODO: Implement ticket sync
        return true;
    }
    
    private boolean syncAttendance(SyncQueue item) {
        // TODO: Implement attendance sync
        return true;
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
    
    private UpdateEventRequest toUpdateEventRequest(Event event) {
        return new UpdateEventRequest(
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
