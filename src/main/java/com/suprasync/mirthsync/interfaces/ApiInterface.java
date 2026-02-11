package com.suprasync.mirthsync.interfaces;

import com.suprasync.mirthsync.core.AppConfig;
import org.jdom2.Element;

import java.io.File;
import java.util.List;

/**
 * Interface defining operations for different Mirth API types.
 * Each API type (channels, code templates, etc.) implements this interface.
 */
public interface ApiInterface {
    
    /**
     * Get the REST API path for this API type.
     * 
     * @return The REST path (e.g., "/channels")
     */
    String getRestPath();
    
    /**
     * Get the local filesystem path for storing this API's files.
     * 
     * @param targetDir The base target directory
     * @return The local path
     */
    String getLocalPath(String targetDir);
    
    /**
     * Find all XML elements for this API type in the server response.
     * 
     * @param doc The XML document from the server
     * @return List of elements
     */
    List<Element> findElements(Element doc);
    
    /**
     * Extract the ID from an element.
     * 
     * @param element The element
     * @return The ID
     */
    String findId(Element element);
    
    /**
     * Extract the name from an element.
     * 
     * @param element The element
     * @return The name
     */
    String findName(Element element);
    
    /**
     * Check if an element is enabled.
     * 
     * @param element The element
     * @return true if enabled
     */
    boolean isEnabled(Element element);
    
    /**
     * Determine if this element should be skipped based on configuration.
     * 
     * @param element The element
     * @param config The application configuration
     * @return true if should be skipped
     */
    default boolean shouldSkip(Element element, AppConfig config) {
        return config.isSkipDisabled() && !isEnabled(element);
    }
    
    /**
     * Preprocess the configuration before operations.
     * 
     * @param config The application configuration
     * @return Updated configuration
     */
    default AppConfig preprocess(AppConfig config) {
        return config;
    }
    
    /**
     * Process after a push operation.
     * 
     * @param config The application configuration
     * @param result The result of the push
     */
    default void afterPush(AppConfig config, Object result) {
        // Default: no action
    }
    
    /**
     * Find local API files for upload.
     * 
     * @param directory The directory to search
     * @return List of XML files
     */
    List<File> findApiFiles(File directory);
    
    /**
     * Deconstruct an XML element into file/content pairs for writing to disk.
     * 
     * @param config The application configuration
     * @param filePath The base file path
     * @param element The element to deconstruct
     * @return List of (path, content) pairs
     */
    List<FileContent> deconstructNode(AppConfig config, String filePath, Element element);
    
    /**
     * A simple record to hold file path and content.
     */
    record FileContent(String path, String content) {}
}
