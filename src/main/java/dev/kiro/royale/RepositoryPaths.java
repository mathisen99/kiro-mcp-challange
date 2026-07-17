package dev.kiro.royale;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

/** Resolves canonical repository-owned source and runtime locations. */
public final class RepositoryPaths {
    private final Path repositoryRoot;
    private final Path botRoot;
    private final Path runtimeRoot;

    private RepositoryPaths(Path root) throws IOException {
        this.repositoryRoot = root.toRealPath();
        this.botRoot = requireContainedDirectory(repositoryRoot.resolve("bots"), repositoryRoot, "bots root");
        Path runtime = repositoryRoot.resolve("runtime").normalize();
        Files.createDirectories(runtime);
        this.runtimeRoot = requireContainedDirectory(runtime, repositoryRoot, "runtime root");
    }

    public static RepositoryPaths locate() throws IOException {
        var configured = System.getProperty("kiro.royale.repositoryRoot");
        Path candidate = configured == null ? Path.of("").toAbsolutePath() : Path.of(configured);
        candidate = candidate.toAbsolutePath().normalize();
        while (candidate != null && !Files.isRegularFile(candidate.resolve("settings.gradle"))) {
            candidate = candidate.getParent();
        }
        if (candidate == null) throw new IOException("Repository root containing settings.gradle was not found");
        return new RepositoryPaths(candidate);
    }

    static RepositoryPaths fromRoot(Path root) throws IOException {
        return new RepositoryPaths(root.toAbsolutePath().normalize());
    }

    public Path repositoryRoot() { return repositoryRoot; }
    public Path botRoot() { return botRoot; }
    public Path runtimeRoot() { return runtimeRoot; }

    public Path runtimePath(String first, String... more) throws IOException {
        Path relative = Path.of(first, more);
        if (relative.isAbsolute()) throw new IOException("Runtime path must be repository-relative");
        Path candidate = runtimeRoot.resolve(relative).normalize();
        if (!candidate.startsWith(runtimeRoot)) throw new IOException("Runtime path escaped the repository runtime directory");
        Path current = runtimeRoot;
        for (Path component : runtimeRoot.relativize(candidate)) {
            current = current.resolve(component);
            if (Files.isSymbolicLink(current)) throw new IOException("Runtime path contains a symbolic link");
            if (Files.notExists(current)) Files.createDirectory(current);
            Path canonicalComponent = current.toRealPath();
            if (!canonicalComponent.startsWith(runtimeRoot) || !Files.isDirectory(canonicalComponent)) {
                throw new IOException("Runtime path escaped the canonical runtime directory");
            }
        }
        return current.toRealPath();
    }

    public String display(Path path) {
        var normalized = path.toAbsolutePath().normalize();
        if (!normalized.startsWith(repositoryRoot)) throw new IllegalArgumentException("Path is outside repository");
        return repositoryRoot.relativize(normalized).toString().replace('\\', '/');
    }

    private static Path requireContainedDirectory(Path candidate, Path owner, String label) throws IOException {
        Path canonical = candidate.toRealPath();
        if (!Files.isDirectory(canonical) || !canonical.startsWith(owner)) {
            throw new IOException(label + " is not a contained directory");
        }
        return canonical;
    }
}
