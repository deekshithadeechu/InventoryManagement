package com.inventory.service;

import com.inventory.dao.*;
import com.inventory.model.InventoryLog;
import com.inventory.model.Product;
import com.inventory.util.SessionManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Optional;

/**
 * Service class for product and inventory management.
 */
public class ProductService {
    
    private static final Logger logger = LoggerFactory.getLogger(ProductService.class);
    
    private final ProductDAO productDAO;
    private final CategoryDAO categoryDAO;
    private final SupplierDAO supplierDAO;
    private final InventoryLogDAO inventoryLogDAO;
    private final SessionManager sessionManager;
    
    public ProductService() {
        this.productDAO = new ProductDAOImpl();
        this.categoryDAO = new CategoryDAOImpl();
        this.supplierDAO = new SupplierDAOImpl();
        this.inventoryLogDAO = new InventoryLogDAOImpl();
        this.sessionManager = SessionManager.getInstance();
    }
    
    /**
     * Gets all active products.
     */
    public List<Product> getAllProducts() {
        return productDAO.findAll();
    }
    
    /**
     * Gets a product by ID.
     */
    public Optional<Product> getProductById(int id) {
        return productDAO.findById(id);
    }
    
    /**
     * Gets a product by SKU.
     */
    public Optional<Product> getProductBySku(String sku) {
        return productDAO.findBySku(sku);
    }
    
    /**
     * Searches products by name, SKU, or description.
     */
    public List<Product> searchProducts(String searchTerm) {
        if (searchTerm == null || searchTerm.trim().isEmpty()) {
            return getAllProducts();
        }
        return productDAO.search(searchTerm.trim());
    }
    
    /**
     * Gets products by category.
     */
    public List<Product> getProductsByCategory(int categoryId) {
        return productDAO.findByCategory(categoryId);
    }
    
    /**
     * Gets products by supplier.
     */
    public List<Product> getProductsBySupplier(int supplierId) {
        return productDAO.findBySupplier(supplierId);
    }
    
    /**
     * Gets low stock products.
     */
    public List<Product> getLowStockProducts() {
        return productDAO.findLowStock();
    }
    
    /**
     * Gets products expiring within specified days.
     */
    public List<Product> getExpiringSoonProducts(int days) {
        return productDAO.findExpiringSoon(days);
    }
    
    /**
     * Gets expired products.
     */
    public List<Product> getExpiredProducts() {
        return productDAO.findExpired();
    }
    
    /**
     * Creates a new product.
     */
    public ServiceResult<Product> createProduct(Product product) {
        // Validate product
        ServiceResult<Void> validation = validateProduct(product, true);
        if (!validation.isSuccess()) {
            return new ServiceResult<>(false, validation.getMessage(), null);
        }
        
        // Check SKU uniqueness
        if (productDAO.skuExists(product.getSku())) {
            return new ServiceResult<>(false, "SKU already exists", null);
        }
        
        Product created = productDAO.create(product);
        
        if (created != null) {
            // Log the action
            logInventoryAction(created.getId(), InventoryLog.Action.ADD, 
                              null, created.getQuantity(), "Product created");
            
            logger.info("Product created: {} ({})", product.getName(), product.getSku());
            return new ServiceResult<>(true, "Product created successfully", created);
        }
        
        return new ServiceResult<>(false, "Failed to create product", null);
    }
    
    /**
     * Updates an existing product.
     */
    public ServiceResult<Product> updateProduct(Product product) {
        // Check if product exists
        Optional<Product> existing = productDAO.findById(product.getId());
        if (existing.isEmpty()) {
            return new ServiceResult<>(false, "Product not found", null);
        }
        
        // Validate product
        ServiceResult<Void> validation = validateProduct(product, false);
        if (!validation.isSuccess()) {
            return new ServiceResult<>(false, validation.getMessage(), null);
        }
        
        // Check SKU uniqueness (if changed)
        Product existingProduct = existing.get();
        if (!existingProduct.getSku().equals(product.getSku()) && 
            productDAO.skuExists(product.getSku())) {
            return new ServiceResult<>(false, "SKU already exists", null);
        }
        
        int oldQuantity = existingProduct.getQuantity();
        
        boolean updated = productDAO.update(product);
        
        if (updated) {
            // Log quantity change if different
            if (oldQuantity != product.getQuantity()) {
                logInventoryAction(product.getId(), InventoryLog.Action.UPDATE,
                                  oldQuantity, product.getQuantity(), "Product updated");
            }
            
            logger.info("Product updated: {} ({})", product.getName(), product.getSku());
            return new ServiceResult<>(true, "Product updated successfully", product);
        }
        
        return new ServiceResult<>(false, "Failed to update product", null);
    }
    
