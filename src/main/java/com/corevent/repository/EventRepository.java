package com.corevent.repository;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.corevent.entity.Event;

@Repository
public interface EventRepository extends JpaRepository<Event, String> {
  List<Event> findByDateAfter(LocalDateTime date);
  
  List<Event> findByDateBefore(LocalDateTime date);
  
  List<Event> findByDateBetween(LocalDateTime startDate, LocalDateTime endDate);
  
  List<Event> findByLocation(String location);
  
  List<Event> findByLocationAndDate(String location, LocalDateTime date);
  
  @Query("SELECT e FROM Event e WHERE e.location = :location AND e.date BETWEEN :startDate AND :endDate")
  List<Event> findByLocationAndDateBetween(@Param("location") String location, 
                                          @Param("startDate") LocalDateTime startDate, 
                                          @Param("endDate") LocalDateTime endDate);
  
  @Query("SELECT e FROM Event e JOIN e.committees c WHERE c.id = :committeeId")
  List<Event> findByCommitteeId(@Param("committeeId") String committeeId);
  
  @Query("SELECT e FROM Event e WHERE e.eventType = :eventType")
  List<Event> findByEventType(@Param("eventType") Event.EventType eventType);
  
  @Query("SELECT e FROM Event e WHERE e.currentParticipants < e.quota")
  List<Event> findAvailableEvents();
  
  @Query("SELECT e FROM Event e JOIN e.committees c WHERE c.id = :userId")
  List<Event> findByCommitteesUserId(@Param("userId") String userId);
}