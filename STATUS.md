# Implementation status

Last updated: 2026-07-17

## Current state

Stage 0 through Stage 5 are **COMPLETE for the owner-requested repository scope**. Tasks 9, 10, 11, 11.1, and 11.2 are complete: an isolated fresh-checkout-like snapshot passed the release flow, tracked-file/secret/configuration review passed with documented fixture exceptions, the GitHub repository is publicly accessible, and current official challenge terms/deadline were rechecked. The owner explicitly deferred the demo video, qualifying social post, and final form submission until later. Those external publication claims remain **not verified**, and this repository-complete state must not be read as evidence that a challenge entry was submitted.

## Stage tracker

- [x] Stage 0 — environment and dependency verification
- [x] Stage 1 — direct real Battle Runner battle
- [x] Stage 2 — custom MCP server
- [x] Stage 3 — Kiro and viewer integration
- [x] Stage 4 — focused hardening and smoke test
- [x] Stage 5 — required repository preparation and evidence; external video/social/form explicitly deferred

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

## Task 5.1 Kiro launcher configuration — 2026-07-17

**Task state: COMPLETE; Stage 3 remains INCOMPLETE.** The workspace MCP entry now uses the repository-relative launcher already proven by the Stage 2 official-client run. The entry is enabled, passes no arguments, auto-approves only `get_arena_status`, `list_bots`, and `inspect_bot`, and leaves `run_battle` subject to explicit approval. Synchronous battle execution is unchanged and no async tools were added.

The current official Kiro IDE MCP configuration reference was rechecked on 2026-07-17. It identifies `.kiro/settings/mcp.json` as the workspace-level file and documents `command`, required `args`, `disabled`, and `autoApprove` with the semantics used here. Kiro runtime behavior was not inferred from this static documentation check.

### Exact commands and exit codes

| Command | Exit code |
|---|---:|
| `python3 -c 'import json, pathlib, sys; p=pathlib.Path(".kiro/settings/mcp.json"); d=json.loads(p.read_text()); s=d["mcpServers"]["kiro-royale"]; expected={"command":"./scripts/kiro-royale-mcp.sh","args":[],"disabled":False,"autoApprove":["get_arena_status","list_bots","inspect_bot"]}; print("actual="+json.dumps(s, sort_keys=True)); print("expected="+json.dumps(expected, sort_keys=True)); sys.exit(0 if s == expected else 1)'` | `0` |
| `test -x scripts/kiro-royale-mcp.sh` | `0` |

### Files changed

- `.kiro/settings/mcp.json` — enabled the fixed repository-relative launcher, removed the obsolete Gradle argument, and retained only read-only auto-approvals.
- `.kiro/specs/kiro-royale/tasks.md` — marked Task 5.1 complete; the pre-existing parent Task 5 state was preserved.
- `STATUS.md` — records this configuration checkpoint without claiming installed-Kiro behavior.
- `DECISIONS.md` — records the enabled configuration separately from unverified runtime behavior.

### Remaining failures and unverified claims

- Configuration blocker: **none**.
- Installed Kiro displaying or connecting to `kiro-royale`: **not verified**.
- Invocation of any tool from Kiro, including explicit approval and execution of `run_battle`: **not verified**.
- The synchronous battle duration fitting Kiro's actual timeout: **not verified**; async infrastructure remains excluded.
- Passive live-viewer observation and official-GUI replay playback: **not verified**.
- At this configuration-only checkpoint, Stage 3 could not yet be marked complete; the later evidence gate below records the required tool invocations and same-battle replay proof.

## Stage 3 Kiro and replay evidence gate — 2026-07-17

**Gate state: COMPLETE.** The installed Kiro IDE displayed `kiro-royale` as connected with exactly four tools, invoked every tool, completed a real synchronous battle, and visibly replayed that same battle in the official Tank Royale GUI. Replay Proof is the selected Visual Proof; passive live-viewer observation was not exercised.

### Exact commands and exit codes

