package com.suprasync.mirthsync;

import com.suprasync.mirthsync.core.DiskMode;
import com.suprasync.mirthsync.files.FileOperations;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Basic smoke tests to verify the Java translation compiles and runs.
 */
public class SmokeTest {
    
    @Test
    public void testDiskModeEnum() {
        assertEquals(DiskMode.CODE, DiskMode.fromString("code"));
        assertEquals(DiskMode.BACKUP, DiskMode.fromString("backup"));
        assertEquals(DiskMode.GROUPS, DiskMode.fromString("groups"));
        assertEquals(DiskMode.ITEMS, DiskMode.fromString("items"));
        
        assertEquals("code", DiskMode.CODE.getValue());
        assertEquals("backup", DiskMode.BACKUP.getValue());
    }
    
    @Test
    public void testDiskModeInvalid() {
        assertThrows(IllegalArgumentException.class, () -> {
            DiskMode.fromString("invalid");
        });
    }
    
    @Test
    public void testFileOperationsSafeName() {
        String safe = FileOperations.safeName("test-file");
        assertEquals("test-file", safe);
        
        // Test that slashes are encoded
        String withSlash = FileOperations.safeName("test/file");
        assertEquals("test%2Ffile", withSlash);
        
        String withBackslash = FileOperations.safeName("test\\file");
        assertEquals("test%5Cfile", withBackslash);
    }
    
    @Test
    public void testFileOperationsRemoveExtension() {
        assertEquals("file", FileOperations.removeExtension("file.xml"));
        assertEquals("path/to/file", FileOperations.removeExtension("path/to/file.txt"));
        assertEquals("noext", FileOperations.removeExtension("noext"));
        assertNull(FileOperations.removeExtension(null));
    }
    
    @Test
    public void testFileOperationsXmlFileSeq() throws IOException {
        // Create temp directory with some XML files
        Path tempDir = Files.createTempDirectory("mirthsync-test");
        try {
            Files.writeString(tempDir.resolve("test1.xml"), "<xml/>");
            Files.writeString(tempDir.resolve("test2.xml"), "<xml/>");
            Files.writeString(tempDir.resolve("test.txt"), "text");
            
            var xmlFiles = FileOperations.xmlFileSeq(0, tempDir.toFile());
            
            // Should find 2 XML files, not the txt file
            assertEquals(2, xmlFiles.size());
            assertTrue(xmlFiles.stream().allMatch(f -> f.getName().endsWith(".xml")));
            
        } finally {
            // Cleanup
            Files.walk(tempDir)
                .sorted((a, b) -> b.compareTo(a)) // Delete files before directories
                .forEach(path -> {
                    try {
                        Files.delete(path);
                    } catch (IOException e) {
                        // Ignore
                    }
                });
        }
    }
}
