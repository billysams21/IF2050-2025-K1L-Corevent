package com.corevent.entity;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "sync_queue")
public class SyncQueue {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;
    
    @Column(nullable = false)
    private String entityType;
    
    @Column(nullable = false)
    private String entityId;
    
    @Column(nullable = false)
    private String operation;
    
    @Column(columnDefinition = "TEXT")
    private String data;
    
    @Column(nullable = false)
    private LocalDateTime createdAt;
    
    private LocalDateTime syncedAt;
    
    @Enumerated(EnumType.STRING)
    private SyncStatus status = SyncStatus.PENDING;
    
    private int retryCount = 0;
    
    private String errorMessage;
    
    // Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    
    public String getEntityType() { return entityType; }
    public void setEntityType(String entityType) { this.entityType = entityType; }
    
    public String getEntityId() { return entityId; }
    public void setEntityId(String entityId) { this.entityId = entityId; }
    
    public String getOperation() { return operation; }
    public void setOperation(String operation) { this.operation = operation; }
    
    public String getData() { return data; }
    public void setData(String data) { this.data = data; }
    
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    
    public LocalDateTime getSyncedAt() { return syncedAt; }
    public void setSyncedAt(LocalDateTime syncedAt) { this.syncedAt = syncedAt; }
    
    public SyncStatus getStatus() { return status; }
    public void setStatus(SyncStatus status) { this.status = status; }
    
    public int getRetryCount() { return retryCount; }
    public void setRetryCount(int retryCount) { this.retryCount = retryCount; }
    
    public String getErrorMessage() { return errorMessage; }
    public void setErrorMessage(String errorMessage) { this.errorMessage = errorMessage; }
    
    // Enum for Sync Status
    public enum SyncStatus {
        PENDING, COMPLETED, FAILED
    }
} 