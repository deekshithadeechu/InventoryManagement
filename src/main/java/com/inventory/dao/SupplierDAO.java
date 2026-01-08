package com.inventory.dao;

import com.inventory.model.Supplier;

import java.util.List;
import java.util.Optional;

/**
 * Data Access Object interface for Supplier operations.
 */
public interface SupplierDAO {
    
    Supplier create(Supplier supplier);
    
    Optional<Supplier> findById(int id);
    
    Optional<Supplier> findByName(String name);
    
    List<Supplier> findAll();
    
    List<Supplier> findAllActive();
    
    List<Supplier> findAllWithProductCount();
    
    List<Supplier> search(String searchTerm);
    
    boolean update(Supplier supplier);
    
    boolean delete(int id);
    
    int count();
}
