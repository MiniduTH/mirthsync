package com.suprasync.mirthsync.git;

import com.suprasync.mirthsync.core.AppConfig;
import com.suprasync.mirthsync.logging.Logger;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.Status;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.lib.PersonIdent;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.storage.file.FileRepositoryBuilder;

import java.io.File;
import java.io.IOException;

/**
 * Git operations integration using JGit.
 * Provides methods for initializing repositories, committing changes, and checking status.
 */
public class GitOperations {
    
    /**
     * Initialize a git repository in the target directory if it doesn't exist.
     * 
     * @param config The application configuration
     * @throws IOException if initialization fails
     */
    public static void initRepository(AppConfig config) throws IOException {
        File targetDir = new File(config.getTarget());
        File gitDir = new File(targetDir, ".git");
        
        if (gitDir.exists()) {
            Logger.debug("Git repository already exists at " + targetDir);
            return;
        }
        
        try {
            Git.init().setDirectory(targetDir).call();
            Logger.info("Initialized git repository at " + targetDir);
        } catch (GitAPIException e) {
            throw new IOException("Failed to initialize git repository", e);
        }
    }
    
    /**
     * Get the git repository for the target directory.
     * 
     * @param targetDir The target directory
     * @return Git repository, or null if not a git repository
     * @throws IOException if repository cannot be opened
     */
    private static Repository getRepository(File targetDir) throws IOException {
        FileRepositoryBuilder builder = new FileRepositoryBuilder();
        builder.findGitDir(targetDir);
        
        if (builder.getGitDir() == null) {
            return null;
        }
        
        return builder.build();
    }
    
    /**
     * Check the status of the git repository.
     * 
     * @param config The application configuration
     * @return Status information as a string
     * @throws IOException if status check fails
     */
    public static String getStatus(AppConfig config) throws IOException {
        File targetDir = new File(config.getTarget());
        Repository repo = getRepository(targetDir);
        
        if (repo == null) {
            return "Not a git repository";
        }
        
        try (Git git = new Git(repo)) {
            Status status = git.status().call();
            
            StringBuilder sb = new StringBuilder();
            sb.append("On branch ").append(repo.getBranch()).append("\n\n");
            
            if (status.isClean()) {
                sb.append("Nothing to commit, working tree clean\n");
            } else {
                if (!status.getAdded().isEmpty()) {
                    sb.append("Changes to be committed:\n");
                    for (String file : status.getAdded()) {
                        sb.append("\tnew file:   ").append(file).append("\n");
                    }
                    sb.append("\n");
                }
                
                if (!status.getModified().isEmpty() || !status.getChanged().isEmpty()) {
                    sb.append("Changes not staged for commit:\n");
                    for (String file : status.getModified()) {
                        sb.append("\tmodified:   ").append(file).append("\n");
                    }
                    sb.append("\n");
                }
                
                if (!status.getUntracked().isEmpty()) {
                    sb.append("Untracked files:\n");
                    for (String file : status.getUntracked()) {
                        sb.append("\t").append(file).append("\n");
                    }
                }
            }
            
            return sb.toString();
            
        } catch (GitAPIException e) {
            throw new IOException("Failed to get git status", e);
        }
    }
    
    /**
     * Commit all changes in the repository.
     * 
     * @param config The application configuration
     * @throws IOException if commit fails
     */
    public static void commitAll(AppConfig config) throws IOException {
        File targetDir = new File(config.getTarget());
        Repository repo = getRepository(targetDir);
        
        if (repo == null) {
            throw new IOException("Not a git repository");
        }
        
        try (Git git = new Git(repo)) {
            // Add all files
            git.add().addFilepattern(".").call();
            
            // Create commit
            PersonIdent author = new PersonIdent(
                config.getGitAuthor() != null ? config.getGitAuthor() : "mirthsync",
                config.getGitEmail() != null ? config.getGitEmail() : "mirthsync@localhost"
            );
            
            git.commit()
                .setMessage(config.getCommitMessage())
                .setAuthor(author)
                .setCommitter(author)
                .setSign(false) // Disable GPG signing
                .call();
            
            Logger.info("Committed changes: " + config.getCommitMessage());
            
        } catch (GitAPIException e) {
            throw new IOException("Failed to commit changes", e);
        }
    }
    
    /**
     * Auto-commit after an operation if configured.
     * 
     * @param config The application configuration
     */
    public static void autoCommitAfterOperation(AppConfig config) {
        if (!config.isAutoCommit()) {
            return;
        }
        
        try {
            File targetDir = new File(config.getTarget());
            Repository repo = getRepository(targetDir);
            
            if (repo == null) {
                if (config.isGitInit()) {
                    initRepository(config);
                    commitAll(config);
                } else {
                    Logger.warn("Not a git repository and --git-init not specified");
                }
            } else {
                commitAll(config);
            }
        } catch (IOException e) {
            Logger.error("Failed to auto-commit", e);
        }
    }
    
    /**
     * Execute a git operation based on the subcommand.
     * 
     * @param config The application configuration
     * @param subcommand The git subcommand (init, status, commit, etc.)
     * @param args Additional arguments
     * @throws IOException if operation fails
     */
    public static void execute(AppConfig config, String subcommand, String... args) throws IOException {
        switch (subcommand.toLowerCase()) {
            case "init":
                initRepository(config);
                break;
            case "status":
                String status = getStatus(config);
                System.out.println(status);
                break;
            case "commit":
                commitAll(config);
                break;
            default:
                Logger.warn("Git subcommand not yet implemented in Java version: " + subcommand);
                throw new UnsupportedOperationException("Git subcommand not implemented: " + subcommand);
        }
    }
}