| Command | Exit code | Observation |
|---|---:|---|
| `./gradlew installDist` | `0` | Refreshed the fixed installed distribution used by the Kiro launcher. |
| `kiro-cli mcp list; kiro-cli mcp status kiro-royale` | `1` | CLI-only session was not logged in; this was not treated as IDE failure. |
| `/home/mathisen/Mathisen/Scripts/Kiro/kiro-launcher.sh /home/mathisen/Mathisen/Filez/Dev/Mathisen/kiro-mcp-challange` | `0` | Forwarded the repository to the already-running installed Kiro IDE instance. |
| `/home/mathisen/Mathisen/Scripts/Kiro/Kiro/kiro /home/mathisen/Mathisen/Filez/Dev/Mathisen/kiro-mcp-challange --verbose` | `0` | Reported `Sent env to running instance`, confirming the existing IDE instance accepted the workspace request. |
| `curl -sSL https://api.github.com/repos/robocode-dev/tank-royale/releases/tags/v1.0.2` | `0` | Located the portable official GUI JAR and published `SHA256SUMS`. |
| `mkdir -p runtime/tools/tank-royale-gui-1.0.2 && curl -sSL https://github.com/robocode-dev/tank-royale/releases/download/v1.0.2/robocode-tankroyale-gui-1.0.2.jar -o runtime/tools/tank-royale-gui-1.0.2/robocode-tankroyale-gui-1.0.2.jar && curl -sSL https://github.com/robocode-dev/tank-royale/releases/download/v1.0.2/SHA256SUMS -o runtime/tools/tank-royale-gui-1.0.2/SHA256SUMS && cd runtime/tools/tank-royale-gui-1.0.2 && rg 'robocode-tankroyale-gui-1.0.2.jar' SHA256SUMS && sha256sum -c SHA256SUMS --ignore-missing` | `0` | Published checksum `f69df7c1a3a47befa6d11bf71f60faa7a1452b98ecf0a417c0c16ac0864e6ab2` passed. |
| `./gradlew test installDist` | `1` | First result-text regression test had the `BattleSuccess` fixture arguments in the wrong order; production compilation succeeded but test compilation failed. |
| `./gradlew test installDist` | `0` | Corrected fixture passed all `10` focused tests and refreshed the installed distribution. |
| `stat -c '%F %s bytes %n' runtime/recordings/direct-1784305918236/game-2026-07-17-19-31-59.battle.gz` | `0` | Same Kiro-triggered replay is a regular `30,141`-byte file. |
| `gzip -t runtime/recordings/direct-1784305918236/game-2026-07-17-19-31-59.battle.gz` | `0` | Recording gzip integrity passed. |
| `java -jar runtime/tools/tank-royale-gui-1.0.2/robocode-tankroyale-gui-1.0.2.jar` | `0` | Opened the checksum-verified official GUI for replay and closed it after the human observation. |

### Installed-Kiro observations

- Human observation: Kiro showed `kiro-royale` as **Connected** and displayed exactly `get_arena_status`, `list_bots`, `inspect_bot`, and `run_battle`.
- Kiro invoked the three auto-approved read-only tools. `get_arena_status` returned `Arena status: ready`; `list_bots` returned `Found 2 registered bundled bots`; `inspect_bot` for `kiro-bot` returned `Inspection for kiro-bot: VALID`.
- Kiro invoked `run_battle` for `kiro-bot` versus `sample-opponent`, one round, recording enabled. The call completed successfully in the installed Kiro session, so one-round synchronous timeout compatibility is verified for this execution. Exact call duration was not exposed and is **not verified**.
- The first Kiro battle call exposed only a short text summary even though structured content existed. Kiro could not surface the required score fields. `McpToolAdapter` was corrected to include genuine result fields in text content, a focused regression test was added, the distribution was refreshed, and Kiro reconnected before the final call.
- Final Kiro result: provenance `OFFICIAL_BATTLE_RUNNER_COMPLETION`, one round, actual URL `ws://127.0.0.1:44725`, recording `runtime/recordings/direct-1784305918236/game-2026-07-17-19-31-59.battle.gz`, and `cleanupComplete=true`.
- Rank 1: Sample Opponent `1.0`; total `60`, survival `50`, bullet damage `0`, ram damage `0`, first places `1`, rounds played `1`.
- Rank 2: Kiro Bot `1.0`; total `0`, survival `0`, bullet damage `0`, ram damage `0`, first places `0`, rounds played `1`.
- These values are genuine observations from this execution only; winner and score repeatability are not claimed.

### Replay Proof observation

- The human reviewer loaded the exact Kiro-triggered `30,141`-byte recording through **Battle → Replay from File** in the official Tank Royale GUI `1.0.2` and confirmed that it loaded and ran.
- The reviewer visibly observed both Bots moving in the arena. They moved mainly backward and forward near their spawn positions. This is consistent with the deliberately simple MVP strategies in `KiroBot` and `SampleOpponent`; it is not evidence of fabricated execution.
- Same-battle official-GUI playback therefore satisfies Replay Proof and Visual Proof for Stage 3.

### Complete Stage 3 changed-file inventory

- `.kiro/settings/mcp.json` — enabled the proven fixed launcher with read-only auto-approvals.
- `.kiro/specs/kiro-royale/tasks.md` — marks Tasks 5.1, 6, and 7.1 complete from verified evidence.
- `src/main/java/dev/kiro/royale/McpToolAdapter.java` — includes genuine ranks, required score fields, recording, URL, provenance, and cleanup state in Kiro-visible text as well as structured content.
- `src/test/java/dev/kiro/royale/Stage2McpContractTest.java` — regression coverage for Kiro-visible genuine battle fields.
- `DECISIONS.md` — records installed-Kiro compatibility and replay fallback selection.
- `STATUS.md` — this Stage 3 evidence gate.

The downloaded official GUI, checksum, Bots, recordings, and other generated artifacts remain below ignored `runtime/` and are not project source changes.

### Remaining failures and unverified claims

- Stage 3 blocker: **none**. Stage 4 is unblocked.
- Passive hosted live-viewer connection and observation: **not verified**; same-battle official-GUI Replay Proof was used instead.
- Exact Kiro `run_battle` duration: **not verified**; successful completion proves only that this one-round call fit the installed Kiro timeout.
- Competitive Bot quality: intentionally out of MVP scope; only genuine functional behavior is claimed.
- Concurrent overlap, timeout, abort, startup-failure, recording-failure, forced-kill, and JVM-shutdown branches: **not verified** by this successful path.
- Demo recording, Stage 4 hardening/real smoke gate, final tracked-file hygiene, and submission publication: **not verified**.

