package com.inventory.dao;

import com.inventory.model.Supplier;
import com.inventory.util.DatabaseUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Implementation of SupplierDAO for MySQL database.
 */
public class SupplierDAOImpl implements SupplierDAO {
    
    private static final Logger logger = LoggerFactory.getLogger(SupplierDAOImpl.class);
    
    @Override
    public Supplier create(Supplier supplier) {
        String sql = """
            INSERT INTO suppliers (name, contact_person, email, phone, address, city, country, is_active)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?)
            """;
        
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            
            stmt.setString(1, supplier.getName());
            stmt.setString(2, supplier.getContactPerson());
            stmt.setString(3, supplier.getEmail());
            stmt.setString(4, supplier.getPhone());
            stmt.setString(5, supplier.getAddress());
            stmt.setString(6, supplier.getCity());
            stmt.setString(7, supplier.getCountry());
            stmt.setBoolean(8, true);
            
            int affectedRows = stmt.executeUpdate();
            
            if (affectedRows > 0) {
                try (ResultSet rs = stmt.getGeneratedKeys()) {
                    if (rs.next()) {
                        supplier.setId(rs.getInt(1));
                        supplier.setCreatedAt(LocalDateTime.now());
                        logger.info("Created new supplier: {}", supplier.getName());
                        return supplier;
                    }
                }
            }
        } catch (SQLException e) {
            logger.error("Error creating supplier: {}", supplier.getName(), e);
            throw new RuntimeException("Failed to create supplier", e);
        }
        return null;
    }
    
    @Override
    public Optional<Supplier> findById(int id) {
        String sql = "SELECT * FROM suppliers WHERE id = ?";
        
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, id);
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapResultSetToSupplier(rs));
                }
            }
        } catch (SQLException e) {
            logger.error("Error finding supplier by id: {}", id, e);
        }
        return Optional.empty();
    }
    
    @Override
    public Optional<Supplier> findByName(String name) {
        String sql = "SELECT * FROM suppliers WHERE name = ? AND is_active = TRUE";
        
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, name);
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapResultSetToSupplier(rs));
                }
            }
        } catch (SQLException e) {
            logger.error("Error finding supplier by name: {}", name, e);
        }
        return Optional.empty();
    }
    
    @Override
    public List<Supplier> findAll() {
        String sql = "SELECT * FROM suppliers ORDER BY name";
        List<Supplier> suppliers = new ArrayList<>();
        
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            
            while (rs.next()) {
                suppliers.add(mapResultSetToSupplier(rs));
            }
        } catch (SQLException e) {
            logger.error("Error finding all suppliers", e);
        }
        return suppliers;
    }
    
    @Override
    public List<Supplier> findAllActive() {
        String sql = "SELECT * FROM suppliers WHERE is_active = TRUE ORDER BY name";
        List<Supplier> suppliers = new ArrayList<>();
        
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            
            while (rs.next()) {
                suppliers.add(mapResultSetToSupplier(rs));
            }
        } catch (SQLException e) {
            logger.error("Error finding active suppliers", e);
        }
        return suppliers;
    }
    
    @Override
    public List<Supplier> findAllWithProductCount() {
        String sql = """
            SELECT s.*, COUNT(p.id) as product_count
            FROM suppliers s
            LEFT JOIN products p ON s.id = p.supplier_id AND p.is_active = TRUE
            WHERE s.is_active = TRUE
            GROUP BY s.id
            ORDER BY s.name
            """;
        List<Supplier> suppliers = new ArrayList<>();
        
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            
            while (rs.next()) {
                Supplier supplier = mapResultSetToSupplier(rs);
                supplier.setProductCount(rs.getInt("product_count"));
                suppliers.add(supplier);
            }
        } catch (SQLException e) {
            logger.error("Error finding suppliers with product count", e);
        }
        return suppliers;
    }
    
    @Override
    public List<Supplier> search(String searchTerm) {
        String sql = """
            SELECT * FROM suppliers 
            WHERE is_active = TRUE 
            AND (name LIKE ? OR contact_person LIKE ? OR email LIKE ?)
            ORDER BY name
            """;
        List<Supplier> suppliers = new ArrayList<>();
        String pattern = "%" + searchTerm + "%";
        
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, pattern);
            stmt.setString(2, pattern);
            stmt.setString(3, pattern);
            
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    suppliers.add(mapResultSetToSupplier(rs));
                }
            }
        } catch (SQLException e) {
            logger.error("Error searching suppliers: {}", searchTerm, e);
        }
        return suppliers;
    }
    
    @Override
    public boolean update(Supplier supplier) {
        String sql = """
            UPDATE suppliers SET name = ?, contact_person = ?, email = ?, phone = ?, 
                                 address = ?, city = ?, country = ?
            WHERE id = ?
            """;
        
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, supplier.getName());
            stmt.setString(2, supplier.getContactPerson());
            stmt.setString(3, supplier.getEmail());
            stmt.setString(4, supplier.getPhone());
            stmt.setString(5, supplier.getAddress());
            stmt.setString(6, supplier.getCity());
            stmt.setString(7, supplier.getCountry());
            stmt.setInt(8, supplier.getId());
            
            int affectedRows = stmt.executeUpdate();
            if (affectedRows > 0) {
                logger.info("Updated supplier: {}", supplier.getName());
                return true;
            }
        } catch (SQLException e) {
            logger.error("Error updating supplier: {}", supplier.getId(), e);
        }
        return false;
    }
    
    @Override
    public boolean delete(int id) {
        String sql = "UPDATE suppliers SET is_active = FALSE WHERE id = ?";
        
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, id);
            int affectedRows = stmt.executeUpdate();
            if (affectedRows > 0) {
                logger.info("Soft deleted supplier: {}", id);
                return true;
            }
        } catch (SQLException e) {
            logger.error("Error deleting supplier: {}", id, e);
        }
        return false;
    }
    
    @Override
    public int count() {
        String sql = "SELECT COUNT(*) FROM suppliers WHERE is_active = TRUE";
        
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            
            if (rs.next()) {
                return rs.getInt(1);
            }
        } catch (SQLException e) {
            logger.error("Error counting suppliers", e);
        }
        return 0;
    }
    
    private Supplier mapResultSetToSupplier(ResultSet rs) throws SQLException {
        Supplier supplier = new Supplier();
        supplier.setId(rs.getInt("id"));
        supplier.setName(rs.getString("name"));
        supplier.setContactPerson(rs.getString("contact_person"));
        supplier.setEmail(rs.getString("email"));
        supplier.setPhone(rs.getString("phone"));
        supplier.setAddress(rs.getString("address"));
        supplier.setCity(rs.getString("city"));
        supplier.setCountry(rs.getString("country"));
        supplier.setActive(rs.getBoolean("is_active"));
        
        Timestamp createdAt = rs.getTimestamp("created_at");
        if (createdAt != null) {
            supplier.setCreatedAt(createdAt.toLocalDateTime());
        }
        
        Timestamp updatedAt = rs.getTimestamp("updated_at");
        if (updatedAt != null) {
            supplier.setUpdatedAt(updatedAt.toLocalDateTime());
        }
        
        return supplier;
    }
}
