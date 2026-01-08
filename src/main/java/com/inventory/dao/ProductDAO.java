package com.inventory.dao;

import com.inventory.model.Product;

import java.util.List;
import java.util.Optional;

/**
 * Data Access Object interface for Product operations.
 */
public interface ProductDAO {
    
    /**
     * Creates a new product in the database.
     * 
     * @param product The product to create
     * @return The created product with generated ID
     */
    Product create(Product product);
    
    /**
     * Finds a product by its ID.
     * 
     * @param id The product ID
     * @return Optional containing the product if found
     */
    Optional<Product> findById(int id);
    
    /**
     * Finds a product by its SKU.
     * 
     * @param sku The SKU to search for
     * @return Optional containing the product if found
     */
    Optional<Product> findBySku(String sku);
    
    /**
     * Gets all active products.
     * 
     * @return List of all active products
     */
    List<Product> findAll();
    
    /**
     * Searches products by name, SKU, or description.
     * 
     * @param searchTerm The search term
     * @return List of matching products
     */
    List<Product> search(String searchTerm);
    
    /**
     * Finds products by category.
     * 
     * @param categoryId The category ID
     * @return List of products in the category
     */
    List<Product> findByCategory(int categoryId);
    
    /**
     * Finds products by supplier.
     * 
     * @param supplierId The supplier ID
     * @return List of products from the supplier
     */
    List<Product> findBySupplier(int supplierId);
    
    /**
     * Finds products with low stock (quantity <= threshold).
     * 
     * @return List of low stock products
     */
    List<Product> findLowStock();
    
    /**
     * Finds products expiring within the specified days.
     * 
     * @param days Number of days until expiry
     * @return List of expiring products
     */
    List<Product> findExpiringSoon(int days);
    
    /**
     * Finds products that have already expired.
     * 
     * @return List of expired products
     */
    List<Product> findExpired();
    
    /**
     * Updates an existing product.
     * 
     * @param product The product to update
     * @return true if update was successful
     */
    boolean update(Product product);
    
    /**
     * Updates the product quantity.
     * 
     * @param productId The product ID
     * @param newQuantity The new quantity
     * @return true if update was successful
     */
    boolean updateQuantity(int productId, int newQuantity);
    
    /**
     * Deletes a product (soft delete).
     * 
     * @param id The product ID to delete
     * @return true if deletion was successful
     */
    boolean delete(int id);
    
    /**
     * Checks if a SKU already exists.
     * 
     * @param sku The SKU to check
     * @return true if SKU exists
     */
    boolean skuExists(String sku);
    
    /**
     * Gets the total count of active products.
     * 
     * @return Total product count
     */
    int count();
    
    /**
     * Gets the count of low stock products.
     * 
     * @return Low stock product count
     */
    int countLowStock();
    
    /**
     * Gets the count of products expiring within days.
     * 
     * @param days Number of days
     * @return Expiring product count
     */
    int countExpiringSoon(int days);
}
