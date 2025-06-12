package com.corevent.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.corevent.dto.EvaluationData;
import com.corevent.dto.SubmitResult;
import com.corevent.entity.Evaluation;
import com.corevent.entity.Event;
import com.corevent.entity.Participant;
import com.corevent.repository.EvaluationRepository;
import com.corevent.repository.EventRepository;
import com.corevent.repository.UserRepository;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@Transactional
public class EvaluationService {
    
    private final EvaluationRepository evaluationRepository;
    private final EventRepository eventRepository;
    private final UserRepository userRepository;
    
    public EvaluationService(EvaluationRepository evaluationRepository,
                           EventRepository eventRepository,
                           UserRepository userRepository) {
        this.evaluationRepository = evaluationRepository;
        this.eventRepository = eventRepository;
        this.userRepository = userRepository;
    }
    
    /**
     * Submit evaluation following DPPL algorithm (page 31-32)
     */
    public SubmitResult submitEvaluation(EvaluationData data) {
        try {
            // 1. Input Validation
            if (data.getEventId() == null || data.getEventId().isEmpty()) {
                return SubmitResult.failure("Event ID cannot be empty");
            }
            
            if (data.getParticipantId() == null) {
                return SubmitResult.failure("Participant ID cannot be empty");
            }
            
            if (data.getScore() == null || data.getScore() < 1 || data.getScore() > 5) {
                return SubmitResult.failure("Score must be between 1-5");
            }
            
            // 2. Business Logic Validation
            Optional<Event> eventOpt = eventRepository.findById(data.getEventId());
            if (eventOpt.isEmpty()) {
                return SubmitResult.failure("Event not found");
            }
            
            Event event = eventOpt.get();
            
            // Check if event is completed
            if (!isEventCompleted(event)) {
                return SubmitResult.failure("Event not yet completed, evaluation cannot be submitted");
            }
            
            // Check if participant exists and is registered for the event
            Optional<Participant> participantOpt = userRepository.findById(data.getParticipantId())
                    .filter(user -> user instanceof Participant)
                    .map(user -> (Participant) user);
                    
            if (participantOpt.isEmpty()) {
                return SubmitResult.failure("Participant not found");
            }
            
            Participant participant = participantOpt.get();
            
            // Check if participant is registered for the event
            if (!isParticipantRegistered(participant, event)) {
                return SubmitResult.failure("Participant is not registered for this event");
            }
            
            // Check if evaluation already submitted
            if (hasSubmittedEvaluation(data.getParticipantId(), data.getEventId())) {
                return SubmitResult.failure("Evaluation has already been submitted previously");
            }
            
            // 3. Create and save evaluation
            Evaluation evaluation = new Evaluation();
            evaluation.setEvent(event);
            evaluation.setParticipant(participant);
            evaluation.submit(data.getScore(), data.getFeedback());
            
            Evaluation savedEvaluation = evaluationRepository.save(evaluation);
            
            log.info("Evaluation submitted successfully for event {} by participant {}", 
                    data.getEventId(), data.getParticipantId());
            
            return SubmitResult.success(savedEvaluation.getEvaluationID());
            
        } catch (Exception e) {
            log.error("Error submitting evaluation", e);
            return SubmitResult.failure("An error occurred while saving evaluation: " + e.getMessage());
        }
    }
    
    /**
     * Get all evaluations for a specific event
     */
    @Transactional(readOnly = true)
    public List<Evaluation> getEventEvaluations(String eventId) {
        return evaluationRepository.findByEventId(eventId);
    }
    
    /**
     * Get all evaluations submitted by a participant
     */
    @Transactional(readOnly = true)
    public List<Evaluation> getParticipantEvaluations(Long participantId) {
        return evaluationRepository.findByParticipantId(participantId);
    }
    
    /**
     * Check if participant has submitted evaluation for an event
     */
    @Transactional(readOnly = true)
    public boolean hasSubmittedEvaluation(Long participantId, String eventId) {
        return evaluationRepository.existsByEventEventIdAndParticipantId(eventId, participantId);
    }
    
    /**
     * Get evaluation by event and participant
     */
    @Transactional(readOnly = true)
    public Optional<Evaluation> getEvaluation(String eventId, Long participantId) {
        return evaluationRepository.findByEventEventIdAndParticipantId(eventId, participantId);
    }
    
    /**
     * Calculate average score for an event (Q-005)
     */
    @Transactional(readOnly = true)
    public Double getEventAverageScore(String eventId) {
        Double average = evaluationRepository.getAverageScoreByEventId(eventId);
        return average != null ? average : 0.0;
    }
    
    /**
     * Get evaluation statistics for an event
     */
    @Transactional(readOnly = true)
    public EvaluationStatistics getEvaluationStatistics(String eventId) {
        List<Object[]> results = evaluationRepository.getEvaluationStatistics(eventId);
    
        if (results == null || results.isEmpty()) {
            return new EvaluationStatistics(0.0, 0L, 0, 0);
        }
        
        Object[] stats = results.get(0);
        
        if (stats == null || stats.length == 0 || stats[0] == null) {
            return new EvaluationStatistics(0.0, 0L, 0, 0);
        }
        
        Double averageScore = ((Number) stats[0]).doubleValue();
        Long totalEvaluations = ((Number) stats[1]).longValue();
        Integer minScore = ((Number) stats[2]).intValue();
        Integer maxScore = ((Number) stats[3]).intValue();
        
        return new EvaluationStatistics(averageScore, totalEvaluations, minScore, maxScore);
    }

    /**
     * Get events with average evaluation scores for committee (Q-010)
     */
    @Transactional(readOnly = true)
    public List<Object[]> getEventsWithAverageScore(Long committeeId) {
        return evaluationRepository.getEventsWithAverageScore(committeeId);
    }
    
    // Private helper methods
    private boolean isEventCompleted(Event event) {
        return LocalDateTime.now().isAfter(event.getDate());
    }
    
    private boolean isParticipantRegistered(Participant participant, Event event) {
        // Check if participant has a ticket for this event
        // This would need to be implemented based on your ticket system
        // For now, we'll assume all participants can evaluate if event is completed
        return true; // TODO: Implement proper check with TicketService
    }
    
    /**
     * Record class for evaluation statistics
     */
    public record EvaluationStatistics(
            Double averageScore,
            Long totalEvaluations, 
            Integer minScore,
            Integer maxScore
    ) {}
}