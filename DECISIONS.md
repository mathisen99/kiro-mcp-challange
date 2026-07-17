# Decision log

Record decisions only after verification.

## ADR-001 — One JVM application

**Status:** accepted for the initial MVP; operational compatibility remains to be exercised.

**Decision:** keep the MCP server and Battle Runner orchestration in one Java application.

**Verified basis:** on 2026-07-17, Java 21 compiled and loaded the pinned MCP SDK, Battle Runner, Java Bot API, and jqwik classes on one Gradle runtime classpath. This verifies dependency/classpath compatibility only; simultaneous MCP and Battle Runner operation is **not verified**.

**Revisit when:** Stage 1 or Stage 2 produces a demonstrated incompatibility.

## ADR-002 — Synchronous battle first

**Status:** accepted for the first vertical slice.

**Decision:** implement synchronous `run_battle` first with 1–5 rounds.

**Verified basis:** the resolved Battle Runner exposes `runBattle(...)` and `startBattleAsync(...)`; no Kiro timeout has been exercised.

**Revisit when:** an actual Kiro or MCP timeout is observed.

## ADR-003 — Passive hosted viewer

**Status:** provisional until Stage 3 compatibility is exercised.

**Decision:** attempt to use the existing third-party passive web viewer rather than building or vendoring one. Use official-GUI replay playback if live compatibility cannot be demonstrated.

**Reason:** the viewer is presentation infrastructure, not the custom integration.

**Verification required:** connect the viewer to the actual loopback WebSocket URL before a Battle Runner match and observe the Kiro-triggered battle. Repository availability alone does not prove compatibility.

## ADR-004 — Stage 0 dependency baseline

**Status:** accepted and resolved on 2026-07-17.

**Decision:** use a Java 21 Gradle application with exact dependency versions and the version-controlled Wrapper files listed below.

| Component | Resolved/pinned value | Execution evidence |
|---|---:|---|
| Java runtime | OpenJDK `21.0.11+10` | `java -version` |
| Java compiler | `javac 21.0.11` | `javac -version` |
| Gradle Wrapper | `9.6.1` | `./gradlew --version` |
| MCP Java SDK | `io.modelcontextprotocol.sdk:mcp:2.0.0` | runtime dependency resolution and Stage 0 probe |
| Tank Royale release | `1.0.2` | upstream release/`VERSION`, resolved runner and Bot API artifacts |
| Battle Runner | `dev.robocode.tankroyale:robocode-tankroyale-runner:1.0.2` | runtime dependency resolution and Stage 0 probe |
| Java Bot API | `dev.robocode.tankroyale:robocode-tankroyale-bot-api:1.0.2` | runtime dependency resolution and Stage 0 probe |
| jqwik | `net.jqwik:jqwik:1.9.3` | test-runtime dependency resolution and Stage 0 probe |
| JUnit BOM | `org.junit:junit-bom:5.13.4` | test configuration resolution during `clean build` |

`jqwik` `1.10.1` was the current upstream release on the verification date, but it was not selected because the official release explicitly adds an Anti-AI Usage Clause beginning with 1.10. Version `1.9.3` is the latest verified pre-1.10 release, resolves on Java 21, advertises JUnit Platform 1.13.1, and provides Java 8-compatible Gradle variants.

**Rejected alternatives:** relying on a system Gradle installation (none was available), dynamic dependency versions, older MCP/Tank Royale releases, and jqwik 1.10.x.

## Stage 0 official sources

Verified on 2026-07-17:

- Gradle distribution: <https://services.gradle.org/distributions/gradle-9.6.1-bin.zip>
- MCP Java SDK release `v2.0.0`: <https://github.com/modelcontextprotocol/java-sdk/releases/tag/v2.0.0>
- MCP Java SDK server/stdio documentation: <https://java.sdk.modelcontextprotocol.io/latest/server/>
- MCP Java SDK `v2.0.0` README (Java 17+ baseline): <https://github.com/modelcontextprotocol/java-sdk/blob/v2.0.0/README.md>
- Tank Royale release `v1.0.2`: <https://github.com/robocode-dev/tank-royale/releases/tag/v1.0.2>
- Tank Royale release version file: <https://raw.githubusercontent.com/robocode-dev/tank-royale/master/VERSION>
- Battle Runner documentation and coordinates: <https://robocode.dev/api/battle-runner.html>
- jqwik `1.9.3` release: <https://github.com/jqwik-team/jqwik/releases/tag/1.9.3>
- jqwik `1.9.3` JUnit Platform setup: <https://jqwik.net/docs/1.9.3/user-guide.html>
- jqwik current release checked for selection rationale: <https://github.com/jqwik-team/jqwik/releases/tag/1.10.1>

