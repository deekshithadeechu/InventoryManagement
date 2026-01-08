package com.inventory.dao;

import com.inventory.model.InventoryLog;
import com.inventory.util.DatabaseUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Implementation of InventoryLogDAO for MySQL database.
 */
public class InventoryLogDAOImpl implements InventoryLogDAO {
    
    private static final Logger logger = LoggerFactory.getLogger(InventoryLogDAOImpl.class);
    
    private static final String SELECT_WITH_JOINS = """
        SELECT l.*, p.name as product_name, p.sku as product_sku, u.username as user_name
        FROM inventory_logs l
        LEFT JOIN products p ON l.product_id = p.id
        LEFT JOIN users u ON l.user_id = u.id
        """;
    
    @Override
    public InventoryLog create(InventoryLog log) {
        String sql = """
            INSERT INTO inventory_logs (product_id, user_id, action, quantity_before, 
                                        quantity_after, quantity_change, notes)
            VALUES (?, ?, ?, ?, ?, ?, ?)
            """;
        
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            
            stmt.setInt(1, log.getProductId());
            stmt.setInt(2, log.getUserId());
            stmt.setString(3, log.getAction().name());
            stmt.setObject(4, log.getQuantityBefore());
            stmt.setObject(5, log.getQuantityAfter());
            stmt.setObject(6, log.getQuantityChange());
            stmt.setString(7, log.getNotes());
            
            int affectedRows = stmt.executeUpdate();
            
            if (affectedRows > 0) {
                try (ResultSet rs = stmt.getGeneratedKeys()) {
                    if (rs.next()) {
                        log.setId(rs.getInt(1));
                        log.setCreatedAt(LocalDateTime.now());
                        logger.debug("Created inventory log entry");
                        return log;
                    }
                }
            }
        } catch (SQLException e) {
            logger.error("Error creating inventory log", e);
            throw new RuntimeException("Failed to create inventory log", e);
        }
        return null;
    }
    
    @Override
    public List<InventoryLog> findAll() {
        String sql = SELECT_WITH_JOINS + " ORDER BY l.created_at DESC";
        List<InventoryLog> logs = new ArrayList<>();
        
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            
            while (rs.next()) {
                logs.add(mapResultSetToLog(rs));
            }
        } catch (SQLException e) {
            logger.error("Error finding all inventory logs", e);
        }
        return logs;
    }
    
    @Override
    public List<InventoryLog> findByProductId(int productId) {
        String sql = SELECT_WITH_JOINS + " WHERE l.product_id = ? ORDER BY l.created_at DESC";
        List<InventoryLog> logs = new ArrayList<>();
        
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, productId);
            
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    logs.add(mapResultSetToLog(rs));
                }
            }
        } catch (SQLException e) {
            logger.error("Error finding logs for product: {}", productId, e);
        }
        return logs;
    }
    
    @Override
    public List<InventoryLog> findByUserId(int userId) {
        String sql = SELECT_WITH_JOINS + " WHERE l.user_id = ? ORDER BY l.created_at DESC";
        List<InventoryLog> logs = new ArrayList<>();
        
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, userId);
            
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    logs.add(mapResultSetToLog(rs));
                }
            }
        } catch (SQLException e) {
            logger.error("Error finding logs for user: {}", userId, e);
        }
        return logs;
    }
    
    @Override
    public List<InventoryLog> findByDateRange(LocalDate startDate, LocalDate endDate) {
        String sql = SELECT_WITH_JOINS + """
            WHERE DATE(l.created_at) BETWEEN ? AND ?
            ORDER BY l.created_at DESC
            """;
        List<InventoryLog> logs = new ArrayList<>();
        
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setDate(1, Date.valueOf(startDate));
            stmt.setDate(2, Date.valueOf(endDate));
            
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    logs.add(mapResultSetToLog(rs));
                }
            }
        } catch (SQLException e) {
            logger.error("Error finding logs by date range", e);
        }
        return logs;
    }
    
    @Override
    public List<InventoryLog> findRecent(int limit) {
        String sql = SELECT_WITH_JOINS + " ORDER BY l.created_at DESC LIMIT ?";
        List<InventoryLog> logs = new ArrayList<>();
        
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, limit);
            
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    logs.add(mapResultSetToLog(rs));
                }
            }
        } catch (SQLException e) {
            logger.error("Error finding recent logs", e);
        }
        return logs;
    }
    
    @Override
    public int countTodayActivities() {
        String sql = "SELECT COUNT(*) FROM inventory_logs WHERE DATE(created_at) = CURRENT_DATE";
        
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            
            if (rs.next()) {
                return rs.getInt(1);
            }
        } catch (SQLException e) {
            logger.error("Error counting today's activities", e);
        }
        return 0;
    }
    
    private InventoryLog mapResultSetToLog(ResultSet rs) throws SQLException {
        InventoryLog log = new InventoryLog();
        log.setId(rs.getInt("id"));
        log.setProductId(rs.getInt("product_id"));
        log.setUserId(rs.getInt("user_id"));
        log.setAction(InventoryLog.Action.valueOf(rs.getString("action")));
        
        int quantityBefore = rs.getInt("quantity_before");
        if (!rs.wasNull()) {
            log.setQuantityBefore(quantityBefore);
        }
        
        int quantityAfter = rs.getInt("quantity_after");
        if (!rs.wasNull()) {
            log.setQuantityAfter(quantityAfter);
        }
        
        int quantityChange = rs.getInt("quantity_change");
        if (!rs.wasNull()) {
            log.setQuantityChange(quantityChange);
        }
        
        log.setNotes(rs.getString("notes"));
        
        Timestamp createdAt = rs.getTimestamp("created_at");
        if (createdAt != null) {
            log.setCreatedAt(createdAt.toLocalDateTime());
        }
        
        // Joined fields
        try {
            log.setProductName(rs.getString("product_name"));
            log.setProductSku(rs.getString("product_sku"));
            log.setUserName(rs.getString("user_name"));
        } catch (SQLException ignored) {
            // These fields may not exist in all queries
        }
        
        return log;
    }
}
