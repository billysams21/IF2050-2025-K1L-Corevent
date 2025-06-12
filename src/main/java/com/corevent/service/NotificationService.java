package com.corevent.service;

import java.util.List;
import java.util.ArrayList;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.corevent.dto.notification.NotificationResult;
import com.corevent.dto.notification.NotificationResult.NotificationStatistics;
import com.corevent.entity.Event;
import com.corevent.entity.Notification;
import com.corevent.entity.Participant;
import com.corevent.entity.Committee;
import com.corevent.entity.User;
import com.corevent.repository.NotificationRepository;
import com.corevent.repository.EventRepository;
import com.corevent.repository.UserRepository;
import com.corevent.util.SessionManager;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class NotificationService {
    
    @Autowired
    private NotificationRepository notificationRepository;
    
    @Autowired
    private EventRepository eventRepository;
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private JavaMailSender mailSender;
    
    /**
     * Implementasi algoritma sendNotificationToParticipants dari DPPL
     * Input: eventID, title, message, participantList  
     * Output: NotificationResult dengan statistik
     */
    @Transactional
    public NotificationResult sendNotificationToParticipants(String eventID, String title, String message, List<String> participantList) {
        
        // Validasi input
        if (eventID == null || eventID.isEmpty()) {
            return new NotificationResult(false, "Event ID cannot be empty");
        }
        
        if (message == null || message.isEmpty()) {
            return new NotificationResult(false, "Notification message cannot be empty");
        }
        
        if (title == null || title.isEmpty()) {
            return new NotificationResult(false, "Notification title cannot be empty");
        }
        
        if (participantList == null || participantList.isEmpty()) {
            return new NotificationResult(false, "Participant list cannot be empty");
        }
        
        // Cek keberadaan event
        Event event = eventRepository.findById(eventID).orElse(null);
        if (event == null) {
            return new NotificationResult(false, "Event not found");
        }
        
        // Dapatkan committee yang sedang login
        Committee committee = getCurrentCommittee();
        if (committee == null) {
            return new NotificationResult(false, "Committee not found or not logged in");
        }
        
        // Inisialisasi counter
        int successCount = 0;
        int failureCount = 0;
        
        // Loop untuk setiap peserta
        for (String participantID : participantList) {
            try {
                // Cari data peserta
                Participant participant = getParticipantByID(Long.parseLong(participantID));
                if (participant == null) {
                    log.warn("Participant with ID {} not found", participantID);
                    failureCount++;
                    continue;
                }
                
                // Buat objek notifikasi
                Notification notification = new Notification(title, message, event, committee, participant);
                notification.setStatus(Notification.NotificationStatus.PENDING);
                
                // Kirim notifikasi (email)
                String result = sendEmailNotification(participant.getEmail(), notification);
                
                if ("SUCCESS".equals(result)) {
                    notification.setStatus(Notification.NotificationStatus.SENT);
                    successCount++;
                    log.info("Notification sent successfully to participant {}", participant.getEmail());
                } else {
                    notification.setStatus(Notification.NotificationStatus.FAILED);
                    notification.setErrorMessage(result);
                    failureCount++;
                    log.error("Failed to send notification to participant {}: {}", participant.getEmail(), result);
                }
                
                // Simpan notifikasi ke database
                notificationRepository.save(notification);
                
            } catch (Exception e) {
                log.error("Error processing notification for participant {}: {}", participantID, e.getMessage());
                failureCount++;
            }
        }
        
        // Return hasil dengan statistik
        NotificationStatistics statistics = new NotificationStatistics(successCount, failureCount, participantList.size());
        return new NotificationResult(true, statistics);
    }
    
    /**
     * Mendapatkan daftar peserta untuk event tertentu
     */
    public List<Participant> getEventParticipants(String eventID) {
        List<Participant> participants = new ArrayList<>();
        
        // Implementasi untuk mendapatkan peserta dari event
        // Untuk saat ini, return semua participant sebagai contoh
        List<User> allUsers = userRepository.findAll();
        for (User user : allUsers) {
            if (user instanceof Participant) {
                participants.add((Participant) user);
            }
        }
        
        return participants;
    }
    
    /**
     * Mengirim email notification
     */
    private String sendEmailNotification(String email, Notification notification) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(email);
            message.setSubject(notification.getTitle());
            message.setText(buildEmailContent(notification));
            message.setFrom("noreply@corevent.com");
            
            mailSender.send(message);
            
            return "SUCCESS";
            
        } catch (Exception e) {
            log.error("Failed to send email to {}: {}", email, e.getMessage());
            return "Email send failed: " + e.getMessage();
        }
    }
    
    /**
     * Membuat konten email
     */
    private String buildEmailContent(Notification notification) {
        StringBuilder content = new StringBuilder();
        content.append("Dear Participant,\n\n");
        content.append(notification.getMessage());
        content.append("\n\n");
        
        if (notification.getEvent() != null) {
            content.append("Event: ").append(notification.getEvent().getEventName()).append("\n");
            content.append("Date: ").append(notification.getEvent().getDate()).append("\n");
            content.append("Location: ").append(notification.getEvent().getLocation()).append("\n\n");
        }
        
        content.append("Best regards,\n");
        content.append("Corevent Team");
        
        return content.toString();
    }
    
    /**
     * Mendapatkan committee yang sedang login
     */
    private Committee getCurrentCommittee() {
        User currentUser = SessionManager.getInstance().getCurrentUser();
        if (currentUser instanceof Committee) {
            return (Committee) currentUser;
        }
        return null;
    }
    
    /**
     * Mendapatkan participant berdasarkan ID
     */
    private Participant getParticipantByID(Long participantID) {
        User user = userRepository.findById(participantID).orElse(null);
        if (user instanceof Participant) {
            return (Participant) user;
        }
        return null;
    }
    
    /**
     * Mendapatkan notifikasi berdasarkan event
     */
    public List<Notification> getNotificationsByEvent(String eventID) {
        Event event = eventRepository.findById(eventID).orElse(null);
        if (event != null) {
            return notificationRepository.findByEvent(event);
        }
        return new ArrayList<>();
    }
    
    /**
     * Mendapatkan statistik notifikasi untuk event
     */
    public NotificationStatistics getNotificationStatistics(String eventID) {
        Event event = eventRepository.findById(eventID).orElse(null);
        if (event == null) {
            return new NotificationStatistics(0, 0, 0);
        }
        
        int sent = (int) notificationRepository.countByEventAndStatus(event, Notification.NotificationStatus.SENT);
        int failed = (int) notificationRepository.countByEventAndStatus(event, Notification.NotificationStatus.FAILED);
        int pending = (int) notificationRepository.countByEventAndStatus(event, Notification.NotificationStatus.PENDING);
        int total = sent + failed + pending;
        
        return new NotificationStatistics(sent, failed, total);
    }
    
    /**
     * Mendapatkan semua event yang bisa dikelola oleh committee
     */
    public List<Event> getAvailableEventsForNotification() {
        Committee committee = getCurrentCommittee();
        if (committee != null) {
            return eventRepository.findByCommitteesUserId(committee.getUserId());
        }
        return new ArrayList<>();
    }
} 