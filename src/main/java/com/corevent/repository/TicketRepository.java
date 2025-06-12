package com.corevent.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.corevent.entity.Ticket;

@Repository
public interface TicketRepository extends JpaRepository<Ticket, String> {
    @Query("SELECT t FROM Ticket t WHERE t.event.eventId = :eventId")
    List<Ticket> findByEventId(@Param("eventId") String eventId);

    @Query("SELECT t FROM Ticket t WHERE t.participant.id = :participantId")
    List<Ticket> findByParticipantId(@Param("participantId") String participantId);

    Optional<Ticket> findByQrCode(String qrCode);

    @Query("SELECT t FROM Ticket t WHERE t.event.eventId = :eventId AND t.status = :status")
    List<Ticket> findByEventIdAndStatus(@Param("eventId") String eventId,
            @Param("status") Ticket.TicketStatus status);

    @Query("SELECT COUNT(t) FROM Ticket t WHERE t.event.eventId = :eventId")
    int countByEventId(@Param("eventId") String eventId);
}