## Stage 4 hardening and test evidence gate — 2026-07-17

**Gate state: COMPLETE.** All mandatory focused example/contract tests and the dedicated genuine one-round integration smoke passed after the demonstrated Stage 3 path. Optional jqwik Tasks 8.3–8.18 were not requested, were not implemented, and remain individually `not verified`.

### Exact commands and exit codes

| Command | Exit code | Observation |
|---|---:|---|
| `./gradlew clean test` | `1` | Initial Stage 4 run compiled production/tests and executed all 20 tests; one focused assertion exposed that an unexpected engine `IllegalStateException` was classified as `BATTLE_ABORTED` instead of `INTERNAL_ERROR`. The real stdio and controlled-process contract tests already passed. |
| `./gradlew clean test` | `0` | Rerun after narrowing completion-validation handling: clean application/Bot build and all `20` focused example/contract tests passed. |
| `./gradlew realSmoke` | `0` | Dedicated production smoke ran one genuine official round with both real bundled Bots; `1` smoke test passed. |
| `git status --short` | `0` | Captured the complete Stage 4 source/evidence working-tree inventory; the pre-existing task-state file modification was preserved. |

The two successful Gradle commands were executed sequentially as the shell command `./gradlew clean test && ./gradlew realSmoke`; because the second command ran and the combined command exited `0`, both component exit codes were `0`.

### Focused suite counts and observations

- Property tests: `0` executed. Optional Tasks 8.3–8.18 were skipped by request and are not represented by example tests.
- Focused example/regression tests: `18` passed (`6` Stage 1 core regressions, `4` existing MCP handler regressions, and `8` new Stage 4 hardening examples).
- MCP transport/lifecycle contract tests: `2` passed.
- Total from `./gradlew clean test`: `20` tests, `0` failures, `0` errors, `0` skipped.
- The focused examples exercised canonical Bot symlink escape, runtime symlink escape without outside file creation, unknown Bot IDs, invalid Bot validation before engine invocation, rounds `0`, `1`, `5`, and `6`, duplicate IDs, non-integer rounds, unexpected properties, command/host-shaped fields, `record=false`, typed boundary failures, unexpected-exception sanitization, timeout classification, shutdown close, positive finite timeout construction, bounded/redacted diagnostics, endpoint-not-ready `null`, exact mode stdout architecture, argument-list process launch, no process-environment mutation, and loopback-only launch configuration.
- The official SDK stdio transport contract discovered exactly `[get_arena_status, inspect_bot, list_bots, run_battle]`, verified `additionalProperties=false` for every schema, received nonblank text plus structured success, parsed deliberate server startup diagnostics on stderr without protocol contamination, and observed no runtime tree side effects from read-only calls.
- Controlled fixed local process tests exercised normal exit, non-zero exit, timeout-style termination, and repeated shutdown-style cleanup. Every owned test process stopped within bounded cleanup and repeated cleanup was idempotent. These are contract tests only; they are not genuine battle evidence.
- The expected SLF4J no-provider warning remained on stderr during the transport test. It did not contaminate stdout or fail protocol parsing.

### Genuine real-smoke observations

- `RealBattleSmokeTest` count: `1` passed, `0` failures/errors/skips; test execution time `17.475s` (`17.484s` suite time).
- The smoke depended on the clean focused test gate, application JAR/classes, and `buildBundledBots`; production validation reported both fixed registered Bots valid before launch.
- It invoked the production `BattleService`, internal `BattleEngine` port's sole production implementation `OfficialBattleRunnerAdapter`, official Tank Royale server/runner `1.0.2`, and two real bundled Bot processes. No mock engine or fixture result was used.
- Official lifecycle output reported one round/two Bots, Bot boot, game start, successful battle start, and battle-finished cleanup. Completion provenance was `OFFICIAL_BATTLE_RUNNER_COMPLETION`.
- Actual verified endpoint: `ws://127.0.0.1:32943`.
- Rank 1: Sample Opponent `1.0`; total `60`, survival `50`, bullet damage `0`, ram damage `0`, first places `1`, rounds played `1`.
- Rank 2: Kiro Bot `1.0`; total `0`, survival `0`, bullet damage `0`, ram damage `0`, first places `0`, rounds played `1`.
- These values are this smoke execution's genuine official results only; score/winner repeatability is not claimed.
- Recording: `runtime/recordings/direct-1784307249797/game-2026-07-17-19-54-10.battle.gz`, regular, contained, and non-empty at `33,742` bytes.
- Cleanup: `3` owned processes were observed; every returned `aliveAfterCleanup` value was false and `cleanupComplete=true`. No surviving owned process was reported.

### Complete changed-file inventory

Stage 4 implementation and evidence files:

