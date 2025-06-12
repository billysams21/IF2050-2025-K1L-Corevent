package com.corevent.service;

import java.time.LocalDateTime;

import org.springframework.stereotype.Service;

import com.corevent.entity.Attendance;
import com.corevent.entity.Event;
import com.corevent.entity.Participant;
import com.corevent.repository.AttendanceRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class AttendanceService {
    
    private final AttendanceRepository attendanceRepository;
    
    public Attendance save(Attendance attendance) {
        return attendanceRepository.save(attendance);
    }
    
    public Attendance findByEventIdAndParticipantId(String eventId, Long participantId) {
        return attendanceRepository.findByEventIdAndParticipantId(eventId, participantId)
            .orElse(null);
    }
    
    public int countPresentByEventId(String eventId) {
        return (int) attendanceRepository.countPresentByEventId(eventId);
    }
    
    public int countByEventId(String eventId) {
        return (int) attendanceRepository.countByEventId(eventId);
    }
    
    public Attendance checkIn(Participant participant, Event event) {
        Attendance attendance = findByEventIdAndParticipantId(event.getEventId(), participant.getId());
        
        if (attendance == null) {
            attendance = new Attendance();
            attendance.setEvent(event);
            attendance.setParticipant(participant);
        }
        
        attendance.setStatus(Attendance.AttendanceStatus.PRESENT);
        attendance.setCheckInTime(LocalDateTime.now());
        
        return save(attendance);
    }
    
    public Attendance markAbsent(Participant participant, Event event) {
        Attendance attendance = findByEventIdAndParticipantId(event.getEventId(), participant.getId());
        
        if (attendance == null) {
            attendance = new Attendance();
            attendance.setEvent(event);
            attendance.setParticipant(participant);
        }
        
        attendance.setStatus(Attendance.AttendanceStatus.ABSENT);
        
        return save(attendance);
    }
    
    public Attendance markLate(Participant participant, Event event) {
        Attendance attendance = findByEventIdAndParticipantId(event.getEventId(), participant.getId());
        
        if (attendance == null) {
            attendance = new Attendance();
            attendance.setEvent(event);
            attendance.setParticipant(participant);
        }
        
        attendance.setStatus(Attendance.AttendanceStatus.LATE);
        attendance.setCheckInTime(LocalDateTime.now());
        
        return save(attendance);
    }
    
    public void deleteByEventId(String eventId) {
        attendanceRepository.deleteByEventId(eventId);
    }
} 