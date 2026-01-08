package com.inventory.dao;

import com.inventory.model.Category;
import com.inventory.util.DatabaseUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Implementation of CategoryDAO for MySQL database.
 */
public class CategoryDAOImpl implements CategoryDAO {
    
    private static final Logger logger = LoggerFactory.getLogger(CategoryDAOImpl.class);
    
    @Override
    public Category create(Category category) {
        String sql = "INSERT INTO categories (name, description, color, is_active) VALUES (?, ?, ?, ?)";
        
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            
            stmt.setString(1, category.getName());
            stmt.setString(2, category.getDescription());
            stmt.setString(3, category.getColor());
            stmt.setBoolean(4, true);
            
            int affectedRows = stmt.executeUpdate();
            
            if (affectedRows > 0) {
                try (ResultSet rs = stmt.getGeneratedKeys()) {
                    if (rs.next()) {
                        category.setId(rs.getInt(1));
                        category.setCreatedAt(LocalDateTime.now());
                        logger.info("Created new category: {}", category.getName());
                        return category;
                    }
                }
            }
        } catch (SQLException e) {
            logger.error("Error creating category: {}", category.getName(), e);
            throw new RuntimeException("Failed to create category", e);
        }
        return null;
    }
    
    @Override
    public Optional<Category> findById(int id) {
        String sql = "SELECT * FROM categories WHERE id = ?";
        
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, id);
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapResultSetToCategory(rs));
                }
            }
        } catch (SQLException e) {
            logger.error("Error finding category by id: {}", id, e);
        }
        return Optional.empty();
    }
    
    @Override
    public Optional<Category> findByName(String name) {
        String sql = "SELECT * FROM categories WHERE name = ? AND is_active = TRUE";
        
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, name);
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapResultSetToCategory(rs));
                }
            }
        } catch (SQLException e) {
            logger.error("Error finding category by name: {}", name, e);
        }
        return Optional.empty();
    }
    
    @Override
    public List<Category> findAll() {
        String sql = "SELECT * FROM categories ORDER BY name";
        List<Category> categories = new ArrayList<>();
        
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            
            while (rs.next()) {
                categories.add(mapResultSetToCategory(rs));
            }
        } catch (SQLException e) {
            logger.error("Error finding all categories", e);
        }
        return categories;
    }
    
    @Override
    public List<Category> findAllActive() {
        String sql = "SELECT * FROM categories WHERE is_active = TRUE ORDER BY name";
        List<Category> categories = new ArrayList<>();
        
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            
            while (rs.next()) {
                categories.add(mapResultSetToCategory(rs));
            }
        } catch (SQLException e) {
            logger.error("Error finding active categories", e);
        }
        return categories;
    }
    
    @Override
    public List<Category> findAllWithProductCount() {
        String sql = """
            SELECT c.*, COUNT(p.id) as product_count
            FROM categories c
            LEFT JOIN products p ON c.id = p.category_id AND p.is_active = TRUE
            WHERE c.is_active = TRUE
            GROUP BY c.id
            ORDER BY c.name
            """;
        List<Category> categories = new ArrayList<>();
        
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            
            while (rs.next()) {
                Category category = mapResultSetToCategory(rs);
                category.setProductCount(rs.getInt("product_count"));
                categories.add(category);
            }
        } catch (SQLException e) {
            logger.error("Error finding categories with product count", e);
        }
        return categories;
    }
    
    @Override
    public boolean update(Category category) {
        String sql = "UPDATE categories SET name = ?, description = ?, color = ? WHERE id = ?";
        
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, category.getName());
            stmt.setString(2, category.getDescription());
            stmt.setString(3, category.getColor());
            stmt.setInt(4, category.getId());
            
            int affectedRows = stmt.executeUpdate();
            if (affectedRows > 0) {
                logger.info("Updated category: {}", category.getName());
                return true;
            }
        } catch (SQLException e) {
            logger.error("Error updating category: {}", category.getId(), e);
        }
        return false;
    }
    
    @Override
    public boolean delete(int id) {
        String sql = "UPDATE categories SET is_active = FALSE WHERE id = ?";
        
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, id);
            int affectedRows = stmt.executeUpdate();
            if (affectedRows > 0) {
                logger.info("Soft deleted category: {}", id);
                return true;
            }
        } catch (SQLException e) {
            logger.error("Error deleting category: {}", id, e);
        }
        return false;
    }
    
    @Override
    public boolean nameExists(String name) {
        String sql = "SELECT COUNT(*) FROM categories WHERE name = ? AND is_active = TRUE";
        
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, name);
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
        } catch (SQLException e) {
            logger.error("Error checking category name exists: {}", name, e);
        }
        return false;
    }
    
    @Override
    public int count() {
        String sql = "SELECT COUNT(*) FROM categories WHERE is_active = TRUE";
        
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            
            if (rs.next()) {
                return rs.getInt(1);
            }
        } catch (SQLException e) {
            logger.error("Error counting categories", e);
        }
        return 0;
    }
    
    private Category mapResultSetToCategory(ResultSet rs) throws SQLException {
        Category category = new Category();
        category.setId(rs.getInt("id"));
        category.setName(rs.getString("name"));
        category.setDescription(rs.getString("description"));
        category.setColor(rs.getString("color"));
        category.setActive(rs.getBoolean("is_active"));
        
        Timestamp createdAt = rs.getTimestamp("created_at");
        if (createdAt != null) {
            category.setCreatedAt(createdAt.toLocalDateTime());
        }
        
        Timestamp updatedAt = rs.getTimestamp("updated_at");
        if (updatedAt != null) {
            category.setUpdatedAt(updatedAt.toLocalDateTime());
        }
        
        return category;
    }
}
