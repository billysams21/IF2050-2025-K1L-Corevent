package com.corevent.service;

import com.corevent.dto.EvaluationData;
import com.corevent.dto.SubmitResult;
import com.corevent.entity.Evaluation;
import com.corevent.entity.Event;
import com.corevent.entity.Participant;
import com.corevent.repository.EvaluationRepository;
import com.corevent.repository.EventRepository;
import com.corevent.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

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
     * Submit evaluation following DPPL algorithm (halaman 31-32)
     */
    public SubmitResult submitEvaluation(EvaluationData data) {
        try {
            // 1. Validasi Input
            if (data.getEventId() == null || data.getEventId().isEmpty()) {
                return SubmitResult.failure("Event ID tidak boleh kosong");
            }
            
            if (data.getParticipantId() == null) {
                return SubmitResult.failure("Participant ID tidak boleh kosong");
            }
            
            if (data.getScore() == null || data.getScore() < 1 || data.getScore() > 5) {
                return SubmitResult.failure("Score harus antara 1-5");
            }
            
            // 2. Validasi Business Logic
            Optional<Event> eventOpt = eventRepository.findById(data.getEventId());
            if (eventOpt.isEmpty()) {
                return SubmitResult.failure("Event tidak ditemukan");
            }
            
            Event event = eventOpt.get();
            
            // Check if event is completed
            if (!isEventCompleted(event)) {
                return SubmitResult.failure("Event belum selesai, evaluasi belum dapat diisi");
            }
            
            // Check if participant exists and is registered for the event
            Optional<Participant> participantOpt = userRepository.findById(data.getParticipantId())
                    .filter(user -> user instanceof Participant)
                    .map(user -> (Participant) user);
                    
            if (participantOpt.isEmpty()) {
                return SubmitResult.failure("Participant tidak ditemukan");
            }
            
            Participant participant = participantOpt.get();
            
            // Check if participant is registered for the event
            if (!isParticipantRegistered(participant, event)) {
                return SubmitResult.failure("Peserta tidak terdaftar dalam event ini");
            }
            
            // Check if evaluation already submitted
            if (hasSubmittedEvaluation(data.getParticipantId(), data.getEventId())) {
                return SubmitResult.failure("Evaluasi sudah pernah disubmit sebelumnya");
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
            return SubmitResult.failure("Terjadi kesalahan saat menyimpan evaluasi: " + e.getMessage());
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
        Object[] stats = evaluationRepository.getEvaluationStatistics(eventId);
        if (stats == null || stats.length == 0) {
            return new EvaluationStatistics(0.0, 0L, 0, 0);
        }
        
        Double averageScore = stats[0] != null ? (Double) stats[0] : 0.0;
        Long totalEvaluations = stats[1] != null ? (Long) stats[1] : 0L;
        Integer minScore = stats[2] != null ? (Integer) stats[2] : 0;
        Integer maxScore = stats[3] != null ? (Integer) stats[3] : 0;
        
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