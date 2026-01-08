package com.inventory.controller;

import com.inventory.model.User;
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
 * Controller for the signup view.
 */
public class SignupController {
    
    private static final Logger logger = LoggerFactory.getLogger(SignupController.class);
    
    @FXML private VBox signupContainer;
    @FXML private TextField usernameField;
    @FXML private TextField emailField;
    @FXML private TextField fullNameField;
    @FXML private PasswordField passwordField;
    @FXML private PasswordField confirmPasswordField;
    @FXML private Button signupButton;
    @FXML private Hyperlink loginLink;
    @FXML private Label errorLabel;
    @FXML private ProgressIndicator loadingIndicator;
    
    private final AuthService authService = new AuthService();
    
    @FXML
    public void initialize() {
        // Play entrance animation
        Platform.runLater(this::playEntranceAnimation);
        
        // Clear error on input
        usernameField.textProperty().addListener((obs, old, newVal) -> clearError());
        emailField.textProperty().addListener((obs, old, newVal) -> clearError());
        passwordField.textProperty().addListener((obs, old, newVal) -> clearError());
        confirmPasswordField.textProperty().addListener((obs, old, newVal) -> clearError());
        
        // Enter key to signup
        confirmPasswordField.setOnAction(e -> handleSignup());
        
        // Hide loading indicator
        if (loadingIndicator != null) {
            loadingIndicator.setVisible(false);
        }
    }
    
    /**
     * Handles signup button click.
     */
    @FXML
    private void handleSignup() {
        String username = usernameField.getText().trim();
        String email = emailField.getText().trim();
        String fullName = fullNameField.getText().trim();
        String password = passwordField.getText();
        String confirmPassword = confirmPasswordField.getText();
        
        // Validate inputs
        if (username.isEmpty()) {
            showError("Username is required");
            AlertUtil.shake(usernameField);
            return;
        }
        
        if (username.length() < 3) {
            showError("Username must be at least 3 characters");
            AlertUtil.shake(usernameField);
            return;
        }
        
        if (email.isEmpty()) {
            showError("Email is required");
            AlertUtil.shake(emailField);
            return;
        }
        
        if (password.isEmpty()) {
            showError("Password is required");
            AlertUtil.shake(passwordField);
            return;
        }
        
        if (!password.equals(confirmPassword)) {
            showError("Passwords do not match");
            AlertUtil.shake(confirmPasswordField);
            return;
        }
        
        // Show loading
        setLoading(true);
        
        // Perform registration in background
        new Thread(() -> {
            AuthService.AuthResult result = authService.register(
                username, email, password, fullName, User.Role.STAFF
            );
            
            Platform.runLater(() -> {
                setLoading(false);
                
                if (result.isSuccess()) {
                    logger.info("Registration successful for user: {}", username);
                    AlertUtil.showSuccess("Success", "Account created successfully! Please login.");
                    navigateToLogin();
                } else {
                    showError(result.getMessage());
                    AlertUtil.shake(signupContainer);
                }
            });
        }).start();
    }
    
    /**
     * Handles login link click.
     */
    @FXML
    private void handleLoginLink() {
        navigateToLogin();
    }
    
    /**
     * Navigates to the login view.
     */
    private void navigateToLogin() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/LoginView.fxml"));
            Parent root = loader.load();
            
            Stage stage = (Stage) signupContainer.getScene().getWindow();
            Scene scene = new Scene(root);
            scene.getStylesheets().add(getClass().getResource("/css/styles.css").toExternalForm());
            stage.setScene(scene);
            
        } catch (IOException e) {
            logger.error("Failed to load login view", e);
            AlertUtil.showError("Error", "Failed to load login page");
        }
    }
    
    /**
     * Shows an error message.
     */
    private void showError(String message) {
        if (errorLabel != null) {
            errorLabel.setText(message);
            errorLabel.setVisible(true);
            
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
        signupButton.setDisable(loading);
        usernameField.setDisable(loading);
        emailField.setDisable(loading);
        fullNameField.setDisable(loading);
        passwordField.setDisable(loading);
        confirmPasswordField.setDisable(loading);
        if (loadingIndicator != null) {
            loadingIndicator.setVisible(loading);
        }
    }
    
    /**
     * Plays the entrance animation.
     */
    private void playEntranceAnimation() {
        signupContainer.setOpacity(0);
        signupContainer.setTranslateY(30);
        
        FadeTransition fadeIn = new FadeTransition(Duration.millis(500), signupContainer);
        fadeIn.setFromValue(0);
        fadeIn.setToValue(1);
        
        TranslateTransition slideUp = new TranslateTransition(Duration.millis(500), signupContainer);
        slideUp.setFromY(30);
        slideUp.setToY(0);
        
        ParallelTransition entrance = new ParallelTransition(fadeIn, slideUp);
        entrance.play();
    }
}
