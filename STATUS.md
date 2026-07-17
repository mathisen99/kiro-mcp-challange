# Implementation status

Last updated: 2026-07-17

## Current state

Stage 0, its human checkpoint, Stage 1, and Stage 2 are complete. Stage 2 now exposes exactly four strict stdio MCP tools over the same proven `BattleService` and official Battle Runner adapter. The finite official Java MCP client proof completed initialization, exact tool discovery, all three read-only calls, and one genuine synchronous one-round battle with a non-empty recording and owned-process cleanup.

Stage 2 is **COMPLETE**. The repository-root launcher passed the official-client proof, but `.kiro/settings/mcp.json` remains disabled as required. Stage 3 installed-Kiro connection, Kiro timeout compatibility, and visual proof are not verified.

## Stage tracker

- [x] Stage 0 — environment and dependency verification
- [x] Stage 1 — direct real Battle Runner battle
- [x] Stage 2 — custom MCP server
- [ ] Stage 3 — Kiro and viewer integration
- [ ] Stage 4 — focused hardening and smoke test
- [ ] Stage 5 — documentation, video, and submission

## Verified facts in this repository

- [x] Runtime OpenJDK is `21.0.11+10`; compiler is `javac 21.0.11`.
- [x] Gradle Wrapper `9.6.1` runs with Java 21.
- [x] MCP Java SDK `io.modelcontextprotocol.sdk:mcp:2.0.0` resolves.
- [x] Tank Royale `1.0.2`, Battle Runner `robocode-tankroyale-runner:1.0.2`, and Java Bot API `robocode-tankroyale-bot-api:1.0.2` resolve.
- [x] jqwik `1.9.3` resolves on the JUnit Platform configuration.
- [x] The trivial Stage 0 application compiles, packages, and runs its API-availability probe.
- [x] Wrapper scripts, properties, and JAR are present and not ignored by Git.
- [x] Generated Gradle/build output remains ignored.

## Stage 0 evidence gate — 2026-07-17

**Gate state: COMPLETE.** All automated Stage 0 checks succeeded, and commit `8555529` contains the verified Gradle Wrapper and Stage 0 implementation/evidence files.

### Exact commands and exit codes

| Command | Exit code |
|---|---:|
| `java -version` | `0` |
| `javac -version` | `0` |
| `curl -fsSL https://services.gradle.org/distributions/gradle-9.6.1-bin.zip -o /tmp/gradle-9.6.1-bin.zip && echo "9c0f7faeeb306cb14e4279a3e084ca6b596894089a0638e68a07c945a32c9e14  /tmp/gradle-9.6.1-bin.zip" \| sha256sum -c - && unzip -q -o /tmp/gradle-9.6.1-bin.zip -d /tmp && /tmp/gradle-9.6.1/bin/gradle wrapper --gradle-version 9.6.1 --distribution-type bin` | `0` |
| `./gradlew --version` | `0` |
| `./gradlew dependencies --configuration runtimeClasspath` | `0` |
| `./gradlew clean build` | `0` |
| `./gradlew stage0Probe` | `0` |
| `./gradlew dependencyInsight --dependency io.modelcontextprotocol.sdk:mcp --configuration runtimeClasspath` | `0` |
| `./gradlew dependencyInsight --dependency robocode-tankroyale-runner --configuration runtimeClasspath` | `0` |
| `./gradlew dependencyInsight --dependency robocode-tankroyale-bot-api --configuration runtimeClasspath` | `0` |
| `./gradlew dependencyInsight --dependency jqwik --configuration testRuntimeClasspath` | `0` |

Local `javap -public` inspection of the resolved MCP core, Battle Runner, and Java Bot API JARs also completed successfully for the signatures recorded in `DECISIONS.md`. One preliminary MCP inspection against the empty aggregate `mcp` JAR exited `1`; rerunning against the resolved `mcp-core` JAR exited `0`. This research-command correction did not affect the build or probe gate.

### Exact resolved values

