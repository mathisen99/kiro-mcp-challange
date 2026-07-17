# Requirements Document

## Introduction

Kiro Royale is a one-day minimum viable product that connects Kiro to genuine Robocode Tank Royale battles through a custom Model Context Protocol server. The product lets Kiro inspect two bundled bots, edit the simple Kiro bot between battles, launch an official Battle Runner match, receive genuine ranked score components, and verify the battle through a passive live viewer or a real replay fallback. Delivery follows Stage 0 through Stage 5 in strict order so that direct Battle Runner proof exists before MCP integration and submission work remains focused on the demonstrated path.

This document separates product behavior, repository and process gates, and manual verification evidence. Automated checks establish local behavior where practical; repository inspection verifies static artifacts and scope; manual checks cover Kiro, visual proof, publication, and recording. `STATUS.md` records commands, exit codes, observations, changed files, failures, and unverified claims after every stage.

## Glossary

- **MVP**: The minimum viable product required for the one-day challenge submission.
- **Kiro_Royale_System**: The complete one-day MVP delivered by this repository.
- **Kiro_Royale_Server**: The custom Java MCP server authored in this repository.
- **reviewer**: The person who manually verifies Kiro, visual proof, repository hygiene, demo, and submission evidence.
- **MCP**: Model Context Protocol, used by Kiro to discover and invoke Kiro Royale tools over standard input and standard output.
- **MCP_Mode**: The operating mode in which the Kiro Royale Server communicates with an MCP client over stdio.
- **MCP_Client**: A program that discovers and invokes tools exposed by the Kiro Royale Server.
- **Kiro**: The development environment that connects to the Kiro Royale Server as an MCP client.
- **Battle_Runner**: The official Robocode Tank Royale JVM API that starts and controls genuine battles.
- **Tank_Royale_Server**: The local Robocode service started by the Battle Runner for bot and viewer connections.
- **Bot**: An executable Robocode Tank Royale program registered with the Kiro Royale System.
- **Bundled_Bot**: One of the two reviewed bots stored under the repository `bots/` root: `kiro-bot` or `sample-opponent`.
- **Bot_ID**: A stable identifier that maps to one configured Bundled Bot without accepting a filesystem path from an MCP caller.
- **Bot_Registry**: The component that maps Bot IDs to configured Bundled Bot metadata and repository-relative directories.
- **kiro-bot**: The deliberately simple Bundled Bot whose strategy source is intended for Kiro-assisted editing.
- **sample-opponent**: The predictable, functional Bundled Bot used as the initial opponent.
- **Genuine_Battle**: A battle executed by the official Battle Runner with real Bot processes rather than mocks, fixtures, random values, placeholders, or hardcoded results.
- **Genuine_Result**: Final ranking and score data emitted by the Genuine Battle execution path.
- **Battle_Recording**: A real `.battle.gz` replay produced by the Battle Runner.
- **Passive_Viewer**: The third-party hosted Tank Royale browser viewer selected for the MVP, which observes a local battle without controlling it.
- **Official_GUI**: The official Robocode Tank Royale graphical application used to play a Battle Recording when Replay Proof is required.
- **Live_Viewer_Proof**: Manual observation of a Genuine Battle in the Passive Viewer while the battle runs.
- **Replay_Proof**: Successful manual loading and playback in the Official GUI of a Battle Recording produced by the demonstrated Genuine Battle. File existence alone is not Replay Proof.
- **Visual_Proof**: Live Viewer Proof when available, or Replay Proof as the fallback when live viewing cannot be demonstrated.
- **Runtime_Directory**: The ignored repository-relative `runtime/` directory for generated logs, results, and recordings.
- **Stage_Evidence**: A `STATUS.md` entry containing exact commands, exit codes, changed files, observed output, remaining failures, and explicitly unverified claims.
- **Stage_0**: Environment, official API, dependency, and trivial build verification.
- **Stage_1**: Direct Genuine Battle proof before MCP abstractions.
- **Stage_2**: MCP wrapping of the proven battle path.
- **Stage_3**: Kiro connection and Visual Proof verification.
- **Stage_4**: Focused hardening, unit tests, and real integration smoke testing.
- **Stage_5**: Documentation, demo recording, public repository, and submission completion.
- **Real_Integration_Smoke_Test**: A test that invokes the real Battle Runner and both Bundled Bots for one round without using a mock battle engine or fixture result.
- **Sanitized_Failure**: An actionable failure response that omits internal stack traces and does not claim battle success.
- **Read_Only_Tool**: An MCP tool that does not launch a battle or modify repository or runtime state.
- **Repository_Root**: The root directory of the Kiro Royale source repository.
- **Loopback_Interface**: A local-only network interface, such as `127.0.0.1`, that is not exposed to remote hosts.

