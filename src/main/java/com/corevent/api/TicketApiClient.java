package com.corevent.api;

import java.util.List;

import org.springframework.stereotype.Component;

import com.corevent.dto.event.BuyTicketRequest;
import com.corevent.dto.event.BuyTicketResponse;
import com.corevent.dto.event.CheckInResponse;
import com.corevent.entity.Ticket;

import jakarta.annotation.PostConstruct;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Path;
import retrofit2.http.Query;

@Component
public class TicketApiClient extends BaseApiClient {
    
    private TicketApi ticketApi;
    
    @PostConstruct
    public void init() {
        retrofit = createRetrofit();
        ticketApi = retrofit.create(TicketApi.class);
    }
    
    public Call<BuyTicketResponse> buyTicket(BuyTicketRequest request) {
        return ticketApi.buyTicket(request);
    }
    
    public Call<CheckInResponse> checkIn(String qrCode) {
        return ticketApi.checkIn(qrCode);
    }
    
    public Call<Ticket> getTicket(String ticketId) {
        return ticketApi.getTicket(ticketId);
    }
    
    public Call<List<Ticket>> getParticipantTickets(String participantId) {
        return ticketApi.getParticipantTickets(participantId);
    }
    
    // API Interface
    public interface TicketApi {
        @POST("/api/tickets/buy")
        Call<BuyTicketResponse> buyTicket(@Body BuyTicketRequest request);
        
        @POST("/api/tickets/checkin")
        Call<CheckInResponse> checkIn(@Query("qrCode") String qrCode);
        
        @GET("/api/tickets/{ticketId}")
        Call<Ticket> getTicket(@Path("ticketId") String ticketId);
        
        @GET("/api/participants/{participantId}/tickets")
        Call<List<Ticket>> getParticipantTickets(@Path("participantId") String participantId);
    }
}