package com.inventory.controller;

import com.inventory.service.AlertService;
import com.inventory.service.AuthService;
import com.inventory.util.AlertUtil;
import com.inventory.util.SessionManager;
import javafx.animation.*;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import javafx.util.Duration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

/**
 * Controller for the main application layout with sidebar navigation.
 */
public class MainController {
    
    private static final Logger logger = LoggerFactory.getLogger(MainController.class);
    
    @FXML private BorderPane mainContainer;
    @FXML private VBox sidebar;
    @FXML private StackPane contentArea;
    @FXML private Label userNameLabel;
    @FXML private Label userRoleLabel;
    @FXML private Label alertBadge;
    
    // Navigation buttons
    @FXML private Button dashboardBtn;
    @FXML private Button productsBtn;
    @FXML private Button categoriesBtn;
    @FXML private Button suppliersBtn;
    @FXML private Button reportsBtn;
    @FXML private Button alertsBtn;
    @FXML private Button settingsBtn;
    @FXML private Button logoutBtn;
    @FXML private ToggleButton darkModeToggle;
    
    private Button currentActiveButton;
    private final AuthService authService = new AuthService();
    private final AlertService alertService = new AlertService();
    private boolean isDarkMode = false;
    
    @FXML
    public void initialize() {
        // Set user info
        updateUserInfo();
        
        // Update alert badge
        updateAlertBadge();
        
        // Load dashboard by default
        Platform.runLater(() -> {
            navigateTo("Dashboard", "/views/DashboardView.fxml");
            setActiveButton(dashboardBtn);
            playEntranceAnimation();
        });
        
        // Setup dark mode toggle
        if (darkModeToggle != null) {
            darkModeToggle.setOnAction(e -> toggleDarkMode());
        }
    }
    
    /**
     * Updates user info in sidebar.
     */
    private void updateUserInfo() {
        SessionManager session = SessionManager.getInstance();
        if (userNameLabel != null) {
            userNameLabel.setText(session.getCurrentDisplayName());
        }
        if (userRoleLabel != null) {
            userRoleLabel.setText(session.getCurrentRole());
        }
    }
    
    /**
     * Updates the alert badge count.
     */
    private void updateAlertBadge() {
        if (alertBadge != null) {
            int count = alertService.getTotalAlertCount();
            if (count > 0) {
                alertBadge.setText(count > 99 ? "99+" : String.valueOf(count));
                alertBadge.setVisible(true);
            } else {
                alertBadge.setVisible(false);
            }
        }
    }
    
    // Navigation handlers
    
    @FXML
    private void handleDashboard() {
        navigateTo("Dashboard", "/views/DashboardView.fxml");
        setActiveButton(dashboardBtn);
    }
    
    @FXML
    private void handleProducts() {
        navigateTo("Products", "/views/ProductsView.fxml");
        setActiveButton(productsBtn);
    }
    
    @FXML
    private void handleCategories() {
        navigateTo("Categories", "/views/CategoriesView.fxml");
        setActiveButton(categoriesBtn);
    }
    
    @FXML
    private void handleSuppliers() {
        navigateTo("Suppliers", "/views/SuppliersView.fxml");
        setActiveButton(suppliersBtn);
    }
    
    @FXML
    private void handleReports() {
        navigateTo("Reports", "/views/ReportsView.fxml");
        setActiveButton(reportsBtn);
    }
    
    @FXML
    private void handleAlerts() {
        navigateTo("Alerts", "/views/AlertsView.fxml");
        setActiveButton(alertsBtn);
    }
    
    @FXML
    private void handleSettings() {
        navigateTo("Settings", "/views/SettingsView.fxml");
        setActiveButton(settingsBtn);
    }
    
    @FXML
    private void handleLogout() {
        if (AlertUtil.showConfirmation("Logout", "Are you sure you want to logout?")) {
            authService.logout();
            navigateToLogin();
        }
    }
    
    /**
     * Navigates to a view and loads it in the content area.
     */
    private void navigateTo(String viewName, String fxmlPath) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Node content = loader.load();
            
            // Fade transition
            FadeTransition fadeOut = new FadeTransition(Duration.millis(150), contentArea);
            fadeOut.setFromValue(1);
            fadeOut.setToValue(0);
            fadeOut.setOnFinished(e -> {
                contentArea.getChildren().clear();
                contentArea.getChildren().add(content);
                
                FadeTransition fadeIn = new FadeTransition(Duration.millis(200), contentArea);
                fadeIn.setFromValue(0);
                fadeIn.setToValue(1);
                fadeIn.play();
            });
            fadeOut.play();
            
            logger.debug("Navigated to: {}", viewName);
            
        } catch (IOException e) {
            logger.error("Failed to load view: {}", fxmlPath, e);
            showErrorContent("Failed to load " + viewName);
        }
    }
    
    /**
     * Shows error content in the content area.
     */
    private void showErrorContent(String message) {
        VBox errorBox = new VBox(20);
        errorBox.setAlignment(javafx.geometry.Pos.CENTER);
        
        Label errorIcon = new Label("⚠️");
        errorIcon.setStyle("-fx-font-size: 48px;");
        
        Label errorLabel = new Label(message);
        errorLabel.getStyleClass().add("error-message");
        
        errorBox.getChildren().addAll(errorIcon, errorLabel);
        
        contentArea.getChildren().clear();
        contentArea.getChildren().add(errorBox);
    }
    
    /**
     * Sets the active navigation button.
     */
    private void setActiveButton(Button button) {
        if (currentActiveButton != null) {
            currentActiveButton.getStyleClass().remove("nav-button-active");
        }
        button.getStyleClass().add("nav-button-active");
        currentActiveButton = button;
    }
    
    /**
     * Toggles dark mode.
     */
    private void toggleDarkMode() {
        isDarkMode = !isDarkMode;
        
        if (isDarkMode) {
            mainContainer.getStyleClass().add("dark-mode");
        } else {
            mainContainer.getStyleClass().remove("dark-mode");
        }
        
        logger.debug("Dark mode: {}", isDarkMode);
    }
    
    /**
     * Navigates to login screen.
     */
    private void navigateToLogin() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/LoginView.fxml"));
            Parent root = loader.load();
            
            Stage stage = (Stage) mainContainer.getScene().getWindow();
            Scene scene = new Scene(root);
            scene.getStylesheets().add(getClass().getResource("/css/styles.css").toExternalForm());
            
            stage.setWidth(500);
            stage.setHeight(600);
            stage.centerOnScreen();
            stage.setScene(scene);
            
        } catch (IOException e) {
            logger.error("Failed to navigate to login", e);
        }
    }
    
    /**
     * Plays the entrance animation for the main layout.
     */
    private void playEntranceAnimation() {
        // Sidebar slide in
        sidebar.setTranslateX(-250);
        TranslateTransition sidebarSlide = new TranslateTransition(Duration.millis(400), sidebar);
        sidebarSlide.setFromX(-250);
        sidebarSlide.setToX(0);
        
        // Content fade in
        contentArea.setOpacity(0);
        FadeTransition contentFade = new FadeTransition(Duration.millis(500), contentArea);
        contentFade.setFromValue(0);
        contentFade.setToValue(1);
        contentFade.setDelay(Duration.millis(200));
        
        ParallelTransition entrance = new ParallelTransition(sidebarSlide, contentFade);
        entrance.play();
    }
    
    /**
     * Refreshes the current view (useful after data changes).
     */
    public void refreshCurrentView() {
        if (currentActiveButton == dashboardBtn) {
            handleDashboard();
        } else if (currentActiveButton == productsBtn) {
            handleProducts();
        }
        updateAlertBadge();
    }
}