## Requirements

### Requirement 1: Preserve the one-day staged MVP

**User Story:** As a challenge participant, I want implementation to follow the proven critical path, so that the one-day MVP reaches a valid end-to-end submission without stretch-feature delays.

#### Acceptance Criteria

1. THE implementation process SHALL require successful exit evidence for every preceding stage before work begins on a later-numbered stage.
2. UNTIL Stage_0 has recorded successful Stage_Evidence, THE implementation process SHALL NOT begin Stage_1 implementation.
3. UNTIL Stage_1 has recorded successful Stage_Evidence for a direct Genuine_Battle, THE implementation process SHALL NOT begin Stage_2 implementation.
4. WHEN each stage reaches an exit condition, THE Kiro_Royale_System SHALL add Stage_Evidence to `STATUS.md` before the next stage begins.
5. THE Kiro_Royale_System SHALL use one Java JVM application for Battle_Runner orchestration and MCP unless `DECISIONS.md` records a verified incompatibility.
6. THE Kiro_Royale_System SHALL limit MVP functionality to the demonstrated battle path and required submission artifacts.

7. WHEN a stage review occurs, THE reviewer SHALL confirm that `STATUS.md` lists the exact commands executed for the stage.
8. WHEN a stage review occurs, THE reviewer SHALL confirm that `STATUS.md` lists the exit code for each recorded command.
9. WHEN a stage review occurs, THE reviewer SHALL confirm that `STATUS.md` lists files changed during the stage.
10. WHEN a stage review occurs, THE reviewer SHALL confirm that `STATUS.md` lists observed output and manual observations.
11. WHEN a stage review occurs, THE reviewer SHALL confirm that `STATUS.md` lists remaining failures.
12. WHEN a stage review occurs, THE reviewer SHALL confirm that `STATUS.md` labels each unexercised claim as `not verified`.

### Requirement 2: Verify the environment and dependencies in Stage 0

**User Story:** As an implementer, I want current official APIs and dependencies verified before application abstractions are written, so that stale assumptions do not block the real integration.

#### Acceptance Criteria

1. WHEN Stage_0 begins, THE Kiro_Royale_System SHALL verify that Java 21 is usable on the implementation host.
2. WHEN Stage_0 begins, THE Kiro_Royale_System SHALL verify the current official Tank Royale release and Battle_Runner artifact coordinates.
3. WHEN Stage_0 begins, THE Kiro_Royale_System SHALL verify the current official MCP Java SDK release and stdio server API.
4. WHEN dependency resolution succeeds, THE Kiro_Royale_System SHALL pin the resolved dependency versions in the build configuration.
5. WHEN resolved versions and official sources are verified, THE Kiro_Royale_System SHALL record the verified values and sources in `DECISIONS.md`.
6. WHEN the Stage_0 build setup is complete, THE Kiro_Royale_System SHALL make the Gradle Wrapper available for verification.
7. WHEN Stage_0 reaches its exit condition, THE Kiro_Royale_System SHALL commit the verified Gradle Wrapper.
8. WHEN Stage_0 reaches its exit condition, THE Kiro_Royale_System SHALL build a trivial Java application with all required dependencies resolved.

### Requirement 3: Prove a direct genuine battle in Stage 1

**User Story:** As an evaluator, I want a direct official Battle Runner match before MCP work, so that the custom server wraps a proven real integration rather than simulated data.

