-- ============================================
-- Smart Inventory Management System
-- Database Schema for MySQL
-- ============================================

-- Create database
CREATE DATABASE IF NOT EXISTS inventory_db;
USE inventory_db;

-- ============================================
-- Users Table
-- ============================================
CREATE TABLE IF NOT EXISTS users (
    id INT PRIMARY KEY AUTO_INCREMENT,
    username VARCHAR(50) NOT NULL UNIQUE,
    email VARCHAR(100) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    full_name VARCHAR(100),
    role ENUM('ADMIN', 'STAFF') NOT NULL DEFAULT 'STAFF',
    is_active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    last_login TIMESTAMP NULL,
    INDEX idx_username (username),
    INDEX idx_email (email),
    INDEX idx_role (role)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ============================================
-- Categories Table
-- ============================================
CREATE TABLE IF NOT EXISTS categories (
    id INT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(100) NOT NULL UNIQUE,
    description TEXT,
    color VARCHAR(7) DEFAULT '#3498db',
    is_active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_name (name)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ============================================
-- Suppliers Table
-- ============================================
CREATE TABLE IF NOT EXISTS suppliers (
    id INT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(150) NOT NULL,
    contact_person VARCHAR(100),
    email VARCHAR(100),
    phone VARCHAR(20),
    address TEXT,
    city VARCHAR(50),
    country VARCHAR(50),
    is_active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_name (name),
    INDEX idx_email (email)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ============================================
-- Products Table
-- ============================================
CREATE TABLE IF NOT EXISTS products (
    id INT PRIMARY KEY AUTO_INCREMENT,
    sku VARCHAR(50) NOT NULL UNIQUE,
    name VARCHAR(200) NOT NULL,
    description TEXT,
    category_id INT,
    supplier_id INT,
    quantity INT NOT NULL DEFAULT 0,
    unit VARCHAR(20) DEFAULT 'pcs',
    price DECIMAL(10, 2) NOT NULL DEFAULT 0.00,
    cost_price DECIMAL(10, 2) DEFAULT 0.00,
    low_stock_threshold INT DEFAULT 10,
    expiry_date DATE NULL,
    barcode VARCHAR(50),
    location VARCHAR(100),
    is_active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (category_id) REFERENCES categories(id) ON DELETE SET NULL,
    FOREIGN KEY (supplier_id) REFERENCES suppliers(id) ON DELETE SET NULL,
    INDEX idx_sku (sku),
    INDEX idx_name (name),
    INDEX idx_category (category_id),
    INDEX idx_supplier (supplier_id),
    INDEX idx_expiry (expiry_date),
    INDEX idx_quantity (quantity)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ============================================
-- Inventory Logs Table (Activity Tracking)
-- ============================================
CREATE TABLE IF NOT EXISTS inventory_logs (
    id INT PRIMARY KEY AUTO_INCREMENT,
    product_id INT NOT NULL,
    user_id INT NOT NULL,
    action ENUM('ADD', 'UPDATE', 'DELETE', 'STOCK_IN', 'STOCK_OUT', 'ADJUSTMENT') NOT NULL,
    quantity_before INT,
    quantity_after INT,
    quantity_change INT,
    notes TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (product_id) REFERENCES products(id) ON DELETE CASCADE,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    INDEX idx_product (product_id),
    INDEX idx_user (user_id),
    INDEX idx_action (action),
    INDEX idx_created (created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ============================================
-- Alert Settings Table
-- ============================================
CREATE TABLE IF NOT EXISTS alert_settings (
    id INT PRIMARY KEY AUTO_INCREMENT,
    setting_key VARCHAR(50) NOT NULL UNIQUE,
    setting_value VARCHAR(255) NOT NULL,
    description TEXT,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ============================================
-- Insert Default Data
-- ============================================

-- Default Admin User (password: admin123)
INSERT INTO users (username, email, password_hash, full_name, role) VALUES
('admin', 'admin@inventory.com', '$2a$12$LQv3c1yqBWVHxkd0LHAkCOYz6TtxMQJqhN8/X4edEwLIq0O3Hb.ZC', 'System Administrator', 'ADMIN');

-- Default Categories
INSERT INTO categories (name, description, color) VALUES
('Electronics', 'Electronic devices and accessories', '#3498db'),
('Food & Beverages', 'Food items and drinks', '#2ecc71'),
('Clothing', 'Apparel and accessories', '#9b59b6'),
('Office Supplies', 'Office and stationery items', '#f39c12'),
('Healthcare', 'Medical and healthcare products', '#e74c3c'),
('Home & Garden', 'Home improvement and garden items', '#1abc9c');

-- Default Suppliers
INSERT INTO suppliers (name, contact_person, email, phone, address, city, country) VALUES
('Tech Distributors Inc.', 'John Smith', 'john@techdist.com', '+1-555-0101', '123 Tech Street', 'San Francisco', 'USA'),
('Global Foods Ltd.', 'Maria Garcia', 'maria@globalfoods.com', '+1-555-0102', '456 Food Avenue', 'Los Angeles', 'USA'),
('Fashion Hub', 'Emily Chen', 'emily@fashionhub.com', '+1-555-0103', '789 Style Blvd', 'New York', 'USA'),
('Office World', 'Robert Johnson', 'robert@officeworld.com', '+1-555-0104', '321 Business Park', 'Chicago', 'USA');

-- Default Alert Settings
INSERT INTO alert_settings (setting_key, setting_value, description) VALUES
('low_stock_threshold', '10', 'Default threshold for low stock alerts'),
('expiry_alert_days', '7', 'Number of days before expiry to trigger alert'),
('email_alerts_enabled', 'false', 'Enable email notifications for alerts');

-- Sample Products
INSERT INTO products (sku, name, description, category_id, supplier_id, quantity, price, cost_price, low_stock_threshold, expiry_date, location) VALUES
('ELEC-001', 'Wireless Mouse', 'Ergonomic wireless mouse with USB receiver', 1, 1, 150, 29.99, 15.00, 20, NULL, 'Warehouse A - Shelf 1'),
('ELEC-002', 'USB-C Hub', '7-in-1 USB-C hub with HDMI', 1, 1, 75, 49.99, 25.00, 15, NULL, 'Warehouse A - Shelf 2'),
('ELEC-003', 'Bluetooth Earbuds', 'True wireless earbuds with noise cancellation', 1, 1, 5, 79.99, 40.00, 10, NULL, 'Warehouse A - Shelf 3'),
('FOOD-001', 'Organic Coffee Beans', '1kg bag of premium organic coffee', 2, 2, 200, 24.99, 12.00, 30, DATE_ADD(CURRENT_DATE, INTERVAL 90 DAY), 'Warehouse B - Section 1'),
('FOOD-002', 'Green Tea Pack', '50 premium green tea bags', 2, 2, 8, 12.99, 6.00, 25, DATE_ADD(CURRENT_DATE, INTERVAL 5 DAY), 'Warehouse B - Section 2'),
('CLTH-001', 'Cotton T-Shirt', 'Premium cotton t-shirt - Various sizes', 3, 3, 300, 19.99, 8.00, 50, NULL, 'Warehouse C - Rack 1'),
('OFFC-001', 'A4 Paper Ream', '500 sheets of premium A4 paper', 4, 4, 3, 8.99, 4.00, 20, NULL, 'Warehouse D - Shelf 1'),
('OFFC-002', 'Ballpoint Pens (12pk)', 'Pack of 12 blue ballpoint pens', 4, 4, 150, 5.99, 2.50, 30, NULL, 'Warehouse D - Shelf 2');

COMMIT;