- `build.gradle` — makes the focused suite build the installed distribution, excludes the tagged real smoke from normal tests, and adds the dedicated ordered `realSmoke` task.
- `src/main/java/dev/kiro/royale/BattleEngine.java` — internal application boundary used by production and controlled tests.
- `src/main/java/dev/kiro/royale/BattleEngineException.java` — fixed typed safe engine failure categories.
- `src/main/java/dev/kiro/royale/OwnedProcessCleanup.java` — bounded idempotent cleanup for registered owned handles.
- `src/main/java/dev/kiro/royale/RepositoryPaths.java` — canonical repository/Bot/runtime roots and symlink-safe runtime directory creation.
- `src/main/java/dev/kiro/royale/BotRegistry.java` — canonical containment of registered directories and required config/source/launch files with generic internal failures.
- `src/main/java/dev/kiro/royale/BoundedDiagnostics.java` — line/character bounds and configured-secret redaction.
- `src/main/java/dev/kiro/royale/BattleService.java` — injectable internal engine port, typed timeout/engine/internal failure handling, and truthful `record=false` consistency.
- `src/main/java/dev/kiro/royale/OfficialBattleRunnerAdapter.java` — production engine implementation, corrected timeout propagation, typed startup/recording failures, bounded cleanup utility, and configured diagnostic redaction.
- `src/test/java/dev/kiro/royale/Stage4FocusedUnitTest.java` — eight mandatory focused hardening examples.
- `src/test/java/dev/kiro/royale/McpTransportLifecycleContractTest.java` — two real-stdio/controlled-lifecycle contract tests.
- `src/test/java/dev/kiro/royale/RealBattleSmokeTest.java` — tagged genuine one-round production integration smoke.
- `DECISIONS.md` — ADR-008 records only execution-verified Stage 4 safety, transport, lifecycle, and smoke facts.
- `STATUS.md` — this evidence gate.

Pre-existing working-tree state preserved but not edited by this Stage 4 execution:

- `.kiro/specs/kiro-royale/tasks.md` — orchestrator-owned task-state transition (`[-]` parent state); this execution subagent did not update task status.

Generated `.gradle/**`, `build/**`, `runtime/bots/**`, `runtime/official-server/**`, `runtime/recordings/**`, and `.jqwik-database` entries remain ignored validation/runtime artifacts and are not source changes.

### Remaining failures and unverified claims

- Mandatory Stage 4 blocker: **none**. Stage 5 is unblocked.
- Initial focused-suite failure: **resolved** by ensuring only result-mapper validation maps to `BATTLE_ABORTED`; unexpected engine runtime exceptions now map to sanitized `INTERNAL_ERROR`.
- Task 8.3 / Property 1, omitted battle defaults: **not verified** by jqwik.
- Task 8.4 / Property 2, strict launchable request domain: **not verified** by jqwik.
- Task 8.5 / Property 3, canonical registered Bot containment: **not verified** by jqwik; fixed examples passed only.
- Task 8.6 / Property 4, ordered two-result mapping: **not verified** by jqwik; fixed regressions and real smoke passed only.
- Task 8.7 / Property 5, lossless genuine result mapping: **not verified** by jqwik; fixed regressions and real smoke passed only.
- Task 8.8 / Property 6, rejection of non-genuine completion: **not verified** by jqwik.
- Task 8.9 / Property 7, contained artifacts/truthful recordings: **not verified** by jqwik; fixed examples and real smoke passed only.
- Task 8.10 / Property 8, exclusive active ownership: **not verified** by jqwik; atomic fixed regression passed only.
- Task 8.11 / Property 9, complete truthful arena status: **not verified** by jqwik; endpoint-null fixed example passed only.
- Task 8.12 / Property 10, safe lossless Bot listing: **not verified** by jqwik.
- Task 8.13 / Property 11, safe lossless Bot inspection: **not verified** by jqwik.
- Task 8.14 / Property 12, dual success representations: **not verified** by jqwik; real transport examples passed only.
- Task 8.15 / Property 13, immutable finite positive timeouts: **not verified** by jqwik; fixed constructor examples passed only.
- Task 8.16 / Property 14, bounded captured output: **not verified** by jqwik; fixed bounded diagnostics examples passed only.
- Task 8.17 / Property 15, configured-secret redaction: **not verified** by jqwik; fixed overlapping-secret example passed only.
- Task 8.18 / Property 16, sanitized result-free failures: **not verified** by jqwik; fixed typed/unexpected examples passed only.
- Genuine production timeout, abort, Bot startup failure, server startup failure, recording failure, forced-kill fallback, and live-battle JVM-shutdown race: **not verified**. Controlled boundary/process tests do not establish those official-runner branches.
- Passive hosted live-viewer proof remains **not verified**; Stage 3 used same-battle official-GUI Replay Proof.
- Cross-platform socket activation, Windows/macOS Bot launch, and `/usr/bin/true`, `/usr/bin/false`, `/usr/bin/sleep` availability outside the exercised Linux host: **not verified**.
- Demo recording, public repository/video publication, fresh-checkout-like reproduction, and final tracked-file hygiene: **not verified** Stage 5 work.

## Stage 5 submission preparation (Task 9) — 2026-07-17

**Task 9 and subtasks 9.1–9.3 state: COMPLETE; Stage 5 gate remains INCOMPLETE.** The demonstrated path now has public-facing setup/tool/replay/security documentation, repository-relative release configuration, a finite transparent local preflight, an MIT project license, third-party attribution, and a claim matrix for human-supplied links. This task does not complete the subsequent human checkpoint or publication evidence Tasks 11.1–11.2.

### Exact command and exit codes

The following command ran from the repository root:

| Command | Exit code | Observation |
|---|---:|---|
| `chmod +x scripts/verify-release.sh && ./scripts/verify-release.sh` | `0` | Made the tracked release preflight executable and ran all finite local checks below. |

The script printed and checked every component command separately:

