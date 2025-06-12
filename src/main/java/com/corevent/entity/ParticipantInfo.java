package com.corevent.entity;

import java.time.LocalDateTime;

public class ParticipantInfo {
  private String participantId;
  private String fullName;
  private String email;
  private String phoneNumber;
  private String institution;
  private String ticketStatus;
  private String attendanceStatus;
  private LocalDateTime registrationDate;
  private String paymentStatus;
  private Double amountPaid;

  public ParticipantInfo() {
  }

  public ParticipantInfo(String participantId, String fullName, String email,
                        String phoneNumber, String institution, String ticketStatus,
                        String attendanceStatus, LocalDateTime registrationDate) {
    this.participantId = participantId;
    this.fullName = fullName;
    this.email = email;
    this.phoneNumber = phoneNumber;
    this.institution = institution;
    this.ticketStatus = ticketStatus;
    this.attendanceStatus = attendanceStatus;
    this.registrationDate = registrationDate;
  }

  // Getters and Setters
  public String getParticipantId() {
    return participantId;
  }

  public void setParticipantId(String participantId) {
    this.participantId = participantId;
  }

  public String getFullName() {
    return fullName;
  }

  public void setFullName(String fullName) {
    this.fullName = fullName;
  }

  public String getEmail() {
    return email;
  }

  public void setEmail(String email) {
    this.email = email;
  }

  public String getPhoneNumber() {
    return phoneNumber;
  }

  public void setPhoneNumber(String phoneNumber) {
    this.phoneNumber = phoneNumber;
  }

  public String getInstitution() {
    return institution;
  }

  public void setInstitution(String institution) {
    this.institution = institution;
  }

  public String getTicketStatus() {
    return ticketStatus;
  }

  public void setTicketStatus(String ticketStatus) {
    this.ticketStatus = ticketStatus;
  }

  public String getAttendanceStatus() {
    return attendanceStatus;
  }

  public void setAttendanceStatus(String attendanceStatus) {
    this.attendanceStatus = attendanceStatus;
  }

  public LocalDateTime getRegistrationDate() {
    return registrationDate;
  }

  public void setRegistrationDate(LocalDateTime registrationDate) {
    this.registrationDate = registrationDate;
  }

  public String getPaymentStatus() {
    return paymentStatus;
  }

  public void setPaymentStatus(String paymentStatus) {
    this.paymentStatus = paymentStatus;
  }
}