    /**
     * Adjusts stock quantity.
     */
    public ServiceResult<Product> adjustStock(int productId, int quantityChange, String reason) {
        Optional<Product> productOpt = productDAO.findById(productId);
        if (productOpt.isEmpty()) {
            return new ServiceResult<>(false, "Product not found", null);
        }
        
        Product product = productOpt.get();
        int oldQuantity = product.getQuantity();
        int newQuantity = oldQuantity + quantityChange;
        
        if (newQuantity < 0) {
            return new ServiceResult<>(false, "Insufficient stock. Available: " + oldQuantity, null);
        }
        
        boolean updated = productDAO.updateQuantity(productId, newQuantity);
        
        if (updated) {
            product.setQuantity(newQuantity);
            
            // Determine action type
            InventoryLog.Action action = quantityChange > 0 ? 
                InventoryLog.Action.STOCK_IN : InventoryLog.Action.STOCK_OUT;
            
            logInventoryAction(productId, action, oldQuantity, newQuantity, reason);
            
            logger.info("Stock adjusted for product {}: {} -> {}", 
                       product.getSku(), oldQuantity, newQuantity);
            return new ServiceResult<>(true, "Stock adjusted successfully", product);
        }
        
        return new ServiceResult<>(false, "Failed to adjust stock", null);
    }
    
    /**
     * Deletes a product (soft delete).
     */
    public ServiceResult<Void> deleteProduct(int productId) {
        Optional<Product> productOpt = productDAO.findById(productId);
        if (productOpt.isEmpty()) {
            return new ServiceResult<>(false, "Product not found", null);
        }
        
        Product product = productOpt.get();
        boolean deleted = productDAO.delete(productId);
        
        if (deleted) {
            logInventoryAction(productId, InventoryLog.Action.DELETE,
                              product.getQuantity(), 0, "Product deleted");
            
            logger.info("Product deleted: {} ({})", product.getName(), product.getSku());
            return new ServiceResult<>(true, "Product deleted successfully", null);
        }
        
        return new ServiceResult<>(false, "Failed to delete product", null);
    }
    
    /**
     * Gets the total count of products.
     */
    public int getProductCount() {
        return productDAO.count();
    }
    
    /**
     * Gets the count of low stock products.
     */
    public int getLowStockCount() {
        return productDAO.countLowStock();
    }
    
    /**
     * Gets the count of products expiring soon.
     */
    public int getExpiringSoonCount(int days) {
        return productDAO.countExpiringSoon(days);
    }
    
    /**
     * Validates product data.
     */
    private ServiceResult<Void> validateProduct(Product product, boolean isNew) {
        if (product.getSku() == null || product.getSku().trim().isEmpty()) {
            return new ServiceResult<>(false, "SKU is required", null);
        }
        
        if (product.getName() == null || product.getName().trim().isEmpty()) {
            return new ServiceResult<>(false, "Product name is required", null);
        }
        
        if (product.getPrice() == null || product.getPrice().doubleValue() < 0) {
            return new ServiceResult<>(false, "Price must be a positive number", null);
        }
        
        if (product.getQuantity() < 0) {
            return new ServiceResult<>(false, "Quantity cannot be negative", null);
        }
        
        if (product.getLowStockThreshold() < 0) {
            return new ServiceResult<>(false, "Low stock threshold cannot be negative", null);
        }
        
        return new ServiceResult<>(true, "Valid", null);
    }
    
    /**
     * Logs an inventory action.
     */
    private void logInventoryAction(int productId, InventoryLog.Action action,
                                    Integer quantityBefore, Integer quantityAfter, 
                                    String notes) {
        int userId = sessionManager.getCurrentUserId();
        if (userId <= 0) {
            userId = 1; // Default to admin if no session
        }
        
        InventoryLog log = new InventoryLog(productId, userId, action, 
                                            quantityBefore, quantityAfter, notes);
        inventoryLogDAO.create(log);
    }
    
    /**
     * Generic service result class.
     */
    public static class ServiceResult<T> {
        private final boolean success;
        private final String message;
        private final T data;
        
        public ServiceResult(boolean success, String message, T data) {
            this.success = success;
            this.message = message;
            this.data = data;
        }
        
        public boolean isSuccess() {
            return success;
        }
        
        public String getMessage() {
            return message;
        }
        
        public T getData() {
            return data;
        }
    }
}
