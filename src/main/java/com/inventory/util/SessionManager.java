package com.inventory.util;

import com.inventory.model.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Session manager for tracking the currently logged-in user.
 * Provides centralized session state management and role-based access checks.
 */
public class SessionManager {
    
    private static final Logger logger = LoggerFactory.getLogger(SessionManager.class);
    
    private static SessionManager instance;
    private User currentUser;
    
    // Private constructor for singleton pattern
    private SessionManager() {}
    
    /**
     * Gets the singleton instance of SessionManager.
     * 
     * @return The SessionManager instance
     */
    public static synchronized SessionManager getInstance() {
        if (instance == null) {
            instance = new SessionManager();
        }
        return instance;
    }
    
    /**
     * Sets the current user session.
     * 
     * @param user The user to set as the current session user
     */
    public void setCurrentUser(User user) {
        this.currentUser = user;
        if (user != null) {
            logger.info("User session started: {} ({})", user.getUsername(), user.getRole());
        } else {
            logger.info("User session cleared");
        }
    }
    
    /**
     * Gets the current logged-in user.
     * 
     * @return The current user, or null if not logged in
     */
    public User getCurrentUser() {
        return currentUser;
    }
    
    /**
     * Checks if a user is currently logged in.
     * 
     * @return true if a user is logged in, false otherwise
     */
    public boolean isLoggedIn() {
        return currentUser != null;
    }
    
    /**
     * Checks if the current user has admin role.
     * 
     * @return true if the current user is an admin, false otherwise
     */
    public boolean isAdmin() {
        return currentUser != null && currentUser.isAdmin();
    }
    
    /**
     * Checks if the current user has staff role.
     * 
     * @return true if the current user is staff, false otherwise
     */
    public boolean isStaff() {
        return currentUser != null && currentUser.getRole() == User.Role.STAFF;
    }
    
    /**
     * Gets the current user's ID.
     * 
     * @return The current user's ID, or -1 if not logged in
     */
    public int getCurrentUserId() {
        return currentUser != null ? currentUser.getId() : -1;
    }
    
    /**
     * Gets the current user's username.
     * 
     * @return The current user's username, or "Guest" if not logged in
     */
    public String getCurrentUsername() {
        return currentUser != null ? currentUser.getUsername() : "Guest";
    }
    
    /**
     * Gets the current user's display name.
     * 
     * @return The current user's full name or username
     */
    public String getCurrentDisplayName() {
        if (currentUser == null) {
            return "Guest";
        }
        return currentUser.getFullName() != null && !currentUser.getFullName().isEmpty()
                ? currentUser.getFullName()
                : currentUser.getUsername();
    }
    
    /**
     * Gets the current user's role as a display string.
     * 
     * @return The current user's role, or "Guest" if not logged in
     */
    public String getCurrentRole() {
        return currentUser != null ? currentUser.getDisplayRole() : "Guest";
    }
    
    /**
     * Clears the current session (logout).
     */
    public void logout() {
        if (currentUser != null) {
            logger.info("User logged out: {}", currentUser.getUsername());
        }
        currentUser = null;
    }
    
    /**
     * Checks if the current user has permission to perform an action.
     * Admins have all permissions, staff have limited permissions.
     * 
     * @param permission The permission to check
     * @return true if the user has the permission, false otherwise
     */
    public boolean hasPermission(Permission permission) {
        if (currentUser == null) {
            return false;
        }
        
        // Admins have all permissions
        if (currentUser.isAdmin()) {
            return true;
        }
        
        // Staff permissions
        return switch (permission) {
            case VIEW_PRODUCTS, VIEW_CATEGORIES, VIEW_SUPPLIERS, VIEW_DASHBOARD -> true;
            case ADD_PRODUCT, EDIT_PRODUCT, ADJUST_STOCK -> true;
            case DELETE_PRODUCT, MANAGE_USERS, MANAGE_SETTINGS, GENERATE_REPORTS -> false;
        };
    }
    
    /**
     * Permission types for role-based access control.
     */
    public enum Permission {
        VIEW_PRODUCTS,
        ADD_PRODUCT,
        EDIT_PRODUCT,
        DELETE_PRODUCT,
        ADJUST_STOCK,
        VIEW_CATEGORIES,
        VIEW_SUPPLIERS,
        VIEW_DASHBOARD,
        GENERATE_REPORTS,
        MANAGE_USERS,
        MANAGE_SETTINGS
    }
}
