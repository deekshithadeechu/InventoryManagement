package com.inventory.controller;

import com.inventory.model.InventoryLog;
import com.inventory.model.Product;
import com.inventory.service.AlertService;
import com.inventory.service.DashboardService;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.chart.*;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

/**
 * Controller for the dashboard view.
 */
public class DashboardController {
    
    private static final Logger logger = LoggerFactory.getLogger(DashboardController.class);
    
    @FXML private Label totalProductsLabel;
    @FXML private Label totalItemsLabel;
    @FXML private Label lowStockLabel;
    @FXML private Label expiringSoonLabel;
    @FXML private Label inventoryValueLabel;
    @FXML private Label todayActivityLabel;
    
    @FXML private PieChart categoryChart;
    @FXML private BarChart<String, Number> stockChart;
    @FXML private CategoryAxis stockChartXAxis;
    @FXML private NumberAxis stockChartYAxis;
    
    @FXML private VBox lowStockList;
    @FXML private VBox expiringList;
    @FXML private VBox recentActivityList;
    
    @FXML private HBox statsContainer;
    
    private final DashboardService dashboardService = new DashboardService();
    private final AlertService alertService = new AlertService();
    
    @FXML
    public void initialize() {
        Platform.runLater(this::loadDashboardData);
    }
    
    /**
     * Loads all dashboard data.
     */
    private void loadDashboardData() {
        loadStatistics();
        loadCategoryChart();
        loadStockChart();
        loadLowStockItems();
        loadExpiringItems();
        loadRecentActivity();
    }
    
    /**
     * Loads statistics cards.
     */
    private void loadStatistics() {
        DashboardService.DashboardStats stats = dashboardService.getStats();
        
        if (totalProductsLabel != null) {
            totalProductsLabel.setText(String.valueOf(stats.totalProducts));
        }
        if (totalItemsLabel != null) {
            totalItemsLabel.setText(String.valueOf(stats.totalItems));
        }
        if (lowStockLabel != null) {
            lowStockLabel.setText(String.valueOf(stats.lowStockCount));
            if (stats.lowStockCount > 0) {
                lowStockLabel.getStyleClass().add("stat-warning");
            }
        }
        if (expiringSoonLabel != null) {
            expiringSoonLabel.setText(String.valueOf(stats.expiringSoonCount));
            if (stats.expiringSoonCount > 0) {
                expiringSoonLabel.getStyleClass().add("stat-warning");
            }
        }
        if (inventoryValueLabel != null) {
            inventoryValueLabel.setText(stats.getFormattedValue());
        }
        if (todayActivityLabel != null) {
            todayActivityLabel.setText(String.valueOf(stats.todayActivities));
        }
        
        logger.debug("Dashboard statistics loaded");
    }
    
    /**
     * Loads category distribution pie chart.
     */
    private void loadCategoryChart() {
        if (categoryChart == null) return;
        
        Map<String, Integer> distribution = dashboardService.getCategoryDistribution();
        
        categoryChart.getData().clear();
        
        for (Map.Entry<String, Integer> entry : distribution.entrySet()) {
            PieChart.Data slice = new PieChart.Data(
                entry.getKey() + " (" + entry.getValue() + ")", 
                entry.getValue()
            );
            categoryChart.getData().add(slice);
        }
        
        categoryChart.setTitle("Products by Category");
        categoryChart.setLegendVisible(true);
        categoryChart.setLabelsVisible(true);
        
        // Add tooltips
        for (PieChart.Data data : categoryChart.getData()) {
            Tooltip tooltip = new Tooltip(data.getName());
            Tooltip.install(data.getNode(), tooltip);
            
            data.getNode().setOnMouseEntered(e -> 
                data.getNode().setStyle("-fx-pie-color: derive(-fx-background, -20%);")
            );
            data.getNode().setOnMouseExited(e -> 
                data.getNode().setStyle("")
            );
        }
        
        logger.debug("Category chart loaded with {} categories", distribution.size());
    }
    
    /**
     * Loads stock level bar chart.
     */
    private void loadStockChart() {
        if (stockChart == null) return;
        
        Map<String, DashboardService.StockLevel> stockLevels = dashboardService.getStockLevels();
        
        stockChart.getData().clear();
        
        XYChart.Series<String, Number> quantitySeries = new XYChart.Series<>();
        quantitySeries.setName("Stock Quantity");
        
        XYChart.Series<String, Number> thresholdSeries = new XYChart.Series<>();
        thresholdSeries.setName("Low Stock Threshold");
        
        for (Map.Entry<String, DashboardService.StockLevel> entry : stockLevels.entrySet()) {
            String name = entry.getKey();
            // Truncate long names
            if (name.length() > 15) {
                name = name.substring(0, 12) + "...";
            }
            
            quantitySeries.getData().add(new XYChart.Data<>(name, entry.getValue().quantity));
            thresholdSeries.getData().add(new XYChart.Data<>(name, entry.getValue().threshold));
        }
        
        stockChart.getData().addAll(quantitySeries, thresholdSeries);
        stockChart.setTitle("Stock Levels (Top 10 by Value)");
        
        logger.debug("Stock chart loaded with {} products", stockLevels.size());
    }
    
