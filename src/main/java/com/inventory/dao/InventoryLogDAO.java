package com.inventory.dao;

import com.inventory.model.InventoryLog;

import java.time.LocalDate;
import java.util.List;

/**
 * Data Access Object interface for InventoryLog operations.
 */
public interface InventoryLogDAO {
    
    InventoryLog create(InventoryLog log);
    
    List<InventoryLog> findAll();
    
    List<InventoryLog> findByProductId(int productId);
    
    List<InventoryLog> findByUserId(int userId);
    
    List<InventoryLog> findByDateRange(LocalDate startDate, LocalDate endDate);
    
    List<InventoryLog> findRecent(int limit);
    
    int countTodayActivities();
}
