package com.suprasync.mirthsync.core;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Application configuration record.
 * Holds all configuration parameters for a mirthSync operation.
 */
public class AppConfig {
    // Server configuration
    private String server;
    private String username;
    private String password;
    private String token;
    private boolean ignoreCertWarnings;
    
    // Target configuration
    private String target;
    private DiskMode diskMode;
    private String restrictToPath;
    
    // Action configuration
    private String action;
    private List<String> arguments;
    
    // Operation flags
    private boolean force;
    private boolean deploy;
    private boolean deployAll;
    private boolean interactive;
    private boolean includeConfigurationMap;
    private boolean skipDisabled;
    private boolean deleteOrphaned;
    
    // Git configuration
    private boolean gitInit;
    private boolean autoCommit;
    private String commitMessage;
    private String gitAuthor;
    private String gitEmail;
    
    // Runtime state
    private int verbosity;
    private String exitMessage;
    private int exitCode;
    private AtomicReference<List<String>> bulkDeployChannels;
    
    // API state (would be more complex in full implementation)
    private String api;
    private Object elLoc; // XML location - would be proper type in full implementation
    
    public AppConfig() {
        this.arguments = new ArrayList<>();
        this.restrictToPath = "";
        this.diskMode = DiskMode.CODE;
        this.commitMessage = "mirthsync commit";
        this.exitCode = 0;
    }
    
    // Getters and setters
    public String getServer() { return server; }
    public void setServer(String server) { this.server = server; }
    
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
    
    public String getToken() { return token; }
    public void setToken(String token) { this.token = token; }
    
    public boolean isIgnoreCertWarnings() { return ignoreCertWarnings; }
    public void setIgnoreCertWarnings(boolean ignoreCertWarnings) { 
        this.ignoreCertWarnings = ignoreCertWarnings; 
    }
    
    public String getTarget() { return target; }
    public void setTarget(String target) { this.target = target; }
    
    public DiskMode getDiskMode() { return diskMode; }
    public void setDiskMode(DiskMode diskMode) { this.diskMode = diskMode; }
    
    public String getRestrictToPath() { return restrictToPath; }
    public void setRestrictToPath(String restrictToPath) { 
        this.restrictToPath = restrictToPath; 
    }
    
    public String getAction() { return action; }
    public void setAction(String action) { this.action = action; }
    
    public List<String> getArguments() { return arguments; }
    public void setArguments(List<String> arguments) { this.arguments = arguments; }
    
    public boolean isForce() { return force; }
    public void setForce(boolean force) { this.force = force; }
    
    public boolean isDeploy() { return deploy; }
    public void setDeploy(boolean deploy) { this.deploy = deploy; }
    
    public boolean isDeployAll() { return deployAll; }
    public void setDeployAll(boolean deployAll) { this.deployAll = deployAll; }
    
    public boolean isInteractive() { return interactive; }
    public void setInteractive(boolean interactive) { this.interactive = interactive; }
    
    public boolean isIncludeConfigurationMap() { return includeConfigurationMap; }
    public void setIncludeConfigurationMap(boolean includeConfigurationMap) {
        this.includeConfigurationMap = includeConfigurationMap;
    }
    
    public boolean isSkipDisabled() { return skipDisabled; }
    public void setSkipDisabled(boolean skipDisabled) { 
        this.skipDisabled = skipDisabled; 
    }
    
    public boolean isDeleteOrphaned() { return deleteOrphaned; }
    public void setDeleteOrphaned(boolean deleteOrphaned) { 
        this.deleteOrphaned = deleteOrphaned; 
    }
    
    public boolean isGitInit() { return gitInit; }
    public void setGitInit(boolean gitInit) { this.gitInit = gitInit; }
    
    public boolean isAutoCommit() { return autoCommit; }
    public void setAutoCommit(boolean autoCommit) { this.autoCommit = autoCommit; }
    
    public String getCommitMessage() { return commitMessage; }
    public void setCommitMessage(String commitMessage) { 
        this.commitMessage = commitMessage; 
    }
    
    public String getGitAuthor() { return gitAuthor; }
    public void setGitAuthor(String gitAuthor) { this.gitAuthor = gitAuthor; }
    
    public String getGitEmail() { return gitEmail; }
    public void setGitEmail(String gitEmail) { this.gitEmail = gitEmail; }
    
    public int getVerbosity() { return verbosity; }
    public void setVerbosity(int verbosity) { this.verbosity = verbosity; }
    
    public String getExitMessage() { return exitMessage; }
    public void setExitMessage(String exitMessage) { this.exitMessage = exitMessage; }
    
    public int getExitCode() { return exitCode; }
    public void setExitCode(int exitCode) { this.exitCode = exitCode; }
    
    public boolean hasErrors() {
        return exitCode != 0 || (exitMessage != null && !exitMessage.isEmpty());
    }
    
    public AtomicReference<List<String>> getBulkDeployChannels() { 
        return bulkDeployChannels; 
    }
    
    public void setBulkDeployChannels(AtomicReference<List<String>> bulkDeployChannels) {
        this.bulkDeployChannels = bulkDeployChannels;
    }
    
    public String getApi() { return api; }
    public void setApi(String api) { this.api = api; }
    
    public Object getElLoc() { return elLoc; }
    public void setElLoc(Object elLoc) { this.elLoc = elLoc; }
}
