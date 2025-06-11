package com.corevent.api;

import java.util.List;

import org.springframework.stereotype.Component;

import com.corevent.dto.*;
import com.corevent.entity.Event;

import jakarta.annotation.PostConstruct;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Path;

@Component
public class EventApiClient extends BaseApiClient {
    
    private EventApi eventApi;
    
    @PostConstruct
    public void init() {
        retrofit = createRetrofit();
        eventApi = retrofit.create(EventApi.class);
    }
    
    public Call<Event> createEvent(CreateEventRequest request) {
        return eventApi.createEvent(request);
    }
    
    public Call<Event> updateEvent(String eventId, UpdateEventRequest request) {
        return eventApi.updateEvent(eventId, request);
    }
    
    public Call<Event> getEvent(String eventId) {
        return eventApi.getEvent(eventId);
    }
    
    public Call<List<Event>> getCommitteeEvents(String committeeId) {
        return eventApi.getCommitteeEvents(committeeId);
    }
    
    public Call<List<ParticipantInfo>> getEventParticipants(String eventId) {
        return eventApi.getEventParticipants(eventId);
    }
    
    public Call<Void> deleteEvent(String eventId) {
        return eventApi.deleteEvent(eventId);
    }
    
    // API Interface
    public interface EventApi {
        @POST("/api/events")
        Call<Event> createEvent(@Body CreateEventRequest request);
        
        @PUT("/api/events/{eventId}")
        Call<Event> updateEvent(@Path("eventId") String eventId, 
                               @Body UpdateEventRequest request);
        
        @GET("/api/events/{eventId}")
        Call<Event> getEvent(@Path("eventId") String eventId);
        
        @GET("/api/committees/{committeeId}/events")
        Call<List<Event>> getCommitteeEvents(@Path("committeeId") String committeeId);
        
        @GET("/api/events/{eventId}/participants")
        Call<List<ParticipantInfo>> getEventParticipants(@Path("eventId") String eventId);
        
        @DELETE("/api/events/{eventId}")
        Call<Void> deleteEvent(@Path("eventId") String eventId);
    }
}