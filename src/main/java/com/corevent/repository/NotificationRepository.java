package com.corevent.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.corevent.entity.Notification;
import com.corevent.entity.Event;
import com.corevent.entity.Committee;
import com.corevent.entity.Participant;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {
    
    // Find notifications by event
    List<Notification> findByEvent(Event event);
    
    // Find notifications by committee
    List<Notification> findByCommittee(Committee committee);
    
    // Find notifications by participant
    List<Notification> findByParticipant(Participant participant);
    
    // Find by notification ID
    Optional<Notification> findByNotificationID(String notificationID);
    
    // Find notifications by status
    List<Notification> findByStatus(Notification.NotificationStatus status);
    
    // Find notifications by event and status
    List<Notification> findByEventAndStatus(Event event, Notification.NotificationStatus status);
    
    // Count notifications by status for specific event
    @Query("SELECT COUNT(n) FROM Notification n WHERE n.event = :event AND n.status = :status")
    long countByEventAndStatus(@Param("event") Event event, @Param("status") Notification.NotificationStatus status);
    
    // Get notification statistics for an event
    @Query("SELECT n.status, COUNT(n) FROM Notification n WHERE n.event = :event GROUP BY n.status")
    List<Object[]> getNotificationStatisticsByEvent(@Param("event") Event event);
} 