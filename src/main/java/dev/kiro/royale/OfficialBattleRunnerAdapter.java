package dev.kiro.royale;

import dev.robocode.tankroyale.runner.BattleHandle;
import dev.robocode.tankroyale.runner.BattleRunner;
import dev.robocode.tankroyale.runner.BattleSetup;
import dev.robocode.tankroyale.runner.BotEntry;
import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.ConcurrentHashMap;
import static dev.kiro.royale.Models.*;

/** Thin Stage 1 boundary around the official Battle Runner 1.0.2 API. */
public final class OfficialBattleRunnerAdapter implements AutoCloseable {
    private final RepositoryPaths paths;
    private final Duration botConnectTimeout;
    private final Duration wallClockTimeout;
    private final Duration cleanupGrace;
    private final AtomicReference<Session> activeSession = new AtomicReference<>();
    private final AtomicReference<String> readyEndpoint = new AtomicReference<>();

    public OfficialBattleRunnerAdapter(RepositoryPaths paths, Duration botConnectTimeout,
                                       Duration wallClockTimeout, Duration cleanupGrace) {
        this.paths = paths;
        this.botConnectTimeout = requireFinitePositive(botConnectTimeout, "bot connect timeout");
        this.wallClockTimeout = requireFinitePositive(wallClockTimeout, "battle wall-clock timeout");
        this.cleanupGrace = requireFinitePositive(cleanupGrace, "cleanup grace period");
    }

    public Optional<String> readyEndpoint() {
        return Optional.ofNullable(readyEndpoint.get());
    }

    public EngineExecution run(List<ValidatedBot> bots, int rounds, boolean record) throws Exception {
        if (bots.size() != 2) throw new IllegalArgumentException("Exactly two validated bots are required");
        var diagnostics = new BoundedDiagnostics(40);
        var session = new Session(bots, rounds, record, diagnostics);
        if (!activeSession.compareAndSet(null, session)) throw new IllegalStateException("A battle is already active");
        ExecutorService worker = Executors.newSingleThreadExecutor(r -> {
            Thread thread = new Thread(r, "official-battle-runner");
            thread.setDaemon(true);
            return thread;
        });
        Future<EngineExecution> future = worker.submit(session::execute);
        try {
            return future.get(wallClockTimeout.toMillis(), TimeUnit.MILLISECONDS);
        } catch (TimeoutException exception) {
            diagnostics.add("battle wall-clock timeout after " + wallClockTimeout.toSeconds() + "s");
            session.abort();
            future.cancel(true);
            throw new IllegalStateException("Official battle timed out", exception);
        } catch (ExecutionException exception) {
            Throwable cause = exception.getCause();
            if (cause instanceof Exception checked) throw checked;
            throw new IllegalStateException("Official battle failed", cause);
        } finally {
            session.abort();
            activeSession.compareAndSet(session, null);
            worker.shutdownNow();
            worker.awaitTermination(cleanupGrace.toMillis(), TimeUnit.MILLISECONDS);
        }
    }

    @Override
    public void close() {
        Session session = activeSession.getAndSet(null);
        if (session != null) session.abort();
    }

    private final class Session {
        private final List<ValidatedBot> bots;
        private final int rounds;
        private final boolean record;
        private final BoundedDiagnostics diagnostics;
        private final AtomicReference<BattleRunner> runnerRef = new AtomicReference<>();
        private final AtomicReference<BattleHandle> handleRef = new AtomicReference<>();
        private final AtomicReference<Process> serverProcessRef = new AtomicReference<>();
        private final AtomicBoolean cleaned = new AtomicBoolean();
        private final List<ProcessHandle> ownedProcesses = new java.util.concurrent.CopyOnWriteArrayList<>();
        private final ConcurrentHashMap<Long, String> ownedCommands = new ConcurrentHashMap<>();
        private volatile int port;

        private Session(List<ValidatedBot> bots, int rounds, boolean record, BoundedDiagnostics diagnostics) {
            this.bots = List.copyOf(bots);
            this.rounds = rounds;
            this.record = record;
            this.diagnostics = diagnostics;
        }