#### Acceptance Criteria

1. THE Kiro_Royale_System SHALL provide `kiro-bot` and `sample-opponent` as two valid Bundled Bots based on current official Java Bot examples.
2. THE kiro-bot SHALL expose a simple primary strategy source suitable for repository editing.
3. THE sample-opponent SHALL use a simple deterministic strategy for a given sequence of battle events and SHALL remain functional in one-round matches.
4. WHEN the Stage_1 diagnostic command runs, THE Kiro_Royale_System SHALL start the Battle_Runner directly without routing execution through MCP.
5. WHEN the Battle_Runner starts a Genuine_Battle, THE Kiro_Royale_System SHALL start the Tank_Royale_Server on the Loopback_Interface.
6. WHEN the direct Genuine_Battle runs, THE Kiro_Royale_System SHALL launch both Bundled Bots as real Bot processes.
7. WHEN the direct Genuine_Battle completes, THE Kiro_Royale_System SHALL return exactly two Genuine Results ordered by rank.
8. WHEN a Genuine_Result is returned, THE Kiro_Royale_System SHALL include rank, name, version, total score, survival score, bullet damage, ram damage, first places, and rounds played.
9. WHERE battle recording is enabled, WHEN the direct Genuine_Battle completes, THE Kiro_Royale_System SHALL create a Battle_Recording under the Runtime_Directory.
10. IF the direct Battle_Runner execution fails, THEN THE Kiro_Royale_System SHALL return a failure instead of score data.
11. THE Kiro_Royale_System SHALL NOT require repeated battles to produce identical rankings or scores unless the official Battle_Runner configuration is proven to make all relevant battle conditions reproducible.

### Requirement 4: Expose the proven path through MCP in Stage 2

**User Story:** As a Kiro user, I want a small custom MCP interface over the proven battle path, so that Kiro can inspect bots and launch real matches.

#### Acceptance Criteria

1. WHEN Stage_2 reaches its exit condition, THE Kiro_Royale_Server SHALL expose `get_arena_status`, `list_bots`, `inspect_bot`, and `run_battle` as the four initial MCP tools.
2. THE Kiro_Royale_Server SHALL use MCP over stdio.
3. WHILE the Kiro_Royale_Server operates in MCP_Mode, THE Kiro_Royale_Server SHALL reserve stdout for MCP protocol traffic.
4. WHILE the Kiro_Royale_Server operates in MCP_Mode, THE Kiro_Royale_Server SHALL route diagnostics to stderr or ignored files under the Runtime_Directory.
5. WHEN an MCP_Client discovers tools, THE Kiro_Royale_Server SHALL advertise the four initial MCP tools.
6. WHEN `run_battle` receives a valid request, THE Kiro_Royale_Server SHALL invoke the same proven Genuine_Battle path established in Stage_1.
7. WHILE no current Kiro tool timeout blocks synchronous execution, THE Kiro_Royale_Server SHALL execute `run_battle` synchronously by default.
8. WHEN an actual Kiro tool timeout is observed, THE Kiro_Royale_System SHALL record timeout evidence before adding asynchronous status and result tools.

### Requirement 5: Provide bounded MCP tool contracts

**User Story:** As a Kiro user, I want concise structured tool responses, so that Kiro can reason about arena readiness, bot source, and genuine battle outcomes.

#### Acceptance Criteria

