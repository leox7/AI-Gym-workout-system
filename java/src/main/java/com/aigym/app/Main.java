package com.aigym.app;

import com.aigym.app.utils.DatabaseConnection;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

/**
 * Main application class for the AI Gym Workout System.
 * This class initializes the JavaFX application and loads the main UI.
 */
public class Main extends Application {
    private static final Logger logger = LoggerFactory.getLogger(Main.class);

    @Override
    public void start(Stage primaryStage) {
        try {
            // Check database connection first
            DatabaseConnection dbConnection = DatabaseConnection.getInstance();
            if (!dbConnection.isDatabaseAvailable()) {
                showDatabaseErrorAndExit(primaryStage);
                return;
            }
            
            // Log the resource path to help with debugging
            String resourcePath = "/fxml/login.fxml";
            logger.info("Attempting to load resource: {}", resourcePath);
            logger.info("Resource URL: {}", getClass().getResource(resourcePath));
            
            // Load the login view
            FXMLLoader loader = new FXMLLoader(getClass().getResource(resourcePath));
            
            if (loader == null) {
                throw new IOException("FXMLLoader could not be created");
            }
            
            Parent root = loader.load();
            
            if (root == null) {
                throw new IOException("Failed to load FXML root node");
            }
            
            // Configure the primary stage
            primaryStage.setTitle("AI Gym Workout System");
            primaryStage.setScene(new Scene(root));
            primaryStage.setMinWidth(1024);
            primaryStage.setMinHeight(768);
            primaryStage.show();
            
            logger.info("Application started successfully");
        } catch (IOException e) {
            logger.error("Failed to start application due to IO error", e);
            System.err.println("Error starting application (IO): " + e.getMessage());
            e.printStackTrace();
            showErrorAndExit("Application Error", 
                            "Failed to load application interface", 
                            "Error details: " + e.getMessage());
        } catch (NullPointerException e) {
            logger.error("Failed to start application due to null pointer", e);
            System.err.println("Error starting application (NPE): " + e.getMessage());
            System.err.println("This is likely due to a missing FXML file or resource");
            e.printStackTrace();
            showErrorAndExit("Resource Error", 
                            "Failed to load application resources", 
                            "Error details: " + e.getMessage());
        } catch (Exception e) {
            logger.error("Unexpected error during application start", e);
            System.err.println("Unexpected error: " + e.getMessage());
            e.printStackTrace();
            showErrorAndExit("Application Error", 
                            "An unexpected error occurred", 
                            "Error details: " + e.getMessage());
        }
    }
    
    private void showDatabaseErrorAndExit(Stage stage) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Database Connection Error");
        alert.setHeaderText("Cannot connect to database");
        alert.setContentText("The application could not connect to the MySQL database. " +
                            "Please ensure that MySQL is running and that the connection details are correct.");
        
        alert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                Platform.exit();
            }
        });
    }
    
    private void showErrorAndExit(String title, String header, String content) {
        Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle(title);
            alert.setHeaderText(header);
            alert.setContentText(content);
            
            alert.showAndWait().ifPresent(response -> {
                if (response == ButtonType.OK) {
                    Platform.exit();
                }
            });
        });
    }

    /**
     * Main method that launches the JavaFX application.
     * @param args command line arguments
     */
    public static void main(String[] args) {
        try {
            // Clear any system properties that might affect JavaFX initialization
            System.clearProperty("javafx.preloader");
            
            // Set verbose debugging for JavaFX
            System.setProperty("javafx.verbose", "true");
            
            // Launch the application
            launch(args);
        } catch (Exception e) {
            Logger logger = LoggerFactory.getLogger(Main.class);
            logger.error("Failed to launch application", e);
            System.err.println("Error launching application: " + e.getMessage());
            e.printStackTrace();
        }
    }
}