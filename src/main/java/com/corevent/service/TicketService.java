package com.corevent.service;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Service;

import com.corevent.api.TicketApiClient;
import com.corevent.dto.event.BuyTicketRequest;
import com.corevent.dto.event.BuyTicketResponse;
import com.corevent.entity.Event;
import com.corevent.entity.Participant;
import com.corevent.entity.Ticket;
import com.corevent.repository.TicketRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class TicketService {

    private final TicketRepository ticketRepository;
    private final TicketApiClient ticketApiClient;
    private final EventService eventService;

    public Ticket save(Ticket ticket) {
        return ticketRepository.save(ticket);
    }

    public List<Ticket> findByEventId(String eventId) {
        return ticketRepository.findByEventId(eventId);
    }

    public List<Ticket> findByParticipantId(String participantId) {
        return ticketRepository.findByParticipantId(participantId);
    }

    public Ticket findByQrCode(String qrCode) {
        return ticketRepository.findByQrCode(qrCode).orElse(null);
    }

    public List<Ticket> findByEventIdAndStatus(String eventId, Ticket.TicketStatus status) {
        return ticketRepository.findByEventIdAndStatus(eventId, status);
    }

    public int countByEventId(String eventId) {
        return ticketRepository.countByEventId(eventId);
    }

    public BuyTicketResponse buyTicket(Participant participant, Event event, BuyTicketRequest request) {
        try {
            // Check if event is available
            if (!event.isAvailable()) {
                return new BuyTicketResponse(false, "Event is not available");
            }

            // Check if participant already has a ticket
            List<Ticket> existingTickets = findByParticipantId(participant.getUserId());
            if (existingTickets.stream().anyMatch(t -> t.getEvent().getEventId().equals(event.getEventId()))) {
                return new BuyTicketResponse(false, "You already have a ticket for this event");
            }

            // Create new ticket
            Ticket ticket = new Ticket();
            ticket.setEvent(event);
            ticket.setParticipant(participant);
            ticket.setPurchaseDate(LocalDateTime.now());
            ticket.generateQR();

            // Save ticket
            ticket = save(ticket);

            // Increment event participants
            event.incrementParticipants();
            eventService.update(event);

            // Try to sync with server
            try {
                BuyTicketResponse response = ticketApiClient.buyTicket(request).execute().body();
                if (response != null && response.isSuccess()) {
                    return response;
                }
            } catch (Exception e) {
                log.error("Failed to sync ticket with server", e);
            }

            return new BuyTicketResponse(true, "Ticket purchased successfully", ticket.getTicketId(), null);

        } catch (Exception e) {
            log.error("Failed to buy ticket", e);
            return new BuyTicketResponse(false, "Failed to buy ticket");
        }
    }

    public boolean validateTicket(String qrCode) {
        Ticket ticket = findByQrCode(qrCode);
        return ticket != null && ticket.validate();
    }

    public void markTicketAsUsed(String qrCode) {
        Ticket ticket = findByQrCode(qrCode);
        if (ticket != null) {
            ticket.markAsUsed();
            save(ticket);
        }
    }
}
