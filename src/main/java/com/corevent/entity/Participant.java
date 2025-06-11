package com.corevent.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

@Entity
@Table(name = "participants")
public class Participant extends User {
  
  @Column(nullable = false)
  private String fullName;
  
  private String phoneNumber;
  
  private String institution;
  
  // Getters and Setters
  public String getFullName() { return fullName; }
  public void setFullName(String fullName) { this.fullName = fullName; }
  
  public String getPhoneNumber() { return phoneNumber; }
  public void setPhoneNumber(String phoneNumber) { this.phoneNumber = phoneNumber; }
  
  public String getInstitution() { return institution; }
  public void setInstitution(String institution) { this.institution = institution; }
}