        private EngineExecution execute() throws Exception {
            Path recordingDirectory = record
                    ? paths.runtimePath("recordings", "direct-" + Instant.now().toEpochMilli())
                    : null;
            port = reserveLoopbackPort();
            String endpoint = "ws://127.0.0.1:" + port;
            Process serverProcess = startLoopbackOfficialServer(port);
            serverProcessRef.set(serverProcess);
            registerOwned(serverProcess.toHandle(), String.join(" ", loopbackServerCommand(port)));
            String listenerBinding = awaitLoopbackListener(port, serverProcess);
            readyEndpoint.set(endpoint);
            diagnostics.add("official socket-activated server endpoint=" + endpoint);
            diagnostics.add("listenerBinding=" + listenerBinding);
            diagnostics.add("botConnectTimeout=" + botConnectTimeout.toSeconds() + "s");
            diagnostics.add("wallClockTimeout=" + wallClockTimeout.toSeconds() + "s");

            BattleRunner runner = BattleRunner.create(builder -> {
                builder.externalServer(endpoint);
                builder.botConnectTimeout(botConnectTimeout);
                builder.suppressServerOutput();
                if (recordingDirectory != null) builder.enableRecording(recordingDirectory);
            });
            runnerRef.set(runner);
            AtomicBoolean completionEvent = new AtomicBoolean();
            var eventOwner = new Object();
            try {
                var setup = BattleSetup.oneVsOne(builder -> {
                    builder.setNumberOfRounds(rounds);
                    builder.setDefaultTurnsPerSecond(30);
                });
                var entries = bots.stream().map(bot -> BotEntry.of(bot.canonicalDirectory())).toList();
                BattleHandle handle = runner.startBattleAsync(setup, entries);
                handleRef.set(handle);
                diagnostics.add("official battle started");
                captureOwnedDescendants();
                handle.getOnGameEnded().on(eventOwner, event -> {
                    completionEvent.set(true);
                    diagnostics.add("official GameEnded event observed");
                });
                handle.getOnGameAborted().on(eventOwner, event -> diagnostics.add("official GameAborted event observed"));
                var official = handle.awaitResults();
                completionEvent.set(true); // awaitResults can only return the handle's official GameEnded payload.
                diagnostics.add("official completion returned rounds=" + official.getNumberOfRounds());
                captureOwnedDescendants();
                var engineResults = official.getResults().stream()
                        .map(result -> new EngineResult(result.getRank(), result.getName(), result.getVersion(),
                                result.getTotalScore(), result.getSurvival(), result.getBulletDamage(),
                                result.getRamDamage(), result.getFirstPlaces()))
                        .toList();
                var completion = new EngineCompletion(true, completionEvent.get(), official.getNumberOfRounds(),
                        engineResults, CompletionProvenance.OFFICIAL_BATTLE_RUNNER_COMPLETION);
                Optional<String> recordingPath = verifyRecording(recordingDirectory);
                cleanup();
                return new EngineExecution(completion, endpoint, recordingPath, processEvidence(),
                        ownedProcesses.stream().noneMatch(ProcessHandle::isAlive), diagnostics.snapshot());
            } finally {
                try {
                    var handle = handleRef.get();
                    if (handle != null) {
                        handle.getOnGameEnded().off(eventOwner);
                        handle.getOnGameAborted().off(eventOwner);
                    }
                } catch (RuntimeException ignored) {
                    diagnostics.add("event handler cleanup reported an error");
                }
                cleanup();
            }
        }

        private Optional<String> verifyRecording(Path recordingDirectory) throws IOException {
            if (!record) return Optional.empty();
            if (recordingDirectory == null || !recordingDirectory.normalize().startsWith(paths.runtimeRoot())) {
                throw new IOException("Recording target is not contained under runtime");
            }
            try (var files = Files.list(recordingDirectory)) {
                var recordings = files.filter(path -> path.getFileName().toString().endsWith(".battle.gz"))
                        .filter(Files::isRegularFile).filter(path -> {
                            try { return Files.size(path) > 0; } catch (IOException ignored) { return false; }
                        }).toList();
                if (recordings.size() != 1) throw new IOException("Expected exactly one non-empty official recording");
                Path recording = recordings.getFirst().toRealPath();
                if (!recording.startsWith(paths.runtimeRoot().toRealPath())) throw new IOException("Recording escaped runtime");
                diagnostics.add("verified recording bytes=" + Files.size(recording));
                return Optional.of(paths.display(recording));
            }
        }