| Value | Resolved result |
|---|---|
| Java runtime | `openjdk version "21.0.11" 2026-04-21`, build `21.0.11+10` |
| Java compiler | `javac 21.0.11` |
| Gradle | `9.6.1`, revision `309d128bd9fe8c0b71311878fc660b9cbaa07c51` |
| Gradle launcher/daemon JVM | `21.0.11`, `/usr/lib/jvm/java-21-openjdk` |
| OS reported by Gradle | `Linux 7.1.3-arch1-3 amd64` |
| MCP Java SDK | `io.modelcontextprotocol.sdk:mcp:2.0.0` |
| Tank Royale | `1.0.2` |
| Battle Runner | `dev.robocode.tankroyale:robocode-tankroyale-runner:1.0.2` |
| Java Bot API | `dev.robocode.tankroyale:robocode-tankroyale-bot-api:1.0.2` |
| jqwik | `net.jqwik:jqwik:1.9.3` |
| JUnit BOM | `org.junit:junit-bom:5.13.4` |

### Observed output

- `./gradlew dependencies --configuration runtimeClasspath`: root project `kiro-royale`; all three production coordinates resolved; `BUILD SUCCESSFUL`.
- `./gradlew clean build`: `BUILD SUCCESSFUL in 1s`; six tasks executed. There are intentionally no tests in Stage 0.
- `./gradlew stage0Probe`: loaded MCP stdio/server/tool types at implementation version `2.0.0`; loaded Battle Runner/setup/entry/results and Java Bot API types at implementation version `1.0.2`; loaded `net.jqwik.api.Property`; ended with `STAGE0_PROBE_OK: MCP, Battle Runner, Java Bot API, and jqwik APIs are available`; `BUILD SUCCESSFUL`.
- The probe emitted an SLF4J warning that no provider is installed and selected the NOP logger. This did not fail the probe; logging configuration remains later-stage work.
- Official release checks found MCP Java SDK `v2.0.0` and Tank Royale `v1.0.2` as current. jqwik `1.10.1` is current but was deliberately not selected because its official release adds an Anti-AI Usage Clause; `1.9.3` is the verified pre-1.10 pin.
- No service, Bot process, battle, network listener, or recording was started.

### Complete changed-file inventory

Stage 0 implementation/evidence files:

- `build.gradle` — Java 21 application build, exact dependencies, JUnit Platform, and `stage0Probe` task.
- `settings.gradle` — root project name.
- `gradlew` — Unix Gradle Wrapper launcher.
- `gradlew.bat` — Windows Gradle Wrapper launcher.
- `gradle/wrapper/gradle-wrapper.jar` — Wrapper bootstrap JAR; explicitly allowed by `.gitignore`.
- `gradle/wrapper/gradle-wrapper.properties` — pinned Gradle 9.6.1 distribution.
- `src/main/java/dev/kiro/royale/Stage0DependencyProbe.java` — class/API availability probe only.
- `DECISIONS.md` — verified versions, sources, API surface, and unresolved runtime facts.
- `STATUS.md` — this gate evidence.

Other working-tree entries observed and preserved, not created or edited as Stage 0 implementation by this execution subagent:

- `.kiro/specs/kiro-royale/tasks.md` — pre-existing task-state change (`[ ]` to `[-]` for Task 1).
- `.kiro/specs/kiro-royale/tasks.meta.json` — pre-existing untracked task metadata.

Ignored `.gradle/` and `build/` outputs are generated validation artifacts and are not project changes.

### Remaining failures/blockers

- No Stage 0 gate command failed.
- The Wrapper files and reviewed Stage 0 implementation/evidence files are committed in `8555529`.
- No system `gradle` executable was available; the verified Wrapper is the supported build path.
- `.kiro/specs/kiro-royale/.config` does not exist. The available requirements/design/tasks identify this as a feature spec, not a bugfix spec.
- SLF4J has no runtime provider in the Stage 0 probe. This is non-blocking now and must be resolved before MCP logging/protocol isolation is implemented.

### Unexercised claims — not verified

