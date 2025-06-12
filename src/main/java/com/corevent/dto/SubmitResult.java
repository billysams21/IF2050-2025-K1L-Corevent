package com.corevent.dto;

import lombok.Data;
import lombok.AllArgsConstructor;

@Data
@AllArgsConstructor
public class SubmitResult {
    private boolean success;
    private String message;
    private String evaluationId;
    
    public static SubmitResult success(String evaluationId) {
        return new SubmitResult(true, "Evaluation submitted successfully", evaluationId);
    }
    
    public static SubmitResult failure(String message) {
        return new SubmitResult(false, message, null);
    }
} 