package com.corevent.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class EvaluationData {
    
    @NotNull(message = "Event ID is required")
    private String eventId;
    
    @NotNull(message = "Participant ID is required") 
    private Long participantId;
    
    @NotNull(message = "Score is required")
    @Min(value = 1, message = "Score must be between 1 and 5")
    @Max(value = 5, message = "Score must be between 1 and 5")
    private Integer score;
    
    @Size(max = 500, message = "Feedback must not exceed 500 characters")
    private String feedback;
} 