- MCP server construction, runtime tool registration, handshake, discovery, calls, structured response transmission, shutdown, and stdout-only JSON-RPC behavior: **not verified**.
- Repository-root MCP launcher and `.kiro/settings/mcp.json`: **not verified**.
- Embedded Tank Royale server startup, loopback binding, actual endpoint/port discovery, readiness, and external-server behavior: **not verified**.
- Exact complete Bot JSON configuration/launch format and both bundled Bot builds: **not verified**.
- Bot process launch, PID ownership, stream handling, finite timeouts, termination, and cleanup: **not verified**.
- Direct Battle Runner execution, official completion/abort events, two real results, ranking/order, score values, and one-round behavior: **not verified**.
- Recording creation, path, non-empty replay validation, and official-GUI playback: **not verified**.
- Active MCP SDK plus active Battle Runner operational compatibility in one JVM: **not verified**; only shared classpath compatibility is verified.
- Kiro connection/tool timeout, passive viewer compatibility, real smoke test, demo recording, publication, and tracked-file hygiene review: **not verified**.

## Stage 1 gate

**COMPLETE.** The first direct run exposed an embedded-server wildcard bind. The resolution evidence below proves the replacement official socket-activation/external-runner path binds only to `127.0.0.1` and completes the same genuine direct battle. Stage 2 is unblocked.

## Task 2 human verification checkpoint — 2026-07-17

**Checkpoint state: COMPLETE.** The technical Stage 0 evidence reproduced, and
commit `8555529` contains the required Gradle Wrapper and reviewed Stage 0 files.
Stage 1 is authorized.

### Reviewer commands and exit codes

| Command | Exit code | Observation |
|---|---:|---|
| `java -version` | `0` | OpenJDK `21.0.11+10` |
| `javac -version` | `0` | `javac 21.0.11` |
| `./gradlew --version` | `1` | Initial sandbox-only failure: the reviewer environment could not create the normal user-cache lock file. |
| `./gradlew dependencies --configuration runtimeClasspath` | `1` | Same sandbox-only user-cache lock failure. |
| `./gradlew clean build` | `1` | Same sandbox-only user-cache lock failure. |
| `./gradlew stage0Probe` | `1` | Same sandbox-only user-cache lock failure. |
| `./gradlew --version && ./gradlew dependencies --configuration runtimeClasspath && ./gradlew clean build && ./gradlew stage0Probe` | `0` | Re-run with permission to use the existing Gradle user cache; Gradle `9.6.1`, dependency resolution, clean build, and `STAGE0_PROBE_OK` all succeeded. |
| `git ls-files --error-unmatch gradlew gradlew.bat gradle/wrapper/gradle-wrapper.jar gradle/wrapper/gradle-wrapper.properties build.gradle settings.gradle src/main/java/dev/kiro/royale/Stage0DependencyProbe.java` | `1` | Git reported every listed Stage 0 implementation file as unknown/untracked. |
| `git show --stat --oneline --decorate 8555529` | `0` | The Stage 0 commit contains the reviewed build, Wrapper, probe, decisions, task state, and evidence files. |
| `git ls-tree -r --name-only 8555529` | `0` | The committed tree contains every required Stage 0 implementation file. |
| `./gradlew clean build stage0Probe` | `0` | Final post-commit verification: seven tasks executed, `BUILD SUCCESSFUL`, and `STAGE0_PROBE_OK`. |

### Human review observations

- Current official release pages still identify MCP Java SDK `2.0.0` and Tank
  Royale `1.0.2` as their latest releases; official Battle Runner documentation
  confirms the recorded runner coordinate and capabilities.
- `DECISIONS.md` distinguishes reflected/public API availability from runtime facts
  that remain `not verified`; no unsupported runtime success claim was found.
- Repository inspection found only `Stage0DependencyProbe.java`. It performs class
  loading and method-name reflection; it does not construct an MCP server, register
  a tool, create a Battle Runner, launch a process, or run a battle. No Stage 1 or
  Stage 2 adapter implementation began early.