1. WHEN any MCP tool succeeds, THE Kiro_Royale_Server SHALL return a concise human-readable summary and machine-readable JSON-compatible result fields.
2. WHEN `get_arena_status` is called, THE Kiro_Royale_Server SHALL return application version, Java runtime version, resolved Robocode version, repository-relative bot root, discovered bot count, active-battle state, the actual WebSocket URL when the local server is available, viewer instructions, repository-relative recording directory, and blocking prerequisite failures.
3. WHEN `list_bots` is called, THE Kiro_Royale_Server SHALL return each registered Bot ID, name, version, repository-relative directory, language, validation status, and source label.
4. WHEN `inspect_bot` receives a registered Bot_ID, THE Kiro_Royale_Server SHALL return Bot metadata, repository-relative source file names, the primary editable source file, build information, run information, and validation issues.
5. WHEN `run_battle` omits the rounds input, THE Kiro_Royale_Server SHALL use one round.
6. WHEN `run_battle` provides a valid rounds input, THE Kiro_Royale_Server SHALL use the provided round count.
7. WHEN `run_battle` omits the record input, THE Kiro_Royale_Server SHALL enable Battle_Recording.
8. WHEN `run_battle` sets record to false, THE Kiro_Royale_Server SHALL NOT require a Battle_Recording or claim that one was created.
9. WHEN `run_battle` receives a valid request, THE Kiro_Royale_Server SHALL require exactly two distinct registered Bot IDs.
10. WHEN `run_battle` receives a valid request, THE Kiro_Royale_Server SHALL accept an integer round count from 1 through 5 inclusive.
11. BEFORE `run_battle` starts a battle, THE Kiro_Royale_Server SHALL validate both selected Bundled Bots.
12. THE `run_battle` input schema SHALL accept only `botIds`, `rounds`, `record`, and `showBattle` and SHALL reject additional properties.
13. IF `run_battle` receives duplicate Bot IDs, fewer or more than two Bot IDs, a non-integer round count, or a round count outside 1 through 5, THEN THE Kiro_Royale_Server SHALL return a Sanitized_Failure without starting a battle.
14. WHEN `run_battle` completes successfully, THE Kiro_Royale_Server SHALL return Genuine Results ordered by rank.
15. WHEN `run_battle` completes successfully, THE Kiro_Royale_Server SHALL include rank, name, version, total score, survival score, bullet damage, ram damage, first places, and rounds played for each Genuine_Result.
16. WHERE battle recording is enabled, WHEN `run_battle` creates a Battle_Recording, THE Kiro_Royale_Server SHALL return the repository-relative recording path.
17. WHILE a battle is executing, THE Kiro_Royale_Server SHALL report that one battle is active.
18. IF a second battle request arrives while a battle is executing, THEN THE Kiro_Royale_Server SHALL return a Sanitized_Failure.
19. WHEN `run_battle` omits `showBattle`, THE Kiro_Royale_Server SHALL NOT open a viewer and SHALL preserve headless execution.
20. WHEN `run_battle` sets `showBattle` to true, THE Kiro_Royale_Server SHALL open only the configured trusted Passive_Viewer URL and SHALL use its application-owned loopback endpoint.
21. WHEN `showBattle` is true, THE Kiro_Royale_Server SHALL give the viewer a finite application-owned pre-battle connection window and SHALL record whether an established loopback client was mechanically observed.
22. WHEN a viewer-enabled Genuine_Battle completes, THE Kiro_Royale_Server SHALL keep the loopback viewer session available for a finite application-owned post-result interval before cleanup so the victory result can be read.
22. IF the trusted viewer cannot be opened, THEN THE Kiro_Royale_Server SHALL return a Sanitized_Failure without starting a battle; absence of a kernel-level connection observation SHALL NOT fabricate failure when the browser launch succeeded and a late connection can still display the battle.
23. WHEN a viewer-enabled battle completes successfully, THE Kiro_Royale_Server SHALL report separately whether viewing was requested and whether a pre-battle connection was mechanically observed.

### Requirement 6: Enforce filesystem, network, and process safety

**User Story:** As a repository owner, I want narrow local execution boundaries, so that MCP calls cannot become arbitrary operating-system access.

#### Acceptance Criteria

