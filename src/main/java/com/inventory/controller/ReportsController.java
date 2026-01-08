package com.inventory.controller;

import com.inventory.model.Product;
import com.inventory.service.ProductService;
import com.inventory.util.AlertUtil;
import com.inventory.util.ReportGenerator;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Controller for the reports view.
 */
public class ReportsController {
    
    private static final Logger logger = LoggerFactory.getLogger(ReportsController.class);
    
    @FXML private ComboBox<String> reportTypeCombo;
    @FXML private DatePicker startDatePicker;
    @FXML private DatePicker endDatePicker;
    @FXML private ComboBox<String> formatCombo;
    @FXML private CheckBox includeExpiredCheck;
    @FXML private CheckBox includeLowStockCheck;
    @FXML private Button generateButton;
    @FXML private ProgressIndicator loadingIndicator;
    @FXML private Label statusLabel;
    
    private final ProductService productService = new ProductService();
    
    @FXML
    public void initialize() {
        setupControls();
    }
    
    /**
     * Sets up the form controls.
     */
    private void setupControls() {
        // Report types
        reportTypeCombo.getItems().addAll(
            "Full Inventory Report",
            "Low Stock Report",
            "Expiring Products Report",
            "Category Summary Report"
        );
        reportTypeCombo.setValue("Full Inventory Report");
        
        // Export formats
        formatCombo.getItems().addAll("PDF", "CSV");
        formatCombo.setValue("PDF");
        
        // Date pickers default values
        startDatePicker.setValue(LocalDate.now().minusMonths(1));
        endDatePicker.setValue(LocalDate.now());
        
        // Loading indicator
        if (loadingIndicator != null) {
            loadingIndicator.setVisible(false);
        }
        
        // Report type change listener
        reportTypeCombo.valueProperty().addListener((obs, old, newVal) -> {
            updateFormVisibility(newVal);
        });
    }
    
    /**
     * Updates form visibility based on report type.
     */
    private void updateFormVisibility(String reportType) {
        boolean showDates = reportType.contains("Summary");
        if (startDatePicker != null) {
            startDatePicker.setDisable(!showDates);
        }
        if (endDatePicker != null) {
            endDatePicker.setDisable(!showDates);
        }
    }
    
    /**
     * Handles generate button click.
     */
    @FXML
    private void handleGenerate() {
        String reportType = reportTypeCombo.getValue();
        String format = formatCombo.getValue();
        
        if (reportType == null || format == null) {
            AlertUtil.showError("Error", "Please select report type and format");
            return;
        }
        
        // File chooser
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Save Report");
        
        String timestamp = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        String fileName = reportType.replace(" ", "_") + "_" + timestamp;
        
        if (format.equals("PDF")) {
            fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("PDF Files", "*.pdf")
            );
            fileChooser.setInitialFileName(fileName + ".pdf");
        } else {
            fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("CSV Files", "*.csv")
            );
            fileChooser.setInitialFileName(fileName + ".csv");
        }
        
        File file = fileChooser.showSaveDialog(generateButton.getScene().getWindow());
        
        if (file != null) {
            generateReport(reportType, format, file);
        }
    }
    
    /**
     * Generates the report.
     */
    private void generateReport(String reportType, String format, File file) {
        setLoading(true);
        updateStatus("Generating report...");
        
        new Thread(() -> {
            try {
                List<Product> products = getProductsForReport(reportType);
                
                if (products.isEmpty()) {
                    Platform.runLater(() -> {
                        setLoading(false);
                        AlertUtil.showWarning("No Data", "No products found for the selected criteria");
                        updateStatus("");
                    });
                    return;
                }
                
                String title = reportType;
                
                if (format.equals("PDF")) {
                    ReportGenerator.generatePdfReport(products, file, title);
                } else {
                    ReportGenerator.generateCsvReport(products, file);
                }
                
                Platform.runLater(() -> {
                    setLoading(false);
                    updateStatus("Report generated successfully!");
                    AlertUtil.showSuccess("Success", 
                        "Report generated successfully!\n\nLocation: " + file.getAbsolutePath());
                });
                
                logger.info("Report generated: {} ({})", reportType, format);
                
            } catch (Exception e) {
                logger.error("Error generating report", e);
                Platform.runLater(() -> {
                    setLoading(false);
                    updateStatus("Error generating report");
                    AlertUtil.showError("Error", "Failed to generate report: " + e.getMessage());
                });
            }
        }).start();
    }
    
    /**
     * Gets products based on report type.
     */
    private List<Product> getProductsForReport(String reportType) {
        return switch (reportType) {
            case "Low Stock Report" -> productService.getLowStockProducts();
            case "Expiring Products Report" -> productService.getExpiringSoonProducts(30);
            default -> productService.getAllProducts();
        };
    }
    
    /**
     * Sets loading state.
     */
    private void setLoading(boolean loading) {
        if (loadingIndicator != null) {
            loadingIndicator.setVisible(loading);
        }
        generateButton.setDisable(loading);
    }
    
    /**
     * Updates status label.
     */
    private void updateStatus(String message) {
        if (statusLabel != null) {
            statusLabel.setText(message);
        }
    }
    
    /**
     * Handles quick export to PDF.
     */
    @FXML
    private void handleQuickPdf() {
        reportTypeCombo.setValue("Full Inventory Report");
        formatCombo.setValue("PDF");
        handleGenerate();
    }
    
    /**
     * Handles quick export to CSV.
     */
    @FXML
    private void handleQuickCsv() {
        reportTypeCombo.setValue("Full Inventory Report");
        formatCombo.setValue("CSV");
        handleGenerate();
    }
}
