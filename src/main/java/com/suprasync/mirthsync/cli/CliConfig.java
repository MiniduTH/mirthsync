package com.suprasync.mirthsync.cli;

import com.suprasync.mirthsync.core.AppConfig;
import com.suprasync.mirthsync.core.DiskMode;
import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

/**
 * Command-line interface configuration using Picocli.
 * Handles parsing and validation of command-line arguments.
 */
@Command(name = "mirthsync", 
         mixinStandardHelpOptions = true,
         version = "mirthsync 3.5.2",
         description = "MirthSync - Mirth Connect version control and CI/CD automation tool")
public class CliConfig implements Runnable {
    
    @Option(names = {"-s", "--server"}, 
            description = "Full HTTP(s) URL of the Mirth Connect server",
            required = false)
    private String server;
    
    @Option(names = {"-u", "--username"}, 
            description = "Username used for authentication")
    private String username;
    
    @Option(names = {"-p", "--password"}, 
            description = "Password used for authentication")
    private String password;
    
    @Option(names = {"--token"}, 
            description = "Authentication token (HTTP session token). Mutually exclusive with username/password")
    private String token;
    
    @Option(names = {"-i", "--ignore-cert-warnings"}, 
            description = "Ignore certificate warnings",
            defaultValue = "false")
    private boolean ignoreCertWarnings;
    
    @Option(names = {"-v"}, 
            description = "Verbosity level. Specify multiple times to increase level")
    private boolean[] verbosity;
    
    @Option(names = {"-f", "--force"}, 
            description = "Overwrite existing files during pull and remote items during push",
            defaultValue = "false")
    private boolean force;
    
    @Option(names = {"-t", "--target"}, 
            description = "Base directory used for pushing or pulling files",
            required = true)
    private String target;
    
    @Option(names = {"-m", "--disk-mode"}, 
            description = "Disk format: backup, groups, items, or code (default)",
            defaultValue = "code")
    private String diskMode;
    
    @Option(names = {"-r", "--restrict-to-path"}, 
            description = "Limit scope to a specific path within target directory",
            defaultValue = "")
    private String restrictToPath;
    
    @Option(names = {"--include-configuration-map"}, 
            description = "Include configuration map in push/pull",
            defaultValue = "false")
    private boolean includeConfigurationMap;
    
    @Option(names = {"--skip-disabled"}, 
            description = "Skip disabled channels",
            defaultValue = "false")
    private boolean skipDisabled;
    
    @Option(names = {"-d", "--deploy"}, 
            description = "Deploy channels immediately after push",
            defaultValue = "false")
    private boolean deploy;
    
    @Option(names = {"--deploy-all"}, 
            description = "Deploy all channels in one API call at the end",
            defaultValue = "false")
    private boolean deployAll;
    
    @Option(names = {"-I", "--interactive"}, 
            description = "Allow console prompts for user input",
            defaultValue = "false")
    private boolean interactive;
    
    @Option(names = {"--commit-message"}, 
            description = "Commit message for git operations",
            defaultValue = "mirthsync commit")
    private String commitMessage;
    
    @Option(names = {"--git-author"}, 
            description = "Git author name for commits")
    private String gitAuthor;
    
    @Option(names = {"--git-email"}, 
            description = "Git author email for commits")
    private String gitEmail;
    
    @Option(names = {"--auto-commit"}, 
            description = "Automatically commit changes after operations",
            defaultValue = "false")
    private boolean autoCommit;
    
    @Option(names = {"--git-init"}, 
            description = "Initialize git repository if not present",
            defaultValue = "false")
    private boolean gitInit;
    
    @Option(names = {"--delete-orphaned"}, 
            description = "Delete orphaned local files during pull",
            defaultValue = "false")
    private boolean deleteOrphaned;
    
    @Parameters(index = "0", 
                description = "Action: push, pull, or git")
    private String action;
    
    @Parameters(index = "1..*", 
                arity = "0..*",
                description = "Additional arguments for git commands")
    private List<String> arguments = new ArrayList<>();
    
    private AppConfig appConfig;
    
    @Override
    public void run() {
        // This method is called by Picocli after parsing
        // We don't need to do anything here as we handle execution in parseArgs
    }
    
