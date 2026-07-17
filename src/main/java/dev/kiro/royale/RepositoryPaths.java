package dev.kiro.royale;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

/** Resolves canonical repository-owned source and runtime locations. */
public final class RepositoryPaths {
    private final Path repositoryRoot;
    private final Path botRoot;
    private final Path runtimeRoot;

    private RepositoryPaths(Path repositoryRoot) throws IOException {
        this.repositoryRoot = repositoryRoot.toRealPath();
        this.botRoot = this.repositoryRoot.resolve("bots").toRealPath();
        this.runtimeRoot = this.repositoryRoot.resolve("runtime").toAbsolutePath().normalize();
        Files.createDirectories(runtimeRoot);
    }

    public static RepositoryPaths locate() throws IOException {
        var configured = System.getProperty("kiro.royale.repositoryRoot");
        Path candidate = configured == null ? Path.of("").toAbsolutePath() : Path.of(configured);
        candidate = candidate.normalize();
        while (candidate != null && !Files.isRegularFile(candidate.resolve("settings.gradle"))) {
            candidate = candidate.getParent();
        }
        if (candidate == null) throw new IOException("Repository root containing settings.gradle was not found");
        return new RepositoryPaths(candidate);
    }

    public Path repositoryRoot() { return repositoryRoot; }
    public Path botRoot() { return botRoot; }
    public Path runtimeRoot() { return runtimeRoot; }

    public Path runtimePath(String first, String... more) throws IOException {
        Path candidate = runtimeRoot.resolve(Path.of(first, more)).toAbsolutePath().normalize();
        if (!candidate.startsWith(runtimeRoot)) throw new IOException("Runtime path escaped the repository runtime directory");
        Files.createDirectories(candidate);
        return candidate;
    }

    public String display(Path path) {
        var normalized = path.toAbsolutePath().normalize();
        if (!normalized.startsWith(repositoryRoot)) throw new IllegalArgumentException("Path is outside repository");
        return repositoryRoot.relativize(normalized).toString().replace('\\', '/');
    }
}