- Commit `8555529` contains `gradlew`, `gradlew.bat`, the Wrapper properties/JAR,
  build files, dependency probe, decisions, and Stage 0 evidence. `git ls-tree`
  confirmed every required implementation file is tracked in that commit.
- Generated `.gradle/` and `build/` output and Kiro's local `tasks.meta.json`
  execution/session history remain ignored and uncommitted.

### Changed files from this review

- `STATUS.md` — added the human checkpoint evidence, initial blocked verdict, and verified resolution.
- `.kiro/specs/kiro-royale/tasks.md` — kept the gates incomplete while the commit was absent, then marked Tasks 1 and 2 complete after commit verification.
- `.gitignore` — excludes Kiro's local `tasks.meta.json` execution/session metadata.

The follow-up resolution changes mark the verified Stage 0 and Task 2 gates complete
after commit `8555529` was inspected.

### Resolution

The reviewed Stage 0 implementation/evidence files, including all Gradle Wrapper
files, were committed without generated output. The committed tree was inspected,
Task 2 passed, and Stage 1 is unblocked.

## Stage 1 evidence gate — 2026-07-17

**Initial gate state: INCOMPLETE; resolved below.** A genuine direct one-round match passed, but the first embedded listener was observed on a wildcard address. The later blocker-resolution run below replaces that path and supplies the required loopback evidence.

### Exact commands and exit codes

| Command | Exit code |
|---|---:|
| `./gradlew clean test` | `0` |
| `./gradlew clean directBattle` | `0` |

Both commands ran from the repository root. `directBattle` depends on the normal application `build` and `buildBundledBots`, invokes `dev.kiro.royale.DirectBattleDiagnostic`, and calls the official Battle Runner directly; no MCP SDK type or MCP execution path is involved.

### Build, validation, and direct-battle observations

- `./gradlew clean test`: both Java Bot sources compiled into `runtime/bots/classes`, the pinned Java Bot API was copied into `runtime/bots/lib`, application/test compilation succeeded, and all `6` focused Stage 1 JUnit tests passed. Registry validation found exactly `kiro-bot` and `sample-opponent` and rejected an unknown ID in the focused suite.
- Direct validation reported exactly two configured identities as `VALID`: `kiro-bot` / `Kiro Bot` / `1.0` / `editable-primary`, and `sample-opponent` / `Sample Opponent` / `1.0` / `bundled-deterministic`.
- The official runner logged one round and two bots, booted both bots, started the game, emitted the observed official completion event, and returned `numberOfRounds=1`.
- Actual reported endpoint: `ws://localhost:42253`.
- Actual Linux listener observation while the official battle was active: `listenerBinding=wildcard:42253`. This is the precise gate blocker; a localhost URL is not evidence of a loopback-only bind.
- Real owned Bot process observations: PID `1821249`, role `kiro-bot`; PID `1821258`, role `sample-opponent`. Both were observed alive during the battle. The final returned process snapshots showed both PIDs no longer alive after cleanup; their command metadata was unavailable after process exit and therefore printed as `unknown` rather than being invented.
- Official result 1: rank `1`, name `Sample Opponent`, version `1.0`, total score `60`, survival score `50`, bullet damage `0`, ram damage `0`, first places `1`, rounds played `1`.
- Official result 2: rank `2`, name `Kiro Bot`, version `1.0`, total score `0`, survival score `0`, bullet damage `0`, ram damage `0`, first places `0`, rounds played `1`.
- No repeatability claim is made for winner, rank, or scores.
- Recording enabled: exactly one regular non-empty official artifact was verified at `runtime/recordings/direct-1784299991739/game-2026-07-17-17-53-13.battle.gz`, size `35,232` bytes. The generated artifact is ignored and remains under canonical `runtime/`.
- Cleanup observation: official `BattleHandle.close()` and `BattleRunner.close()` completed; both observed Bot PIDs were dead; `CLEANUP_COMPLETE: true`; command ended with `DIRECT_BATTLE_OK` and exit `0`.
- Timeouts exercised as configuration: Bot connection `30s`, battle wall clock `120s`, cleanup grace `5s`. Normal completion exercised these configured finite values; timeout branches themselves were not triggered.