1. THE Bot_Registry SHALL map Bot IDs only to configured Bundled Bot directories under the repository `bots/` root.
2. WHEN the Bot_Registry resolves a Bot directory, THE Bot_Registry SHALL normalize the canonical path before validation.
3. IF a canonical Bot directory is outside the repository `bots/` root, THEN THE Bot_Registry SHALL reject the Bot directory.
4. IF an MCP request supplies an unknown Bot_ID, THEN THE Kiro_Royale_Server SHALL return a Sanitized_Failure.
5. IF an MCP request supplies a filesystem path instead of a Bot_ID, THEN THE Kiro_Royale_Server SHALL reject the request.
6. IF an MCP request supplies an executable command, shell fragment, environment override, network host, or remote repository URL, THEN THE Kiro_Royale_Server SHALL reject the request.
7. WHEN the Kiro_Royale_System launches a child process, THE Kiro_Royale_System SHALL use an argument-list process API without shell interpretation.
8. WHEN the Tank_Royale_Server starts, THE Kiro_Royale_System SHALL bind the service to the Loopback_Interface by default.
9. WHEN the Kiro_Royale_System creates generated logs, results, or recordings, THE Kiro_Royale_System SHALL place the generated files under the Runtime_Directory.
10. WHEN a battle finishes, fails, times out, or the Kiro_Royale_Server shuts down, THE Kiro_Royale_System SHALL terminate child processes started for the battle.
11. WHEN the Kiro_Royale_System writes diagnostic logs, THE Kiro_Royale_System SHALL omit credentials and secret values.
12. THE Kiro_Royale_System SHALL document that Bot execution runs local executable code with the user's permissions.
13. THE Kiro_Royale_System SHALL configure a strictly positive finite Bot connection timeout that cannot be overridden by an MCP caller.
14. THE Kiro_Royale_System SHALL configure a strictly positive finite battle wall-clock timeout that cannot be overridden by an MCP caller.
15. WHEN child-process output or diagnostic detail exceeds a configured finite limit, THE Kiro_Royale_System SHALL bound or truncate the captured content without corrupting MCP protocol output.

### Requirement 7: Preserve genuine results and explicit failure semantics

**User Story:** As an evaluator, I want scores and failures to reflect the official system, so that every claimed outcome is credible.

#### Acceptance Criteria

1. WHEN a battle succeeds, THE Kiro_Royale_System SHALL derive every returned rank and score component from Battle_Runner completion data.
2. IF the Battle_Runner does not report successful completion, THEN THE Kiro_Royale_System SHALL omit a successful battle result.
3. IF a Bot fails validation or startup, THEN THE Kiro_Royale_System SHALL return a Sanitized_Failure identifying the failed prerequisite.
4. IF the local server cannot bind or connect within the configured timeout, THEN THE Kiro_Royale_System SHALL return a Sanitized_Failure identifying the connection problem.
5. IF battle execution aborts or times out, THEN THE Kiro_Royale_System SHALL return a Sanitized_Failure identifying the battle state.
6. IF record is true and recording fails, THEN THE Kiro_Royale_System SHALL return a Sanitized_Failure identifying the recording problem.
7. WHEN an internal exception crosses an MCP tool boundary, THE Kiro_Royale_Server SHALL return an actionable Sanitized_Failure without an internal stack trace.
8. THE Kiro_Royale_System SHALL exclude fabricated, random, fixture-derived, placeholder, and hardcoded score data from production battle responses.

### Requirement 8: Connect Kiro and provide visual proof in Stage 3

**User Story:** As a demo viewer, I want Kiro to launch a visually verifiable real battle, so that the complete MCP integration is evident.

#### Acceptance Criteria

1. WHEN the repository-root MCP launcher has been verified, THE Kiro_Royale_System SHALL configure `.kiro/settings/mcp.json` with the verified launcher.
2. WHILE the repository-root MCP launcher remains unverified, THE Kiro_Royale_System SHALL keep the Kiro MCP configuration disabled.
3. WHEN the repository-root MCP launcher is verified, THE Kiro_Royale_System SHALL enable the Kiro MCP configuration.
4. WHERE MCP tool auto-approval is configured, THE Kiro_Royale_System SHALL auto-approve only Read_Only_Tools.
5. WHEN the local Tank_Royale_Server is ready, THE Kiro_Royale_System SHALL provide its actual loopback WebSocket URL before the demonstrated battle begins so that the Passive_Viewer can connect first.
6. WHERE Battle_Recording is enabled, WHEN a Genuine_Battle completes, THE Kiro_Royale_System SHALL preserve the Battle_Recording for Replay_Proof.

