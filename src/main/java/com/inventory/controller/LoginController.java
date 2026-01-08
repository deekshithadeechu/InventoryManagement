package com.inventory.controller;

import com.inventory.Main;
import com.inventory.service.AuthService;
import com.inventory.util.AlertUtil;
import javafx.animation.FadeTransition;
import javafx.animation.ParallelTransition;
import javafx.animation.TranslateTransition;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.util.Duration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

/**
 * Controller for the login view.
 */
public class LoginController {
    
    private static final Logger logger = LoggerFactory.getLogger(LoginController.class);
    
    @FXML private VBox loginContainer;
    @FXML private TextField usernameField;
    @FXML private PasswordField passwordField;
    @FXML private Button loginButton;
    @FXML private Hyperlink signupLink;
    @FXML private Label errorLabel;
    @FXML private ProgressIndicator loadingIndicator;
    
    private final AuthService authService = new AuthService();
    
    @FXML
    public void initialize() {
        // Play entrance animation
        Platform.runLater(this::playEntranceAnimation);
        
        // Clear error on input
        usernameField.textProperty().addListener((obs, old, newVal) -> clearError());
        passwordField.textProperty().addListener((obs, old, newVal) -> clearError());
        
        // Enter key to login
        passwordField.setOnAction(e -> handleLogin());
        
        // Hide loading indicator
        if (loadingIndicator != null) {
            loadingIndicator.setVisible(false);
        }
    }
    
    /**
     * Handles login button click.
     */
    @FXML
    private void handleLogin() {
        String username = usernameField.getText().trim();
        String password = passwordField.getText();
        
        if (username.isEmpty()) {
            showError("Please enter your username");
            AlertUtil.shake(usernameField);
            return;
        }
        
        if (password.isEmpty()) {
            showError("Please enter your password");
            AlertUtil.shake(passwordField);
            return;
        }
        
        // Show loading
        setLoading(true);
        
        // Perform login in background
        new Thread(() -> {
            AuthService.AuthResult result = authService.login(username, password);
            
            Platform.runLater(() -> {
                setLoading(false);
                
                if (result.isSuccess()) {
                    logger.info("Login successful for user: {}", username);
                    navigateToMain();
                } else {
                    showError(result.getMessage());
                    AlertUtil.shake(loginContainer);
                }
            });
        }).start();
    }
    
    /**
     * Handles signup link click.
     */
    @FXML
    private void handleSignupLink() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/SignupView.fxml"));
            Parent root = loader.load();
            
            Stage stage = (Stage) loginContainer.getScene().getWindow();
            Scene scene = new Scene(root);
            scene.getStylesheets().add(getClass().getResource("/css/styles.css").toExternalForm());
            stage.setScene(scene);
            
        } catch (IOException e) {
            logger.error("Failed to load signup view", e);
            AlertUtil.showError("Error", "Failed to load signup page");
        }
    }
    
    /**
     * Navigates to the main application view.
     */
    private void navigateToMain() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/MainView.fxml"));
            Parent root = loader.load();
            
            Stage stage = (Stage) loginContainer.getScene().getWindow();
            Scene scene = new Scene(root);
            scene.getStylesheets().add(getClass().getResource("/css/styles.css").toExternalForm());
            
            // Set minimum size
            stage.setMinWidth(1200);
            stage.setMinHeight(700);
            stage.setWidth(1400);
            stage.setHeight(800);
            stage.centerOnScreen();
            
            stage.setScene(scene);
            
        } catch (IOException e) {
            logger.error("Failed to load main view", e);
            AlertUtil.showError("Error", "Failed to load application");
        }
    }
    
    /**
     * Shows an error message.
     */
    private void showError(String message) {
        if (errorLabel != null) {
            errorLabel.setText(message);
            errorLabel.setVisible(true);
            
            // Fade in error
            FadeTransition ft = new FadeTransition(Duration.millis(200), errorLabel);
            ft.setFromValue(0);
            ft.setToValue(1);
            ft.play();
        }
    }
    
    /**
     * Clears the error message.
     */
    private void clearError() {
        if (errorLabel != null) {
            errorLabel.setVisible(false);
        }
    }
    
    /**
     * Sets the loading state.
     */
    private void setLoading(boolean loading) {
        loginButton.setDisable(loading);
        usernameField.setDisable(loading);
        passwordField.setDisable(loading);
        if (loadingIndicator != null) {
            loadingIndicator.setVisible(loading);
        }
    }
    
    /**
     * Plays the entrance animation.
     */
    private void playEntranceAnimation() {
        loginContainer.setOpacity(0);
        loginContainer.setTranslateY(30);
        
        FadeTransition fadeIn = new FadeTransition(Duration.millis(500), loginContainer);
        fadeIn.setFromValue(0);
        fadeIn.setToValue(1);
        
        TranslateTransition slideUp = new TranslateTransition(Duration.millis(500), loginContainer);
        slideUp.setFromY(30);
        slideUp.setToY(0);
        
        ParallelTransition entrance = new ParallelTransition(fadeIn, slideUp);
        entrance.play();
    }
}