        private void captureOwnedDescendants() {
            ProcessHandle.current().descendants().filter(ProcessHandle::isAlive).forEach(handle -> {
                String command = commandLine(handle);
                if (isBattleOwned(command) && ownedProcesses.stream().noneMatch(existing -> existing.pid() == handle.pid())) {
                    registerOwned(handle, command);
                    diagnostics.add("observed owned process pid=" + handle.pid() + " role=" + role(command));
                }
            });
        }

        private void registerOwned(ProcessHandle handle, String command) {
            if (ownedProcesses.stream().noneMatch(existing -> existing.pid() == handle.pid())) {
                ownedProcesses.add(handle);
            }
            ownedCommands.putIfAbsent(handle.pid(), command);
        }

        private void cleanup() {
            if (!cleaned.compareAndSet(false, true)) return;
            readyEndpoint.set(null);
            BattleHandle handle = handleRef.getAndSet(null);
            if (handle != null) {
                try { handle.close(); } catch (RuntimeException ignored) { diagnostics.add("battle handle close reported an error"); }
            }
            BattleRunner runner = runnerRef.getAndSet(null);
            if (runner != null) {
                try { runner.close(); } catch (RuntimeException ignored) { diagnostics.add("battle runner close reported an error"); }
            }
            Process serverProcess = serverProcessRef.getAndSet(null);
            if (serverProcess != null && serverProcess.isAlive()) serverProcess.destroy();
            waitForOwnedProcesses();
            diagnostics.add("owned process cleanup complete=" + ownedProcesses.stream().noneMatch(ProcessHandle::isAlive));
        }

        private void abort() { cleanup(); }

        private void waitForOwnedProcesses() {
            long deadline = System.nanoTime() + cleanupGrace.toNanos();
            while (System.nanoTime() < deadline && ownedProcesses.stream().anyMatch(ProcessHandle::isAlive)) {
                try { Thread.sleep(25); } catch (InterruptedException exception) { Thread.currentThread().interrupt(); break; }
            }
            ownedProcesses.stream().filter(ProcessHandle::isAlive).forEach(ProcessHandle::destroy);
            long forceDeadline = System.nanoTime() + cleanupGrace.toNanos();
            while (System.nanoTime() < forceDeadline && ownedProcesses.stream().anyMatch(ProcessHandle::isAlive)) {
                try { Thread.sleep(25); } catch (InterruptedException exception) { Thread.currentThread().interrupt(); break; }
            }
            ownedProcesses.stream().filter(ProcessHandle::isAlive).forEach(ProcessHandle::destroyForcibly);
        }

        private List<ProcessEvidence> processEvidence() {
            return ownedProcesses.stream().map(handle -> {
                String command = ownedCommands.getOrDefault(handle.pid(), commandLine(handle));
                return new ProcessEvidence(handle.pid(), role(command), bound(command, 240), handle.isAlive());
            }).toList();
        }

        private Process startLoopbackOfficialServer(int serverPort) throws IOException {
            Path serverJar = extractOfficialServerJar();
            List<String> command = loopbackServerCommand(serverPort);
            Process process = new ProcessBuilder(command)
                    .directory(paths.repositoryRoot().toFile())
                    .redirectOutput(ProcessBuilder.Redirect.DISCARD)
                    .redirectError(ProcessBuilder.Redirect.DISCARD)
                    .start();
            diagnostics.add("started official socket-activated server pid=" + process.pid());
            return process;
        }

        private Path extractOfficialServerJar() throws IOException {
            Path targetDirectory = paths.runtimePath("official-server", "1.0.2");
            Path target = targetDirectory.resolve("robocode-tankroyale-server.jar");
            try (var input = OfficialBattleRunnerAdapter.class.getResourceAsStream("/robocode-tankroyale-server.jar")) {
                if (input == null) throw new IOException("Pinned Battle Runner does not contain the official server");
                Files.copy(input, target, StandardCopyOption.REPLACE_EXISTING);
            }
            if (!Files.isRegularFile(target) || Files.size(target) == 0) {
                throw new IOException("Official server extraction failed");
            }
            return target;
        }

