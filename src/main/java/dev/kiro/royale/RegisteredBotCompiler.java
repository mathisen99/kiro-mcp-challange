package dev.kiro.royale;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.HexFormat;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import javax.tools.Diagnostic;
import javax.tools.DiagnosticCollector;
import javax.tools.JavaCompiler;
import javax.tools.JavaFileObject;
import javax.tools.StandardJavaFileManager;
import javax.tools.ToolProvider;
import static dev.kiro.royale.Models.BotId;

/** JDK compiler adapter restricted to the two fixed registry sources and ignored runtime outputs. */
final class RegisteredBotCompiler implements BotCompiler {
    private static final int MAX_DIAGNOSTICS = 8;
    private static final int MAX_DIAGNOSTIC_CHARS = 300;

    private final RepositoryPaths paths;
    private final BotRegistry registry;

    RegisteredBotCompiler(RepositoryPaths paths, BotRegistry registry) {
        this.paths = paths;
        this.registry = registry;
    }

    @Override
    public Map<String, String> compile(List<BotId> botIds) throws BotCompilationException {
        JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
        if (compiler == null) {
            throw new BotCompilationException(List.of("A full JDK 21 compiler is required"));
        }

        var prepared = new ArrayList<PreparedCompilation>();
        var hashes = new LinkedHashMap<String, String>();
        try {
            Path apiJar = resolvePinnedBotApiJar();
            for (BotId id : botIds) {
                BotRegistry.CompilationTarget target = registry.compilationTarget(id);
                Path temporaryOutput = paths.runtimePath("bots", "compile",
                        id.value() + "-" + UUID.randomUUID());
                prepared.add(new PreparedCompilation(target, temporaryOutput));
                String sourceHash = sha256(target.source());
                compileOne(compiler, apiJar, target, temporaryOutput);
                if (!sourceHash.equals(sha256(target.source()))) {
                    throw new BotCompilationException(List.of(
                            id.value() + ": source changed during compilation; retry the battle"));
                }
                hashes.put(id.value(), sourceHash);
            }
            for (PreparedCompilation compilation : prepared) install(compilation);
            return Map.copyOf(hashes);
        } catch (BotCompilationException exception) {
            cleanup(prepared);
            throw exception;
        } catch (Exception exception) {
            cleanup(prepared);
            throw new BotCompilationException(List.of("Registered Bot compilation could not complete safely"));
        }
    }

    private void compileOne(JavaCompiler compiler, Path apiJar, BotRegistry.CompilationTarget target,
                            Path output) throws IOException, BotCompilationException {
        var diagnostics = new DiagnosticCollector<JavaFileObject>();
        try (StandardJavaFileManager files = compiler.getStandardFileManager(diagnostics, Locale.ROOT,
                StandardCharsets.UTF_8)) {
            Iterable<? extends JavaFileObject> units = files.getJavaFileObjects(target.source().toFile());
            List<String> options = List.of("--release", "21", "-classpath", apiJar.toString(),
                    "-d", output.toString());
            boolean succeeded = Boolean.TRUE.equals(compiler.getTask(null, files, diagnostics,
                    options, null, units).call());
            Path expectedClass = output.resolve(target.mainClass().replace('.', '/') + ".class");
            if (!succeeded || !Files.isRegularFile(expectedClass)) {
                throw new BotCompilationException(safeDiagnostics(target.id(), diagnostics));
            }
        }
    }

    private Path resolvePinnedBotApiJar() throws IOException {
        Path lib = paths.runtimeRoot().resolve("bots/lib");
        if (!Files.isDirectory(lib)) throw new IOException("Bot runtime library directory is missing");
        try (var entries = Files.list(lib)) {
            List<Path> matches = entries.filter(Files::isRegularFile)
                    .filter(path -> path.getFileName().toString().startsWith("robocode-tankroyale-bot-api-"))
                    .filter(path -> path.getFileName().toString().endsWith(".jar")).toList();
            if (matches.size() != 1) throw new IOException("Expected exactly one pinned Bot API library");
            Path canonical = matches.getFirst().toRealPath();
            if (!canonical.startsWith(paths.runtimeRoot())) throw new IOException("Bot API escaped runtime");
            return canonical;
        }
    }

    private void install(PreparedCompilation compilation) throws IOException {
        Path destination = compilation.target().outputDirectory();
        deleteTree(destination);
        Files.createDirectories(destination.getParent());
        try {
            Files.move(compilation.temporaryOutput(), destination, StandardCopyOption.ATOMIC_MOVE);
        } catch (java.nio.file.AtomicMoveNotSupportedException ignored) {
            Files.move(compilation.temporaryOutput(), destination);
        }
    }

    private List<String> safeDiagnostics(BotId id, DiagnosticCollector<JavaFileObject> diagnostics) {
        List<String> result = diagnostics.getDiagnostics().stream()
                .filter(diagnostic -> diagnostic.getKind() == Diagnostic.Kind.ERROR)
                .limit(MAX_DIAGNOSTICS)
                .map(diagnostic -> id.value() + ":" + diagnostic.getLineNumber() + ":"
                        + diagnostic.getColumnNumber() + " " + safeMessage(diagnostic.getMessage(Locale.ROOT)))
                .toList();
        return result.isEmpty() ? List.of(id.value() + ": compilation failed without a compiler diagnostic") : result;
    }

    private String safeMessage(String message) {
        String safe = message.replace(paths.repositoryRoot().toString(), "[repository]")
                .replaceAll("[\\r\\n\\t]+", " ").replaceAll("\\s+", " ").trim();
        return safe.length() <= MAX_DIAGNOSTIC_CHARS ? safe
                : safe.substring(0, MAX_DIAGNOSTIC_CHARS) + "...[truncated]";
    }

    private static String sha256(Path source) throws Exception {
        return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(Files.readAllBytes(source)));
    }

    private static void cleanup(List<PreparedCompilation> compilations) {
        for (PreparedCompilation compilation : compilations) {
            try { deleteTree(compilation.temporaryOutput()); } catch (IOException ignored) { }
        }
    }

    private static void deleteTree(Path root) throws IOException {
        if (Files.notExists(root)) return;
        if (Files.isSymbolicLink(root)) throw new IOException("Refusing to delete a symbolic-link output");
        try (var paths = Files.walk(root)) {
            for (Path path : paths.sorted(java.util.Comparator.reverseOrder()).toList()) Files.deleteIfExists(path);
        }
    }

    private record PreparedCompilation(BotRegistry.CompilationTarget target, Path temporaryOutput) {}
}