| Component command/check | Exit code | Observation |
|---|---:|---|
| tracked/ignored/configuration hygiene preflight | `0` | `RELEASE_HYGIENE_OK`; required Wrapper/build/source/test/MCP files were tracked, generated/sensitive examples were ignored, no forbidden tracked generated artifact was found, and active executable/configuration files contained no developer-home absolute path. This is automated preflight evidence, not the required final human clean tracked-file review. |
| `./gradlew clean build` | `0` | Clean application and both Bots built; the build's normal test dependency completed 20 tests; `BUILD SUCCESSFUL`. |
| `./gradlew clean test` | `0` | All 20 focused example/transport/lifecycle tests passed; `BUILD SUCCESSFUL`. |
| `./gradlew clean directBattle` | `0` | Production validation reported both fixed Bots `VALID`; a genuine official one-round direct battle completed on loopback with recording and complete cleanup. |
| `./gradlew mcpProof` | `0` | Official Java MCP client initialized, discovered exactly four tools, invoked all three read-only tools, ran one genuine recorded round, parsed protocol-only stdout, and observed complete cleanup. |
| `./gradlew realSmoke` | `0` | The dedicated genuine one-round production smoke test passed with official completion provenance, two ordered results, recording, loopback endpoint, and cleanup of three owned processes. |

The script ended with `RELEASE_VERIFICATION_OK: local automated path passed` and an explicit `NOT VERIFIED` list. It did not automate Kiro, any viewer/GUI, publication, challenge submission, or video work.

### Current direct/MCP/smoke observations

- Direct battle endpoint: `ws://127.0.0.1:40783`; listener reported loopback-only.
- Direct production validation: `kiro-bot` / Kiro Bot `1.0` / `VALID`; `sample-opponent` / Sample Opponent `1.0` / `VALID`.
- Direct result 1: Sample Opponent `1.0`; total `60`, survival `50`, bullet damage `0`, ram damage `0`, first places `1`, rounds played `1`.
- Direct result 2: Kiro Bot `1.0`; total `0`, survival `0`, bullet damage `0`, ram damage `0`, first places `0`, rounds played `1`.
- Direct recording: `runtime/recordings/direct-1784308168015/game-2026-07-17-20-09-28.battle.gz`, `35,291` bytes; all three owned processes were dead after cleanup.
- MCP discovery: `[get_arena_status, inspect_bot, list_bots, run_battle]`; status returned no invented endpoint before a battle and inspection returned the repository-relative Kiro Bot source.
- MCP battle duration: `19,694 ms`; complete proof duration: `20,697 ms`; provenance `OFFICIAL_BATTLE_RUNNER_COMPLETION`.
- MCP result 1: Kiro Bot `1.0`; total `62`, survival `50`, bullet damage `0`, ram damage `2`, first places `1`, rounds played `1`.
- MCP result 2: Sample Opponent `1.0`; total `11`, survival `0`, bullet damage `7`, ram damage `4`, first places `0`, rounds played `1`.
- MCP recording: `runtime/recordings/direct-1784308183793/game-2026-07-17-20-09-44.battle.gz`, `37,872` bytes; protocol parsing and cleanup of three owned processes succeeded.
- Real smoke endpoint: `ws://127.0.0.1:40099`; provenance `OFFICIAL_BATTLE_RUNNER_COMPLETION`.
- Smoke result 1: Sample Opponent `1.0`; total `60`, survival `50`, bullet damage `0`, ram damage `0`, first places `1`, rounds played `1`.
- Smoke result 2: Kiro Bot `1.0`; total `0`, survival `0`, bullet damage `0`, ram damage `0`, first places `0`, rounds played `1`.
- Smoke recording: `runtime/recordings/direct-1784308204683/game-2026-07-17-20-10-05.battle.gz`, `32,139` bytes; cleanup complete for three owned processes.
- The direct, MCP, and smoke results differed. This is expected and reinforces that no winner/score repeatability claim is made.

### Release configuration and documentation observations

- `.kiro/settings/mcp.json` was inspected and remains enabled with repository-relative `./scripts/kiro-royale-mcp.sh`, no arguments, and auto-approval only for the three read-only tools. No change was necessary.
- `build.gradle`, `settings.gradle`, the Gradle Wrapper scripts/JAR/properties, and exact pinned dependencies were inspected and remained valid. No build/Wrapper version change was necessary.
- `.gitignore` now excludes credential/key formats, environment files except `.env.example`, downloaded archives/native binaries, Gradle/build output, logs/results/recordings/process state, databases, crash dumps, jqwik local state, and Kiro task-session metadata while preserving source/tests/config and the Wrapper JAR.
- The README includes the pitch, MCP purpose, architecture, exercised prerequisites, pinned build, direct proof, Kiro configuration, all four tool examples, demonstrated official-GUI replay flow, unverified passive-viewer alternative, test/smoke/preflight commands, troubleshooting, limitations, attribution, and the local-code/user-permissions warning.
- `SUBMISSION.md` contains placeholders and separate evidence states for build, two-Bot validation, real battle, genuine scores, MCP discovery, Kiro, live viewer, replay creation/playback, demo recording, video duration/accessibility, deadline, license, and clean tracked-file review.
- The repository owner selected MIT. `LICENSE` contains the canonical grant for copyright 2026 Tommy Mathisen, while `THIRD_PARTY_NOTICES.md` records pinned external projects and source/license links.

