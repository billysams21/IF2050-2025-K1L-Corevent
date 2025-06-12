package com.corevent.entity;

import java.time.LocalDateTime;
import jakarta.persistence.*;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "evaluations", 
       uniqueConstraints = @UniqueConstraint(columnNames = {"event_id", "participant_id"}))
public class Evaluation {
    
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "evaluation_id")
    private String evaluationID;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "event_id", nullable = false)
    private Event event;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "participant_id", nullable = false)
    private Participant participant;
    
    @Min(value = 1, message = "Score must be between 1 and 5")
    @Max(value = 5, message = "Score must be between 1 and 5")
    @Column(nullable = false)
    private Integer score;
    
    @Size(max = 500, message = "Feedback must not exceed 500 characters")
    @Column(length = 500)
    private String feedback;
    
    @Column(name = "submitted_at", nullable = false)
    private LocalDateTime submittedAt;
    
    @PrePersist
    protected void onCreate() {
        submittedAt = LocalDateTime.now();
    }
    
    // Business Logic Methods
    public void submit(Integer score, String feedback) {
        this.score = score;
        this.feedback = feedback;
        this.submittedAt = LocalDateTime.now();
    }
    
    // Helper methods for UI display
    public String getEventName() {
        return event != null ? event.getEventName() : "";
    }
    
    public String getParticipantName() {
        return participant != null ? participant.getFullName() : "";
    }
}