### Complete changed-file inventory

- `.gitignore` — excludes the local jqwik execution database created by the focused test run.
- `build.gradle` — two-Bot compilation/runtime preparation, normal build wiring, focused-test setup, and the dedicated `directBattle` task.
- `bots/kiro-bot/README.md`
- `bots/kiro-bot/kiro-bot.json`
- `bots/kiro-bot/kiro-bot.sh`
- `bots/kiro-bot/kiro-bot.cmd`
- `bots/kiro-bot/src/main/java/dev/kiro/royale/bots/KiroBot.java`
- `bots/sample-opponent/README.md`
- `bots/sample-opponent/sample-opponent.json`
- `bots/sample-opponent/sample-opponent.sh`
- `bots/sample-opponent/sample-opponent.cmd`
- `bots/sample-opponent/src/main/java/dev/kiro/royale/bots/SampleOpponent.java`
- `src/main/java/dev/kiro/royale/Models.java`
- `src/main/java/dev/kiro/royale/RepositoryPaths.java`
- `src/main/java/dev/kiro/royale/BoundedDiagnostics.java`
- `src/main/java/dev/kiro/royale/BotRegistry.java`
- `src/main/java/dev/kiro/royale/BattleCoordinator.java`
- `src/main/java/dev/kiro/royale/GenuineResultMapper.java`
- `src/main/java/dev/kiro/royale/OfficialBattleRunnerAdapter.java`
- `src/main/java/dev/kiro/royale/BattleService.java`
- `src/main/java/dev/kiro/royale/DirectBattleDiagnostic.java`
- `src/test/java/dev/kiro/royale/Stage1CoreTest.java`
- `DECISIONS.md`
- `STATUS.md`

Generated `runtime/bots/**`, `runtime/recordings/**`, `.gradle/**`, `build/**`, and `.jqwik-database` entries are ignored validation/runtime artifacts, not committed project files. The generated local `.jqwik-database` observed after testing was removed and added to `.gitignore`. The pre-existing task-state modification in `.kiro/specs/kiro-royale/tasks.md` was preserved and was not used to claim completion.

### Remaining failure/blocker

- The pinned official embedded server starts via `ServerWebSocketObserver(InetSocketAddress(port), ...)`, and the public runner builder has no bind-host/address option. Runtime `/proc/net/tcp*` inspection confirmed a wildcard listener for this match. Stage 1 cannot be marked complete without a verified official-server path that binds only to loopback; Stage 2 must not begin.
- No test/build/direct command failed. The failure is the execution-proven network boundary mismatch, not a fabricated or mocked battle result.

### Unexercised claims — not verified

- Loopback-only server binding: **not verified; contradicted by the wildcard listener observation**.
- Official GUI loading or playback of the generated recording (Replay Proof): **not verified**. Only creation, containment, regular-file status, and non-zero size were verified.
- Passive live viewer connection/observation: **not verified**.
- MCP server construction, schemas, stdio protocol integrity, handshake, discovery, client calls, and same-path battle invocation: **not verified; no MCP implementation exists**.
- Kiro MCP connection, every tool invocation, synchronous tool timeout compatibility, and Kiro-triggered battle: **not verified**.
- Server and booter PIDs as explicit returned process evidence: **not verified**; official lifecycle logs and closure ran, while application process evidence captured only the two Bot PIDs.
- Bot-connect timeout, wall-clock timeout, startup-failure, abort, recording-failure, forced-kill, and JVM-shutdown branches: **not verified** by this successful run.
- Windows Bot launch and non-Linux listener inspection: **not verified**.
- Repeated battle score/winner determinism: **not verified and not required**.
- Real Stage 4 integration smoke test, demo recording, publication, and clean tracked-file review: **not verified**.

## Stage 1 blocker resolution and final gate — 2026-07-17

