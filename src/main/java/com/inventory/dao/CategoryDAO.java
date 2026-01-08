package com.inventory.dao;

import com.inventory.model.Category;

import java.util.List;
import java.util.Optional;

/**
 * Data Access Object interface for Category operations.
 */
public interface CategoryDAO {
    
    Category create(Category category);
    
    Optional<Category> findById(int id);
    
    Optional<Category> findByName(String name);
    
    List<Category> findAll();
    
    List<Category> findAllActive();
    
    List<Category> findAllWithProductCount();
    
    boolean update(Category category);
    
    boolean delete(int id);
    
    boolean nameExists(String name);
    
    int count();
}
