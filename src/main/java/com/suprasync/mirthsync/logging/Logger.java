package com.suprasync.mirthsync.logging;

import org.slf4j.LoggerFactory;

/**
 * Logging utility class that wraps SLF4J logging functionality.
 * Provides convenience methods for logging at different levels.
 */
public class Logger {
    private static final org.slf4j.Logger logger = LoggerFactory.getLogger("mirthsync");
    private static int verbosityLevel = 0;

    /**
     * Set the verbosity level for logging.
     * 0 = INFO and above
     * 1 = DEBUG and above  
     * 2+ = TRACE and above
     */
    public static void setVerbosity(int level) {
        verbosityLevel = level;
    }

    public static void info(String message) {
        logger.info(message);
    }

    public static void info(String format, Object... args) {
        logger.info(format, args);
    }
    
    public static void infof(String format, Object... args) {
        logger.info(format, args);
    }

    public static void debug(String message) {
        if (verbosityLevel >= 1) {
            logger.debug(message);
        }
    }

    public static void debugf(String format, Object... args) {
        if (verbosityLevel >= 1) {
            logger.debug(format, args);
        }
    }

    public static void trace(String message) {
        if (verbosityLevel >= 2) {
            logger.trace(message);
        }
    }

    public static void tracef(String format, Object... args) {
        if (verbosityLevel >= 2) {
            logger.trace(format, args);
        }
    }

    public static void warn(String message) {
        logger.warn(message);
    }

    public static void warn(String format, Object... args) {
        logger.warn(format, args);
    }
    
    public static void warnf(String format, Object... args) {
        logger.warn(format, args);
    }

    public static void error(String message) {
        logger.error(message);
    }

    public static void error(String message, Throwable t) {
        logger.error(message, t);
    }

    public static void error(String format, Object... args) {
        logger.error(format, args);
    }
}