    /**
     * Loads low stock items list.
     */
    private void loadLowStockItems() {
        if (lowStockList == null) return;
        
        lowStockList.getChildren().clear();
        
        List<Product> lowStockProducts = dashboardService.getLowStockProducts(5);
        
        if (lowStockProducts.isEmpty()) {
            Label noDataLabel = new Label("No low stock items");
            noDataLabel.getStyleClass().add("no-data-label");
            lowStockList.getChildren().add(noDataLabel);
            return;
        }
        
        for (Product product : lowStockProducts) {
            HBox item = createProductListItem(product, true);
            lowStockList.getChildren().add(item);
        }
    }
    
    /**
     * Loads expiring items list.
     */
    private void loadExpiringItems() {
        if (expiringList == null) return;
        
        expiringList.getChildren().clear();
        
        List<Product> expiringProducts = dashboardService.getExpiringSoonProducts(5);
        
        if (expiringProducts.isEmpty()) {
            Label noDataLabel = new Label("No expiring items");
            noDataLabel.getStyleClass().add("no-data-label");
            expiringList.getChildren().add(noDataLabel);
            return;
        }
        
        for (Product product : expiringProducts) {
            HBox item = createProductListItem(product, false);
            expiringList.getChildren().add(item);
        }
    }
    
    /**
     * Creates a product list item for display.
     */
    private HBox createProductListItem(Product product, boolean showStock) {
        HBox item = new HBox(10);
        item.getStyleClass().add("dashboard-list-item");
        item.setPadding(new Insets(8, 12, 8, 12));
        
        VBox info = new VBox(2);
        Label nameLabel = new Label(product.getName());
        nameLabel.getStyleClass().add("item-name");
        
        Label detailLabel = new Label();
        detailLabel.getStyleClass().add("item-detail");
        
        if (showStock) {
            detailLabel.setText(product.getQuantity() + " " + product.getUnit() + " remaining");
            if (product.isOutOfStock()) {
                item.getStyleClass().add("critical");
            } else {
                item.getStyleClass().add("warning");
            }
        } else {
            long days = product.getDaysUntilExpiry();
            detailLabel.setText("Expires in " + days + " days");
            if (days <= 3) {
                item.getStyleClass().add("critical");
            } else {
                item.getStyleClass().add("warning");
            }
        }
        
        info.getChildren().addAll(nameLabel, detailLabel);
        
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        
        Label skuLabel = new Label(product.getSku());
        skuLabel.getStyleClass().add("item-sku");
        
        item.getChildren().addAll(info, spacer, skuLabel);
        
        return item;
    }
    
    /**
     * Loads recent activity list.
     */
    private void loadRecentActivity() {
        if (recentActivityList == null) return;
        
        recentActivityList.getChildren().clear();
        
        List<InventoryLog> recentLogs = dashboardService.getRecentActivity(5);
        
        if (recentLogs.isEmpty()) {
            Label noDataLabel = new Label("No recent activity");
            noDataLabel.getStyleClass().add("no-data-label");
            recentActivityList.getChildren().add(noDataLabel);
            return;
        }
        
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMM dd, HH:mm");
        
        for (InventoryLog log : recentLogs) {
            HBox item = new HBox(10);
            item.getStyleClass().add("activity-item");
            item.setPadding(new Insets(8, 12, 8, 12));
            
            Label iconLabel = new Label(getActionIcon(log.getAction()));
            iconLabel.getStyleClass().add("activity-icon");
            
            VBox info = new VBox(2);
            Label actionLabel = new Label(log.getActionDisplay() + ": " + 
                (log.getProductName() != null ? log.getProductName() : "Product #" + log.getProductId()));
            actionLabel.getStyleClass().add("activity-action");
            
            Label timeLabel = new Label(log.getCreatedAt().format(formatter) + 
                " by " + (log.getUserName() != null ? log.getUserName() : "User"));
            timeLabel.getStyleClass().add("activity-time");
            
            info.getChildren().addAll(actionLabel, timeLabel);
            
            Region spacer = new Region();
            HBox.setHgrow(spacer, Priority.ALWAYS);
            
            Label changeLabel = new Label(log.getChangeDescription());
            changeLabel.getStyleClass().add("activity-change");
            
            item.getChildren().addAll(iconLabel, info, spacer, changeLabel);
            recentActivityList.getChildren().add(item);
        }
    }
    
    /**
     * Gets icon for inventory action.
     */
    private String getActionIcon(InventoryLog.Action action) {
        return switch (action) {
            case ADD -> "➕";
            case UPDATE -> "✏️";
            case DELETE -> "🗑️";
            case STOCK_IN -> "📥";
            case STOCK_OUT -> "📤";
            case ADJUSTMENT -> "🔄";
        };
    }
    
    /**
     * Refreshes all dashboard data.
     */
    @FXML
    private void handleRefresh() {
        loadDashboardData();
        logger.info("Dashboard refreshed");
    }
}
