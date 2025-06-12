package com.corevent.util;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.corevent.entity.Committee;
import com.corevent.entity.Event;

public class LogManager {
  private static final Logger logger = LoggerFactory.getLogger(LogManager.class);
  private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
  
  public static void logEventCreation(Event event, Committee committee) {
    String message = String.format("Event created: %s (ID: %s) by %s at %s",
            event.getEventName(),
            event.getEventId(),
            committee.getFullName(),
            LocalDateTime.now().format(formatter)
    );
    logger.info(message);
  }
  
  public static void logCheckIn(String participantName, String eventName, boolean success) {
    if (success) {
      logger.info("Check-in successful: {} for event {}", participantName, eventName);
    } else {
      logger.warn("Check-in failed: {} for event {}", participantName, eventName);
    }
  }
  
  public static void logError(String operation, Exception e) {
    logger.error("Error in {}: {}", operation, e.getMessage(), e);
  }
  
  public static void logApiCall(String endpoint, boolean success, long responseTime) {
    if (success) {
      logger.debug("API call to {} successful. Response time: {}ms", endpoint, responseTime);
    } else {
      logger.warn("API call to {} failed. Response time: {}ms", endpoint, responseTime);
    }
  }
  
  public static void logSyncOperation(String entity, int recordCount, boolean success) {
    if (success) {
      logger.info("Sync successful for {}: {} records synchronized", entity, recordCount);
    } else {
      logger.error("Sync failed for {}: {} records pending", entity, recordCount);
    }
  }
}