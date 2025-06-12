package com.corevent.dto.notification;

public class NotificationResult {
    
    private boolean success;
    private String message;
    private NotificationStatistics statistics;
    
    public NotificationResult() {}
    
    public NotificationResult(boolean success, String message) {
        this.success = success;
        this.message = message;
    }
    
    public NotificationResult(boolean success, NotificationStatistics statistics) {
        this.success = success;
        this.statistics = statistics;
        this.message = generateMessageFromStatistics(statistics);
    }
    
    private String generateMessageFromStatistics(NotificationStatistics stats) {
        if (stats.getTotal() == 0) {
            return "No notifications to send";
        }
        
        if (stats.getFailure() == 0) {
            return String.format("Successfully sent all %d notifications", stats.getSuccess());
        } else if (stats.getSuccess() == 0) {
            return String.format("Failed to send all %d notifications", stats.getFailure());
        } else {
            return String.format("Sent %d notifications successfully, %d failed", 
                stats.getSuccess(), stats.getFailure());
        }
    }
    
    // Getters and Setters
    public boolean isSuccess() {
        return success;
    }
    
    public void setSuccess(boolean success) {
        this.success = success;
    }
    
    public String getMessage() {
        return message;
    }
    
    public void setMessage(String message) {
        this.message = message;
    }
    
    public NotificationStatistics getStatistics() {
        return statistics;
    }
    
    public void setStatistics(NotificationStatistics statistics) {
        this.statistics = statistics;
    }
    
    // Inner class for statistics
    public static class NotificationStatistics {
        private int success;
        private int failure;
        private int total;
        
        public NotificationStatistics() {}
        
        public NotificationStatistics(int success, int failure, int total) {
            this.success = success;
            this.failure = failure;
            this.total = total;
        }
        
        // Getters and Setters
        public int getSuccess() {
            return success;
        }
        
        public void setSuccess(int success) {
            this.success = success;
        }
        
        public int getFailure() {
            return failure;
        }
        
        public void setFailure(int failure) {
            this.failure = failure;
        }
        
        public int getTotal() {
            return total;
        }
        
        public void setTotal(int total) {
            this.total = total;
        }
    }
} 