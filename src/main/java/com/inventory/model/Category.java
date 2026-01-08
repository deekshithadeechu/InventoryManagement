package com.inventory.model;

import java.time.LocalDateTime;

/**
 * Category model for organizing products into logical groups.
 */
public class Category {
    
    private int id;
    private String name;
    private String description;
    private String color;
    private boolean isActive;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    // Additional field for display
    private int productCount;
    
    // Default constructor
    public Category() {
        this.color = "#3498db";
        this.isActive = true;
    }
    
    // Constructor for new category
    public Category(String name, String description) {
        this();
        this.name = name;
        this.description = description;
    }
    
    // Constructor with color
    public Category(String name, String description, String color) {
        this(name, description);
        this.color = color;
    }
    
    // Getters and Setters
    public int getId() {
        return id;
    }
    
    public void setId(int id) {
        this.id = id;
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
    
    public String getColor() {
        return color;
    }
    
    public void setColor(String color) {
        this.color = color;
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
    
    public int getProductCount() {
        return productCount;
    }
    
    public void setProductCount(int productCount) {
        this.productCount = productCount;
    }
    
    @Override
    public String toString() {
        return name; // For use in ComboBox
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Category category = (Category) o;
        return id == category.id;
    }
    
    @Override
    public int hashCode() {
        return Integer.hashCode(id);
    }
}