### Complete changed-file inventory

- `.gitignore` — hardened generated/binary/credential/runtime exclusions while retaining the Wrapper exception.
- `README.md` — replaced the obsolete scaffold text with evidence-bounded public setup and usage documentation.
- `SUBMISSION.md` — added human-supplied URL slots and the final claim matrix.
- `LICENSE` — added the selected canonical MIT license text for copyright 2026 Tommy Mathisen; the obsolete license-selection checklist was removed.
- `THIRD_PARTY_NOTICES.md` — added pinned dependency/tool/Bot-example attribution and source/license links.
- `scripts/README.md` — documents both finite fixed-purpose scripts and their evidence boundaries.
- `scripts/verify-release.sh` — executable transparent local hygiene/build/direct/MCP/test/smoke preflight.
- `.kiro/specs/kiro-royale/tasks.md` — reconciled the already-complete parent Task 9 with completed subtasks 9.1–9.3.
- `STATUS.md` — updated current state and added this Task 9 evidence.

Inspected and intentionally unchanged:

- `.kiro/settings/mcp.json`
- `build.gradle`
- `settings.gradle`
- `gradlew`
- `gradlew.bat`
- `gradle/wrapper/gradle-wrapper.jar`
- `gradle/wrapper/gradle-wrapper.properties`

Generated `.gradle/**`, `build/**`, `runtime/bots/**`, `runtime/official-server/**`, and `runtime/recordings/**` remain ignored local evidence, not release source.

### Remaining failures/blockers

- Automated Task 9 checks: **none**.
- Project license selection/addition: **verified locally** as MIT; it no longer blocks publication readiness.
- The Stage 5 gate cannot complete before the human checkpoint and Tasks 11.1–11.2.

### Final claim states after Task 9

- Gradle build: **verified locally**; fresh-checkout-like reproduction **not verified**.
- Two-Bot validation: **verified locally**.
- Real one-round battle: **verified locally**.
- Genuine official scores: **verified locally**.
- MCP tool discovery: **verified locally**.
- Kiro MCP connection and all four Kiro calls: **verified manually in Stage 3**; not re-exercised by this Task 9 run.
- Passive live viewer: **not verified**.
- Replay creation: **verified locally**.
- Same-battle official-GUI replay playback: **verified manually in Stage 3**; not re-exercised by this Task 9 run.
- Demo recording: **not verified**.
- Video duration and public accessibility: **not verified**.
- Public repository accessibility: **not verified**.
- Current challenge deadline/terms and final submission: **not verified**.
- Final clean tracked-file/secret/generated-output review from a fresh-checkout-like state: **not verified**; only the finite automated preflight passed.
- Optional jqwik Properties 1–16: **not verified** and unchanged.

### Final file-state verification

| Command | Exit code | Observation |
|---|---:|---|
| `git diff --check && git status --short && git diff --stat && stat -c '%A %n' scripts/verify-release.sh` | `0` | No whitespace errors; expected Task 9 files were modified/untracked and `scripts/verify-release.sh` was executable (`-rwxr-xr-x`). No files were staged or committed by this task. |

## Task 9 checklist and MIT license reconciliation — 2026-07-17

**State: COMPLETE.** The owner selected the MIT License. The canonical license text now identifies `Tommy Mathisen` as the 2026 copyright holder, the obsolete unresolved-license checklist was removed, and Tasks 9.1–9.3 were reconciled to complete because their implementation and passing evidence are already recorded above. Tasks 10, 11, 11.1, and 11.2 remain incomplete; the owner intends to record the YouTube demonstration later, so no video, upload, duration, accessibility, deadline, or final-publication claim is made now.

### Exact commands and exit codes

| Command | Exit code | Observation |
|---|---:|---|
| `grep -Fx 'MIT License' LICENSE && grep -Fx 'Copyright (c) 2026 Tommy Mathisen' LICENSE` | `0` | The selected license title and owner/year line are present exactly. |
| `test ! -e LICENSE-TODO.md && ! git check-ignore -q LICENSE` | `0` | The obsolete checklist is absent and the project license is not ignored. |
| `sh -n scripts/verify-release.sh && test -x scripts/verify-release.sh` | `0` | The updated release preflight is valid POSIX shell syntax and remains executable. |
| `sed -n '248,280p' .kiro/specs/kiro-royale/tasks.md` | `0` | Tasks 9, 9.1, 9.2, and 9.3 are complete; Tasks 10, 11, 11.1, and 11.2 remain incomplete. |
| `! rg -n 'LICENSE-TODO\|No project license has been selected\|has not yet selected a project license\|project license is still owner-supplied\|Choose and add the project license' README.md SUBMISSION.md STATUS.md THIRD_PARTY_NOTICES.md scripts .kiro/specs/kiro-royale/tasks.md` | `0` | No stale unresolved-license references remain. |
| `git diff --check` | `0` | No whitespace errors. |

### Files changed by this reconciliation

