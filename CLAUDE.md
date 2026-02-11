# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

mirthSync is a Java-based command-line tool for synchronizing Mirth Connect code between servers. It allows pushing/pulling channels, code templates, configuration maps, and global scripts from Mirth Connect instances via REST API.

**Note**: This is the Java version of mirthSync, translated from the original Clojure implementation.

## Build and Development Commands

### Core Build Commands
- `mvn clean compile` - Compile all Java source files
- `mvn test` - Run test suite  
- `mvn clean package` - Build standalone JAR file (uber-JAR)
- `mvn clean` - Clean build artifacts

### Running the Application
- `java -jar target/mirthsync-3.5.2-standalone.jar -h` - Show help
- `java -jar target/mirthsync-3.5.2-standalone.jar -s <server-url> -u <username> -p <password> pull -t <target-dir>` - Pull from server
- `java -jar target/mirthsync-3.5.2-standalone.jar -s <server-url> -u <username> -p <password> push -t <target-dir>` - Push to server
- `java -jar target/mirthsync-3.5.2-standalone.jar -s <server> -u <user> -p <pass> -t <dir> --delete-orphaned pull` - Pull with orphaned file cleanup
- `java -jar target/mirthsync-3.5.2-standalone.jar -s <server> -u <user> -p <pass> -t <dir> --delete-orphaned --interactive pull` - Pull with interactive orphan confirmation
- `java -jar target/mirthsync-3.5.2-standalone.jar -t <target-dir> git init` - Initialize git repository
- `java -jar target/mirthsync-3.5.2-standalone.jar -t <target-dir> git status` - Check git status
- `java -jar target/mirthsync-3.5.2-standalone.jar -t <target-dir> --commit-message "msg" git commit` - Commit changes
- `java -jar target/mirthsync-3.5.2-standalone.jar -s <server> -u <user> -p <pass> -t <dir> --auto-commit pull` - Auto-commit after pull
- `java -jar target/mirthsync-3.5.2-standalone.jar -s <server> -u <user> -p <pass> -t <dir> --git-init --auto-commit push` - Auto-commit with repo init

### Testing
- Tests are organized by module in `src/test/java/com/suprasync/mirthsync/`
- Git functionality tests are in `src/test/java/com/suprasync/mirthsync/git/GitOperationsTest.java`
- CLI tests are in `src/test/java/com/suprasync/mirthsync/cli/CliConfigTest.java`
- Orphan detection tests are in `src/test/java/com/suprasync/mirthsync/actions/DeleteOrphanedTest.java`
- `mvn test` runs all tests
- `mvn test -Dtest=GitOperationsTest` runs only git tests
- `mvn test -Dtest=CliConfigTest` runs only CLI tests
- `mvn test -Dtest=DeleteOrphanedTest` runs only orphan detection tests

### Release Process
- Version is managed in `pom.xml`
- Update version: Edit `<version>` tag in pom.xml
- Build release: `mvn clean package`
- The uber-JAR will be in `target/mirthsync-{version}-standalone.jar`

## Code Architecture

### Core Packages
- `com.suprasync.mirthsync.core` - Main entry point and application orchestration
  - `Main` - Application entry point with main() method
  - `AppConfig` - Configuration data holder
  - `DiskMode` - Enum for disk storage modes
- `com.suprasync.mirthsync.cli` - Command-line argument parsing (Picocli)
  - `CliConfig` - CLI configuration and argument parsing
- `com.suprasync.mirthsync.actions` - Core push/pull operations and orphaned file cleanup
  - `Actions` - Upload/download operations
- `com.suprasync.mirthsync.apis` - API definitions and server interactions
  - `ApiDefinitions` - API management and iteration
- `com.suprasync.mirthsync.http` - HTTP client wrapper for Mirth API calls
  - `HttpClientWrapper` - HTTP communication with authentication
- `com.suprasync.mirthsync.files` - File system operations and directory management
  - `FileOperations` - File utilities
- `com.suprasync.mirthsync.xml` - XML parsing and manipulation (JDOM2)
  - `XmlUtils` - XML processing utilities
- `com.suprasync.mirthsync.interfaces` - Interface definitions for API implementations
  - `ApiInterface` - Common interface for all API types
- `com.suprasync.mirthsync.git` - Git integration (JGit)
  - `GitOperations` - Git operations (init, status, commit)
- `com.suprasync.mirthsync.logging` - Logging utilities (SLF4J)
  - `Logger` - Logging wrapper

