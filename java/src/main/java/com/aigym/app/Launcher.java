package com.aigym.app;

/**
 * Launcher class to handle the JavaFX application launch.
 * This class is used as the main entry point for the packaged application.
 * It addresses module-related issues when running from JAR files by not extending
 * the JavaFX Application class directly.
 */
public class Launcher {
    /**
     * Main method that delegates to the JavaFX application main method.
     * This approach helps avoid module-related issues when packaging the application.
     * @param args command line arguments
     */
    public static void main(String[] args) {
        System.setProperty("javafx.preloader", "com.aigym.app.Main");
        
        // Print debug information
        System.out.println("Starting AI Gym Workout System...");
        System.out.println("Java version: " + System.getProperty("java.version"));
        
        // Launch the JavaFX application
        Main.main(args);
    }
}
