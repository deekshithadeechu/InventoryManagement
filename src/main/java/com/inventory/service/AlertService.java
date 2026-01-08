package com.inventory.service;

import com.inventory.dao.ProductDAO;
import com.inventory.dao.ProductDAOImpl;
import com.inventory.model.Product;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

/**
 * Service class for managing stock and expiry alerts.
 */
public class AlertService {
    
    private static final Logger logger = LoggerFactory.getLogger(AlertService.class);
    
    private static final int DEFAULT_EXPIRY_ALERT_DAYS = 7;
    
    private final ProductDAO productDAO;
    
    public AlertService() {
        this.productDAO = new ProductDAOImpl();
    }
    
    /**
     * Gets all current alerts (low stock + expiring products).
     */
    public List<Alert> getAllAlerts() {
        List<Alert> alerts = new ArrayList<>();
        alerts.addAll(getLowStockAlerts());
        alerts.addAll(getExpiryAlerts(DEFAULT_EXPIRY_ALERT_DAYS));
        return alerts;
    }
    
    /**
     * Gets low stock alerts.
     */
    public List<Alert> getLowStockAlerts() {
        List<Alert> alerts = new ArrayList<>();
        List<Product> lowStockProducts = productDAO.findLowStock();
        
        for (Product product : lowStockProducts) {
            AlertSeverity severity;
            String message;
            
            if (product.isOutOfStock()) {
                severity = AlertSeverity.CRITICAL;
                message = String.format("OUT OF STOCK: %s (%s)", 
                    product.getName(), product.getSku());
            } else {
                severity = AlertSeverity.WARNING;
                message = String.format("Low stock: %s (%s) - Only %d %s remaining (threshold: %d)",
                    product.getName(), product.getSku(), product.getQuantity(), 
                    product.getUnit(), product.getLowStockThreshold());
            }
            
            alerts.add(new Alert(AlertType.LOW_STOCK, severity, message, product));
        }
        
        logger.debug("Found {} low stock alerts", alerts.size());
        return alerts;
    }
    
    /**
     * Gets expiry alerts for products expiring within specified days.
     */
    public List<Alert> getExpiryAlerts(int days) {
        List<Alert> alerts = new ArrayList<>();
        
        // Get expired products
        List<Product> expiredProducts = productDAO.findExpired();
        for (Product product : expiredProducts) {
            String message = String.format("EXPIRED: %s (%s) - Expired on %s",
                product.getName(), product.getSku(), product.getExpiryDate());
            alerts.add(new Alert(AlertType.EXPIRED, AlertSeverity.CRITICAL, message, product));
        }
        
        // Get expiring soon products
        List<Product> expiringSoon = productDAO.findExpiringSoon(days);
        for (Product product : expiringSoon) {
            long daysUntilExpiry = product.getDaysUntilExpiry();
            AlertSeverity severity = daysUntilExpiry <= 3 ? AlertSeverity.WARNING : AlertSeverity.INFO;
            
            String message = String.format("Expiring soon: %s (%s) - Expires in %d days on %s",
                product.getName(), product.getSku(), daysUntilExpiry, product.getExpiryDate());
            alerts.add(new Alert(AlertType.EXPIRING_SOON, severity, message, product));
        }
        
        logger.debug("Found {} expiry alerts", alerts.size());
        return alerts;
    }
    
    /**
     * Gets the count of critical alerts.
     */
    public int getCriticalAlertCount() {
        int count = 0;
        
        // Count out-of-stock products
        for (Product p : productDAO.findLowStock()) {
            if (p.isOutOfStock()) count++;
        }
        
        // Count expired products
        count += productDAO.findExpired().size();
        
        return count;
    }
    
    /**
     * Gets the count of warning alerts.
     */
    public int getWarningAlertCount() {
        int count = 0;
        
        // Count low stock (but not out of stock) products
        for (Product p : productDAO.findLowStock()) {
            if (!p.isOutOfStock()) count++;
        }
        
        // Count products expiring soon
        count += productDAO.countExpiringSoon(DEFAULT_EXPIRY_ALERT_DAYS);
        
        return count;
    }
    
    /**
     * Gets total alert count.
     */
    public int getTotalAlertCount() {
        return productDAO.countLowStock() + 
               productDAO.findExpired().size() + 
               productDAO.countExpiringSoon(DEFAULT_EXPIRY_ALERT_DAYS);
    }
    
    /**
     * Alert types.
     */
    public enum AlertType {
        LOW_STOCK,
        EXPIRING_SOON,
        EXPIRED
    }
    
    /**
     * Alert severity levels.
     */
    public enum AlertSeverity {
        INFO,
        WARNING,
        CRITICAL
    }
    
    /**
     * Alert data class.
     */
    public static class Alert {
        private final AlertType type;
        private final AlertSeverity severity;
        private final String message;
        private final Product product;
        
        public Alert(AlertType type, AlertSeverity severity, String message, Product product) {
            this.type = type;
            this.severity = severity;
            this.message = message;
            this.product = product;
        }
        
        public AlertType getType() {
            return type;
        }
        
        public AlertSeverity getSeverity() {
            return severity;
        }
        
        public String getMessage() {
            return message;
        }
        
        public Product getProduct() {
            return product;
        }
        
        public String getTypeIcon() {
            return switch (type) {
                case LOW_STOCK -> "📦";
                case EXPIRING_SOON -> "⏰";
                case EXPIRED -> "⚠️";
            };
        }
        
        public String getSeverityClass() {
            return switch (severity) {
                case CRITICAL -> "alert-critical";
                case WARNING -> "alert-warning";
                case INFO -> "alert-info";
            };
        }
    }
}
