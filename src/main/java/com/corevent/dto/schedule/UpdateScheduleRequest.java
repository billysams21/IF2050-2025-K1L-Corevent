package com.corevent.dto.schedule;

import java.time.LocalDateTime;

public class UpdateScheduleRequest {
    private String newSchedule;
    private LocalDateTime newDate;
    private String reason;

    // Getters and Setters
    public String getNewSchedule() {
        return newSchedule;
    }

    public void setNewSchedule(String newSchedule) {
        this.newSchedule = newSchedule;
    }

    public LocalDateTime getNewDate() {
        return newDate;
    }

    public void setNewDate(LocalDateTime newDate) {
        this.newDate = newDate;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }
}
