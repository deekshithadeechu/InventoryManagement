package com.inventory.dao;

import com.inventory.model.User;

import java.util.List;
import java.util.Optional;

/**
 * Data Access Object interface for User operations.
 */
public interface UserDAO {
    
    /**
     * Creates a new user in the database.
     * 
     * @param user The user to create
     * @return The created user with generated ID
     */
    User create(User user);
    
    /**
     * Finds a user by their ID.
     * 
     * @param id The user ID
     * @return Optional containing the user if found
     */
    Optional<User> findById(int id);
    
    /**
     * Finds a user by their username.
     * 
     * @param username The username to search for
     * @return Optional containing the user if found
     */
    Optional<User> findByUsername(String username);
    
    /**
     * Finds a user by their email.
     * 
     * @param email The email to search for
     * @return Optional containing the user if found
     */
    Optional<User> findByEmail(String email);
    
    /**
     * Gets all users.
     * 
     * @return List of all users
     */
    List<User> findAll();
    
    /**
     * Gets all active users.
     * 
     * @return List of active users
     */
    List<User> findAllActive();
    
    /**
     * Updates an existing user.
     * 
     * @param user The user to update
     * @return true if update was successful
     */
    boolean update(User user);
    
    /**
     * Updates the user's last login timestamp.
     * 
     * @param userId The user ID
     * @return true if update was successful
     */
    boolean updateLastLogin(int userId);
    
    /**
     * Deletes a user (soft delete - sets is_active to false).
     * 
     * @param id The user ID to delete
     * @return true if deletion was successful
     */
    boolean delete(int id);
    
    /**
     * Checks if a username already exists.
     * 
     * @param username The username to check
     * @return true if username exists
     */
    boolean usernameExists(String username);
    
    /**
     * Checks if an email already exists.
     * 
     * @param email The email to check
     * @return true if email exists
     */
    boolean emailExists(String email);
    
    /**
     * Gets the count of all users.
     * 
     * @return Total user count
     */
    int count();
}