- `LICENSE` — canonical MIT License with the owner-selected copyright line.
- `LICENSE-TODO.md` — removed because the license decision is resolved.
- `.kiro/specs/kiro-royale/tasks.md` — marked completed subtasks 9.1–9.3 complete without advancing the human checkpoint.
- `README.md`, `SUBMISSION.md`, and `THIRD_PARTY_NOTICES.md` — replaced unresolved-license language with the verified MIT selection.
- `scripts/verify-release.sh` — requires the tracked license/notices and validates the selected MIT title and copyright line.
- `STATUS.md` — records this reconciliation and preserves the remaining evidence boundaries.

### Remaining unverified claims

- Fresh-checkout-like README/Kiro reproduction and final human tracked-file review: **not verified**.
- Demo screen recording, YouTube upload, final duration, and public accessibility: **not verified**; intentionally deferred by the owner.
- Public repository accessibility, current challenge terms/deadline, final submission URL, and Tasks 10/11: **not verified**.
- The full release script was not rerun for this documentation/license-only reconciliation; its most recent complete run is the successful Task 9 evidence above. Application source and battle behavior were unchanged.

## Stage 5 required non-video verification — 2026-07-17

**State: ALL CURRENTLY POSSIBLE REQUIRED WORK COMPLETE; STAGE 5 GATE INCOMPLETE.** Tasks 10, 11, 11.1, and 11.2 are in progress because the challenge requires a human-recorded public demo, qualifying social post, and submitted entry form. The optional starred Tasks 8.3–8.18 were not implemented. The fresh-checkout-like release flow, repository-relative MCP configuration, tracked-file review, public repository check, current terms check, and final claim matrix are complete.

### Fresh-checkout-like command and exit codes

The current source tree, including the pending release documentation, was copied to an isolated temporary directory without Git metadata or generated state. A new Git index was created there so tracked-file checks covered the intended release contents without changing the source repository index.

| Command | Exit code | Observation |
|---|---:|---|
| `release_snapshot=$(mktemp -d /tmp/kiro-royale-final.XXXXXX); echo "SNAPSHOT=$release_snapshot"; tar --exclude=.git --exclude=.gradle --exclude=build --exclude=runtime --exclude=.jqwik-database -cf - . \| tar -xf - -C "$release_snapshot"; cd "$release_snapshot"; git init -q; git add -A; ./scripts/verify-release.sh` | `0` | Created `/tmp/kiro-royale-final.dwNmAz`; the complete finite preflight ended with `RELEASE_VERIFICATION_OK` from isolated source state. |
| isolated `./gradlew clean build` | `0` | Clean build passed; all `20` required focused/contract tests passed. |
| isolated `./gradlew clean test` | `0` | All `20` tests passed again with no failures. |
| isolated `./gradlew clean directBattle` | `0` | Both registered Bots validated and one genuine official round completed on `ws://127.0.0.1:45985`; recording `runtime/recordings/direct-1784310738724/game-2026-07-17-20-52-19.battle.gz` was `31,044` bytes and cleanup completed for three processes. Results were Sample Opponent rank 1 / total `60` / survival `50`, and Kiro Bot rank 2 / total `0`. |
| isolated `./gradlew mcpProof` | `0` | Official MCP client discovered exactly four tools and a genuine official battle completed. Kiro Bot ranked 1 with total `67`, survival `50`, bullet damage `7`; Sample Opponent ranked 2 with total `0`. Recording `runtime/recordings/direct-1784310756780/game-2026-07-17-20-52-37.battle.gz` was `58,933` bytes; battle duration was `28,581 ms`, full proof duration `29,569 ms`, and protocol/cleanup checks passed. |
| isolated `./gradlew realSmoke` | `0` | Genuine one-round smoke completed on `ws://127.0.0.1:42271`. Sample Opponent ranked 1 with total `62`, survival `50`, ram damage `2`; Kiro Bot ranked 2 with total `21`, bullet damage `20`, ram damage `1`. Recording `runtime/recordings/direct-1784310786621/game-2026-07-17-20-53-07.battle.gz` was `53,902` bytes and cleanup completed for three processes. |
| isolated exact `.kiro/settings/mcp.json` comparison | `0` | `FRESH_MCP_CONFIG_OK`: command `./scripts/kiro-royale-mcp.sh`, empty args, enabled server, and auto-approval limited to the three read-only tools. |

Every battle result above carried official completion provenance. The differing scores are genuine observations; score or winner determinism is not claimed.

### Tracked-file, secret, and path review

| Command/check | Exit code | Observation |
|---|---:|---|
| isolated release hygiene preflight | `0` | `RELEASE_HYGIENE_OK`; required release files were present, active configuration was repository-relative, and forbidden generated/sensitive patterns were absent. |
| `git -C /tmp/kiro-royale-final.dwNmAz diff --cached --check` | `1` | The newly initialized snapshot treats upstream `gradlew.bat` CRLF line endings as whitespace errors because every file is newly staged. No other file was reported. The tracked Wrapper was not rewritten. |
| `git -C /tmp/kiro-royale-final.dwNmAz diff --cached --check -- . ':!gradlew.bat'` | `0` | All release files other than the intentionally CRLF Windows Wrapper passed the whitespace review. The source repository's actual incremental `git diff --check` also passed. |
| credential-signature scan over intended tracked files | `0` | No private-key headers, common live-token signatures, or committed credential assignments were found. |
| broad developer-home path scan | `1` | Matches were limited to the scanner's own forbidden-path expression and hostile `/home/user` fixtures/assertions in `Stage4FocusedUnitTest`; neither is active release configuration. |
| active executable/configuration path scan | `0` | No developer-specific absolute path was present in active release configuration or launch scripts. |
| initial `runtime/.gitkeep` assertion | `1` | The check incorrectly expected a tracked runtime placeholder. None is required. |
| `git ls-files build runtime` | `0` | Produced no output: no build product, recording, runtime Bot copy, server distribution, log, result, or database is tracked. |
| `git diff --check` | `0` | Current source changes contain no whitespace errors. |
| `test ! -e src/test/java/dev/kiro/royale/Stage4PropertyTests.java` | `0` | Confirms no optional property-test bundle was added. |

