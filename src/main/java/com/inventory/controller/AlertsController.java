package com.inventory.controller;

import com.inventory.service.AlertService;
import com.inventory.service.AlertService.Alert;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * Controller for the alerts view.
 */
public class AlertsController {
    
    private static final Logger logger = LoggerFactory.getLogger(AlertsController.class);
    
    @FXML private VBox alertsContainer;
    @FXML private Label criticalCountLabel;
    @FXML private Label warningCountLabel;
    @FXML private Label totalCountLabel;
    @FXML private ComboBox<String> filterCombo;
    @FXML private Button refreshButton;
    
    private final AlertService alertService = new AlertService();
    
    @FXML
    public void initialize() {
        setupFilters();
        Platform.runLater(this::loadAlerts);
    }
    
    /**
     * Sets up filter controls.
     */
    private void setupFilters() {
        if (filterCombo != null) {
            filterCombo.getItems().addAll("All Alerts", "Low Stock", "Expiring Soon", "Critical Only");
            filterCombo.setValue("All Alerts");
            filterCombo.valueProperty().addListener((obs, old, newVal) -> loadAlerts());
        }
    }
    
    /**
     * Loads alerts.
     */
    private void loadAlerts() {
        updateCounts();
        
        if (alertsContainer == null) return;
        
        alertsContainer.getChildren().clear();
        
        List<Alert> alerts = alertService.getAllAlerts();
        String filter = filterCombo != null ? filterCombo.getValue() : "All Alerts";
        
        // Filter alerts
        List<Alert> filteredAlerts = alerts.stream()
            .filter(a -> {
                if (filter.equals("Low Stock")) {
                    return a.getType() == AlertService.AlertType.LOW_STOCK;
                } else if (filter.equals("Expiring Soon")) {
                    return a.getType() == AlertService.AlertType.EXPIRING_SOON || 
                           a.getType() == AlertService.AlertType.EXPIRED;
                } else if (filter.equals("Critical Only")) {
                    return a.getSeverity() == AlertService.AlertSeverity.CRITICAL;
                }
                return true;
            })
            .toList();
        
        if (filteredAlerts.isEmpty()) {
            Label noAlertsLabel = new Label("🎉 No alerts! Everything looks good.");
            noAlertsLabel.getStyleClass().add("no-alerts-label");
            alertsContainer.getChildren().add(noAlertsLabel);
            return;
        }
        
        for (Alert alert : filteredAlerts) {
            HBox alertCard = createAlertCard(alert);
            alertsContainer.getChildren().add(alertCard);
        }
        
        logger.debug("Loaded {} alerts", filteredAlerts.size());
    }
    
    /**
     * Updates count labels.
     */
    private void updateCounts() {
        int critical = alertService.getCriticalAlertCount();
        int warning = alertService.getWarningAlertCount();
        int total = critical + warning;
        
        if (criticalCountLabel != null) {
            criticalCountLabel.setText(String.valueOf(critical));
        }
        if (warningCountLabel != null) {
            warningCountLabel.setText(String.valueOf(warning));
        }
        if (totalCountLabel != null) {
            totalCountLabel.setText(String.valueOf(total));
        }
    }
    
    /**
     * Creates an alert card.
     */
    private HBox createAlertCard(Alert alert) {
        HBox card = new HBox(15);
        card.getStyleClass().add("alert-card");
        card.getStyleClass().add(alert.getSeverityClass());
        card.setPadding(new Insets(15));
        
        // Icon
        Label iconLabel = new Label(alert.getTypeIcon());
        iconLabel.getStyleClass().add("alert-icon");
        iconLabel.setStyle("-fx-font-size: 24px;");
        
        // Content
        VBox content = new VBox(5);
        HBox.setHgrow(content, Priority.ALWAYS);
        
        Label messageLabel = new Label(alert.getMessage());
        messageLabel.getStyleClass().add("alert-message");
        messageLabel.setWrapText(true);
        
        HBox badges = new HBox(10);
        
        Label typeBadge = new Label(alert.getType().name().replace("_", " "));
        typeBadge.getStyleClass().addAll("badge", "badge-" + alert.getType().name().toLowerCase());
        
        Label severityBadge = new Label(alert.getSeverity().name());
        severityBadge.getStyleClass().addAll("badge", "severity-" + alert.getSeverity().name().toLowerCase());
        
        badges.getChildren().addAll(typeBadge, severityBadge);
        
        content.getChildren().addAll(messageLabel, badges);
        
        // Product info
        VBox productInfo = new VBox(5);
        productInfo.setMinWidth(120);
        
        if (alert.getProduct() != null) {
            Label skuLabel = new Label("SKU: " + alert.getProduct().getSku());
            skuLabel.getStyleClass().add("alert-product-sku");
            
            Label qtyLabel = new Label("Qty: " + alert.getProduct().getQuantity());
            qtyLabel.getStyleClass().add("alert-product-qty");
            
            productInfo.getChildren().addAll(skuLabel, qtyLabel);
        }
        
        card.getChildren().addAll(iconLabel, content, productInfo);
        
        return card;
    }
    
    /**
     * Handles refresh button.
     */
    @FXML
    private void handleRefresh() {
        loadAlerts();
    }
}
