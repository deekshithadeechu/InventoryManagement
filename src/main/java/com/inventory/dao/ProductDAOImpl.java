package com.inventory.dao;

import com.inventory.model.Product;
import com.inventory.util.DatabaseUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.sql.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Implementation of ProductDAO for MySQL database.
 */
public class ProductDAOImpl implements ProductDAO {
    
    private static final Logger logger = LoggerFactory.getLogger(ProductDAOImpl.class);
    
    private static final String SELECT_WITH_JOINS = """
        SELECT p.*, c.name as category_name, s.name as supplier_name
        FROM products p
        LEFT JOIN categories c ON p.category_id = c.id
        LEFT JOIN suppliers s ON p.supplier_id = s.id
        """;
    
    @Override
    public Product create(Product product) {
        String sql = """
            INSERT INTO products (sku, name, description, category_id, supplier_id, 
                                  quantity, unit, price, cost_price, low_stock_threshold,
                                  expiry_date, barcode, location, is_active)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
            """;
        
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            
            stmt.setString(1, product.getSku());
            stmt.setString(2, product.getName());
            stmt.setString(3, product.getDescription());
            stmt.setObject(4, product.getCategoryId());
            stmt.setObject(5, product.getSupplierId());
            stmt.setInt(6, product.getQuantity());
            stmt.setString(7, product.getUnit());
            stmt.setBigDecimal(8, product.getPrice());
            stmt.setBigDecimal(9, product.getCostPrice());
            stmt.setInt(10, product.getLowStockThreshold());
            stmt.setObject(11, product.getExpiryDate());
            stmt.setString(12, product.getBarcode());
            stmt.setString(13, product.getLocation());
            stmt.setBoolean(14, true);
            
            int affectedRows = stmt.executeUpdate();
            
            if (affectedRows > 0) {
                try (ResultSet rs = stmt.getGeneratedKeys()) {
                    if (rs.next()) {
                        product.setId(rs.getInt(1));
                        product.setCreatedAt(LocalDateTime.now());
                        logger.info("Created new product: {} ({})", product.getName(), product.getSku());
                        return product;
                    }
                }
            }
        } catch (SQLException e) {
            logger.error("Error creating product: {}", product.getSku(), e);
            throw new RuntimeException("Failed to create product", e);
        }
        return null;
    }
    
    @Override
    public Optional<Product> findById(int id) {
        String sql = SELECT_WITH_JOINS + " WHERE p.id = ? AND p.is_active = TRUE";
        
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, id);
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapResultSetToProduct(rs));
                }
            }
        } catch (SQLException e) {
            logger.error("Error finding product by id: {}", id, e);
        }
        return Optional.empty();
    }
    
    @Override
    public Optional<Product> findBySku(String sku) {
        String sql = SELECT_WITH_JOINS + " WHERE p.sku = ? AND p.is_active = TRUE";
        
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, sku);
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapResultSetToProduct(rs));
                }
            }
        } catch (SQLException e) {
            logger.error("Error finding product by sku: {}", sku, e);
        }
        return Optional.empty();
    }
    
    @Override
    public List<Product> findAll() {
        String sql = SELECT_WITH_JOINS + " WHERE p.is_active = TRUE ORDER BY p.updated_at DESC";
        List<Product> products = new ArrayList<>();
        
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            
            while (rs.next()) {
                products.add(mapResultSetToProduct(rs));
            }
        } catch (SQLException e) {
            logger.error("Error finding all products", e);
        }
        return products;
    }
    
    @Override
    public List<Product> search(String searchTerm) {
        String sql = SELECT_WITH_JOINS + """
            WHERE p.is_active = TRUE 
            AND (p.name LIKE ? OR p.sku LIKE ? OR p.description LIKE ? OR p.barcode LIKE ?)
            ORDER BY p.name
            """;
        List<Product> products = new ArrayList<>();
        String pattern = "%" + searchTerm + "%";
        
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, pattern);
            stmt.setString(2, pattern);
            stmt.setString(3, pattern);
            stmt.setString(4, pattern);
            
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    products.add(mapResultSetToProduct(rs));
                }
            }
        } catch (SQLException e) {
            logger.error("Error searching products: {}", searchTerm, e);
        }
        return products;
    }
    
    @Override
    public List<Product> findByCategory(int categoryId) {
        String sql = SELECT_WITH_JOINS + " WHERE p.category_id = ? AND p.is_active = TRUE ORDER BY p.name";
        List<Product> products = new ArrayList<>();
        
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, categoryId);
            
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    products.add(mapResultSetToProduct(rs));
                }
            }
        } catch (SQLException e) {
            logger.error("Error finding products by category: {}", categoryId, e);
        }
        return products;
    }
    
    @Override
    public List<Product> findBySupplier(int supplierId) {
        String sql = SELECT_WITH_JOINS + " WHERE p.supplier_id = ? AND p.is_active = TRUE ORDER BY p.name";
        List<Product> products = new ArrayList<>();
        
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, supplierId);
            
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    products.add(mapResultSetToProduct(rs));
                }
            }
        } catch (SQLException e) {
            logger.error("Error finding products by supplier: {}", supplierId, e);
        }
        return products;
    }
    
    @Override
    public List<Product> findLowStock() {
        String sql = SELECT_WITH_JOINS + """
            WHERE p.is_active = TRUE AND p.quantity <= p.low_stock_threshold
            ORDER BY p.quantity ASC
            """;
        List<Product> products = new ArrayList<>();
        
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            
            while (rs.next()) {
                products.add(mapResultSetToProduct(rs));
            }
        } catch (SQLException e) {
            logger.error("Error finding low stock products", e);
        }
        return products;
    }
    
    @Override
    public List<Product> findExpiringSoon(int days) {
        String sql = SELECT_WITH_JOINS + """
            WHERE p.is_active = TRUE 
            AND p.expiry_date IS NOT NULL 
            AND p.expiry_date BETWEEN CURRENT_DATE AND DATE_ADD(CURRENT_DATE, INTERVAL ? DAY)
            ORDER BY p.expiry_date ASC
            """;
        List<Product> products = new ArrayList<>();
        
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, days);
            
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    products.add(mapResultSetToProduct(rs));
                }
            }
        } catch (SQLException e) {
            logger.error("Error finding expiring products", e);
        }
        return products;
    }
    
    @Override
    public List<Product> findExpired() {
        String sql = SELECT_WITH_JOINS + """
            WHERE p.is_active = TRUE 
            AND p.expiry_date IS NOT NULL 
            AND p.expiry_date < CURRENT_DATE
            ORDER BY p.expiry_date ASC
            """;
        List<Product> products = new ArrayList<>();
        
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            
            while (rs.next()) {
                products.add(mapResultSetToProduct(rs));
            }
        } catch (SQLException e) {
            logger.error("Error finding expired products", e);
        }
        return products;
    }
    
    @Override
    public boolean update(Product product) {
        String sql = """
            UPDATE products SET 
                sku = ?, name = ?, description = ?, category_id = ?, supplier_id = ?,
                quantity = ?, unit = ?, price = ?, cost_price = ?, low_stock_threshold = ?,
                expiry_date = ?, barcode = ?, location = ?
            WHERE id = ?
            """;
        
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, product.getSku());
            stmt.setString(2, product.getName());
            stmt.setString(3, product.getDescription());
            stmt.setObject(4, product.getCategoryId());
            stmt.setObject(5, product.getSupplierId());
            stmt.setInt(6, product.getQuantity());
            stmt.setString(7, product.getUnit());
            stmt.setBigDecimal(8, product.getPrice());
            stmt.setBigDecimal(9, product.getCostPrice());
            stmt.setInt(10, product.getLowStockThreshold());
            stmt.setObject(11, product.getExpiryDate());
            stmt.setString(12, product.getBarcode());
            stmt.setString(13, product.getLocation());
            stmt.setInt(14, product.getId());
            
            int affectedRows = stmt.executeUpdate();
            if (affectedRows > 0) {
                logger.info("Updated product: {} ({})", product.getName(), product.getSku());
                return true;
            }
        } catch (SQLException e) {
            logger.error("Error updating product: {}", product.getId(), e);
        }
        return false;
    }
    
    @Override
    public boolean updateQuantity(int productId, int newQuantity) {
        String sql = "UPDATE products SET quantity = ? WHERE id = ?";
        
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, newQuantity);
            stmt.setInt(2, productId);
            
            int affectedRows = stmt.executeUpdate();
            if (affectedRows > 0) {
                logger.info("Updated quantity for product {}: {}", productId, newQuantity);
                return true;
            }
        } catch (SQLException e) {
            logger.error("Error updating quantity for product: {}", productId, e);
        }
        return false;
    }
    
    @Override
    public boolean delete(int id) {
        String sql = "UPDATE products SET is_active = FALSE WHERE id = ?";
        
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, id);
            int affectedRows = stmt.executeUpdate();
            if (affectedRows > 0) {
                logger.info("Soft deleted product: {}", id);
                return true;
            }
        } catch (SQLException e) {
            logger.error("Error deleting product: {}", id, e);
        }
        return false;
    }
    
    @Override
    public boolean skuExists(String sku) {
        String sql = "SELECT COUNT(*) FROM products WHERE sku = ? AND is_active = TRUE";
        
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, sku);
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
        } catch (SQLException e) {
            logger.error("Error checking sku exists: {}", sku, e);
        }
        return false;
    }
    
    @Override
    public int count() {
        String sql = "SELECT COUNT(*) FROM products WHERE is_active = TRUE";
        
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            
            if (rs.next()) {
                return rs.getInt(1);
            }
        } catch (SQLException e) {
            logger.error("Error counting products", e);
        }
        return 0;
    }
    
    @Override
    public int countLowStock() {
        String sql = "SELECT COUNT(*) FROM products WHERE is_active = TRUE AND quantity <= low_stock_threshold";
        
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            
            if (rs.next()) {
                return rs.getInt(1);
            }
        } catch (SQLException e) {
            logger.error("Error counting low stock products", e);
        }
        return 0;
    }
    
    @Override
    public int countExpiringSoon(int days) {
        String sql = """
            SELECT COUNT(*) FROM products 
            WHERE is_active = TRUE 
            AND expiry_date IS NOT NULL 
            AND expiry_date BETWEEN CURRENT_DATE AND DATE_ADD(CURRENT_DATE, INTERVAL ? DAY)
            """;
        
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, days);
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        } catch (SQLException e) {
            logger.error("Error counting expiring products", e);
        }
        return 0;
    }
    
    /**
     * Maps a ResultSet row to a Product object.
     */
    private Product mapResultSetToProduct(ResultSet rs) throws SQLException {
        Product product = new Product();
        product.setId(rs.getInt("id"));
        product.setSku(rs.getString("sku"));
        product.setName(rs.getString("name"));
        product.setDescription(rs.getString("description"));
        
        int categoryId = rs.getInt("category_id");
        if (!rs.wasNull()) {
            product.setCategoryId(categoryId);
        }
        
        int supplierId = rs.getInt("supplier_id");
        if (!rs.wasNull()) {
            product.setSupplierId(supplierId);
        }
        
        product.setQuantity(rs.getInt("quantity"));
        product.setUnit(rs.getString("unit"));
        product.setPrice(rs.getBigDecimal("price"));
        product.setCostPrice(rs.getBigDecimal("cost_price"));
        product.setLowStockThreshold(rs.getInt("low_stock_threshold"));
        
        Date expiryDate = rs.getDate("expiry_date");
        if (expiryDate != null) {
            product.setExpiryDate(expiryDate.toLocalDate());
        }
        
        product.setBarcode(rs.getString("barcode"));
        product.setLocation(rs.getString("location"));
        product.setActive(rs.getBoolean("is_active"));
        
        Timestamp createdAt = rs.getTimestamp("created_at");
        if (createdAt != null) {
            product.setCreatedAt(createdAt.toLocalDateTime());
        }
        
        Timestamp updatedAt = rs.getTimestamp("updated_at");
        if (updatedAt != null) {
            product.setUpdatedAt(updatedAt.toLocalDateTime());
        }
        
        // Joined fields
        try {
            product.setCategoryName(rs.getString("category_name"));
            product.setSupplierName(rs.getString("supplier_name"));
        } catch (SQLException ignored) {
            // These fields may not exist in all queries
        }
        
        return product;
    }
}