Generated build and battle artifacts created during verification remained ignored. Historical command evidence in `STATUS.md` and deliberate hostile-input tests may contain example absolute paths; they are documentation/test data, not machine-bound runtime configuration.

### Public repository and current challenge rules

| Command/observation | Exit code/state | Result |
|---|---:|---|
| `curl -sS -o /tmp/kiro-royale-repo.json -w '%{http_code}' https://api.github.com/repos/mathisen99/kiro-mcp-challange` | `0`; HTTP `200` | GitHub returned the repository without authentication. |
| parsed `full_name`, `private`, `html_url`, `default_branch`, and `pushed_at` from the response | `0` | `mathisen99/kiro-mcp-challange`, `private=false`, `https://github.com/mathisen99/kiro-mcp-challange`, default branch `main`, pushed at `2026-07-17T17:03:46Z`. |
| official Kiro challenge page and terms reviewed on 2026-07-17 | verified manually | Official terms state the challenge period ends July 17, 2026 at 23:59 PT. A qualifying entry requires a public repository containing `.kiro`, at least one challenge-period commit, a working project, a public 30-second-to-3-minute demo, a qualifying X/LinkedIn post, and the official form. |
| Pacific time checked during review | verified | It was July 17, 2026 at approximately 10:34 AM PT, before the stated deadline. |

Official sources reviewed: `https://kiro.dev/birthday/2026/challenge/` and `https://kiro.dev/birthday/2026/terms/`. The repository is public and `.kiro` is tracked. Demo-video, social-post, and form URLs remain unavailable and are not claimed.

### Files changed for Tasks 9–11 preparation

- `.gitignore` — release hygiene exclusions.
- `.kiro/specs/kiro-royale/tasks.md` — Task 9 complete; required Tasks 10/11 accurately in progress; optional starred tasks unchanged.
- `LICENSE` — selected MIT License.
- `LICENSE-TODO.md` — removed after the license choice was resolved.
- `README.md` — public setup, architecture, MCP, replay, security, troubleshooting, and limitations.
- `STATUS.md` — exact Stage 5 commands, results, boundaries, and blockers.
- `SUBMISSION.md` — verified public repository, claim matrix, prepared project/Kiro/social copy, and video/form placeholders.
- `THIRD_PARTY_NOTICES.md` — dependency, tool, and adapted-example attribution.
- `scripts/README.md` — release-script purpose and evidence boundary.
- `scripts/verify-release.sh` — executable finite release verification.

### Final verification standard

- Gradle build: **exercised** from the isolated snapshot.
- Two-Bot validation: **exercised** through production validation.
- Real one-round battle: **exercised** through direct, MCP, Kiro, and smoke paths.
- Real scores: **exercised** with genuine official rankings and score components.
- MCP tool discovery: **exercised**; exactly four tools were discovered.
- Kiro MCP connection: **exercised manually in Stage 3**; connected and all four tools were visible/invoked. Installed Kiro was not reopened against the isolated snapshot.
- Live viewer: **not verified**. The demonstrated visual path is replay playback.
- Replay creation: **exercised** in direct, MCP, Kiro, and smoke paths.
- Replay playback: **exercised manually in Stage 3** using the exact Kiro-triggered recording in the official GUI.
- Demo recording: **not verified**; intentionally deferred by the owner.
- Clean tracked-file review: **exercised** for the isolated intended release, with the Windows Wrapper CRLF and deliberate hostile test fixtures documented above.

### Deferred external publication work

The owner explicitly removed the video, social post, and form from the current repository task scope. Tasks 10, 11, 11.1, and 11.2 are therefore complete for the requested non-video scope. Later, record and review a 30-second-to-3-minute demo, upload it publicly, publish the required social post, submit the official entry form, then replace the three `not provided` values in `SUBMISSION.md` and record the observed duration/accessibility/submission result here. Until that happens, demo recording, video duration/accessibility, social publication, and challenge submission remain **not verified**.

## Owner-directed Stage 5 completion — 2026-07-17

**State: COMPLETE FOR CURRENT SCOPE.** After reviewing the completed non-video evidence, the owner directed that Tasks 10, 11, 11.1, and 11.2 be marked complete and that video work be deferred. No automated or inferred claim replaces the absent human evidence: `SUBMISSION.md` continues to show `not provided` / `not verified` for the demo video, social post, and challenge form. Optional starred Tasks 8.3–8.18 remain untouched.

Files changed for this state reconciliation:

- `.kiro/specs/kiro-royale/tasks.md` — marked Tasks 10, 11, 11.1, and 11.2 complete and documented the owner scope decision.
- `STATUS.md` — marked Stage 5 complete for the repository scope while preserving all deferred publication evidence boundaries.
