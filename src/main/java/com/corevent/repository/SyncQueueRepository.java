package com.corevent.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.corevent.entity.SyncQueue;

@Repository
public interface SyncQueueRepository extends JpaRepository<SyncQueue, String> {
    List<SyncQueue> findByStatusOrderByCreatedAt(SyncQueue.SyncStatus status);
    List<SyncQueue> findByEntityTypeAndEntityId(String entityType, String entityId);
    List<SyncQueue> findByStatusAndRetryCountLessThan(SyncQueue.SyncStatus status, int maxRetries);
} 