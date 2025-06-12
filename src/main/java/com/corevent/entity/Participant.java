package com.corevent.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;

@Entity
@Table(name = "participants")
public class Participant extends User {
  
  private String institution;
  
  // Getters and Setters
  public String getInstitution() { return institution; }
  public void setInstitution(String institution) { this.institution = institution; }
}