External documentation facts above and below are paraphrased; resolved artifacts and local `javap` inspection are the authority for the pinned class surface.

## Stage 0 verified API surface

The `Stage0DependencyProbe` loads classes and checks public method names without constructing a server, starting a process, or running a battle. Local `javap -public` inspection additionally verified these signatures in the resolved JARs.

### MCP Java SDK 2.0.0

- `StdioServerTransportProvider(McpJsonMapper)` and the explicit input/output-stream constructor exist; `closeGracefully()` is available.
- `McpServer.sync(McpServerTransportProvider)` exists for a single-session synchronous server.
- `McpSyncServer.addTool(SyncToolSpecification)`, `listTools()`, `closeGracefully()`, and `close()` exist.
- `SyncToolSpecification.builder()` exists and its handler maps `(McpSyncServerExchange, CallToolRequest)` to `CallToolResult`.
- `CallToolResult` exposes `content`, `isError`, `structuredContent`, and `meta` and has builders.
- Official SDK documentation identifies `McpJsonDefaults.getMapper()` for stdio construction and documents input-schema validation.

**Not verified:** an MCP server was not instantiated, no tool was registered at runtime, no handshake/tool discovery occurred, no JSON-RPC bytes traversed stdio, and stdout isolation was not exercised.

### Battle Runner 1.0.2

- `BattleRunner.create(Consumer<Builder>)`, `runBattle(BattleSetup, List<BotEntry>)`, `startBattleAsync(...)`, and `close()` exist.
- The builder exposes `embeddedServer()`, `embeddedServer(int)`, `externalServer(String)`, `enableRecording(Path)`, output controls, and `botConnectTimeout(Duration)`.
- `BattleHandle` is `AutoCloseable`, exposes lifecycle events and `awaitResults()`, and has `stop()`.
- `BattleResults` exposes `int getNumberOfRounds()` and `List<BotResult> getResults()`.
- `BotResult` exposes `int` rank/score component accessors and `String` name/version accessors required by the design: rank, total score, survival, bullet damage, ram damage, and first places.
- `BotEntry.of(Path)` and `BotEntry.of(String)` exist.
- Official documentation states that a Bot directory contains `<directory-name>.json`, identity matching uses configuration `name` plus `version`, embedded mode can request a dynamic or fixed port, recording writes `.battle.gz`, and the runner manages server and Bot processes.

**Not verified:** no managed server was started; loopback binding, actual endpoint discovery, readiness, Bot connection, process ownership/cleanup, completion/abort semantics, numeric values, result ordering, and recording creation/path are not verified. The resolved public `BattleRunner` API has no supported public endpoint accessor; an internal `ServerManager.getServerUrl()` exists but must not be assumed as an adapter contract. Public APIs expose lifecycle closure, while PID access appears only in internal implementation classes; Stage 1 must prove safe ownership and cleanup instead of depending on internals.

### Java Bot API 1.0.2

- `Bot` extends `BaseBot`; `BaseBot.start()` exists.
- `BotInfo` exposes name/version metadata and `fromResourceFile`, `fromFile`, and `fromInputStream` loaders.
- The official `v1.0.2` release documents Java SDK 11+ for Java sample Bots and the Bot API coordinate pinned above.

**Not verified:** exact complete Bot JSON schema, executable launch fields, Java sample build/run command, and real Bot startup remain Stage 1 work.

### One-JVM compatibility conclusion

The Java 21 build and Stage 0 probe prove that all selected libraries resolve, compile, and load together in one JVM classpath. Operational coexistence of an active stdio MCP server, managed Tank Royale server, and Bot processes is **not verified** and remains gated by Stages 1–2.

## Stage 0 verification commands

All completed with exit code `0` on 2026-07-17:

```text
java -version
javac -version
./gradlew --version
./gradlew dependencies --configuration runtimeClasspath
./gradlew clean build
./gradlew stage0Probe
```

The dedicated probe ended with:

```text
STAGE0_PROBE_OK: MCP, Battle Runner, Java Bot API, and jqwik APIs are available
```

## Launcher decision

Final command used by `.kiro/settings/mcp.json`:

```text
not verified — Stage 2 must implement and prove the repository-root MCP launcher
```
