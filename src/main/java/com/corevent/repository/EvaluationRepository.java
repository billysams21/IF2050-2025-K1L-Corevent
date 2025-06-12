package com.corevent.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.corevent.entity.Evaluation;

@Repository
public interface EvaluationRepository extends JpaRepository<Evaluation, String> {
    
    /**
     * Find all evaluations for a specific event
     */
    @Query("SELECT e FROM Evaluation e JOIN FETCH e.participant WHERE e.event.eventId = :eventId")
    List<Evaluation> findByEventId(@Param("eventId") String eventId);
    
    /**
     * Find all evaluations submitted by a specific participant
     */
    @Query("SELECT e FROM Evaluation e JOIN FETCH e.event WHERE e.participant.id = :participantId")
    List<Evaluation> findByParticipantId(@Param("participantId") Long participantId);
    
    /**
     * Check if participant has already submitted evaluation for an event
     */
    boolean existsByEventEventIdAndParticipantId(String eventId, Long participantId);
    
    /**
     * Find evaluation by event and participant
     */
    Optional<Evaluation> findByEventEventIdAndParticipantId(String eventId, Long participantId);
    
    /**
     * Q-005: Calculate average score for an event
     */
    @Query("SELECT AVG(e.score) FROM Evaluation e WHERE e.event.eventId = :eventId")
    Double getAverageScoreByEventId(@Param("eventId") String eventId);
    
    /**
     * Q-010: Get events with their average evaluation scores for a committee
     */
    @Query("SELECT e.event.eventId, e.event.eventName, COALESCE(AVG(e.score), 0) as averageScore " +
           "FROM Evaluation e RIGHT JOIN e.event ev " +
           "WHERE ev.eventId IN (SELECT ec.eventId FROM Event ec JOIN ec.committees c WHERE c.id = :committeeId) " +
           "GROUP BY e.event.eventId, e.event.eventName")
    List<Object[]> getEventsWithAverageScore(@Param("committeeId") Long committeeId);
    
    /**
     * Count total evaluations for an event
     */
    @Query("SELECT COUNT(e) FROM Evaluation e WHERE e.event.eventId = :eventId")
    Long countByEventId(@Param("eventId") String eventId);
    
    /**
     * Get evaluation statistics for an event
     */
    @Query("SELECT " +
           "COALESCE(AVG(CAST(e.score AS double)), 0.0) as averageScore, " +
           "COUNT(e) as totalEvaluations, " +
           "COALESCE(MIN(e.score), 0) as minScore, " +
           "COALESCE(MAX(e.score), 0) as maxScore " +
           "FROM Evaluation e WHERE e.event.eventId = :eventId")
    List<Object[]> getEvaluationStatistics(@Param("eventId") String eventId);
} 