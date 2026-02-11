package com.suprasync.mirthsync.files;

import com.suprasync.mirthsync.logging.Logger;

import java.io.File;
import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

/**
 * File system operations and utilities for mirthSync.
 * Provides methods for finding, filtering, and manipulating files.
 */
public class FileOperations {

    /**
     * Encode special characters in path names that could cause issues.
     * Forward slash becomes %2F, backslash becomes %5C.
     * 
     * @param name The path component to encode
     * @return The encoded name
     */
    private static String encodePathChars(String name) {
        if (name == null) {
            return null;
        }
        return name.replace("/", "%2F").replace("\\", "%5C");
    }

    /**
     * Validates that a string is safe for file creation and doesn't span paths.
     * Encodes special characters as needed.
     * 
     * @param name The name to validate
     * @return The safe (possibly encoded) name
     * @throws IllegalArgumentException if the name is unsafe
     */
    public static String safeName(String name) {
        Logger.debugf("safe-name pre: (%s)", name);
        
        if (name == null) {
            return null;
        }
        
        String encoded = encodePathChars(name);
        
        // Verify the name doesn't try to escape the directory
        File testFile = new File(encoded);
        if (!encoded.equals(testFile.getName())) {
            throw new IllegalArgumentException(
                "Name does not appear to be safe for file creation - " + name + 
                " - Check for invalid characters.");
        }
        
        Logger.debugf("safe-name post: (%s)", encoded);
        return encoded;
    }

    /**
     * Remove the file extension from a file path.
     * 
     * @param filePath The file path
     * @return The path without extension
     */
    public static String removeExtension(String filePath) {
        if (filePath == null) {
            return null;
        }
        int lastDot = filePath.lastIndexOf('.');
        return lastDot > 0 ? filePath.substring(0, lastDot) : filePath;
    }

    /**
     * Check if a file name ends with .xml (case-insensitive).
     * 
     * @param file The file to check
     * @return true if the file ends with .xml
     */
    private static boolean endsWithXml(File file) {
        return file.getName().toLowerCase().endsWith(".xml");
    }

    /**
     * Create a predicate that checks if a filename matches (case-insensitive).
     * 
     * @param filename The filename to match
     * @return A predicate for the match
     */
    private static Predicate<File> filenameMatches(String filename) {
        String lowerFilename = filename.toLowerCase();
        return file -> file.getName().toLowerCase().equals(lowerFilename);
    }

    /**
     * Walk a directory tree up to a maximum depth, applying predicates to filter files.
     * 
     * @param maxDepth Maximum depth to traverse (0 = only the root directory)
     * @param predicates List of predicates to filter files
     * @param rootPath The root path to start from
     * @return List of files matching all predicates
     */
    private static List<File> filteredFileSeq(int maxDepth, List<Predicate<File>> predicates, Path rootPath) {
        List<File> result = new ArrayList<>();
        
        try {
            Files.walkFileTree(rootPath, java.util.EnumSet.noneOf(FileVisitOption.class), maxDepth + 1,
                new SimpleFileVisitor<Path>() {
                    @Override
                    public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) {
                        File f = file.toFile();
                        if (predicates.stream().allMatch(pred -> pred.test(f))) {
                            result.add(f);
                        }
                        return FileVisitResult.CONTINUE;
                    }
                    
                    @Override
                    public FileVisitResult visitFileFailed(Path file, IOException exc) {
                        Logger.warn("Failed to visit file: " + file);
                        return FileVisitResult.CONTINUE;
                    }
                });
        } catch (IOException e) {
            Logger.error("Error walking file tree: " + rootPath, e);
        }
        
        return result;
    }

    /**
     * Get a sequence of XML files at the specified directory up to the given depth.
     * 
     * @param depth Maximum depth to traverse
     * @param dir The directory to search
     * @return List of XML files
     */
    public static List<File> xmlFileSeq(int depth, File dir) {
        List<Predicate<File>> predicates = List.of(
            File::isFile,
            FileOperations::endsWithXml
        );
        return filteredFileSeq(depth, predicates, dir.toPath());
    }

    /**
     * Get XML files that do NOT have a specific name.
     * 
     * @param depth Maximum depth to traverse
     * @param name The name to exclude (without .xml extension)
     * @param dir The directory to search
     * @return List of XML files not matching the name
     */
    public static List<File> withoutNamedXmlFilesSeq(int depth, String name, File dir) {
        String xmlName = name + ".xml";
        List<Predicate<File>> predicates = List.of(
            File::isFile,
            FileOperations::endsWithXml,
            filenameMatches(xmlName).negate()
        );
        return filteredFileSeq(depth, predicates, dir.toPath());
    }

    /**
     * Get XML files that have a specific name.
     * 
     * @param depth Maximum depth to traverse
     * @param name The name to include (without .xml extension)
     * @param dir The directory to search
     * @return List of XML files matching the name
     */
    public static List<File> onlyNamedXmlFilesSeq(int depth, String name, File dir) {
        String xmlName = name + ".xml";
        List<Predicate<File>> predicates = List.of(
            File::isFile,
            FileOperations::endsWithXml,
            filenameMatches(xmlName)
        );
        return filteredFileSeq(depth, predicates, dir.toPath());
    }

    /**
     * Ensure parent directories exist for a file path.
     * 
     * @param filePath The file path
     * @throws IOException if directories cannot be created
     */
    public static void makeParents(Path filePath) throws IOException {
        Path parent = filePath.getParent();
        if (parent != null && !Files.exists(parent)) {
            Files.createDirectories(parent);
        }
    }

    /**
     * Write content to a file, creating parent directories as needed.
     * 
     * @param filePath The file path
     * @param content The content to write
     * @throws IOException if write fails
     */
    public static void writeFile(Path filePath, String content) throws IOException {
        makeParents(filePath);
        Files.writeString(filePath, content, StandardOpenOption.CREATE, 
                         StandardOpenOption.TRUNCATE_EXISTING);
    }

    /**
     * Read the entire contents of a file as a string.
     * 
     * @param filePath The file path
     * @return The file contents
     * @throws IOException if read fails
     */
    public static String readFile(Path filePath) throws IOException {
        return Files.readString(filePath);
    }
}
