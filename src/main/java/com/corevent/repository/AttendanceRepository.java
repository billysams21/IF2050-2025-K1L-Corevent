package com.corevent.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.corevent.entity.Attendance;

@Repository
public interface AttendanceRepository extends JpaRepository<Attendance, String> {
    
    @Query("SELECT a FROM Attendance a WHERE a.event.eventId = :eventId AND a.participant.id = :participantId")
    Optional<Attendance> findByEventIdAndParticipantId(@Param("eventId") String eventId, @Param("participantId") Long participantId);
    
    @Query("SELECT a FROM Attendance a WHERE a.event.eventId = :eventId")
    List<Attendance> findByEventId(@Param("eventId") String eventId);
    
    @Query("SELECT a FROM Attendance a WHERE a.participant.id = :participantId")
    List<Attendance> findByParticipantId(@Param("participantId") Long participantId);
    
    @Query("SELECT COUNT(a) FROM Attendance a WHERE a.event.eventId = :eventId")
    long countByEventId(@Param("eventId") String eventId);
    
    @Query("SELECT COUNT(a) FROM Attendance a WHERE a.event.eventId = :eventId AND a.status = :status")
    long countByEventIdAndStatus(@Param("eventId") String eventId, @Param("status") Attendance.AttendanceStatus status);
    
    default long countPresentByEventId(String eventId) {
        return countByEventIdAndStatus(eventId, Attendance.AttendanceStatus.PRESENT);
    }
    
    @Modifying
    @Query("DELETE FROM Attendance a WHERE a.event.eventId = :eventId")
    void deleteByEventId(@Param("eventId") String eventId);
} 