**Final gate state: COMPLETE.** The direct production path now extracts the official `1.0.2` server JAR packaged in the pinned Battle Runner dependency into ignored `runtime/`, gives it an application-owned inherited socket bound by `/usr/bin/systemd-socket-activate` to `127.0.0.1`, and connects `BattleRunner` through its public `externalServer` API. This is still the official Tank Royale server and official Battle Runner; no fake engine, proxy, or MCP path is involved.

### Exact resolution commands and exit codes

| Command | Exit code | Observation |
|---|---:|---|
| `command -v systemd-socket-activate` | `0` | Resolved `/usr/bin/systemd-socket-activate`. |
| `systemd-socket-activate --version` | `0` | systemd `261`; the installed launcher supports `--listen`, `--inetd`, and `--now`. |
| `java -jar robocode-tankroyale-server.jar --help` against the server JAR extracted from the resolved runner | `0` | Official server `1.0.2` advertises `--port inherit` for socket activation. |
| Pinned official-source fetch for `ServerWebSocketObserver.kt`, `ServerCli.kt`, and `BattleRunner.kt` | `6` then `0` | The sandbox DNS attempt failed; the authorized retry succeeded and confirmed inherited `ServerSocketChannel`, `--port inherit`, and public `externalServer(String)` behavior. |
| `systemd-socket-activate --listen=127.0.0.1:45678 --now java -jar robocode-tankroyale-server.jar --port inherit` | `2` | Proved fd-3 mode alone is not accepted by Java `System.inheritedChannel()` on this host. |
| `systemd-socket-activate --listen=127.0.0.1:45678 --inetd --now java -jar robocode-tankroyale-server.jar --port inherit` | manually interrupted after inspection | The official server stayed active; `ss` and `/proc/net/tcp` showed only `127.0.0.1:45678`. The probe was then stopped. |
| `./gradlew clean test` | `1` then `0` | The sandbox attempt could not write the existing Gradle cache lock; the authorized rerun passed all six focused Stage 1 tests. |
| `./gradlew clean directBattle` | `0` | Fresh official one-round battle passed with loopback listener, two real Bots, genuine results, recording, and cleanup. |
| `ps -p 1835919,1836060,1836069 -o pid=,stat=,args=` | `1` | No listed official-server or Bot PID remained after the battle. |
| `stat -c '%n %s bytes' runtime/recordings/direct-1784300659081/game-2026-07-17-18-04-20.battle.gz` | `0` | Verified a regular `30,152`-byte recording under ignored `runtime/`. |

### Final direct-battle observations

- Actual endpoint: `ws://127.0.0.1:44943`.
- Actual listener evidence during execution: `listenerBinding=loopback:44943`; wildcard and non-loopback observations are treated as fatal startup failures by the adapter.
- The official socket-activated server was observed as owned PID `1835919`. Kiro Bot PID `1836060` and Sample Opponent PID `1836069` were observed as real child processes. All three were dead after cleanup, and the command reported `CLEANUP_COMPLETE: true`.
- The official `GameEnded` event was observed and `BattleResults.getNumberOfRounds()` returned `1`.
- Rank 1: Sample Opponent `1.0`; total `60`, survival `50`, bullet damage `0`, ram damage `0`, first places `1`, rounds played `1`.
- Rank 2: Kiro Bot `1.0`; total `0`, survival `0`, bullet damage `0`, ram damage `0`, first places `0`, rounds played `1`.
- These values are recorded as this run's genuine results; winner/score repeatability is not claimed.
- Recording: `runtime/recordings/direct-1784300659081/game-2026-07-17-18-04-20.battle.gz`, `30,152` bytes.
- `DIRECT_BATTLE_OK` and Gradle `BUILD SUCCESSFUL` completed in `18s`.

### Resolution files changed

- `src/main/java/dev/kiro/royale/OfficialBattleRunnerAdapter.java` — replaces unsafe embedded binding with the pinned official server's loopback inherited-socket mode, public external-runner mode, startup bind verification, and owned-server cleanup/evidence.
- `.kiro/specs/kiro-royale/tasks.md` — marks Stage 1 leaves 3.1–3.6 complete after execution evidence.
- `DECISIONS.md` — records the verified socket-activation decision and host prerequisite.
- `STATUS.md` — records the commands, results, changed files, resolved blocker, and remaining unverified claims.

