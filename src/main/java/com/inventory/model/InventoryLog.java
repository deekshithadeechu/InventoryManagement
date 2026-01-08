package com.inventory.model;

import java.time.LocalDateTime;

/**
 * InventoryLog model for tracking all inventory changes and activities.
 * Used for audit trail and reporting.
 */
public class InventoryLog {
    
    public enum Action {
        ADD("Added"),
        UPDATE("Updated"),
        DELETE("Deleted"),
        STOCK_IN("Stock In"),
        STOCK_OUT("Stock Out"),
        ADJUSTMENT("Adjustment");
        
        private final String displayName;
        
        Action(String displayName) {
            this.displayName = displayName;
        }
        
        public String getDisplayName() {
            return displayName;
        }
    }
    
    private int id;
    private int productId;
    private int userId;
    private Action action;
    private Integer quantityBefore;
    private Integer quantityAfter;
    private Integer quantityChange;
    private String notes;
    private LocalDateTime createdAt;
    
    // Additional display fields (from joins)
    private String productName;
    private String productSku;
    private String userName;
    
    // Default constructor
    public InventoryLog() {
        this.createdAt = LocalDateTime.now();
    }
    
    // Constructor for creating new log entry
    public InventoryLog(int productId, int userId, Action action, Integer quantityBefore, 
                        Integer quantityAfter, String notes) {
        this();
        this.productId = productId;
        this.userId = userId;
        this.action = action;
        this.quantityBefore = quantityBefore;
        this.quantityAfter = quantityAfter;
        this.quantityChange = (quantityAfter != null && quantityBefore != null) 
                              ? quantityAfter - quantityBefore : null;
        this.notes = notes;
    }
    
    // Getters and Setters
    public int getId() {
        return id;
    }
    
    public void setId(int id) {
        this.id = id;
    }
    
    public int getProductId() {
        return productId;
    }
    
    public void setProductId(int productId) {
        this.productId = productId;
    }
    
    public int getUserId() {
        return userId;
    }
    
    public void setUserId(int userId) {
        this.userId = userId;
    }
    
    public Action getAction() {
        return action;
    }
    
    public void setAction(Action action) {
        this.action = action;
    }
    
    public Integer getQuantityBefore() {
        return quantityBefore;
    }
    
    public void setQuantityBefore(Integer quantityBefore) {
        this.quantityBefore = quantityBefore;
    }
    
    public Integer getQuantityAfter() {
        return quantityAfter;
    }
    
    public void setQuantityAfter(Integer quantityAfter) {
        this.quantityAfter = quantityAfter;
    }
    
    public Integer getQuantityChange() {
        return quantityChange;
    }
    
    public void setQuantityChange(Integer quantityChange) {
        this.quantityChange = quantityChange;
    }
    
    public String getNotes() {
        return notes;
    }
    
    public void setNotes(String notes) {
        this.notes = notes;
    }
    
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
    
    public String getProductName() {
        return productName;
    }
    
    public void setProductName(String productName) {
        this.productName = productName;
    }
    
    public String getProductSku() {
        return productSku;
    }
    
    public void setProductSku(String productSku) {
        this.productSku = productSku;
    }
    
    public String getUserName() {
        return userName;
    }
    
    public void setUserName(String userName) {
        this.userName = userName;
    }
    
    /**
     * Gets a formatted string describing the quantity change.
     */
    public String getChangeDescription() {
        if (quantityChange == null) {
            return "N/A";
        }
        if (quantityChange > 0) {
            return "+" + quantityChange;
        }
        return String.valueOf(quantityChange);
    }
    
    /**
     * Gets the action display name.
     */
    public String getActionDisplay() {
        return action != null ? action.getDisplayName() : "Unknown";
    }
    
    @Override
    public String toString() {
        return "InventoryLog{" +
                "id=" + id +
                ", productId=" + productId +
                ", action=" + action +
                ", quantityChange=" + quantityChange +
                ", createdAt=" + createdAt +
                '}';
    }
}