    /**
     * Parse command-line arguments and create an AppConfig.
     * 
     * @param args Command-line arguments
     * @return Populated AppConfig, or null if parsing failed
     */
    public static AppConfig parseArgs(String[] args) {
        CliConfig cli = new CliConfig();
        CommandLine cmd = new CommandLine(cli);
        
        try {
            cmd.parseArgs(args);
            
            // Validate
            if (cli.action == null) {
                System.err.println("Error: Action (push, pull, or git) is required");
                cmd.usage(System.err);
                return null;
            }
            
            if (!List.of("push", "pull", "git").contains(cli.action)) {
                System.err.println("Error: Action must be one of: push, pull, git");
                return null;
            }
            
            // Git operations don't require server credentials
            boolean isGitAction = "git".equals(cli.action);
            if (!isGitAction) {
                // Check for required server and authentication
                if (cli.server == null || cli.server.isEmpty()) {
                    System.err.println("Error: --server is required for push/pull operations");
                    return null;
                }
                
                // Validate server URL
                try {
                    new URL(cli.server);
                } catch (MalformedURLException e) {
                    System.err.println("Error: Invalid server URL: " + cli.server);
                    return null;
                }
                
                // Check authentication (either username/password OR token)
                boolean hasUsernamePassword = cli.username != null && cli.password != null 
                                               && !cli.password.isEmpty();
                boolean hasToken = cli.token != null && !cli.token.isEmpty();
                
                if (!hasUsernamePassword && !hasToken) {
                    System.err.println("Error: Either username/password or token is required");
                    return null;
                }
                
                if (hasUsernamePassword && hasToken) {
                    System.err.println("Error: Cannot use both username/password and token authentication");
                    return null;
                }
            }
            
            // Check for password from environment if not provided
            if (cli.password == null || cli.password.isEmpty()) {
                String envPassword = System.getenv("MIRTHSYNC_PASSWORD");
                if (envPassword != null && !envPassword.isEmpty()) {
                    cli.password = envPassword;
                }
            }
            
            // Prompt for password if interactive and no password provided
            if (cli.interactive && (cli.password == null || cli.password.isEmpty()) 
                && (cli.token == null || cli.token.isEmpty())) {
                cli.password = readPassword("Password: ");
            }
            
            return cli.toAppConfig();
            
        } catch (CommandLine.ParameterException e) {
            System.err.println("Error: " + e.getMessage());
            cmd.usage(System.err);
            return null;
        }
    }
    
    /**
     * Read a password from console.
     * 
     * @param prompt The prompt to display
     * @return The password, or null if console unavailable
     */
    private static String readPassword(String prompt) {
        java.io.Console console = System.console();
        if (console != null) {
            char[] passwordChars = console.readPassword(prompt);
            return new String(passwordChars);
        }
        return null;
    }
    
    /**
     * Convert CLI options to AppConfig.
     * 
     * @return Populated AppConfig
     */
    private AppConfig toAppConfig() {
        AppConfig config = new AppConfig();
        
        config.setServer(stripTrailingSlashes(server));
        config.setUsername(username);
        config.setPassword(password);
        config.setToken(token);
        config.setIgnoreCertWarnings(ignoreCertWarnings);
        config.setTarget(stripTrailingSlashes(target));
        config.setDiskMode(DiskMode.fromString(diskMode));
        config.setRestrictToPath(stripTrailingSlashes(restrictToPath));
        config.setAction(action);
        config.setArguments(arguments);
        config.setForce(force);
        config.setDeploy(deploy);
        config.setDeployAll(deployAll);
        config.setInteractive(interactive);
        config.setIncludeConfigurationMap(includeConfigurationMap);
        config.setSkipDisabled(skipDisabled);
        config.setCommitMessage(commitMessage);
        config.setGitAuthor(gitAuthor != null ? gitAuthor : System.getProperty("user.name"));
        config.setGitEmail(gitEmail);
        config.setAutoCommit(autoCommit);
        config.setGitInit(gitInit);
        config.setDeleteOrphaned(deleteOrphaned);
        config.setVerbosity(verbosity != null ? verbosity.length : 0);
        
        return config;
    }
    
    /**
     * Remove trailing slashes from a path string.
     * 
     * @param path The path
     * @return Path without trailing slashes
     */
    private String stripTrailingSlashes(String path) {
        if (path == null) {
            return null;
        }
        return path.replaceAll("[/\\\\]+$", "");
    }
}