### Remaining failures and unverified claims

- Remaining Stage 1 blocker: **none on the exercised Linux host**. Stage 2 may begin.
- The loopback solution requires executable `/usr/bin/systemd-socket-activate` with inetd descriptor passing. Windows, macOS, other launcher locations, and Linux hosts without this executable are **not verified** and must fail closed rather than fall back to wildcard binding.
- Official GUI replay playback and passive live-viewer observation: **not verified**.
- MCP construction, tool discovery/calls, stdout isolation, Kiro connection, and Kiro-triggered battle: **not verified; no MCP implementation exists**.
- Timeout, abort, startup-failure, recording-failure, forced-kill, and JVM-shutdown branches: **not verified** by the successful battle.
- Demo recording, publication, Stage 4 smoke test, and clean tracked-file review: **not verified**.

## Stage 2 evidence gate — 2026-07-17

**Gate state: COMPLETE.** The repository-root launcher was exercised by the official MCP Java client. The client discovered exactly four tools, invoked every read-only tool, and invoked the same production battle service for one genuine round. `.kiro/settings/mcp.json` remains unchanged with `disabled: true`; Stage 3 is unblocked but has not begun.

### Exact commands and exit codes

| Command | Exit code |
|---|---:|
| `chmod +x "scripts/kiro-royale-mcp.sh" && ./gradlew clean installDist test` | `0` |
| `./gradlew mcpProof` | `0` |

Both commands ran from the repository root. The first clean command built both bundled Bots, compiled the application and proof harness, installed the distribution used by the launcher, and passed all `9` focused tests. The second task used `io.modelcontextprotocol.client.transport.StdioClientTransport` to start `./scripts/kiro-royale-mcp.sh`; no mock server, fake battle engine, fixture result, or direct-mode shortcut was used.

### MCP discovery and contract observations

- Official-client initialization succeeded with server name `kiro-royale` and version `0.1.0-SNAPSHOT`.
- Tool discovery returned exactly `[get_arena_status, inspect_bot, list_bots, run_battle]` when sorted. There were no extra async, status, result, command, path, network, or repository tools.
- Every advertised input schema reported `additionalProperties=false`. The server also enabled SDK input validation and defensively validated handler arguments.
- `get_arena_status` returned ready, two Bots, `battleActive=false`, and `websocketUrl=null` before a managed battle server existed; it did not invent an endpoint.
- `list_bots` returned exactly `kiro-bot` and `sample-opponent`, both `VALID`, with repository-relative directories.
- `inspect_bot` returned `bots/kiro-bot/src/main/java/dev/kiro/royale/bots/KiroBot.java` as the repository-relative primary editable source.
- The proof's `run_battle` request supplied only the two stable Bot IDs. Omitted `rounds` and `record` therefore exercised the defaults of one round and recording enabled.
- All four successful responses carried nonblank text plus JSON-compatible structured content decoded by the official client.

### Genuine battle observations through MCP

- The synchronous MCP battle call took `18,281 ms`; the complete initialization/discovery/read/battle proof took `19,284 ms`.
- Production provenance was `OFFICIAL_BATTLE_RUNNER_COMPLETION`; exactly two results were returned in ascending rank order for the configured identities.
- Rank 1: Sample Opponent `1.0`; total `60`, survival `50`, bullet damage `0`, ram damage `0`, first places `1`, rounds played `1`.
- Rank 2: Kiro Bot `1.0`; total `0`, survival `0`, bullet damage `0`, ram damage `0`, first places `0`, rounds played `1`.
- These are this execution's genuine official values. Winner and score repeatability are not claimed.
- Recording: `runtime/recordings/direct-1784301344444/game-2026-07-17-18-15-45.battle.gz`, regular and non-empty at `34,640` bytes.
- The structured result reported `cleanupComplete=true`. The proof validated all three observed owned battle process entries had `aliveAfterCleanup=false`; it ended with `MCP_CLEANUP_OK: ownedProcesses=3`.
- The official client closed cleanly and Gradle returned exit `0`. MCP server PID termination was not separately inspected after client close.

