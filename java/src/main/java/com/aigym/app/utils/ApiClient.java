package com.aigym.app.utils;

import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

/**
 * Utility class for making API calls to the Python Flask backend.
 */
public class ApiClient {
    private static final Logger logger = LoggerFactory.getLogger(ApiClient.class);
    
    // Base URL for the Flask API
    public static final String API_BASE_URL = "http://localhost:5000";
    
    // Singleton instance
    private static ApiClient instance;
    private final CloseableHttpClient httpClient;
    
    /**
     * Private constructor to prevent direct instantiation.
     */
    private ApiClient() {
        httpClient = HttpClients.createDefault();
    }
    
    /**
     * Get the singleton instance of the ApiClient.
     * @return the ApiClient instance
     */
    public static synchronized ApiClient getInstance() {
        if (instance == null) {
            instance = new ApiClient();
        }
        return instance;
    }
    
    /**
     * Send a GET request to the specified endpoint.
     * @param endpoint the API endpoint
     * @return the response as a JSONObject
     * @throws IOException if an I/O error occurs
     */
    public JSONObject get(String endpoint) throws IOException {
        String url = endpoint.startsWith("/api") ? API_BASE_URL + endpoint : API_BASE_URL + "/api" + endpoint;
        HttpGet request = new HttpGet(url);
        request.setHeader("Accept", "application/json");
        
        try (CloseableHttpResponse response = httpClient.execute(request)) {
            HttpEntity entity = response.getEntity();
            String result = EntityUtils.toString(entity, StandardCharsets.UTF_8);
            logger.debug("GET response from {}: {}", endpoint, result);
            return new JSONObject(result);
        } catch (IOException e) {
            logger.error("Error executing GET request to {}", endpoint, e);
            throw e;
        }
    }
    
    /**
     * Send a POST request to the specified endpoint with the given data.
     * @param endpoint the API endpoint
     * @param data the request data as a JSONObject
     * @return the response as a JSONObject
     * @throws IOException if an I/O error occurs
     */
    public JSONObject post(String endpoint, JSONObject data) throws IOException {
        String url = endpoint.startsWith("/api") ? API_BASE_URL + endpoint : API_BASE_URL + "/api" + endpoint;
        HttpPost request = new HttpPost(url);
        request.setHeader("Content-Type", "application/json");
        request.setHeader("Accept", "application/json");
        
        StringEntity entity = new StringEntity(data.toString(), StandardCharsets.UTF_8);
        request.setEntity(entity);
        
        try (CloseableHttpResponse response = httpClient.execute(request)) {
            HttpEntity responseEntity = response.getEntity();
            String result = EntityUtils.toString(responseEntity, StandardCharsets.UTF_8);
            logger.debug("POST response from {}: {}", endpoint, result);
            return new JSONObject(result);
        } catch (IOException e) {
            logger.error("Error executing POST request to {}", endpoint, e);
            throw e;
        }
    }
    
    /**
     * Close the HTTP client and release resources.
     */
    public void close() {
        try {
            httpClient.close();
            logger.info("HTTP client closed successfully");
        } catch (IOException e) {
            logger.error("Failed to close HTTP client", e);
        }
    }
}
