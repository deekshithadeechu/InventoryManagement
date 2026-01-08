package com.inventory.util;

import javafx.animation.*;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import javafx.util.Duration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Optional;

/**
 * Utility class for displaying JavaFX alerts and notifications.
 * Provides styled dialogs and toast notifications.
 */
public class AlertUtil {
    
    private static final Logger logger = LoggerFactory.getLogger(AlertUtil.class);
    
    /**
     * Shows an information alert.
     */
    public static void showInfo(String title, String message) {
        showAlert(Alert.AlertType.INFORMATION, title, message);
    }
    
    /**
     * Shows a warning alert.
     */
    public static void showWarning(String title, String message) {
        showAlert(Alert.AlertType.WARNING, title, message);
    }
    
    /**
     * Shows an error alert.
     */
    public static void showError(String title, String message) {
        showAlert(Alert.AlertType.ERROR, title, message);
        logger.error("{}: {}", title, message);
    }
    
    /**
     * Shows a success alert.
     */
    public static void showSuccess(String title, String message) {
        showAlert(Alert.AlertType.INFORMATION, title, message);
    }
    
    /**
     * Shows a confirmation dialog.
     * 
     * @return true if user clicked OK, false otherwise
     */
    public static boolean showConfirmation(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        
        styleAlert(alert);
        
        Optional<ButtonType> result = alert.showAndWait();
        return result.isPresent() && result.get() == ButtonType.OK;
    }
    
    /**
     * Shows a confirmation dialog with custom button text.
     * 
     * @return true if user clicked the confirm button
     */
    public static boolean showConfirmation(String title, String message, 
                                           String confirmText, String cancelText) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        
        ButtonType confirmButton = new ButtonType(confirmText, ButtonBar.ButtonData.OK_DONE);
        ButtonType cancelButton = new ButtonType(cancelText, ButtonBar.ButtonData.CANCEL_CLOSE);
        alert.getButtonTypes().setAll(confirmButton, cancelButton);
        
        styleAlert(alert);
        
        Optional<ButtonType> result = alert.showAndWait();
        return result.isPresent() && result.get() == confirmButton;
    }
    
    /**
     * Shows a text input dialog.
     * 
     * @return The entered text, or null if cancelled
     */
    public static String showTextInput(String title, String message, String defaultValue) {
        TextInputDialog dialog = new TextInputDialog(defaultValue);
        dialog.setTitle(title);
        dialog.setHeaderText(null);
        dialog.setContentText(message);
        
        Optional<String> result = dialog.showAndWait();
        return result.orElse(null);
    }
    
    /**
     * Shows a styled alert dialog.
     */
    private static void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        
        styleAlert(alert);
        alert.showAndWait();
    }
    
    /**
     * Applies styling to an alert dialog.
     */
    private static void styleAlert(Alert alert) {
        DialogPane dialogPane = alert.getDialogPane();
        dialogPane.getStylesheets().add(
            AlertUtil.class.getResource("/css/styles.css").toExternalForm()
        );
        dialogPane.getStyleClass().add("custom-dialog");
    }
    
    /**
     * Shows a toast notification that auto-hides.
     * 
     * @param parent The parent node to show the toast on
     * @param message The message to display
     * @param type The type of toast (success, warning, error)
     */
    public static void showToast(StackPane parent, String message, ToastType type) {
        Label toast = new Label(message);
        toast.getStyleClass().addAll("toast", "toast-" + type.name().toLowerCase());
        toast.setMaxWidth(400);
        toast.setWrapText(true);
        
        parent.getChildren().add(toast);
        
        // Fade in
        FadeTransition fadeIn = new FadeTransition(Duration.millis(300), toast);
        fadeIn.setFromValue(0);
        fadeIn.setToValue(1);
        
        // Slide up
        TranslateTransition slideIn = new TranslateTransition(Duration.millis(300), toast);
        slideIn.setFromY(50);
        slideIn.setToY(0);
        
        // Fade out after delay
        FadeTransition fadeOut = new FadeTransition(Duration.millis(300), toast);
        fadeOut.setFromValue(1);
        fadeOut.setToValue(0);
        fadeOut.setDelay(Duration.seconds(3));
        fadeOut.setOnFinished(e -> parent.getChildren().remove(toast));
        
        ParallelTransition show = new ParallelTransition(fadeIn, slideIn);
        SequentialTransition animation = new SequentialTransition(show, fadeOut);
        animation.play();
    }
    
    /**
     * Toast notification types.
     */
    public enum ToastType {
        SUCCESS,
        WARNING,
        ERROR,
        INFO
    }
    
    /**
     * Applies a shake animation to a node (useful for error indication).
     */
    public static void shake(Node node) {
        TranslateTransition tt = new TranslateTransition(Duration.millis(50), node);
        tt.setFromX(0);
        tt.setByX(10);
        tt.setCycleCount(6);
        tt.setAutoReverse(true);
        tt.setOnFinished(e -> node.setTranslateX(0));
        tt.play();
    }
    
    /**
     * Applies a pulse animation to a node.
     */
    public static void pulse(Node node) {
        ScaleTransition st = new ScaleTransition(Duration.millis(200), node);
        st.setFromX(1);
        st.setFromY(1);
        st.setToX(1.1);
        st.setToY(1.1);
        st.setCycleCount(2);
        st.setAutoReverse(true);
        st.play();
    }
    
    /**
     * Applies a fade-in animation to a node.
     */
    public static void fadeIn(Node node, int durationMs) {
        FadeTransition ft = new FadeTransition(Duration.millis(durationMs), node);
        ft.setFromValue(0);
        ft.setToValue(1);
        ft.play();
    }
    
    /**
     * Applies a slide-in animation to a node.
     */
    public static void slideIn(Node node, double fromX, int durationMs) {
        TranslateTransition tt = new TranslateTransition(Duration.millis(durationMs), node);
        node.setTranslateX(fromX);
        tt.setToX(0);
        
        FadeTransition ft = new FadeTransition(Duration.millis(durationMs), node);
        ft.setFromValue(0);
        ft.setToValue(1);
        
        ParallelTransition pt = new ParallelTransition(tt, ft);
        pt.play();
    }
}
