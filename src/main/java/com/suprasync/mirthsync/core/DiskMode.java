package com.suprasync.mirthsync.core;

/**
 * Enumeration representing the disk storage modes for Mirth Connect configurations.
 * Controls the granularity of file extraction.
 */
public enum DiskMode {
    /**
     * Backup mode: Equivalent to Mirth Administrator backup and restore.
     * Everything is stored in a single XML file.
     */
    BACKUP("backup"),
    
    /**
     * Groups mode: All items expanded to "Group" or "Library" level.
     * Items are organized into group-level XML files.
     */
    GROUPS("groups"),
    
    /**
     * Items mode: Expand items one level deeper than 'groups'.
     * Channels and Code Templates are in their own individual XML files.
     */
    ITEMS("items"),
    
    /**
     * Code mode: Default behavior. Expands everything to the most granular level
     * (Javascript, SQL, etc). Each code snippet is extracted to a separate file.
     */
    CODE("code");
    
    private final String value;
    
    DiskMode(String value) {
        this.value = value;
    }
    
    public String getValue() {
        return value;
    }
    
    /**
     * Parse a disk mode from a string value.
     * @param value The string representation of the disk mode
     * @return The corresponding DiskMode enum value
     * @throws IllegalArgumentException if the value is not valid
     */
    public static DiskMode fromString(String value) {
        for (DiskMode mode : values()) {
            if (mode.value.equalsIgnoreCase(value)) {
                return mode;
            }
        }
        throw new IllegalArgumentException("Invalid disk mode: " + value);
    }
    
    @Override
    public String toString() {
        return value;
    }
}
