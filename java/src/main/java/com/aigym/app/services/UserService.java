/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.aigym.app.services;

import com.aigym.app.models.User;
import com.aigym.app.utils.DatabaseConnection;
import org.mindrot.jbcrypt.BCrypt;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

public class UserService {
    private static final Logger logger = LoggerFactory.getLogger(UserService.class);
    private final DatabaseConnection dbConnection;
    
    public UserService() {
        this.dbConnection = DatabaseConnection.getInstance();
    }
    
    public User authenticateUser(String username, String password) {
        String query = "SELECT * FROM users WHERE username = ?";
        
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            
            stmt.setString(1, username);
            ResultSet rs = stmt.executeQuery();
            
            if (rs.next()) {
                String storedHash = rs.getString("password_hash");
                if (BCrypt.checkpw(password, storedHash)) {
                    return mapResultSetToUser(rs);
                }
            }
            
            return null; // Authentication failed
        } catch (SQLException e) {
            logger.error("Error authenticating user", e);
            throw new RuntimeException("Database error during authentication", e);
        }
    }
    
    public User registerUser(User user, String plainPassword) {
        String hashedPassword = BCrypt.hashpw(plainPassword, BCrypt.gensalt());
        
        String query = "INSERT INTO users (username, email, password_hash, first_name, last_name, " +
                       "date_of_birth, gender, height, weight, fitness_goal, fitness_level, role) " +
                       "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)) {
            
            stmt.setString(1, user.getUsername());
            stmt.setString(2, user.getEmail());
            stmt.setString(3, hashedPassword);
            stmt.setString(4, user.getFirstName());
            stmt.setString(5, user.getLastName());
            stmt.setDate(6, user.getDateOfBirth() != null ? 
                         Date.valueOf(user.getDateOfBirth()) : null);
            stmt.setString(7, user.getGender());
            stmt.setDouble(8, user.getHeight());
            stmt.setDouble(9, user.getWeight());
            stmt.setString(10, user.getFitnessGoal());
            stmt.setString(11, user.getFitnessLevel());
            stmt.setString(12, user.getRole().toString());
            
            int affectedRows = stmt.executeUpdate();
            
            if (affectedRows == 0) {
                throw new SQLException("Creating user failed, no rows affected.");
            }
            
            try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    user.setId(generatedKeys.getInt(1));
                } else {
                    throw new SQLException("Creating user failed, no ID obtained.");
                }
            }
            
            return user;
        } catch (SQLException e) {
            logger.error("Error registering user", e);
            throw new RuntimeException("Database error during registration", e);
        }
    }
    
    /**
     * Updates user information in the database
     * @param user The user object with updated information
     * @return true if the update was successful, false otherwise
     */
    public boolean updateUser(User user) {
        String query = "UPDATE users SET username = ?, email = ?, first_name = ?, last_name = ?, " +
                       "date_of_birth = ?, gender = ?, height = ?, weight = ?, " +
                       "fitness_goal = ?, fitness_level = ?, role = ? " +
                       "WHERE id = ?";
        
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            
            stmt.setString(1, user.getUsername());
            stmt.setString(2, user.getEmail());
            stmt.setString(3, user.getFirstName());
            stmt.setString(4, user.getLastName());
            stmt.setDate(5, user.getDateOfBirth() != null ? 
                         Date.valueOf(user.getDateOfBirth()) : null);
            stmt.setString(6, user.getGender());
            stmt.setDouble(7, user.getHeight());
            stmt.setDouble(8, user.getWeight());
            stmt.setString(9, user.getFitnessGoal());
            stmt.setString(10, user.getFitnessLevel());
            stmt.setString(11, user.getRole().toString());
            stmt.setInt(12, user.getId());
            
            int affectedRows = stmt.executeUpdate();
            return affectedRows > 0;
        } catch (SQLException e) {
            logger.error("Error updating user", e);
            throw new RuntimeException("Database error during user update", e);
        }
    }
    
    /**
     * Verifies if the provided password matches the stored password for a user
     * @param username The username of the user
     * @param password The password to verify
     * @return true if the password matches, false otherwise
     */
    public boolean verifyPassword(String username, String password) {
        String query = "SELECT password_hash FROM users WHERE username = ?";
        
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            
            stmt.setString(1, username);
            ResultSet rs = stmt.executeQuery();
            
            if (rs.next()) {
                String storedHash = rs.getString("password_hash");
                return BCrypt.checkpw(password, storedHash);
            }
            
            return false;
        } catch (SQLException e) {
            logger.error("Error verifying password", e);
            throw new RuntimeException("Database error during password verification", e);
        }
    }
    
    /**
     * Updates a user's password
     * @param userId The ID of the user
     * @param newPassword The new password (plain text)
     * @return true if the update was successful, false otherwise
     */
    public boolean updatePassword(int userId, String newPassword) {
        String hashedPassword = BCrypt.hashpw(newPassword, BCrypt.gensalt());
        String query = "UPDATE users SET password_hash = ? WHERE id = ?";
        
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            
            stmt.setString(1, hashedPassword);
            stmt.setInt(2, userId);
            
            int affectedRows = stmt.executeUpdate();
            return affectedRows > 0;
        } catch (SQLException e) {
            logger.error("Error updating password", e);
            throw new RuntimeException("Database error during password update", e);
        }
    }
    
    private User mapResultSetToUser(ResultSet rs) throws SQLException {
        User user = new User();
        user.setId(rs.getInt("id"));
        user.setUsername(rs.getString("username"));
        user.setEmail(rs.getString("email"));
        user.setPasswordHash(rs.getString("password_hash"));
        user.setFirstName(rs.getString("first_name"));
        user.setLastName(rs.getString("last_name"));
        
        Date dateOfBirth = rs.getDate("date_of_birth");
        if (dateOfBirth != null) {
            user.setDateOfBirth(dateOfBirth.toLocalDate());
        }
        
        user.setGender(rs.getString("gender"));
        user.setHeight(rs.getDouble("height"));
        user.setWeight(rs.getDouble("weight"));
        user.setFitnessGoal(rs.getString("fitness_goal"));
        user.setFitnessLevel(rs.getString("fitness_level"));
        
        String role = rs.getString("role");
        if (role != null) {
            user.setRole(User.UserRole.valueOf(role));
        }
        
        Timestamp createdAt = rs.getTimestamp("created_at");
        if (createdAt != null) {
            user.setCreatedAt(createdAt.toLocalDateTime());
        }
        
        Timestamp updatedAt = rs.getTimestamp("updated_at");
        if (updatedAt != null) {
            user.setUpdatedAt(updatedAt.toLocalDateTime());
        }
        
        return user;
    }
    
    // Static variable to hold the current logged-in user
    private static User currentUser;
    
    /**
     * Sets the current logged-in user
     * @param user The user to set as current
     */
    public void setCurrentUser(User user) {
        UserService.currentUser = user;
    }
    
    /**
     * Gets the current logged-in user
     * @return The current user, or null if no user is logged in
     */
    public User getCurrentUser() {
        return UserService.currentUser;
    }
    
    // Additional methods for user management...
}
