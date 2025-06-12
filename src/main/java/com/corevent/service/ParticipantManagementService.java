package com.corevent.service;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.corevent.entity.Attendance;
import com.corevent.entity.Participant;
import com.corevent.entity.ParticipantInfo;
import com.corevent.entity.Ticket;
import com.corevent.repository.AttendanceRepository;
import com.corevent.repository.TicketRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class ParticipantManagementService {
    
    private final TicketRepository ticketRepository;
    private final AttendanceRepository attendanceRepository;
    
    public List<ParticipantInfo> getEventParticipants(String eventId) {
        List<Ticket> tickets = ticketRepository.findByEventId(eventId);
        
        return tickets.stream()
            .map(ticket -> {
                Participant participant = ticket.getParticipant();
                Attendance attendance = attendanceRepository.findByEventIdAndParticipantId(eventId, participant.getId())
                    .orElse(null);
                
                ParticipantInfo info = new ParticipantInfo();
                info.setParticipantId(participant.getId().toString());
                info.setFullName(participant.getFullName());
                info.setEmail(participant.getEmail());
                info.setPhoneNumber(participant.getPhoneNumber());
                info.setInstitution(participant.getInstitution());
                info.setTicketStatus(ticket.getStatus().name());
                info.setAttendanceStatus(attendance != null ? attendance.getStatus().name() : "NOT_CHECKED_IN");
                info.setRegistrationDate(ticket.getPurchaseDate());
                
                if (ticket.getPayment() != null) {
                    info.setPaymentStatus(ticket.getPayment().getStatus().name());
                    info.setAmountPaid(ticket.getPayment().getAmount());
                }
                
                return info;
            })
            .collect(Collectors.toList());
    }
    
    public List<ParticipantInfo> getEventParticipantsByStatus(String eventId, Ticket.TicketStatus ticketStatus) {
        List<Ticket> tickets = ticketRepository.findByEventIdAndStatus(eventId, ticketStatus);
        
        return tickets.stream()
            .map(ticket -> {
                Participant participant = ticket.getParticipant();
                Attendance attendance = attendanceRepository.findByEventIdAndParticipantId(eventId, participant.getId())
                    .orElse(null);
                
                ParticipantInfo info = new ParticipantInfo();
                info.setParticipantId(participant.getId().toString());
                info.setFullName(participant.getFullName());
                info.setEmail(participant.getEmail());
                info.setPhoneNumber(participant.getPhoneNumber());
                info.setInstitution(participant.getInstitution());
                info.setTicketStatus(ticket.getStatus().name());
                info.setAttendanceStatus(attendance != null ? attendance.getStatus().name() : "NOT_CHECKED_IN");
                info.setRegistrationDate(ticket.getPurchaseDate());
                
                if (ticket.getPayment() != null) {
                    info.setPaymentStatus(ticket.getPayment().getStatus().name());
                    info.setAmountPaid(ticket.getPayment().getAmount());
                }
                
                return info;
            })
            .collect(Collectors.toList());
    }
} 