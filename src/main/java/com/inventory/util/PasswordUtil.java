package com.inventory.util;

import at.favre.lib.crypto.bcrypt.BCrypt;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Utility class for secure password hashing using BCrypt.
 * Provides methods for hashing passwords and verifying password matches.
 */
public class PasswordUtil {
    
    private static final Logger logger = LoggerFactory.getLogger(PasswordUtil.class);
    
    // BCrypt cost factor (higher = more secure but slower)
    // 12 is a good balance between security and performance
    private static final int BCRYPT_COST = 12;
    
    /**
     * Hashes a plain text password using BCrypt.
     * 
     * @param plainPassword The plain text password to hash
     * @return The BCrypt hash of the password
     * @throws IllegalArgumentException if password is null or empty
     */
    public static String hashPassword(String plainPassword) {
        if (plainPassword == null || plainPassword.isEmpty()) {
            throw new IllegalArgumentException("Password cannot be null or empty");
        }
        
        String hash = BCrypt.withDefaults().hashToString(BCRYPT_COST, plainPassword.toCharArray());
        logger.debug("Password hashed successfully");
        return hash;
    }
    
    /**
     * Verifies a plain text password against a BCrypt hash.
     * 
     * @param plainPassword The plain text password to verify
     * @param hashedPassword The BCrypt hash to verify against
     * @return true if the password matches the hash, false otherwise
     */
    public static boolean verifyPassword(String plainPassword, String hashedPassword) {
        if (plainPassword == null || hashedPassword == null) {
            logger.warn("Attempted to verify null password or hash");
            return false;
        }
        
        BCrypt.Result result = BCrypt.verifyer().verify(plainPassword.toCharArray(), hashedPassword);
        
        if (result.verified) {
            logger.debug("Password verification successful");
        } else {
            logger.debug("Password verification failed");
        }
        
        return result.verified;
    }
    
    /**
     * Validates password strength requirements.
     * 
     * @param password The password to validate
     * @return ValidationResult containing success status and message
     */
    public static ValidationResult validatePasswordStrength(String password) {
        if (password == null || password.isEmpty()) {
            return new ValidationResult(false, "Password cannot be empty");
        }
        
        if (password.length() < 8) {
            return new ValidationResult(false, "Password must be at least 8 characters long");
        }
        
        if (password.length() > 128) {
            return new ValidationResult(false, "Password cannot exceed 128 characters");
        }
        
        boolean hasUppercase = password.chars().anyMatch(Character::isUpperCase);
        boolean hasLowercase = password.chars().anyMatch(Character::isLowerCase);
        boolean hasDigit = password.chars().anyMatch(Character::isDigit);
        boolean hasSpecial = password.chars().anyMatch(ch -> !Character.isLetterOrDigit(ch));
        
        if (!hasUppercase) {
            return new ValidationResult(false, "Password must contain at least one uppercase letter");
        }
        
        if (!hasLowercase) {
            return new ValidationResult(false, "Password must contain at least one lowercase letter");
        }
        
        if (!hasDigit) {
            return new ValidationResult(false, "Password must contain at least one digit");
        }
        
        if (!hasSpecial) {
            return new ValidationResult(false, "Password must contain at least one special character");
        }
        
        return new ValidationResult(true, "Password meets all requirements");
    }
    
    /**
     * Simple validation result class.
     */
    public static class ValidationResult {
        private final boolean valid;
        private final String message;
        
        public ValidationResult(boolean valid, String message) {
            this.valid = valid;
            this.message = message;
        }
        
        public boolean isValid() {
            return valid;
        }
        
        public String getMessage() {
            return message;
        }
    }
}
