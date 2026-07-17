package dev.kiro.royale;

import dev.robocode.tankroyale.botapi.BotInfo;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import static dev.kiro.royale.Models.*;

/** Static catalog for exactly the two reviewed bundled bots. */
public final class BotRegistry {
    private record Registration(
            BotId id, String directory, String expectedName, String expectedVersion,
            String sourceFile, String mainClass, String sourceLabel) {}

    private final RepositoryPaths paths;
    private final Map<BotId, Registration> registrations;

    public BotRegistry(RepositoryPaths paths) {
        this.paths = paths;
        var configured = new LinkedHashMap<BotId, Registration>();
        register(configured, new Registration(new BotId("kiro-bot"), "kiro-bot", "Kiro Bot", "1.0",
                "src/main/java/dev/kiro/royale/bots/KiroBot.java", "dev.kiro.royale.bots.KiroBot", "editable-primary"));
        register(configured, new Registration(new BotId("sample-opponent"), "sample-opponent", "Sample Opponent", "1.0",
                "src/main/java/dev/kiro/royale/bots/SampleOpponent.java", "dev.kiro.royale.bots.SampleOpponent", "bundled-deterministic"));
        registrations = Map.copyOf(configured);
    }

    private static void register(Map<BotId, Registration> target, Registration registration) {
        if (target.put(registration.id(), registration) != null) throw new IllegalStateException("Duplicate bot registration");
    }

    public List<BotDescriptor> list() {
        return registrations.values().stream().map(this::inspectRegistration).map(BotInspection::descriptor).toList();
    }

    public BotInspection inspect(BotId id) {
        var registration = registrations.get(id);
        if (registration == null) throw new IllegalArgumentException("Unknown bot ID: " + id.value());
        return inspectRegistration(registration);
    }

    public ValidatedBot resolveValidated(BotId id) {
        var registration = registrations.get(id);
        if (registration == null) throw new IllegalArgumentException("Unknown bot ID: " + id.value());
        var inspection = inspectRegistration(registration);
        if (!inspection.validationIssues().isEmpty()) {
            throw new IllegalStateException(id.value() + " is invalid: " + String.join("; ", inspection.validationIssues()));
        }
        try {
            Path directory = paths.botRoot().resolve(registration.directory()).toRealPath();
            return new ValidatedBot(inspection.descriptor(), directory, inspection.runArguments());
        } catch (IOException exception) {
            throw new IllegalStateException(id.value() + " cannot be resolved", exception);
        }
    }

    private BotInspection inspectRegistration(Registration registration) {
        var issues = new java.util.ArrayList<String>();
        Path directory = paths.botRoot().resolve(registration.directory()).normalize();
        Path canonical = null;
        try {
            canonical = directory.toRealPath();
            if (!canonical.startsWith(paths.botRoot())) issues.add("canonical directory escapes bots root");
        } catch (IOException exception) {
            issues.add("bot directory is missing or unreadable");
        }

        Path config = directory.resolve(registration.directory() + ".json");
        if (!Files.isRegularFile(config)) {
            issues.add("required bot configuration is missing");
        } else {
            try {
                BotInfo info = BotInfo.fromFile(config.toString());
                if (!registration.expectedName().equals(info.getName())) issues.add("configured name does not match registry");
                if (!registration.expectedVersion().equals(info.getVersion())) issues.add("configured version does not match registry");
            } catch (RuntimeException exception) {
                issues.add("bot configuration is invalid");
            }
        }

        Path source = directory.resolve(registration.sourceFile()).normalize();
        if (!source.startsWith(directory) || !Files.isRegularFile(source)) issues.add("primary strategy source is missing");
        Path script = directory.resolve(registration.directory() + (isWindows() ? ".cmd" : ".sh"));
        if (!Files.isRegularFile(script)) issues.add("reviewed launch script is missing");

        Path classes = paths.runtimeRoot().resolve("bots/classes").resolve(registration.id().value());
        Path classFile = classes.resolve(registration.mainClass().replace('.', '/') + ".class");
        if (!Files.isRegularFile(classFile)) issues.add("compiled bot class is missing; run buildBundledBots");
        Path lib = paths.runtimeRoot().resolve("bots/lib");
        boolean apiJarPresent = false;
        if (Files.isDirectory(lib)) {
            try (var entries = Files.list(lib)) {
                apiJarPresent = entries.anyMatch(path -> path.getFileName().toString().startsWith("robocode-tankroyale-bot-api-") && path.getFileName().toString().endsWith(".jar"));
            } catch (IOException ignored) {
                // issue added below
            }
        }
        if (!apiJarPresent) issues.add("pinned Java Bot API runtime is missing; run buildBundledBots");

        var descriptor = new BotDescriptor(registration.id(), registration.expectedName(), registration.expectedVersion(),
                "Java 21", "bots/" + registration.directory(), issues.isEmpty() ? ValidationStatus.VALID : ValidationStatus.INVALID,
                registration.sourceLabel());
        String sourceDisplay = "bots/" + registration.directory() + "/" + registration.sourceFile();
        List<String> buildArguments = List.of("./gradlew", "buildBundledBots");
        List<String> runArguments = List.of("bots/" + registration.directory() + "/" + script.getFileName());
        return new BotInspection(descriptor, List.of(sourceDisplay), sourceDisplay, buildArguments, runArguments, List.copyOf(issues));
    }

    private static boolean isWindows() {
        return System.getProperty("os.name", "").toLowerCase(java.util.Locale.ROOT).contains("win");
    }
}
