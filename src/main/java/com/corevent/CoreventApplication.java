package main.java.com.corevent;

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
        launch(args);
    }
    
    @Override
    public void init() throws Exception {
        springContext = SpringApplication.run(CoreventApplication.class);
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/login.fxml"));
        loader.setControllerFactory(springContext::getBean);
        rootNode = loader.load();
    }
    
    @Override
    public void start(Stage primaryStage) throws Exception {
        primaryStage.setTitle("Corevent - Event Management System");
        
        Scene scene = new Scene(rootNode, 1200, 800);
        scene.getStylesheets().add(getClass().getResource("/css/style.css").toExternalForm());
        
        // Set application icon
        primaryStage.getIcons().add(new Image(getClass().getResourceAsStream("/images/icon.png")));
        
        primaryStage.setScene(scene);
        primaryStage.setMinWidth(1000);
        primaryStage.setMinHeight(700);
        primaryStage.centerOnScreen();
        primaryStage.show();
    }
    
    @Override
    public void stop() throws Exception {
        springContext.close();
        Platform.exit();
    }
}