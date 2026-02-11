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
            try {
                run(config);
                Logger.info("Finished!");
                return 0;
            } catch (Exception e) {
                Logger.error("Error during operation: " + e.getMessage(), e);
                return 1;
            }
            
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
    private static void run(AppConfig config) throws Exception {
        String action = config.getAction();
        
        if ("git".equals(action)) {
            // Git operations don't require server authentication
            if (config.getArguments().isEmpty()) {
                throw new IllegalArgumentException("Git subcommand required");
            }
            
            String subcommand = config.getArguments().get(0);
            String[] args = config.getArguments().subList(1, config.getArguments().size())
                .toArray(new String[0]);
            
            com.suprasync.mirthsync.git.GitOperations.execute(config, subcommand, args);
            
        } else if ("pull".equals(action) || "push".equals(action)) {
            // These operations require server authentication
            com.suprasync.mirthsync.http.HttpClientWrapper client = 
                new com.suprasync.mirthsync.http.HttpClientWrapper(config);
            
            client.authenticate();
            
            // Get list of APIs to process
            var apis = com.suprasync.mirthsync.apis.ApiDefinitions.getApis(config);
            
            // Preprocess APIs
            config = com.suprasync.mirthsync.apis.ApiDefinitions.iterateApis(
                config, apis, com.suprasync.mirthsync.apis.ApiDefinitions::preprocessApi);
            
            if ("pull".equals(action)) {
                // Capture local files before pull for orphan detection
                config = com.suprasync.mirthsync.apis.ApiDefinitions.iterateApis(
                    config, apis, com.suprasync.mirthsync.actions.Actions::capturePrePullLocalFiles);
                
                // Execute pull
                config = com.suprasync.mirthsync.apis.ApiDefinitions.iterateApis(
                    config, apis, com.suprasync.mirthsync.actions.Actions::download);
                
                // Clean up orphaned files
                com.suprasync.mirthsync.actions.Actions.cleanupOrphanedFiles(config);
                
            } else { // push
                // Initialize bulk deploy if needed
                if (config.isDeployAll()) {
                    config.setBulkDeployChannels(new java.util.concurrent.atomic.AtomicReference<>(new java.util.ArrayList<>()));
                }
                
                // Execute push
                config = com.suprasync.mirthsync.apis.ApiDefinitions.iterateApis(
                    config, apis, com.suprasync.mirthsync.actions.Actions::upload);
                
                // Deploy all channels if configured
                if (config.isDeployAll()) {
                    com.suprasync.mirthsync.apis.ApiDefinitions.deployAllChannels(config);
                }
            }
            
            // Auto-commit if configured
            com.suprasync.mirthsync.git.GitOperations.autoCommitAfterOperation(config);
            
        } else {
            throw new IllegalArgumentException("Unknown action: " + action);
        }
    }
}
