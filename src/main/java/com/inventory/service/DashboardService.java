package com.inventory.service;

import com.inventory.dao.*;
import com.inventory.model.Category;
import com.inventory.model.InventoryLog;
import com.inventory.model.Product;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Service class for dashboard statistics and analytics.
 */
public class DashboardService {
    
    private static final Logger logger = LoggerFactory.getLogger(DashboardService.class);
    
    private final ProductDAO productDAO;
    private final CategoryDAO categoryDAO;
    private final SupplierDAO supplierDAO;
    private final InventoryLogDAO inventoryLogDAO;
    
    public DashboardService() {
        this.productDAO = new ProductDAOImpl();
        this.categoryDAO = new CategoryDAOImpl();
        this.supplierDAO = new SupplierDAOImpl();
        this.inventoryLogDAO = new InventoryLogDAOImpl();
    }
    
    /**
     * Gets all dashboard statistics.
     */
    public DashboardStats getStats() {
        DashboardStats stats = new DashboardStats();
        
        stats.totalProducts = productDAO.count();
        stats.lowStockCount = productDAO.countLowStock();
        stats.expiringSoonCount = productDAO.countExpiringSoon(7);
        stats.totalCategories = categoryDAO.count();
        stats.totalSuppliers = supplierDAO.count();
        stats.todayActivities = inventoryLogDAO.countTodayActivities();
        
        // Calculate total inventory value
        List<Product> allProducts = productDAO.findAll();
        stats.totalInventoryValue = allProducts.stream()
            .map(p -> p.getPrice().multiply(BigDecimal.valueOf(p.getQuantity())))
            .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        stats.totalItems = allProducts.stream()
            .mapToInt(Product::getQuantity)
            .sum();
        
        logger.debug("Dashboard stats calculated: {} products, {} low stock, {} expiring",
                    stats.totalProducts, stats.lowStockCount, stats.expiringSoonCount);
        
        return stats;
    }
    
    /**
     * Gets category distribution data for pie chart.
     */
    public Map<String, Integer> getCategoryDistribution() {
        Map<String, Integer> distribution = new HashMap<>();
        List<Category> categories = categoryDAO.findAllWithProductCount();
        
        for (Category category : categories) {
            if (category.getProductCount() > 0) {
                distribution.put(category.getName(), category.getProductCount());
            }
        }
        
        return distribution;
    }
    
    /**
     * Gets stock level data for bar chart.
     */
    public Map<String, StockLevel> getStockLevels() {
        Map<String, StockLevel> levels = new HashMap<>();
        List<Product> products = productDAO.findAll();
        
        // Get top 10 products by value
        products.stream()
            .sorted((a, b) -> b.getTotalValue().compareTo(a.getTotalValue()))
            .limit(10)
            .forEach(p -> {
                StockLevel level = new StockLevel();
                level.quantity = p.getQuantity();
                level.threshold = p.getLowStockThreshold();
                level.isLowStock = p.isLowStock();
                levels.put(p.getName(), level);
            });
        
        return levels;
    }
    
    /**
     * Gets recent inventory activity.
     */
    public List<InventoryLog> getRecentActivity(int limit) {
        return inventoryLogDAO.findRecent(limit);
    }
    
    /**
     * Gets low stock products for dashboard display.
     */
    public List<Product> getLowStockProducts(int limit) {
        return productDAO.findLowStock().stream()
            .limit(limit)
            .toList();
    }
    
    /**
     * Gets expiring soon products for dashboard display.
     */
    public List<Product> getExpiringSoonProducts(int limit) {
        return productDAO.findExpiringSoon(7).stream()
            .limit(limit)
            .toList();
    }
    
    /**
     * Dashboard statistics data class.
     */
    public static class DashboardStats {
        public int totalProducts;
        public int totalItems;
        public int lowStockCount;
        public int expiringSoonCount;
        public int totalCategories;
        public int totalSuppliers;
        public int todayActivities;
        public BigDecimal totalInventoryValue = BigDecimal.ZERO;
        
        public String getFormattedValue() {
            if (totalInventoryValue.compareTo(new BigDecimal("1000000")) >= 0) {
                return String.format("$%.1fM", totalInventoryValue.doubleValue() / 1000000);
            } else if (totalInventoryValue.compareTo(new BigDecimal("1000")) >= 0) {
                return String.format("$%.1fK", totalInventoryValue.doubleValue() / 1000);
            }
            return String.format("$%.2f", totalInventoryValue);
        }
    }
    
    /**
     * Stock level data class.
     */
    public static class StockLevel {
        public int quantity;
        public int threshold;
        public boolean isLowStock;
    }
}