### Application Flow
1. CLI parsing in `CliConfig.parseArgs()`
2. Configuration creation in `AppConfig`
3. Authentication via `HttpClientWrapper.authenticate()`
4. API iteration through `ApiDefinitions.iterateApis()`
5. For pull operations: Pre-pull local file capture for orphan detection
6. Action execution (push/pull) via `Actions.upload()` or `Actions.download()`
7. For pull operations: Post-pull orphan detection and cleanup
8. File operations through `FileOperations`

### Key Concepts
- **Disk Modes**: Enum controlling granularity of file extraction (backup, groups, items, code)
- **API Interface**: All Mirth entity types implement `ApiInterface`
- **Streams API**: Used for filtering and transforming collections
- **XML Processing**: JDOM2 for DOM-based XML manipulation
- **Orphan Detection**: Automatic detection of local files that no longer exist on remote server during pull operations
- **Pre-pull State Capture**: Local files are captured before pull operations to enable accurate orphan detection

### Code Quality
- When implementing similar logic across multiple classes, extract shared utility methods to maintain DRY principles.
- Remove dead code promptly when refactoring to avoid confusion and maintenance overhead.
- Always verify that removed methods are not referenced elsewhere before deletion.
- Use private helper methods for internal implementation details that shouldn't be part of the public API.

### Git Integration
- **GPG Signing**: Commits created by mirthsync explicitly disable GPG signing (`.setSign(false)`) to avoid password prompts in automated contexts. This overrides user's `~/.gitconfig` settings where `commit.gpgsign = true` may be set.
- This ensures mirthsync can create commits non-interactively regardless of the user's git configuration.

### Feature Development Best Practices
- **Orphan Detection**: Always detect orphaned files during pull operations and warn users about them, even when automatic deletion is disabled.
- **User Experience**: Provide clear warnings with file lists and actionable instructions when potentially destructive operations are available but not enabled.
- **Safety First**: Implement confirmation prompts for destructive operations like file deletion, especially in interactive mode.
- **Path Handling**: Use canonical paths when comparing file locations to handle symbolic links and path normalization correctly.
- **Pre-operation State Capture**: For operations that need to compare before/after state, capture the initial state before making changes.

### Debugging and Troubleshooting Best Practices
- **Integration Tests**: When troubleshooting issues, create integration tests that exercise the full stack.
- **API Investigation**: Always check OpenAPI/Swagger specifications when API calls aren't working as expected.
- **Mirth Configuration Map Structure**: Configuration map entries require specific XML structure. Simple string values will not work.
- **CLI Flag Consistency**: Ensure CLI arguments are respected across all disk modes.
- **Complex Conditional Logic**: In boolean filtering logic, add comments to clarify intent and review conditions for logical redundancy.

### Dependencies
- Requires Java 17 or higher (LTS)
- Built with Maven
- Key libraries:
  - Picocli - CLI argument parsing
  - JDOM2 - XML processing
  - JGit - Git integration
  - SLF4J/Logback - Logging
  - Java 11+ HttpClient - HTTP communication

### Environment Variables
- `MIRTHSYNC_PASSWORD` - Alternative to --password command line option

### Maven Profiles and Plugins
- **maven-compiler-plugin**: Compiles Java 17 source code
- **maven-shade-plugin**: Creates uber-JAR with all dependencies
- **maven-surefire-plugin**: Runs JUnit 5 tests
- **exec-maven-plugin**: Extracts test data during build

## Translation Notes

This codebase was translated from Clojure to Java. Key translation patterns:

- **Clojure protocols** → Java interfaces (`ApiInterface`)
- **Clojure multimethods** → Java strategy pattern (interface + implementations)
- **Clojure atoms** → Java `AtomicReference`
- **Clojure maps as data** → Java records or POJOs (`AppConfig`)
- **Clojure sequences** → Java Streams API
- **Clojure zipper operations** → JDOM2 DOM traversal
- **Clojure `defn` functions** → Java static methods or instance methods
- **Clojure keywords** → Java enums (`DiskMode`)

## Current Status

**Implemented**:
- ✅ Maven build system with uber-JAR generation
- ✅ CLI parsing with Picocli
- ✅ Configuration management
- ✅ Logging infrastructure
- ✅ File operations
- ✅ XML processing utilities
- ✅ HTTP client with authentication
- ✅ Git operations (init, status, commit)
- ✅ Application orchestration in Main

**TODO** (Stub implementations need completion):
- ⚠️ Full upload/download logic in Actions
- ⚠️ All API type implementations (Channels, CodeTemplates, GlobalScripts, etc.)
- ⚠️ Complete orphan file detection and cleanup
- ⚠️ XML deconstruction for different disk modes
- ⚠️ Deploy operations for channels
- ⚠️ Comprehensive test suite

## Committing Changes
- Do not include AI tool attributions in commit messages
