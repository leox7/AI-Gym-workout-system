package com.aigym.app.utils;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Utility class for managing database connections.
 * Implements the Singleton pattern to ensure a single connection instance.
 */
public class DatabaseConnection {
    private static final Logger logger = LoggerFactory.getLogger(DatabaseConnection.class);
    
    // Database connection parameters
    private static final String DB_URL = "jdbc:mysql://localhost:3306/aigym_db";
    private static final String DB_USER = "root";
    private static final String DB_PASSWORD = "Askleon@07"; 
    
    // Connection timeout parameters
    private static final int CONNECTION_TIMEOUT = 5; // seconds
    
    // Singleton instance
    private static DatabaseConnection instance;
    private Connection connection;
    private boolean databaseAvailable = true;
    
    /**
     * Private constructor to prevent direct instantiation.
     */
    private DatabaseConnection() {
        try {
            // Load the MySQL JDBC driver
            Class.forName("com.mysql.cj.jdbc.Driver");
            logger.info("MySQL JDBC Driver loaded successfully");
            
            // Test connection on startup
            testConnection();
        } catch (ClassNotFoundException e) {
            logger.error("MySQL JDBC Driver not found", e);
            databaseAvailable = false;
        } catch (SQLException e) {
            logger.error("Failed to establish initial database connection", e);
            databaseAvailable = false;
        }
    }
    
    /**
     * Get the singleton instance of the DatabaseConnection.
     * @return the DatabaseConnection instance
     */
    public static synchronized DatabaseConnection getInstance() {
        if (instance == null) {
            instance = new DatabaseConnection();
        }
        return instance;
    }
    
    /**
     * Test if the database connection can be established.
     * @throws SQLException if connection fails
     */
    private void testConnection() throws SQLException {
        try (Connection testConn = DriverManager.getConnection(
                DB_URL + "?connectTimeout=" + (CONNECTION_TIMEOUT * 1000), 
                DB_USER, 
                DB_PASSWORD)) {
            logger.info("Database connection test successful");
            databaseAvailable = true;
        } catch (SQLException e) {
            databaseAvailable = false;
            logger.error("Database connection test failed", e);
            throw e;
        }
    }
    
    /**
     * Check if the database is available.
     * @return true if the database is available, false otherwise
     */
    public boolean isDatabaseAvailable() {
        return databaseAvailable;
    }
    
    /**
     * Get a connection to the database.
     * @return a Connection object
     * @throws SQLException if a database access error occurs
     */
    public Connection getConnection() throws SQLException {
        if (!databaseAvailable) {
            throw new SQLException("Database is not available. Check your MySQL server.");
        }
        
        if (connection == null || connection.isClosed()) {
            try {
                connection = DriverManager.getConnection(
                    DB_URL + "?connectTimeout=" + (CONNECTION_TIMEOUT * 1000), 
                    DB_USER, 
                    DB_PASSWORD);
                logger.info("Database connection established successfully");
            } catch (SQLException e) {
                databaseAvailable = false;
                logger.error("Failed to establish database connection", e);
                throw e;
            }
        }
        return connection;
    }
    
    /**
     * Close the database connection.
     */
    public void closeConnection() {
        if (connection != null) {
            try {
                connection.close();
                logger.info("Database connection closed successfully");
            } catch (SQLException e) {
                logger.error("Failed to close database connection", e);
            }
        }
    }
}
