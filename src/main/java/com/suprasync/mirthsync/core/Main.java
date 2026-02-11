package com.suprasync.mirthsync.core;

import com.suprasync.mirthsync.cli.CliConfig;
import com.suprasync.mirthsync.logging.Logger;

import java.io.PrintWriter;

/**
 * Main entry point for mirthSync application.
 * Handles command-line execution and orchestrates the application flow.
 */
public class Main {

    /**
     * Main entry point for command-line execution.
     * 
     * @param args Command-line arguments
     */
    public static void main(String[] args) {
        int exitCode = mainFunc(args, null, null);
        System.exit(exitCode);
    }

    /**
     * Main function that can be called programmatically with custom output streams.
     * This allows for testing and embedding.
     * 
     * @param args Command-line arguments
     * @param outStream Output stream (or null for System.out)
     * @param errStream Error stream (or null for System.err)
     * @return Exit code (0 for success, non-zero for failure)
     */
    public static int mainFunc(String[] args, PrintWriter outStream, PrintWriter errStream) {
        try {
            // Parse command-line arguments
            AppConfig config = CliConfig.parseArgs(args);
            
            if (config == null || config.hasErrors()) {
                return 1;
            }
            
            // Set up logging based on verbosity
            Logger.setVerbosity(config.getVerbosity());
            
            // Run the appropriate action
            run(config);
            
            Logger.info("Finished!");
            return 0;
            
        } catch (Exception e) {
            Logger.error("Error executing mirthSync", e);
            e.printStackTrace();
            return 1;
        }
    }

    /**
     * Execute the configured action (push, pull, or git operation).
     * 
     * @param config The application configuration
     */
    private static void run(AppConfig config) {
        String action = config.getAction();
        
        if ("git".equals(action)) {
            // Git operations don't require server authentication
            Logger.info("Executing git " + config.getArguments().get(0));
            // TODO: GitOperations.execute(config);
            Logger.warn("Git operations not yet implemented in Java version");
        } else if ("pull".equals(action)) {
            // Pull operation
            Logger.info("Authenticating to server at " + config.getServer() + " as " + config.getUsername());
            // TODO: Implement pull operation
            Logger.warn("Pull operation not yet implemented in Java version");
        } else if ("push".equals(action)) {
            // Push operation
            Logger.info("Authenticating to server at " + config.getServer() + " as " + config.getUsername());
            // TODO: Implement push operation
            Logger.warn("Push operation not yet implemented in Java version");
        } else {
            throw new IllegalArgumentException("Unknown action: " + action);
        }
    }
}