        private List<String> loopbackServerCommand(int serverPort) {
            Path socketActivator = Path.of("/usr/bin/systemd-socket-activate");
            if (!Files.isExecutable(socketActivator)) {
                throw new IllegalStateException("Loopback socket activation is unavailable on this host");
            }
            Path java = Path.of(System.getProperty("java.home"), "bin", "java");
            return List.of(socketActivator.toString(), "--listen=127.0.0.1:" + serverPort,
                    "--inetd", "--now", java.toString(), "-jar",
                    paths.runtimeRoot().resolve("official-server/1.0.2/robocode-tankroyale-server.jar").toString(),
                    "--port", "inherit");
        }

        private String awaitLoopbackListener(int serverPort, Process serverProcess) throws Exception {
            long deadline = System.nanoTime() + Duration.ofSeconds(10).toNanos();
            while (System.nanoTime() < deadline) {
                String binding = inspectListenerBinding(serverPort);
                if (binding.equals("loopback:" + serverPort)) return binding;
                if (binding.startsWith("wildcard:") || binding.startsWith("non-loopback:")) {
                    throw new IllegalStateException("Official server did not bind only to loopback");
                }
                if (!serverProcess.isAlive()) throw new IllegalStateException("Official loopback server exited during startup");
                Thread.sleep(25);
            }
            throw new IllegalStateException("Official loopback listener was not ready within 10 seconds");
        }
    }

    private static Duration requireFinitePositive(Duration duration, String label) {
        if (duration == null || duration.isZero() || duration.isNegative() || duration.compareTo(Duration.ofDays(1)) > 0) {
            throw new IllegalArgumentException(label + " must be finite and positive");
        }
        return duration;
    }

    private static int reserveLoopbackPort() throws IOException {
        try (var socket = new ServerSocket(0, 1, InetAddress.getLoopbackAddress())) {
            return socket.getLocalPort();
        }
    }

    private static String commandLine(ProcessHandle handle) {
        var info = handle.info();
        return info.commandLine().orElseGet(() -> info.command().orElse("unknown"));
    }

    private static boolean isBattleOwned(String command) {
        return command.contains("robocode-tankroyale-server") || command.contains("robocode-tankroyale-booter")
                || command.contains("dev.kiro.royale.bots.KiroBot") || command.contains("dev.kiro.royale.bots.SampleOpponent");
    }

    private static String role(String command) {
        if (command.contains("KiroBot")) return "kiro-bot";
        if (command.contains("SampleOpponent")) return "sample-opponent";
        if (command.contains("booter")) return "official-booter";
        if (command.contains("server")) return "official-server";
        return "owned-battle-process";
    }

    private static String bound(String value, int max) { return value.length() <= max ? value : value.substring(0, max) + "...[truncated]"; }

    static String inspectListenerBinding(int port) {
        String hexPort = String.format("%04X", port);
        for (Path table : List.of(Path.of("/proc/net/tcp"), Path.of("/proc/net/tcp6"))) {
            if (!Files.isReadable(table)) continue;
            try {
                for (String line : Files.readAllLines(table)) {
                    String[] columns = line.trim().split("\\s+");
                    if (columns.length < 4 || !"0A".equals(columns[3])) continue;
                    String[] local = columns[1].split(":");
                    if (local.length == 2 && hexPort.equalsIgnoreCase(local[1])) {
                        String address = local[0];
                        if (address.chars().allMatch(ch -> ch == '0')) return "wildcard:" + port;
                        if ("0100007F".equalsIgnoreCase(address) || "00000000000000000000000001000000".equalsIgnoreCase(address)) {
                            return "loopback:" + port;
                        }
                        return "non-loopback:" + port + "(" + address + ")";
                    }
                }
            } catch (IOException ignored) {
                // Fall through to unavailable.
            }
        }
        return "unavailable:" + port;
    }
}
