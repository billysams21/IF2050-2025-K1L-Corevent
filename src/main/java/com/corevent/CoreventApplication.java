package com.corevent;

import java.io.IOException;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;

@SpringBootApplication
public class CoreventApplication extends Application {
    
    private ConfigurableApplicationContext springContext;
    private Parent rootNode;
    
    public static void main(String[] args) {
        Application.launch(args);
    }
    
    @Override
    public void init() throws Exception {
        try {
            springContext = SpringApplication.run(CoreventApplication.class);
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/login.fxml"));
            loader.setControllerFactory(springContext::getBean);
            rootNode = loader.load();
        } catch (IOException e) {
            e.printStackTrace();
            Platform.exit();
            throw new RuntimeException("Failed to load FXML file", e);
        }
    }
    
    @Override
    public void start(Stage primaryStage) {
        try {
            primaryStage.setTitle("Corevent - Event Management System");
            
            Scene scene = new Scene(rootNode, 1200, 800);
            scene.getStylesheets().add(getClass().getResource("/css/style.css").toExternalForm());
            
            try {
                primaryStage.getIcons().add(new Image(getClass().getResourceAsStream("/images/icon.png")));
            } catch (Exception e) {
                System.err.println("Warning: Could not load application icon: " + e.getMessage());
            }
            
            primaryStage.setScene(scene);
            primaryStage.setMinWidth(1000);
            primaryStage.setMinHeight(700);
            primaryStage.centerOnScreen();
            primaryStage.show();
        } catch (Exception e) {
            e.printStackTrace();
            Platform.exit();
        }
    }
    
    @Override
    public void stop() {
        if (springContext != null) {
            springContext.close();
        }
        Platform.exit();
    }
}