7. WHEN Stage_3 reaches its exit condition, THE reviewer SHALL verify that Kiro displays the connected Kiro_Royale_Server.
8. WHEN Stage_3 reaches its exit condition, THE reviewer SHALL invoke each of the four initial MCP tools from Kiro.
9. WHEN Kiro invokes `run_battle`, THE reviewer SHALL verify that Kiro receives Genuine Results from the demonstrated battle.
10. WHERE the Passive_Viewer can connect to the local WebSocket service, WHEN Kiro invokes `run_battle`, THE reviewer SHALL verify Live_Viewer_Proof.
11. WHEN the Passive_Viewer is used, THE reviewer SHALL identify it as a third-party passive observer and SHALL verify that it does not control the battle.
12. IF Live_Viewer_Proof cannot be completed because the viewer is unavailable, incompatible, too late to observe the battle, or blocked from the local viewer connection, THEN THE reviewer SHALL verify Replay_Proof from the same demonstrated Genuine_Battle.
13. IF Replay_Proof is used as the Visual_Proof fallback, THEN THE reviewer SHALL record live viewer verification as `not verified` in `STATUS.md`.
14. IF neither Live_Viewer_Proof nor Replay_Proof is demonstrated, THEN THE reviewer SHALL keep Stage_3 incomplete.

### Requirement 9: Harden and test the demonstrated path in Stage 4

**User Story:** As an implementer, I want focused automated checks plus one real integration test, so that high-risk boundaries are covered without expanding the one-day scope.

#### Acceptance Criteria

1. WHEN the focused unit test suite runs, THE Kiro_Royale_System SHALL verify rejection of a canonical Bot path outside the repository `bots/` root.
2. WHEN the focused unit test suite runs, THE Kiro_Royale_System SHALL verify rejection of an unknown Bot_ID.
3. WHEN the focused unit test suite runs, THE Kiro_Royale_System SHALL verify rejection of round counts below 1 and above 5.
4. WHEN the focused unit test suite runs, THE Kiro_Royale_System SHALL verify rejection of duplicate Bot IDs, non-integer rounds, and unexpected input properties.
5. WHEN the focused unit test suite runs, THE Kiro_Royale_System SHALL verify conversion of internal exceptions into Sanitized_Failures.
6. WHEN the Real_Integration_Smoke_Test runs, THE Kiro_Royale_System SHALL build the application and both Bundled Bots.
7. WHEN the Real_Integration_Smoke_Test runs, THE Kiro_Royale_System SHALL validate both Bundled Bots.
8. WHEN the Real_Integration_Smoke_Test runs, THE Kiro_Royale_System SHALL execute one Genuine_Battle for one round through the real Battle_Runner.
9. WHEN the Real_Integration_Smoke_Test completes, THE Kiro_Royale_System SHALL assert that exactly two ranked Genuine Results were returned.
10. WHEN the Real_Integration_Smoke_Test completes, THE Kiro_Royale_System SHALL assert that result names and versions match Bundled Bot configuration.
11. WHEN the Real_Integration_Smoke_Test completes, THE Kiro_Royale_System SHALL verify a real Battle_Runner completion event.
12. WHERE recording is enabled for the Real_Integration_Smoke_Test, WHEN the Genuine_Battle completes, THE Kiro_Royale_System SHALL verify that a real Battle_Recording exists.
13. THE Real_Integration_Smoke_Test SHALL use real Bundled Bot processes instead of a mock battle engine or fixture result.

### Requirement 10: Complete reproducible documentation and repository hygiene in Stage 5

**User Story:** As a new user, I want reproducible setup and transparent limitations, so that the public repository can be evaluated from a fresh checkout.

#### Acceptance Criteria

