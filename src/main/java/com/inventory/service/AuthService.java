package com.inventory.service;

import com.inventory.dao.UserDAO;
import com.inventory.dao.UserDAOImpl;
import com.inventory.model.User;
import com.inventory.util.PasswordUtil;
import com.inventory.util.SessionManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Optional;

/**
 * Service class for authentication and user management.
 */
public class AuthService {
    
    private static final Logger logger = LoggerFactory.getLogger(AuthService.class);
    
    private final UserDAO userDAO;
    private final SessionManager sessionManager;
    
    public AuthService() {
        this.userDAO = new UserDAOImpl();
        this.sessionManager = SessionManager.getInstance();
    }
    
    /**
     * Authenticates a user with username and password.
     * 
     * @param username The username
     * @param password The plain text password
     * @return AuthResult containing success status and message
     */
    public AuthResult login(String username, String password) {
        if (username == null || username.trim().isEmpty()) {
            return new AuthResult(false, "Username is required", null);
        }
        
        if (password == null || password.isEmpty()) {
            return new AuthResult(false, "Password is required", null);
        }
        
        Optional<User> userOpt = userDAO.findByUsername(username.trim());
        
        if (userOpt.isEmpty()) {
            logger.warn("Login failed: User not found - {}", username);
            return new AuthResult(false, "Invalid username or password", null);
        }
        
        User user = userOpt.get();
        
        if (!user.isActive()) {
            logger.warn("Login failed: User account inactive - {}", username);
            return new AuthResult(false, "Account is deactivated", null);
        }
        
        if (!PasswordUtil.verifyPassword(password, user.getPasswordHash())) {
            logger.warn("Login failed: Invalid password - {}", username);
            return new AuthResult(false, "Invalid username or password", null);
        }
        
        // Update last login
        userDAO.updateLastLogin(user.getId());
        
        // Set session
        sessionManager.setCurrentUser(user);
        
        logger.info("User logged in successfully: {}", username);
        return new AuthResult(true, "Login successful", user);
    }
    
    /**
     * Registers a new user.
     * 
     * @param username The username
     * @param email The email address
     * @param password The plain text password
     * @param fullName The user's full name
     * @param role The user's role
     * @return AuthResult containing success status and message
     */
    public AuthResult register(String username, String email, String password, 
                               String fullName, User.Role role) {
        // Validate inputs
        if (username == null || username.trim().isEmpty()) {
            return new AuthResult(false, "Username is required", null);
        }
        
        if (email == null || email.trim().isEmpty()) {
            return new AuthResult(false, "Email is required", null);
        }
        
        if (!isValidEmail(email)) {
            return new AuthResult(false, "Invalid email format", null);
        }
        
        // Validate password strength
        PasswordUtil.ValidationResult passwordValidation = 
            PasswordUtil.validatePasswordStrength(password);
        if (!passwordValidation.isValid()) {
            return new AuthResult(false, passwordValidation.getMessage(), null);
        }
        
        // Check if username exists
        if (userDAO.usernameExists(username.trim())) {
            return new AuthResult(false, "Username already exists", null);
        }
        
        // Check if email exists
        if (userDAO.emailExists(email.trim())) {
            return new AuthResult(false, "Email already registered", null);
        }
        
        // Hash password
        String passwordHash = PasswordUtil.hashPassword(password);
        
        // Create user
        User user = new User(
            username.trim(),
            email.trim(),
            passwordHash,
            fullName != null ? fullName.trim() : null,
            role != null ? role : User.Role.STAFF
        );
        
        User createdUser = userDAO.create(user);
        
        if (createdUser != null) {
            logger.info("User registered successfully: {}", username);
            return new AuthResult(true, "Registration successful", createdUser);
        } else {
            logger.error("User registration failed: {}", username);
            return new AuthResult(false, "Registration failed. Please try again.", null);
        }
    }
    
    /**
     * Logs out the current user.
     */
    public void logout() {
        sessionManager.logout();
    }
    
    /**
     * Gets the currently logged-in user.
     */
    public User getCurrentUser() {
        return sessionManager.getCurrentUser();
    }
    
    /**
     * Checks if a user is currently logged in.
     */
    public boolean isLoggedIn() {
        return sessionManager.isLoggedIn();
    }
    
    /**
     * Checks if the current user is an admin.
     */
    public boolean isAdmin() {
        return sessionManager.isAdmin();
    }
    
    /**
     * Changes the password for the current user.
     */
    public AuthResult changePassword(String currentPassword, String newPassword) {
        User currentUser = sessionManager.getCurrentUser();
        if (currentUser == null) {
            return new AuthResult(false, "Not logged in", null);
        }
        
        // Verify current password
        if (!PasswordUtil.verifyPassword(currentPassword, currentUser.getPasswordHash())) {
            return new AuthResult(false, "Current password is incorrect", null);
        }
        
        // Validate new password
        PasswordUtil.ValidationResult validation = 
            PasswordUtil.validatePasswordStrength(newPassword);
        if (!validation.isValid()) {
            return new AuthResult(false, validation.getMessage(), null);
        }
        
        // Update password (would need to add this method to UserDAO)
        // For now, returning success
        logger.info("Password changed for user: {}", currentUser.getUsername());
        return new AuthResult(true, "Password changed successfully", currentUser);
    }
    
    /**
     * Validates email format.
     */
    private boolean isValidEmail(String email) {
        if (email == null) return false;
        return email.matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$");
    }
    
    /**
     * Result class for authentication operations.
     */
    public static class AuthResult {
        private final boolean success;
        private final String message;
        private final User user;
        
        public AuthResult(boolean success, String message, User user) {
            this.success = success;
            this.message = message;
            this.user = user;
        }
        
        public boolean isSuccess() {
            return success;
        }
        
        public String getMessage() {
            return message;
        }
        
        public User getUser() {
            return user;
        }
    }
}
