package com.inventory.model;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

/**
 * Product model representing inventory items.
 * Contains all product details including stock levels, pricing, and expiry information.
 */
public class Product {
    
    private int id;
    private String sku;
    private String name;
    private String description;
    private Integer categoryId;
    private Integer supplierId;
    private int quantity;
    private String unit;
    private BigDecimal price;
    private BigDecimal costPrice;
    private int lowStockThreshold;
    private LocalDate expiryDate;
    private String barcode;
    private String location;
    private boolean isActive;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    // Additional display fields (from joins)
    private String categoryName;
    private String supplierName;
    
    // Default constructor
    public Product() {
        this.quantity = 0;
        this.unit = "pcs";
        this.price = BigDecimal.ZERO;
        this.costPrice = BigDecimal.ZERO;
        this.lowStockThreshold = 10;
        this.isActive = true;
    }
    
    // Constructor for new product
    public Product(String sku, String name, Integer categoryId, Integer supplierId, 
                   int quantity, BigDecimal price) {
        this();
        this.sku = sku;
        this.name = name;
        this.categoryId = categoryId;
        this.supplierId = supplierId;
        this.quantity = quantity;
        this.price = price;
    }
    
    // Getters and Setters
    public int getId() {
        return id;
    }
    
    public void setId(int id) {
        this.id = id;
    }
    
    public String getSku() {
        return sku;
    }
    
    public void setSku(String sku) {
        this.sku = sku;
    }
    
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public String getDescription() {
        return description;
    }
    
    public void setDescription(String description) {
        this.description = description;
    }
    
    public Integer getCategoryId() {
        return categoryId;
    }
    
    public void setCategoryId(Integer categoryId) {
        this.categoryId = categoryId;
    }
    
    public Integer getSupplierId() {
        return supplierId;
    }
    
    public void setSupplierId(Integer supplierId) {
        this.supplierId = supplierId;
    }
    
    public int getQuantity() {
        return quantity;
    }
    
    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }
    
    public String getUnit() {
        return unit;
    }
    
    public void setUnit(String unit) {
        this.unit = unit;
    }
    
    public BigDecimal getPrice() {
        return price;
    }
    
    public void setPrice(BigDecimal price) {
        this.price = price;
    }
    
    public BigDecimal getCostPrice() {
        return costPrice;
    }
    
    public void setCostPrice(BigDecimal costPrice) {
        this.costPrice = costPrice;
    }
    
    public int getLowStockThreshold() {
        return lowStockThreshold;
    }
    
    public void setLowStockThreshold(int lowStockThreshold) {
        this.lowStockThreshold = lowStockThreshold;
    }
    
    public LocalDate getExpiryDate() {
        return expiryDate;
    }
    
    public void setExpiryDate(LocalDate expiryDate) {
        this.expiryDate = expiryDate;
    }
    
    public String getBarcode() {
        return barcode;
    }
    
    public void setBarcode(String barcode) {
        this.barcode = barcode;
    }
    
    public String getLocation() {
        return location;
    }
    
    public void setLocation(String location) {
        this.location = location;
    }
    
    public boolean isActive() {
        return isActive;
    }
    
    public void setActive(boolean active) {
        isActive = active;
    }
    
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
    
    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
    
    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
    
    public String getCategoryName() {
        return categoryName;
    }
    
    public void setCategoryName(String categoryName) {
        this.categoryName = categoryName;
    }
    
    public String getSupplierName() {
        return supplierName;
    }
    
    public void setSupplierName(String supplierName) {
        this.supplierName = supplierName;
    }
    
    // Utility methods
    
    /**
     * Checks if the product is low on stock based on its threshold.
     */
    public boolean isLowStock() {
        return quantity <= lowStockThreshold;
    }
    
    /**
     * Checks if the product is out of stock.
     */
    public boolean isOutOfStock() {
        return quantity <= 0;
    }
    
    /**
     * Checks if the product is expiring within the specified days.
     */
    public boolean isExpiringSoon(int days) {
        if (expiryDate == null) {
            return false;
        }
        long daysUntilExpiry = ChronoUnit.DAYS.between(LocalDate.now(), expiryDate);
        return daysUntilExpiry >= 0 && daysUntilExpiry <= days;
    }
    
    /**
     * Checks if the product has already expired.
     */
    public boolean isExpired() {
        if (expiryDate == null) {
            return false;
        }
        return LocalDate.now().isAfter(expiryDate);
    }
    
    /**
     * Gets the number of days until expiry.
     */
    public long getDaysUntilExpiry() {
        if (expiryDate == null) {
            return Long.MAX_VALUE;
        }
        return ChronoUnit.DAYS.between(LocalDate.now(), expiryDate);
    }
    
    /**
     * Calculates the total value of this product in stock.
     */
    public BigDecimal getTotalValue() {
        return price.multiply(BigDecimal.valueOf(quantity));
    }
    
    /**
     * Calculates the profit margin per unit.
     */
    public BigDecimal getProfitMargin() {
        if (costPrice == null || costPrice.compareTo(BigDecimal.ZERO) == 0) {
            return price;
        }
        return price.subtract(costPrice);
    }
    
    /**
     * Gets the stock status as a human-readable string.
     */
    public String getStockStatus() {
        if (isOutOfStock()) {
            return "Out of Stock";
        } else if (isLowStock()) {
            return "Low Stock";
        }
        return "In Stock";
    }
    
    /**
     * Gets the expiry status as a human-readable string.
     */
    public String getExpiryStatus() {
        if (expiryDate == null) {
            return "N/A";
        }
        if (isExpired()) {
            return "Expired";
        }
        long days = getDaysUntilExpiry();
        if (days <= 7) {
            return "Expiring Soon (" + days + " days)";
        }
        return "Valid";
    }
    
    @Override
    public String toString() {
        return "Product{" +
                "id=" + id +
                ", sku='" + sku + '\'' +
                ", name='" + name + '\'' +
                ", quantity=" + quantity +
                ", price=" + price +
                '}';
    }
}
