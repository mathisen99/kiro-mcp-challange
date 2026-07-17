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

Final Stage 2 repository-root launcher candidate:

```text
./scripts/kiro-royale-mcp.sh
```

The launcher accepts no arguments and executes the installed distribution in fixed `mcp-stdio` mode. It was verified from the repository root after `./gradlew installDist` by the official Java MCP client proof. Task 5.1 now configures `.kiro/settings/mcp.json` with this command, no arguments, `disabled: false`, and auto-approval only for `get_arena_status`, `list_bots`, and `inspect_bot`; `run_battle` still requires explicit approval. The [current official Kiro MCP configuration reference](https://kiro.dev/docs/mcp/configuration/) confirms this workspace file location and field shape. Installed-Kiro startup, calls, and timeout compatibility are **not verified** and remain Stage 3 human evidence.

## ADR-005 — Stage 1 direct Battle Runner integration

**Status:** accepted for the genuine direct battle path on 2026-07-17; the initial embedded-server bind blocker is resolved on the exercised Linux host.

**Execution-verified decisions:**

- The exact static registry is `kiro-bot` (`Kiro Bot` `1.0`) and `sample-opponent` (`Sample Opponent` `1.0`). Each reviewed directory contains `<directory-name>.json`, Java 21 strategy source, and fixed `.sh`/`.cmd` launch metadata. `./gradlew clean test` built both bots and all six focused Stage 1 tests passed.
- Generated Bot classes and the pinned Bot API runtime are placed only below `runtime/bots`; the registered source/configuration directories remain below canonical `bots/` paths.
- The direct production path uses `BattleRunner.create`, `externalServer("ws://127.0.0.1:<reserved-port>")`, `botConnectTimeout(Duration.ofSeconds(30))`, `startBattleAsync`, `BattleHandle.awaitResults`, and deterministic `BattleHandle`/`BattleRunner` closure. The application wall-clock timeout is `120` seconds and owned-process cleanup grace is `5` seconds. These values are immutable from direct battle input.
- The successful run observed the official `GameEnded` completion event and mapped `BattleResults.getNumberOfRounds()` plus `BotResult` accessors. Official component types are `int`: rank, total score, survival, bullet damage, ram damage, and first places; identity accessors are `String` name/version. Internal score fields use `long`, so mapping widens without truncation or calculation.
- `enableRecording(Path)` was given a unique contained directory. The final gate run created exactly one regular non-empty `.battle.gz`; it verified `30,152` bytes at `runtime/recordings/direct-1784300659081/game-2026-07-17-18-04-20.battle.gz`.
- The final gate run observed the owned official server PID `1835919`, Kiro Bot PID `1836060`, and Sample Opponent PID `1836069`. After official handle/runner closure and application-owned server termination, all three were no longer alive and cleanup reported complete. The application never enumerates or terminates unrelated processes.
- The embedded runner mode was rejected for this MVP because upstream `v1.0.2` constructs an address without a public bind-host option and execution showed `wildcard:42253`. The pinned official server also supports `--port inherit`; its source constructs `ServerWebSocketObserver` from `System.inheritedChannel()`. On the exercised Linux host, `/usr/bin/systemd-socket-activate --listen=127.0.0.1:<port> --inetd --now` supplies that channel. The runner then uses its public external-server mode. The final URL was `ws://127.0.0.1:44943`, runtime inspection reported `loopback:44943`, and the same official one-round match succeeded. This satisfies the Stage 1 loopback gate without a proxy or modified server.
- This loopback implementation deliberately fails closed when `/usr/bin/systemd-socket-activate` is unavailable. Other operating systems, alternate binary locations, and hosts without Java-compatible inetd descriptor passing are not verified; the adapter must not fall back to the wildcard embedded mode.
- The runner consumed managed server/booter output while `suppressServerOutput()` prevented unbounded forwarding. Bot stdout is discarded and bot stderr is consumed by the official booter. Application lifecycle diagnostics are retained in a fixed 40-line bounded tail.

**Exact successful commands:** `./gradlew clean test` (exit `0`) and `./gradlew clean directBattle` (exit `0`). The direct command did not route through MCP.

**Official implementation references used for this adapter:** [Battle Runner README v1.0.2](https://github.com/robocode-dev/tank-royale/blob/v1.0.2/runner/README.md), [BattleRunner v1.0.2](https://github.com/robocode-dev/tank-royale/blob/v1.0.2/runner/src/main/kotlin/dev/robocode/tankroyale/runner/BattleRunner.kt), [Java sample bots v1.0.2](https://github.com/robocode-dev/tank-royale/tree/v1.0.2/sample-bots/java), [ServerCli v1.0.2](https://github.com/robocode-dev/tank-royale/blob/v1.0.2/server/src/main/kotlin/dev/robocode/tankroyale/server/cli/ServerCli.kt), and [ServerWebSocketObserver v1.0.2](https://github.com/robocode-dev/tank-royale/blob/v1.0.2/server/src/main/kotlin/dev/robocode/tankroyale/server/connection/ServerWebSocketObserver.kt). External source descriptions are paraphrased.

**Not verified:** replay playback in the official GUI; passive viewer compatibility; MCP server/client behavior; Kiro integration or timeout; booter PID capture as explicit returned process evidence; non-Linux socket activation/listener inspection; Windows Bot launch; timeout/failure/shutdown execution branches; repeated-winner or repeated-score determinism.

## ADR-006 — Stage 2 stdio MCP server and official-client proof

**Status:** accepted and execution-verified on 2026-07-17 for the repository-root official MCP client path.

**Decision:** expose exactly `get_arena_status`, `list_bots`, `inspect_bot`, and synchronous `run_battle` through the official MCP Java SDK `2.0.0`, delegating battle execution to the Stage 1 `BattleService` and `OfficialBattleRunnerAdapter` without a second engine path.

**Execution-verified SDK APIs and representation:**

- Server: `StdioServerTransportProvider(McpJsonMapper, InputStream, OutputStream)`, `McpServer.sync(...)`, strict tool-name and input validation, `SyncToolSpecification`, and `CallToolResult` text plus `structuredContent`.
- Client proof: `ServerParameters`, `StdioClientTransport`, `McpClient.sync(...)`, `initialize()`, `listTools()`, and `callTool(...)`.
- Structured content crossed the real stdio JSON-RPC boundary and was decoded by the official client as JSON-compatible maps/lists. Every successful call also carried nonblank text content.
- Each input schema had `additionalProperties: false`. No-input tools accepted empty objects only; inspection accepted only `botId`; battle accepted only `botIds`, optional integer `rounds` in `1..5`, and optional Boolean `record`. The successful proof omitted both optional battle fields and observed the defaults of one round and recording enabled.
- Tool discovery returned exactly the four names. No async/status/result tools or other tools were added.

**Protocol and launcher decision:** `./scripts/kiro-royale-mcp.sh` is the repository-root launcher candidate. It accepts no arguments, emits launcher errors only to stderr, and executes the installed distribution with the fixed `mcp-stdio` mode. The server configures stdio transport with stdout as the protocol output; startup and runner diagnostics observed by the client were on stderr. Successful official-client parsing of initialization, discovery, and four calls established that no ordinary stdout contaminated those protocol frames.

**Genuine execution evidence:** `./gradlew mcpProof` completed with exit `0`. The synchronous battle call took `18,281 ms`; the complete proof took `19,284 ms`. It returned official provenance, exactly two ascending results, a `34,640`-byte recording at `runtime/recordings/direct-1784301344444/game-2026-07-17-18-15-45.battle.gz`, and cleanup evidence for three owned battle processes. These are observations from this run only, not deterministic score claims.

**Stage 2 close state:** installed Kiro startup, Kiro tool timeout compatibility, Kiro calls, passive viewer compatibility, official-GUI replay playback, and MCP server PID inspection after client shutdown were not verified at that gate. Installed-Kiro calls and replay playback were later verified in ADR-007; passive-viewer compatibility and MCP server PID inspection remain unverified.

## ADR-007 — Stage 3 installed Kiro and visual-proof path

**Status:** accepted and execution-verified on 2026-07-17.

**Decision:** retain the synchronous four-tool MCP design and use same-battle official-GUI Replay Proof as the Stage 3 visual fallback. Do not add asynchronous tools or a custom viewer.

**Installed-Kiro evidence:** the installed Kiro IDE showed `kiro-royale` connected with exactly the four expected tools. Kiro invoked `get_arena_status`, `list_bots`, `inspect_bot`, and `run_battle`. The final one-round battle completed within Kiro's actual timeout and returned provenance, actual loopback URL, recording, cleanup state, both ranks, and every required score component in Kiro-visible text. Exact duration was not exposed.

**Compatibility correction:** the first Kiro `run_battle` call succeeded but Kiro surfaced only text content, not the SDK structured-content map. Because the original text contained only a completion count, Kiro could not report genuine score fields. The adapter now includes the genuine result projection in both representations. A focused regression test passed before Kiro reconnected and repeated the call successfully.

**Visual-proof decision:** passive live-viewer proof remains unverified. The final Kiro-triggered battle produced `runtime/recordings/direct-1784305918236/game-2026-07-17-19-31-59.battle.gz`, verified as a regular `30,141`-byte valid gzip file. The official Tank Royale GUI `1.0.2` portable JAR was downloaded from the official `v1.0.2` release under ignored `runtime/` and matched published SHA-256 `f69df7c1a3a47befa6d11bf71f60faa7a1452b98ecf0a417c0c16ac0864e6ab2`. The human reviewer loaded and ran that exact replay and visibly observed both Bots moving. Their basic back-and-forth behavior is consistent with the intentionally simple bundled strategies.

**Not verified:** passive hosted viewer compatibility, exact Kiro call duration, competitive strategy quality, demo recording, and Stage 4/final-submission claims.

## ADR-008 — Stage 4 focused hardening and genuine smoke gate

**Status:** accepted and execution-verified on 2026-07-17 for the demonstrated Linux vertical slice.

**Decision:** retain the synchronous four-tool architecture and harden only its existing boundaries. `BattleService` now depends on an internal `BattleEngine` port, while `OfficialBattleRunnerAdapter` remains the sole production implementation. This permits controlled boundary/lifecycle tests without creating a second production battle path.

**Execution-verified safety decisions:**

- Repository, Bot, and runtime directories are resolved through canonical real paths. Registered Bot directories and required config/source/launch files must remain beneath the canonical `bots/` root. Runtime directory creation rejects traversal and symbolic-link components before creating generated descendants. A fixed supported-host symlink escape example passed.
- Bot-connect, wall-clock, and cleanup durations remain application-owned, positive, finite, and absent from all MCP schemas. The real smoke used the existing production values of `30s`, `120s`, and `5s`; controlled tests verified timeout classification and bounded cleanup behavior.
- Diagnostics now have both line and per-line character bounds and redact configured non-empty secret values before retention. MCP boundary failures expose only fixed allowlisted codes/messages and no raw exception text, result list, or recording claim.
- Production process launch remains `new ProcessBuilder(command)` with an application-owned argument list, no caller environment mutation, and the official service command fixed to `--listen=127.0.0.1:<port>`. MCP mode has no ordinary `System.out.print*` writer; the real official SDK transport test parsed protocol responses while deliberate startup diagnostics arrived through stderr.
- The exact production tool inventory remains four. The real stdio contract test verified strict schemas, dual text/structured success, read-only runtime behavior, and clean client/server closure. Controlled fixed local processes verified bounded idempotent cleanup after normal exit, non-zero exit, timeout-style termination, and shutdown-style repeated cleanup; these controlled cases are contract evidence, not genuine battle evidence.
- `./gradlew clean test` passed `20` tests: `18` focused example/regression tests and `2` MCP transport/lifecycle contract tests. No optional jqwik property task was implemented or executed.
- `./gradlew realSmoke` passed one dedicated integration test using production registry validation, the production official Battle Runner adapter, the official loopback server, and both real bundled Bot processes for exactly one round. It observed `OFFICIAL_BATTLE_RUNNER_COMPLETION`, ranks `1` and `2`, the configured names/versions, all required score fields, endpoint `ws://127.0.0.1:32943`, a `33,742`-byte recording, and complete cleanup of three owned processes.

**Not verified:** optional Properties 1–16 (Tasks 8.3–8.18), genuine production timeout/abort/startup/recording-failure/forced-kill branches, a live-battle JVM-shutdown race, passive live-viewer compatibility, cross-platform socket activation and controlled-process commands, deterministic winners/scores, and all Stage 5 publication/demo/hygiene claims.
