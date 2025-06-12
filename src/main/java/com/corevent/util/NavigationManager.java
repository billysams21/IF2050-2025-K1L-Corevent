package com.corevent.util;

import java.io.IOException;

import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import lombok.extern.slf4j.Slf4j;

// Manager for navigating between scenes in the application
@Slf4j
@Component
public class NavigationManager {
  
  private final ApplicationContext springContext;
  private Stage primaryStage;
  
  public NavigationManager(ApplicationContext springContext) {
    this.springContext = springContext;
  }
  
  public void setPrimaryStage(Stage primaryStage) {
    this.primaryStage = primaryStage;
  }

  public void navigateToLogin() throws IOException {
    loadScene("/fxml/login.fxml", "Corevent - Login");
  }
  
  public void navigateToRegister() throws IOException {
    loadScene("/fxml/register.fxml", "Corevent - Register");
  }
  
  public void navigateToCommitteeDashboard() throws IOException {
    loadScene("/fxml/committee-dashboard.fxml", "Corevent - Committee Dashboard");
  }
  
  public void navigateToParticipantDashboard() throws IOException {
    loadScene("/fxml/participant-dashboard.fxml", "Corevent - Participant Dashboard");
  }
  
  public void navigateToCreateEvent() throws IOException {
    loadScene("/fxml/create-event.fxml", "Corevent - Create Event");
  }
  
  public void navigateToManageEvents() throws IOException {
    loadScene("/fxml/manage-events.fxml", "Corevent - Manage Events");
  }
  
  public void navigateToManageAttendance() throws IOException {
    loadScene("/fxml/manage-attendance.fxml", "Corevent - Manage Attendance");
  }
  
  public void navigateToProfile() {
    try {
        loadScene("/fxml/profile.fxml", "My Profile");
    } catch (Exception e) {
        log.error("Failed to navigate to profile", e);
        throw new RuntimeException("Error loading profile page", e);
    }
  }
  
  public void navigateToEventDetails(String eventId) throws IOException {
    loadScene("/fxml/event-details.fxml", "Corevent - Event Details");
  }
  
  public void navigateToEditEvent(String eventId) throws IOException {
    loadScene("/fxml/edit-event.fxml", "Corevent - Edit Event");
  }
  
  public void navigateToParticipantManagement() throws IOException {
    loadScene("/fxml/participant-management.fxml", "Corevent - Participant Management");
  }
  
  public void navigateToCheckIn() throws IOException {
    loadScene("/fxml/check-in.fxml", "Corevent - Check-In");
  }
  
  public void navigateToEvaluationResults() throws IOException {
    loadScene("/fxml/evaluation-results.fxml", "Corevent - Evaluation Results");
  }
  
  public void navigateToBrowseEvents() throws IOException {
    loadScene("/fxml/browse-events.fxml", "Corevent - Browse Events");
  }
  
  public void navigateToMyTickets() throws IOException {
    loadScene("/fxml/my-tickets.fxml", "Corevent - My Tickets");
  }
  
  public void navigateToMyEvaluations() throws IOException {
    loadScene("/fxml/my-evaluations.fxml", "Corevent - My Evaluations");
  }
  
  public void navigateToDashboard(String role) {
    try {
        if (role != null && role.equals("COMMITTEE")) {
            navigateToCommitteeDashboard();
        } else if (role != null && role.equals("PARTICIPANT")) {
            navigateToParticipantDashboard();
        } else {
            log.error("Unknown role: {}", role);
            throw new IllegalArgumentException("Invalid user role");
        }
    } catch (Exception e) {
        log.error("Failed to load dashboard", e);
        throw new RuntimeException("Error loading dashboard", e);
    }
  }
  
  private void loadScene(String fxmlPath, String title) throws IOException {
    FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
    loader.setControllerFactory(springContext::getBean);
    
    Parent root = loader.load();
    Scene scene = new Scene(root);
    
    scene.getStylesheets().add(getClass().getResource("/css/style.css").toExternalForm());
    
    primaryStage.setTitle(title);
    primaryStage.setScene(scene);
    primaryStage.show();
  }
  
  public Stage openNewWindow(String fxmlPath, String title) throws IOException {
    FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
    loader.setControllerFactory(springContext::getBean);
    
    Parent root = loader.load();
    Scene scene = new Scene(root);
    scene.getStylesheets().add(getClass().getResource("/css/style.css").toExternalForm());
    
    Stage newStage = new Stage();
    newStage.setTitle(title);
    newStage.setScene(scene);
    newStage.show();
    
    return newStage;
  }
  
  // Show dialog with FXML file
  public Stage showDialog(String fxmlPath, String title) throws IOException {
    Stage dialogStage = openNewWindow(fxmlPath, title);
    dialogStage.initOwner(primaryStage);
    dialogStage.initModality(javafx.stage.Modality.WINDOW_MODAL);
    return dialogStage;
  }
  
  public void logout() {
    try {
      // Clear session
      SessionManager.getInstance().clearSession();
      
      // Navigate back to login
      navigateToLogin();
    } catch (IOException e) {
      e.printStackTrace();
      if (primaryStage != null) {
        primaryStage.close();
      }
    }
  }
}