1. WHEN Stage_5 reaches its exit condition, THE Kiro_Royale_System SHALL provide a public README containing the project pitch, MCP purpose, architecture, prerequisites, setup, build, direct battle verification, Kiro configuration, MCP tool examples, viewer instructions, replay fallback, test commands, troubleshooting, limitations, licenses, and attribution.
2. WHEN Stage_5 reaches its exit condition, THE Kiro_Royale_System SHALL provide README commands that have corresponding successful Stage_Evidence.
3. WHEN Stage_5 reaches its exit condition, THE Kiro_Royale_System SHALL commit a functional `.kiro/settings/mcp.json` that uses repository-relative startup behavior.
4. WHEN Stage_5 reaches its exit condition, THE Kiro_Royale_System SHALL exclude credentials, `.env`, downloaded binaries, build outputs, logs, results, recordings, and local databases from tracked files.
5. WHEN Stage_5 reaches its exit condition, THE Kiro_Royale_System SHALL include the Gradle Wrapper, source code, tests, `.kiro` directory, selected license, and pinned resolved dependency versions in the repository.
6. WHEN Stage_5 reaches its exit condition, THE Kiro_Royale_System SHALL complete the repository and video links in `SUBMISSION.md`.

7. WHEN final verification begins, THE reviewer SHALL test the documented setup and `.kiro/settings/mcp.json` from a fresh-checkout-like repository state.
8. WHEN final verification begins, THE reviewer SHALL inspect tracked files for secrets, machine-specific absolute paths, generated files, and build output.
9. WHEN final verification begins, THE reviewer SHALL confirm that the public repository is accessible.
10. WHEN final verification begins, THE reviewer SHALL recheck the official challenge page for current submission terms and deadlines.
11. WHEN final reporting begins, THE reviewer SHALL state separately whether the Gradle build, two-Bot validation, real one-round battle, Genuine Results, MCP tool discovery, Kiro MCP connection, live viewer, replay creation, demo recording, and clean tracked-file review were exercised.

### Requirement 11: Record and submit the end-to-end demo

**User Story:** As a challenge evaluator, I want a short end-to-end demonstration, so that the custom MCP integration and real external-system result are visible.

#### Acceptance Criteria

1. WHEN the demo recording begins, THE reviewer SHALL show Kiro with the connected Kiro_Royale_Server.
2. WHEN the demo recording continues, THE reviewer SHALL show Kiro invoking `list_bots` and `inspect_bot` for the Bundled Bots.
3. WHEN the demo recording continues, THE reviewer SHALL show Kiro invoking `run_battle` for a Genuine_Battle.
4. WHERE Live_Viewer_Proof is available, WHEN the Genuine_Battle runs, THE reviewer SHALL show the live battle in the Passive_Viewer.
5. IF Live_Viewer_Proof is unavailable, THEN THE reviewer SHALL show the demonstrated Genuine Battle's Battle Recording loading and playing in the Official GUI.
6. WHEN the Genuine_Battle completes, THE reviewer SHALL show returned ranks and score components in Kiro.
7. WHEN the final video is exported, THE reviewer SHALL keep the video duration at or below three minutes.
8. WHEN Stage_5 reaches its exit condition, THE reviewer SHALL publish the demo video and record the video link in `SUBMISSION.md`.

### Requirement 12: Exclude stretch features from the MVP

**User Story:** As a challenge participant, I want explicit scope exclusions, so that optional platform work cannot displace mandatory proof and submission tasks.

#### Acceptance Criteria

1. WHILE any Stage_0 through Stage_5 exit condition remains incomplete, THE Kiro_Royale_System SHALL exclude leaderboards, ratings, remote matchmaking, remote Bot downloads, arbitrary Bot import, public uploads, containers, sandboxing, a custom viewer, pause controls, resume controls, step controls, per-turn telemetry, intent diagnostics, automatic browser opening, background hooks, tournament brackets, multiple simultaneous battles, SQLite persistence, and a separate feature-parity CLI.
2. WHILE synchronous `run_battle` completes within the observed Kiro tool timeout, THE Kiro_Royale_System SHALL exclude asynchronous battle job infrastructure.
3. WHERE optional work begins after all Stage_0 through Stage_5 exit conditions are satisfied, THE Kiro_Royale_System SHALL prioritize asynchronous timeout handling, compact telemetry, before-and-after comparison, replay handling, and disabled-by-default hooks in that order.
