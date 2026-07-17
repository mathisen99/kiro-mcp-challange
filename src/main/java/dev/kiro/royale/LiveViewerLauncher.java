package dev.kiro.royale;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.concurrent.TimeUnit;

/** Opens only the fixed trusted passive viewer and proves it connected to the fixed loopback endpoint. */
final class LiveViewerLauncher {
    static final String VIEWER_URL = "https://jandurovec.github.io/tank-royale-viewer/";
    static final int VIEWER_PORT = 7654;
    private static final Path OPENER = Path.of("/usr/bin/xdg-open");

    boolean launchAndAwaitConnection(Duration timeout, BoundedDiagnostics diagnostics) throws Exception {
        if (!Files.isExecutable(OPENER) || !desktopSessionAvailable()) {
            throw new IOException("Desktop URL opener is unavailable");
        }
        String sessionUrl = VIEWER_URL + "?kiroRoyaleSession=" + Instant.now().toEpochMilli();
        Process opener = new ProcessBuilder(List.of(OPENER.toString(), sessionUrl))
                .redirectOutput(ProcessBuilder.Redirect.DISCARD)
                .redirectError(ProcessBuilder.Redirect.DISCARD)
                .start();
        if (!opener.waitFor(5, TimeUnit.SECONDS)) {
            opener.destroyForcibly();
            throw new IOException("Desktop URL opener did not finish");
        }
        if (opener.exitValue() != 0) throw new IOException("Desktop URL opener failed");
        diagnostics.add("trusted passive viewer launch completed");

        long deadline = System.nanoTime() + timeout.toNanos();
        while (System.nanoTime() < deadline) {
            if (hasEstablishedLoopbackClient(VIEWER_PORT)) {
                diagnostics.add("passive viewer connection verified on loopback:" + VIEWER_PORT);
                return true;
            }
            Thread.sleep(50);
        }
        diagnostics.add("passive viewer connection was not mechanically observed before battle start");
        return false;
    }

    private static boolean desktopSessionAvailable() {
        return nonBlank(System.getenv("DISPLAY")) || nonBlank(System.getenv("WAYLAND_DISPLAY"));
    }

    private static boolean nonBlank(String value) { return value != null && !value.isBlank(); }

    static boolean hasEstablishedLoopbackClient(int port) {
        String hexPort = String.format("%04X", port);
        for (Path table : List.of(Path.of("/proc/net/tcp"), Path.of("/proc/net/tcp6"))) {
            if (!Files.isReadable(table)) continue;
            try {
                for (String line : Files.readAllLines(table)) {
                    String[] columns = line.trim().split("\\s+");
                    if (columns.length < 4 || !"01".equals(columns[3])) continue;
                    String[] local = columns[1].split(":");
                    if (local.length == 2 && hexPort.equalsIgnoreCase(local[1])
                            && isLoopbackAddress(local[0])) return true;
                }
            } catch (IOException ignored) {
                // Try the other kernel socket table; absence of evidence is handled by the timeout.
            }
        }
        return false;
    }

    private static boolean isLoopbackAddress(String address) {
        return "0100007F".equalsIgnoreCase(address)
                || "00000000000000000000000001000000".equalsIgnoreCase(address);
    }
}
