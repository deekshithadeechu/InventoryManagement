package com.inventory;

import com.inventory.util.DatabaseUtil;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Main entry point for the Smart Inventory Management System.
 * Initializes the JavaFX application and loads the login view.
 */
public class Main extends Application {
    
    private static final Logger logger = LoggerFactory.getLogger(Main.class);
    
    private static final String APP_TITLE = "Smart Inventory Management System";
    private static final int WINDOW_WIDTH = 500;
    private static final int WINDOW_HEIGHT = 650;
    
    @Override
    public void start(Stage primaryStage) {
        try {
            logger.info("Starting {}...", APP_TITLE);
            
            // Load the login view
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/LoginView.fxml"));
            Parent root = loader.load();
            
            // Create scene with stylesheet
            Scene scene = new Scene(root, WINDOW_WIDTH, WINDOW_HEIGHT);
            scene.getStylesheets().add(getClass().getResource("/css/styles.css").toExternalForm());
            
            // Configure stage
            primaryStage.setTitle(APP_TITLE);
            primaryStage.setScene(scene);
            primaryStage.setMinWidth(450);
            primaryStage.setMinHeight(600);
            primaryStage.setResizable(true);
            
            // Center on screen
            primaryStage.centerOnScreen();
            
            // Show the stage
            primaryStage.show();
            
            logger.info("{} started successfully", APP_TITLE);
            
        } catch (Exception e) {
            logger.error("Failed to start application", e);
            e.printStackTrace();
            System.exit(1);
        }
    }
    
    @Override
    public void stop() {
        logger.info("Shutting down {}...", APP_TITLE);
        
        // Close database connections
        DatabaseUtil.closeDataSource();
        
        logger.info("Application shutdown complete");
    }
    
    /**
     * Main method - launches the JavaFX application.
     * 
     * @param args Command line arguments
     */
    public static void main(String[] args) {
        // Check database connection before launching
        logger.info("Checking database connection...");
        if (!DatabaseUtil.testConnection()) {
            logger.warn("Database connection test failed. Application will start but may not function correctly.");
            logger.warn("Please ensure MySQL is running and the database 'inventory_db' exists.");
        } else {
            logger.info("Database connection successful");
        }
        
        launch(args);
    }
}