### Protocol safety and diagnostics

- The official client parsed initialization, discovery, and all four tool responses, ending with `MCP_PROTOCOL_STDOUT_VALID`; ordinary stdout contamination would have broken this protocol exchange.
- Server diagnostics captured through the client's dedicated stderr handler included the SLF4J NOP warning, the protocol-safe startup notice, and official Battle Runner lifecycle logs (`Starting battle`, `Booting bots`, `Starting game`, `Battle started successfully`, and `Battle finished, cleaning up`).
- No server diagnostic, Bot output, stack trace, credential, or ordinary status line appeared in the parsed stdout protocol stream.
- The launcher accepts no caller arguments and executes only the fixed installed application mode. MCP schemas accept no path, executable command, environment override, host, URL, timeout, or remote repository input.

### Complete changed-file inventory

- `build.gradle` — installed-distribution main class, Bot build dependency, and finite `mcpProof` task.
- `scripts/kiro-royale-mcp.sh` — fixed repository-root launcher with no caller arguments.
- `src/main/java/dev/kiro/royale/KiroRoyaleApplication.java` — trusted mode selection, protocol-safe stdio server startup, and shutdown cleanup.
- `src/main/java/dev/kiro/royale/McpToolAdapter.java` — exactly four schemas, projections, synchronous battle delegation, structured/text results, and sanitized failures.
- `src/main/java/dev/kiro/royale/Stage2McpProof.java` — finite official Java client handshake/discovery/read/battle proof.
- `src/main/java/dev/kiro/royale/Models.java` — carries verified completion provenance through the shared success model.
- `src/main/java/dev/kiro/royale/BattleService.java` — exposes actual ready endpoint state and preserves official provenance.
- `src/main/java/dev/kiro/royale/BotRegistry.java` — inspection run metadata is repository-relative rather than an absolute internal path.
- `src/main/java/dev/kiro/royale/OfficialBattleRunnerAdapter.java` — publishes the actual endpoint only while its verified loopback listener is ready.
- `src/test/java/dev/kiro/royale/Stage2McpContractTest.java` — three focused strict-inventory/projection/rejection tests; total suite count is nine.
- `DECISIONS.md` — verified SDK, representation, launcher, protocol, duration, and battle decisions.
- `STATUS.md` — this exact Stage 2 evidence.

Generated `build/**`, `.gradle/**`, `runtime/bots/**`, `runtime/official-server/**`, and `runtime/recordings/**` entries are ignored build/runtime evidence, not project source changes. The pre-existing task-state modification in `.kiro/specs/kiro-royale/tasks.md` was preserved; this execution did not transition task status.

### Remaining failures/blockers

- No Stage 2 build, test, launcher, protocol, client, or genuine battle proof command failed.
- There is no Stage 2 blocker. Stage 3 may manually enable and exercise the already-proven launcher in Kiro.
- The SDK and server emit an SLF4J no-provider warning to stderr. This does not contaminate stdout or fail the client proof; adding a logging backend was not necessary for the demonstrated path.

### Unexercised claims — not verified

- Installed Kiro displays/connects to the server and invokes any tool: **not verified**. The workspace entry remains disabled.
- The measured `19,284 ms` proof fits the installed Kiro tool timeout: **not verified**; no Kiro timeout evidence exists, so async infrastructure remains excluded.
- Passive live viewer connection/observation and the actual URL presented before a viewer-connected battle: **not verified**.
- Official GUI loading/playback of this recording (Replay Proof): **not verified**. Recording creation, containment, and non-zero size only were verified.
- MCP server PID absence after client shutdown: **not separately verified**; official transport closure and Gradle completion succeeded.
- Concurrent second-request rejection during a live battle, timeout, abort, startup-failure, recording-failure, forced-kill, and JVM-shutdown branches: **not verified** by this successful proof.
- Stage 4 property suite/real smoke test, demo recording, publication, and clean tracked-file review: **not verified**.
