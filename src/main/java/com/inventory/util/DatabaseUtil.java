package com.inventory.util;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Properties;

/**
 * Database utility class managing connection pooling using HikariCP.
 * Provides centralized database configuration and connection management.
 */
public class DatabaseUtil {
    
    private static final Logger logger = LoggerFactory.getLogger(DatabaseUtil.class);
    private static HikariDataSource dataSource;
    
    // Default configuration
    private static String jdbcUrl = "jdbc:mysql://localhost:3306/inventory_db";
    private static String username = "root";
    private static String password = "";
    
    static {
        initializeDataSource();
    }
    
    /**
     * Initializes the HikariCP data source with configuration.
     */
    private static void initializeDataSource() {
        try {
            // Try to load configuration from properties file
            loadConfiguration();
            
            HikariConfig config = new HikariConfig();
            config.setJdbcUrl(jdbcUrl);
            config.setUsername(username);
            config.setPassword(password);
            
            // Connection pool settings
            config.setMaximumPoolSize(10);
            config.setMinimumIdle(2);
            config.setIdleTimeout(300000); // 5 minutes
            config.setConnectionTimeout(20000); // 20 seconds
            config.setMaxLifetime(1200000); // 20 minutes
            
            // MySQL specific optimizations
            config.addDataSourceProperty("cachePrepStmts", "true");
            config.addDataSourceProperty("prepStmtCacheSize", "250");
            config.addDataSourceProperty("prepStmtCacheSqlLimit", "2048");
            config.addDataSourceProperty("useServerPrepStmts", "true");
            config.addDataSourceProperty("useLocalSessionState", "true");
            config.addDataSourceProperty("rewriteBatchedStatements", "true");
            config.addDataSourceProperty("cacheResultSetMetadata", "true");
            config.addDataSourceProperty("cacheServerConfiguration", "true");
            config.addDataSourceProperty("elideSetAutoCommits", "true");
            config.addDataSourceProperty("maintainTimeStats", "false");
            
            // Connection validation
            config.setConnectionTestQuery("SELECT 1");
            
            dataSource = new HikariDataSource(config);
            logger.info("Database connection pool initialized successfully");
            
        } catch (Exception e) {
            logger.error("Failed to initialize database connection pool", e);
            throw new RuntimeException("Database initialization failed", e);
        }
    }
    
    /**
     * Loads database configuration from properties file.
     */
    private static void loadConfiguration() {
        try (InputStream input = DatabaseUtil.class.getClassLoader()
                .getResourceAsStream("database.properties")) {
            if (input != null) {
                Properties props = new Properties();
                props.load(input);
                jdbcUrl = props.getProperty("db.url", jdbcUrl);
                username = props.getProperty("db.username", username);
                password = props.getProperty("db.password", password);
                logger.info("Loaded database configuration from properties file");
            } else {
                logger.warn("database.properties not found, using default configuration");
            }
        } catch (IOException e) {
            logger.warn("Error loading database.properties, using defaults", e);
        }
    }
    
    /**
     * Gets a connection from the connection pool.
     * 
     * @return A database connection
     * @throws SQLException if connection cannot be obtained
     */
    public static Connection getConnection() throws SQLException {
        if (dataSource == null) {
            throw new SQLException("DataSource is not initialized");
        }
        return dataSource.getConnection();
    }
    
    /**
     * Closes the data source and releases all connections.
     */
    public static void closeDataSource() {
        if (dataSource != null && !dataSource.isClosed()) {
            dataSource.close();
            logger.info("Database connection pool closed");
        }
    }
    
    /**
     * Tests the database connection.
     * 
     * @return true if connection is successful, false otherwise
     */
    public static boolean testConnection() {
        try (Connection conn = getConnection()) {
            return conn != null && !conn.isClosed();
        } catch (SQLException e) {
            logger.error("Database connection test failed", e);
            return false;
        }
    }
    
    /**
     * Gets the current pool statistics for monitoring.
     */
    public static String getPoolStats() {
        if (dataSource != null) {
            return String.format("Pool Stats - Active: %d, Idle: %d, Total: %d, Waiting: %d",
                    dataSource.getHikariPoolMXBean().getActiveConnections(),
                    dataSource.getHikariPoolMXBean().getIdleConnections(),
                    dataSource.getHikariPoolMXBean().getTotalConnections(),
                    dataSource.getHikariPoolMXBean().getThreadsAwaitingConnection());
        }
        return "DataSource not initialized";
    }
    
    /**
     * Reconfigures the data source with new connection parameters.
     * This will close existing connections and create a new pool.
     */
    public static void reconfigure(String newUrl, String newUsername, String newPassword) {
        closeDataSource();
        jdbcUrl = newUrl;
        username = newUsername;
        password = newPassword;
        initializeDataSource();
    }
}
