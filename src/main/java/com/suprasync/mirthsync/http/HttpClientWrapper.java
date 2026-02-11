package com.suprasync.mirthsync.http;

import com.suprasync.mirthsync.core.AppConfig;
import com.suprasync.mirthsync.logging.Logger;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpRequest.BodyPublishers;
import java.net.http.HttpResponse.BodyHandlers;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.X509Certificate;
import java.time.Duration;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

/**
 * HTTP client wrapper for Mirth Connect API calls.
 * Handles authentication, SSL, and common request patterns.
 */
public class HttpClientWrapper {
    
    private final HttpClient client;
    private final AppConfig config;
    private String sessionCookie;
    
    /**
     * Create an HTTP client wrapper.
     * 
     * @param config The application configuration
     */
    public HttpClientWrapper(AppConfig config) {
        this.config = config;
        this.client = createClient(config.isIgnoreCertWarnings());
    }
    
    /**
     * Create an HTTP client, optionally trusting all certificates.
     * 
     * @param ignoreCertWarnings Whether to ignore certificate warnings
     * @return Configured HttpClient
     */
    private HttpClient createClient(boolean ignoreCertWarnings) {
        HttpClient.Builder builder = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(30))
            .followRedirects(HttpClient.Redirect.NORMAL);
        
        if (ignoreCertWarnings) {
            try {
                // Create trust manager that trusts all certificates
                TrustManager[] trustAllCerts = new TrustManager[]{
                    new X509TrustManager() {
                        public X509Certificate[] getAcceptedIssuers() { return null; }
                        public void checkClientTrusted(X509Certificate[] certs, String authType) {}
                        public void checkServerTrusted(X509Certificate[] certs, String authType) {}
                    }
                };
                
                SSLContext sslContext = SSLContext.getInstance("TLS");
                sslContext.init(null, trustAllCerts, new java.security.SecureRandom());
                builder.sslContext(sslContext);
            } catch (NoSuchAlgorithmException | KeyManagementException e) {
                Logger.warn("Failed to configure SSL to ignore certificate warnings: " + e.getMessage());
            }
        }
        
        return builder.build();
    }
    
    /**
     * Authenticate to the Mirth Connect server.
     * 
     * @throws IOException if authentication fails
     */
    public void authenticate() throws IOException, InterruptedException {
        Logger.info("Authenticating to server at " + config.getServer());
        
        if (config.getToken() != null && !config.getToken().isEmpty()) {
            // Use token authentication
            sessionCookie = "JSESSIONID=" + config.getToken();
            Logger.debug("Using token authentication");
        } else {
            // Use username/password authentication
            String loginUrl = config.getServer() + "/users/_login";
            String formData = "username=" + config.getUsername() + 
                            "&password=" + config.getPassword();
            
            HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(loginUrl))
                .header("Content-Type", "application/x-www-form-urlencoded")
                .header("X-Requested-With", "XMLHttpRequest")
                .POST(BodyPublishers.ofString(formData))
                .build();
            
            HttpResponse<String> response = client.send(request, BodyHandlers.ofString());
            
            if (response.statusCode() != 200) {
                throw new IOException("Authentication failed: " + response.statusCode());
            }
            
            // Extract session cookie
            response.headers().firstValue("Set-Cookie").ifPresent(cookie -> {
                if (cookie.startsWith("JSESSIONID=")) {
                    sessionCookie = cookie.split(";")[0];
                }
            });
            
            if (sessionCookie == null) {
                throw new IOException("No session cookie received from server");
            }
            
            Logger.debug("Authentication successful");
        }
    }
    
    /**
     * Execute an HTTP GET request.
     * 
     * @param path The API path (relative to server URL)
     * @return Response body
     * @throws IOException if request fails
     */
    public String get(String path) throws IOException, InterruptedException {
        String url = config.getServer() + path;
        
        HttpRequest request = HttpRequest.newBuilder()
            .uri(URI.create(url))
            .header("X-Requested-With", "XMLHttpRequest")
            .header("Cookie", sessionCookie)
            .GET()
            .build();
        
        Logger.debugf("GET %s", url);
        HttpResponse<String> response = client.send(request, BodyHandlers.ofString());
        
        if (response.statusCode() != 200) {
            throw new IOException("GET request failed: " + response.statusCode() + " " + path);
        }
        
        return response.body();
    }
    
    /**
     * Execute an HTTP PUT request with XML body.
     * 
     * @param path The API path
     * @param xmlBody The XML body content
     * @return Response status code
     * @throws IOException if request fails
     */
    public int putXml(String path, String xmlBody) throws IOException, InterruptedException {
        String url = config.getServer() + path;
        
        HttpRequest request = HttpRequest.newBuilder()
            .uri(URI.create(url))
            .header("X-Requested-With", "XMLHttpRequest")
            .header("Cookie", sessionCookie)
            .header("Content-Type", "application/xml")
            .PUT(BodyPublishers.ofString(xmlBody))
            .build();
        
        Logger.debugf("PUT %s", url);
        HttpResponse<String> response = client.send(request, BodyHandlers.ofString());
        
        return response.statusCode();
    }
    
    /**
     * Execute an HTTP POST request with XML body.
     * 
     * @param path The API path
     * @param xmlBody The XML body content
     * @return Response body
     * @throws IOException if request fails
     */
    public String postXml(String path, String xmlBody) throws IOException, InterruptedException {
        String url = config.getServer() + path;
        
        HttpRequest request = HttpRequest.newBuilder()
            .uri(URI.create(url))
            .header("X-Requested-With", "XMLHttpRequest")
            .header("Cookie", sessionCookie)
            .header("Content-Type", "application/xml")
            .POST(BodyPublishers.ofString(xmlBody))
            .build();
        
        Logger.debugf("POST %s", url);
        HttpResponse<String> response = client.send(request, BodyHandlers.ofString());
        
        if (response.statusCode() < 200 || response.statusCode() >= 300) {
            throw new IOException("POST request failed: " + response.statusCode() + " " + path);
        }
        
        return response.body();
    }
}
