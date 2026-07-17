# Implementation Plan: Kiro Royale

## Overview

Implement the one-day Java 21/Gradle MVP in strict Stage 0 → 1 → 2 → 3 → 4 → 5 order. Each stage ends with a `STATUS.md` evidence gate; no later-stage leaf task may start until that gate is complete. Stage 1 must directly execute a genuine one-round Battle Runner match with both real bundled Bots before any MCP adapter is written. Stage 2 must prove discovery and a genuine battle through an MCP client before Kiro or viewer work. Stage 4 hardening, property tests, and the real smoke test begin only after the full Kiro/MCP/visual path has been manually exercised in Stage 3.

## Tasks

- [x] 1. Stage 0 — verify the environment, official APIs, dependencies, and build
  - [x] 1.1 Verify current official integration facts without implementing adapters
    - Run `java -version` and verify that the selected runtime and compiler are Java 21.
    - Check current official Tank Royale, Battle Runner, Java Bot API, MCP Java SDK stdio server, and jqwik sources; verify artifact coordinates, releases, required Java level, and the specific API capabilities identified as uncertain in the design.
    - Record source URLs and candidate facts for later evidence, but do not create `OfficialBattleRunnerAdapter`, `McpStdioAdapter`, Bot launch code, or assumed external signatures in this task.
    - _Requirements: 1.2, 2.1, 2.2, 2.3_

  - [x] 1.2 Create the pinned Gradle build, Wrapper, and compile-only Stage 0 probe
    - Create the Java 21 Gradle application/build structure and Gradle Wrapper, pin only versions that successfully resolve, and configure JUnit Platform plus the verified jqwik dependency without adding tests yet.
    - Add a trivial `Stage0DependencyProbe` that compiles and runs against the verified dependencies only far enough to prove class/API availability; it must not contain battle orchestration or MCP adapter behavior.
    - Verify from the repository root with `./gradlew --version`, `./gradlew dependencies --configuration runtimeClasspath`, `./gradlew clean build`, and the dedicated Stage 0 probe task.
    - Ensure Wrapper scripts and JAR are version-controlled project files rather than downloaded runtime artifacts.
    - _Requirements: 1.5, 2.4, 2.6, 2.7, 2.8_

  - [x] 1.3 Record only verified Stage 0 decisions in `DECISIONS.md`
    - Replace version-resolution TODOs with the exact Java, Gradle Wrapper, MCP Java SDK, Tank Royale, Battle Runner, Java Bot API, and jqwik versions that resolved, including commands, official sources, and verification date.
    - Record verified stdio construction/tool registration APIs, Battle Runner lifecycle/result/recording/process APIs, Bot configuration format, loopback/endpoint behavior, and any incompatibility affecting the one-JVM decision; leave unsupported facts explicitly unresolved rather than guessing.
    - _Requirements: 1.5, 2.2, 2.3, 2.4, 2.5_

  - [x] 1.4 Write the Stage 0 exit evidence gate in `STATUS.md`
    - Add a `Stage 0` entry containing the exact commands from Tasks 1.1–1.2, every exit code, exact resolved values, observed probe/build output, the complete changed-file list, remaining failures, and every unexercised claim labeled `not verified`.
    - Mark Stage 0 complete only if Java 21 works, all pinned dependencies resolve, the Wrapper is included in the version-controlled change, and the trivial probe/build succeeds; otherwise keep the gate incomplete and block Stage 1.
    - _Requirements: 1.1, 1.2, 1.4, 1.7–1.12, 2.1–2.8_

- [x] 2. Human verification checkpoint — Stage 0 exit
  - Confirm the Stage 0 evidence is reproducible, the Gradle Wrapper is committed as required, `DECISIONS.md` contains only verified facts, and no Battle Runner or MCP adapter implementation began early. Do not authorize Stage 1 while any Stage 0 exit fact is missing.

- [x] 3. Stage 1 — prove the direct genuine two-Bot battle
  - [x] 3.1 Implement and build the two real bundled Java Bots
    - Adapt current official Java Bot examples into `bots/kiro-bot` and `bots/sample-opponent` using the Stage 0-verified configuration and launch format.
    - Give `kiro-bot` one obvious primary editable strategy source and give `sample-opponent` a simple deterministic event-response strategy; wire both Bot builds into Gradle without accepting caller-provided commands.
    - Keep Bot identity, version, source labels, and launch metadata consistent across their configurations and Java sources.
    - _Requirements: 3.1, 3.2, 3.3, 6.7, 12.1_

  - [x] 3.2 Implement the minimal internal models, runtime paths, registry, and Bot validation needed by direct mode
    - Add repository-root/runtime-path resolution, the static registry for exactly `kiro-bot` and `sample-opponent`, Bot descriptors/inspection models, and validation based only on Stage 0-verified Bot files.
    - Keep all generated targets under canonical `runtime/`; expose only repository-relative paths and application-owned argument lists.
    - Reject invalid registered Bot prerequisites before launch, but defer broad generated-input/property coverage to Stage 4.
    - _Requirements: 3.1, 5.11, 6.1–6.3, 6.7, 6.9, 12.1_

  - [x] 3.3 Implement the official Battle Runner adapter and owned lifecycle for direct mode
    - Implement the verified official managed-server, loopback binding, finite Bot-connect timeout, real child-Bot launch, one-active-battle lifecycle, official completion capture, and idempotent cleanup using argument-list process APIs.
    - Consume child streams without MCP stdout assumptions, bound initial diagnostics, and terminate only application-owned processes on success, failure, timeout, and shutdown.
    - Do not add MCP SDK types, MCP schemas, tool handlers, or a second battle implementation.
    - _Requirements: 3.4–3.6, 3.10, 6.7, 6.8, 6.10, 6.13–6.15, 7.2–7.5_

  - [x] 3.4 Implement direct diagnostic result mapping and optional genuine recording
    - Add the direct diagnostic entry/task that selects the two registered Bots and invokes the official adapter for exactly one round by default, outside MCP.
    - Map only the verified official successful-completion event into exactly two ascending-ranked results with rank, name, version, total score, survival score, bullet damage, ram damage, first places, and rounds played.
    - When recording is enabled, allocate and verify a contained, non-empty `.battle.gz` under `runtime/recordings`; return failure rather than score data for non-success completion or required-recording failure.
    - _Requirements: 3.4, 3.7–3.11, 5.15, 6.9, 7.1, 7.2, 7.5, 7.6, 7.8_

  - [x] 3.5 Directly execute and inspect the genuine one-round match
    - From the repository root, run the dedicated Gradle command that builds the application and both Bots and executes `direct-battle` for one round with recording enabled; this command must call the official Battle Runner directly and must not route through MCP or a fake engine.
    - Require exit code 0, an observed official completion event, two real Bot processes, exactly two ordered configured identities, every required official score component, and a verified real recording when enabled; do not assert repeatable winners or scores.
    - Capture the exact actual command, loopback endpoint observation, process/lifecycle observations, result output, recording path, and cleanup outcome for the gate task.
    - _Requirements: 1.3, 3.4–3.11, 7.1, 7.2, 7.8_

  - [x] 3.6 Record Stage 1 adapter decisions and exit evidence
    - Update `DECISIONS.md` with only facts verified by the direct run: current Bot configuration/launch format, official completion/result accessors and numeric types, loopback/endpoint behavior, recording mechanism, timeout values, and proven process ownership/cleanup behavior.
    - Add the `STATUS.md` Stage 1 entry with the exact build/direct-battle commands and exit codes, observed real processes and official completion/results, exact changed files, replay outcome, remaining failures, and all unverified visual/MCP/Kiro claims marked `not verified`.
    - Mark Stage 1 complete only after the genuine direct match succeeds; otherwise leave it incomplete and block every Stage 2 leaf task.
    - _Requirements: 1.1, 1.3, 1.4, 1.7–1.12, 3.1–3.11_

- [x] 4. Stage 2 — expose and prove the same path through MCP
  - [x] 4.1 Implement the four strict stdio MCP tools over the proven services
    - Add the verified SDK stdio server adapter and register exactly `get_arena_status`, `list_bots`, `inspect_bot`, and `run_battle` with schemas that reject additional properties and do not accept paths, commands, environments, hosts, URLs, or remote repositories.
    - Implement required status/list/inspection projections, battle defaults and 1–5 round validation, exactly two distinct registered IDs, one-active-battle rejection, concise summaries, structured fields, and sanitized failures.
    - Delegate `run_battle` synchronously to the exact Stage 1 `BattleService`/official adapter path; do not add async jobs or additional tools without observed timeout evidence.
    - _Requirements: 4.1, 4.2, 4.5–4.8, 5.1–5.18, 6.4–6.6, 7.3–7.7, 12.1, 12.2_

  - [x] 4.2 Implement protocol-safe MCP startup and the repository-root launcher
    - Add the MCP runtime mode and repository-relative launcher using the verified Stage 0 SDK/build output; configure logging before server initialization so stdout carries only MCP protocol traffic and diagnostics/child output go to stderr or bounded ignored runtime files.
    - Reject unknown startup modes before external work, share the Stage 1 composition root, and close active application-owned resources on MCP/JVM shutdown.
    - Keep `.kiro/settings/mcp.json` disabled in this task; Stage 3 may enable it only after the launcher passes the MCP client proof.
    - _Requirements: 4.2–4.4, 6.9–6.11, 8.1–8.3_

  - [x] 4.3 Implement a finite MCP client proof harness
    - Add a repository-local Java proof program/Gradle task using the verified MCP client API to launch the repository-root MCP command, complete handshake/tool discovery, invoke all three read-only tools, and invoke `run_battle` once for the two registered Bots and one real round.
    - Make the proof fail on any extra/missing tool, malformed structured response, protocol contamination, sanitized failure, non-official result provenance, wrong identity/cardinality/rank order, or missing requested recording.
    - Keep this as a finite Stage 2 integration proof, not a long-running server, viewer automation, fake battle, or replacement feature-parity CLI.
    - _Requirements: 4.1–4.7, 5.1–5.18, 7.1, 7.8_

  - [x] 4.4 Execute the MCP client proof against the real launcher and battle path
    - Build the distribution and Bots, then run the exact finite MCP client proof command from the repository root; require successful discovery of exactly four tools and a real one-round `run_battle` result from the same official adapter proven in Stage 1.
    - Capture stdout-protocol validation, stderr diagnostics, actual duration for later Kiro-timeout comparison, genuine result fields, recording path, and process cleanup; no mocked or fixture response satisfies this task.
    - _Requirements: 1.3, 4.5–4.7, 5.14–5.16, 7.1, 7.8_

  - [x] 4.5 Record Stage 2 SDK/launcher decisions and exit evidence
    - Update `DECISIONS.md` with the verified SDK server/client APIs, structured-content representation, final repository-root launcher candidate, and measured synchronous proof duration; do not claim Kiro timeout compatibility yet.
    - Add the `STATUS.md` Stage 2 entry with exact build/proof commands and exit codes, discovered tool names, genuine MCP battle observations, stdout/stderr behavior, changed files, remaining failures, and Kiro/viewer claims marked `not verified`.
    - Mark Stage 2 complete only after the real MCP client proof passes; otherwise leave it incomplete and block `.kiro` enablement and all Stage 3 work.
    - _Requirements: 1.1, 1.4, 1.7–1.12, 4.1–4.8_

- [x] 5. Stage 3 — configure the proven launcher for Kiro
  - [x] 5.1 Enable only the verified repository-relative MCP launcher in Kiro configuration
    - Update `.kiro/settings/mcp.json` only after Task 4.5 is complete, using the launcher proven from the repository root with no machine-specific absolute path.
    - Enable the server and auto-approve only `get_arena_status`, `list_bots`, and `inspect_bot`; require explicit approval for `run_battle`.
    - Preserve synchronous `run_battle`; if Kiro later exhibits an actual timeout, capture the evidence rather than silently adding excluded async infrastructure.
    - _Requirements: 4.7, 4.8, 8.1–8.4, 10.3, 12.2_

- [x] 6. Human verification checkpoint — Stage 3 Kiro and visual proof
  - In the installed Kiro UI, verify that the configured server appears connected and manually invoke all four tools. Confirm `run_battle` returns the genuine ranked score components and note whether its duration fits the actual Kiro timeout.
  - For Live Viewer Proof, connect the third-party passive viewer to the actual reported loopback WebSocket URL before starting the Kiro-triggered battle and confirm that it only observes that battle. If this cannot be demonstrated, load and play the same battle's `.battle.gz` in the official GUI for Replay Proof.
  - Record the exact observations for Task 7.1. Neither code inspection, an existing URL, nor a replay file alone counts as visual proof. Keep Stage 3 incomplete if neither live viewing nor official-GUI replay playback succeeds.

- [x] 7. Stage 3 — persist the human evidence gate
  - [x] 7.1 Record the verified Kiro/viewer outcome in `DECISIONS.md` and `STATUS.md`
    - In `DECISIONS.md`, finalize the launcher evidence, record observed Kiro timeout compatibility, and mark the passive-viewer decision verified or the official-GUI replay fallback selected; record any actual timeout before proposing async tools.
    - Add the `STATUS.md` Stage 3 entry with exact launcher/build commands and exit codes, Kiro tool invocations, changed files, genuine result observations, actual URL, Live Viewer Proof or same-battle Replay Proof, remaining failures, and each unexercised alternative labeled `not verified`.
    - Mark Stage 3 complete only when Kiro invoked every tool and one valid form of Visual Proof was manually demonstrated; otherwise block all Stage 4 leaves.
    - _Requirements: 1.1, 1.4, 1.7–1.12, 4.8, 8.1–8.14_

- [x] 8. Stage 4 — harden and test only the demonstrated path
  - [x] 8.1 Complete focused production hardening around the proven vertical slice
    - Finish canonical real-path/symlink containment, strict request decoding, immutable positive finite timeouts, atomic no-queue battle ownership, truthful recording verification, lossless official-result validation, bounded stream capture, secret redaction, allowlisted failure sanitization, and idempotent owned-process cleanup.
    - Add static/architecture checks that process launch uses argument lists, network binding is loopback-only, generated files remain under `runtime/`, MCP inputs cannot alter commands/environments/network/timeouts, and MCP mode has no ordinary stdout writers.
    - Do not add excluded stretch features, generalized execution, async jobs, a custom viewer, persistence, or simultaneous battles.
    - _Requirements: 5.9–5.18, 6.1–6.15, 7.1–7.8, 12.1, 12.2_

  - [x] 8.2 Add the mandatory focused example-based unit suite
    - Add small JUnit tests for canonical path escape, unknown Bot ID, rounds `0`, `1`, `5`, and `6`, duplicate IDs, non-integer rounds, unexpected properties, invalid Bot short-circuiting the engine, representative typed failures, and internal-exception sanitization.
    - Include controlled timeout/shutdown cleanup, `record=false`, endpoint-not-ready `null`, exact tool inventory, no-input/inspection extra-property rejection, and direct-vs-MCP stdout behavior without duplicating generated property coverage.
    - Run the focused test task and require success before the real smoke test.
    - _Requirements: 4.1–4.4, 5.5–5.18, 6.1–6.15, 7.3–7.7, 9.1–9.5_

  - [ ]* 8.3 Write the jqwik test for Property 1, omitted battle option defaults
    - Create a dedicated property class with at least 100 tries and tag/comment `Feature: kiro-royale, Property 1: Omitted battle options normalize to safe defaults`.
    - Generate valid two-Bot requests with omitted/present options and verify defaults and preserved valid values.
    - **Property 1: Omitted battle options normalize to safe defaults**
    - **Validates: Requirements 5.5, 5.6, 5.7, 5.8**

  - [ ]* 8.4 Write the jqwik test for Property 2, strict launchable request domain
    - Create a dedicated property class with at least 100 tries and the exact Property 2 tag/comment; generate arbitrary JSON-compatible shapes, types, IDs, bounds, and extra keys.
    - Assert that only the strict domain invokes the fake engine and every other value returns sanitized failure with zero invocations.
    - **Property 2: Only the strict battle request domain can launch an engine**
    - **Validates: Requirements 5.9, 5.10, 5.12, 5.13, 6.5, 6.6, 9.3, 9.4**

  - [ ]* 8.5 Write the jqwik test for Property 3, canonical registered Bot containment
    - Create a dedicated temporary-filesystem property class with at least 100 tries and the exact Property 3 tag/comment; cover nested paths, traversal, unknown IDs, and symlink escapes where supported.
    - Assert that only registered mappings resolving beneath the canonical `bots/` root become launchable; report the symlink assumption and retain a fixed supported-host escape example.
    - **Property 3: Registered Bot resolution is canonical and contained**
    - **Validates: Requirements 6.1, 6.2, 6.3, 6.4, 9.1, 9.2**

  - [ ]* 8.6 Write the jqwik test for Property 4, ordered two-result mapping
    - Create a dedicated property class with at least 100 tries and the exact Property 4 tag/comment; generate valid rank permutations for the two expected identities.
    - Assert exact cardinality and ascending official rank regardless of adapter input order.
    - **Property 4: Valid official two-Bot completions produce exactly two ordered results**
    - **Validates: Requirements 3.7, 5.14**

  - [ ]* 8.7 Write the jqwik test for Property 5, lossless genuine result mapping
    - Create a dedicated property class with at least 100 tries and the exact Property 5 tag/comment; generate representable score boundaries and both Bot identities.
    - Assert field-for-field preservation with no calculation, truncation, substitution, or cross-Bot mixing.
    - **Property 5: Genuine result mapping is lossless**
    - **Validates: Requirements 3.8, 5.15, 7.1**

  - [ ]* 8.8 Write the jqwik test for Property 6, rejection of non-genuine completion
    - Create a dedicated property class with at least 100 tries and the exact Property 6 tag/comment; generate absent, failed, aborted, timed-out, malformed, identity/cardinality-mismatched, and non-official completions.
    - Assert failure with no ranked results or successful recording claim.
    - **Property 6: Non-genuine or non-success completion can never become success**
    - **Validates: Requirements 3.10, 7.2, 7.5, 7.8**

  - [ ]* 8.9 Write the jqwik test for Property 7, contained artifacts and truthful recordings
    - Create a dedicated temporary-filesystem property class with at least 100 tries and the exact Property 7 tag/comment; generate artifact identities, kinds, hostile fragments, recording flags, and file states.
    - Assert runtime containment and that a response claims a recording iff it was requested and the expected contained regular non-empty file was verified.
    - **Property 7: Runtime artifacts remain contained and recording claims are truthful**
    - **Validates: Requirements 5.8, 5.16, 6.9, 7.6**

  - [ ]* 8.10 Write the jqwik test for Property 8, exclusive active-battle ownership
    - Create a dedicated concurrency property class with at least 100 bounded schedules and the exact Property 8 tag/comment; use deterministic latches/barriers rather than sleeps.
    - Assert one owner, truthful active status through cleanup, no queue, `BATTLE_ACTIVE` for overlaps, and zero extra engine calls.
    - **Property 8: At most one battle owns the active gate**
    - **Validates: Requirements 5.17, 5.18**

  - [ ]* 8.11 Write the jqwik test for Property 9, complete truthful arena status
    - Create a dedicated property class with at least 100 tries and the exact Property 9 tag/comment; generate prerequisite, registry, active, and endpoint-readiness states.
    - Assert every required field and permit a WebSocket URL iff an actual ready loopback endpoint exists.
    - **Property 9: Arena status projection is complete and does not invent endpoints**
    - **Validates: Requirements 5.2**

  - [ ]* 8.12 Write the jqwik test for Property 10, safe lossless Bot listing
    - Create a dedicated property class with at least 100 tries and the exact Property 10 tag/comment; generate registry descriptor states.
    - Assert one complete projection per registered Bot and no canonical absolute path exposure.
    - **Property 10: Bot listing is a lossless safe projection**
    - **Validates: Requirements 5.3**

  - [ ]* 8.13 Write the jqwik test for Property 11, safe lossless Bot inspection
    - Create a dedicated property class with at least 100 tries and the exact Property 11 tag/comment; generate metadata, source lists, build/run facts, and validation issues.
    - Assert field preservation using repository-relative paths and no absolute path or caller-executable input.
    - **Property 11: Bot inspection is a lossless safe projection**
    - **Validates: Requirements 5.4**

  - [ ]* 8.14 Write the jqwik test for Property 12, dual tool success representations
    - Create a dedicated property class with at least 100 tries and the exact Property 12 tag/comment; generate successful outputs for all four tools.
    - Assert a nonblank concise summary and JSON-compatible structured round-trip without required-value loss.
    - **Property 12: Every tool success has dual human and machine representations**
    - **Validates: Requirements 5.1**

  - [ ]* 8.15 Write the jqwik test for Property 13, immutable finite positive timeouts
    - Create a dedicated property class with at least 100 tries and the exact Property 13 tag/comment; generate positive, zero, negative, malformed, and effectively unbounded durations plus arbitrary accepted MCP requests.
    - Assert policy construction accepts only finite positive durations and requests cannot change them.
    - **Property 13: Timeout policy is finite, positive, and caller-immutable**
    - **Validates: Requirements 6.13, 6.14**

  - [ ]* 8.16 Write the jqwik test for Property 14, bounded captured output
    - Create a dedicated property class with at least 100 tries and the exact Property 14 tag/comment; generate chunked Unicode/line-boundary streams around positive limits.
    - Assert the retained content bound, truncation marker behavior, and absence of writes to MCP stdout.
    - **Property 14: Captured output is always bounded**
    - **Validates: Requirements 6.15**

  - [ ]* 8.17 Write the jqwik test for Property 15, configured-secret redaction
    - Create a dedicated property class with at least 100 tries and the exact Property 15 tag/comment; generate diagnostic text and non-empty secret sets including overlapping/Unicode values.
    - Assert emitted diagnostics contain no configured secret and preserve only a safe redaction indication.
    - **Property 15: Diagnostic redaction removes configured secrets**
    - **Validates: Requirements 6.11**

  - [ ]* 8.18 Write the jqwik test for Property 16, sanitized result-free boundary failures
    - Create a dedicated property class with at least 100 tries and the exact Property 16 tag/comment; generate every boundary failure category with hostile exception text containing path-, command-, environment-, secret-, and stack-like data.
    - Assert allowlisted actionable failure output with no unsafe details, successful results, or recording claim.
    - **Property 16: Boundary failures are sanitized and result-free**
    - **Validates: Requirements 7.3, 7.4, 7.5, 7.6, 7.7, 9.5**

  - [x] 8.19 Add focused MCP transport and lifecycle contract tests
    - Test exactly four discovered tools, strict schemas, structured/text success, no stdout contamination under deliberate diagnostics, runtime-only side effects, and bounded controlled-process cleanup on success, timeout, failure, and shutdown.
    - Use fakes only at application boundaries and never label these tests as genuine battle, Kiro, viewer, or smoke evidence.
    - _Requirements: 4.1–4.5, 5.1, 6.7–6.15, 7.7_

  - [x] 8.20 Implement and execute the real one-round integration smoke test
    - Add a dedicated Gradle smoke task/test that builds the application and both Bots, validates both through production code, invokes the production official Battle Runner adapter on loopback, launches both real processes, and runs exactly one round with finite timeouts.
    - Assert the verified official completion/provenance, exactly two ascending results, configured names/versions, required score mapping, optional real non-empty contained recording, and no surviving owned child processes; prohibit mocks, fixture results, and repeatability assertions.
    - Execute `./gradlew clean test` and the dedicated real smoke command; both must pass after the Stage 3 full path has worked.
    - _Requirements: 3.11, 7.1, 7.8, 9.6–9.13_

  - [x] 8.21 Record the Stage 4 hardening/test evidence gate
    - Add the `STATUS.md` Stage 4 entry with exact clean test and real smoke commands and exit codes, property/unit/contract counts, real completion/result/recording/cleanup observations, changed files, remaining failures, and any skipped optional property task labeled `not verified`.
    - Record any newly verified safety/lifecycle decision in `DECISIONS.md`; do not rewrite prior decisions based only on mocks.
    - Mark Stage 4 complete only when the mandatory focused tests and real smoke pass; otherwise block Stage 5.
    - _Requirements: 1.1, 1.4, 1.7–1.12, 9.1–9.13_

- [x] 9. Stage 5 — make the demonstrated path reproducible and submission-ready
  - [x] 9.1 Finalize the public README and executable setup instructions
    - Document the pitch, MCP purpose, architecture, Java/Gradle prerequisites, pinned setup/build, direct battle proof, Kiro configuration, four tool examples, actual viewer flow, replay fallback, test/smoke commands, troubleshooting, limitations, licenses/attribution, and warning that Bots execute local code with user permissions.
    - Include only commands with corresponding successful `STATUS.md` evidence and no stretch-feature claims.
    - _Requirements: 6.12, 10.1, 10.2, 12.1_

  - [x] 9.2 Harden repository-relative release configuration and tracked-file hygiene
    - Finalize `.gitignore`, license/attribution files, Gradle Wrapper/build files, `.kiro/settings/mcp.json`, and any finite release-verification script so source/tests/config remain tracked while credentials, `.env`, binaries, build outputs, logs, results, recordings, databases, and machine-specific absolute paths do not.
    - Add reproducible commands/checks for a fresh-checkout-like build, two-Bot validation, direct real battle, MCP client proof, focused tests, and real smoke without automating Kiro, viewer, publication, or video claims.
    - _Requirements: 10.3, 10.4, 10.5, 10.7, 10.8_

  - [x] 9.3 Prepare `SUBMISSION.md` and the final claim matrix for human-supplied links
    - Structure `SUBMISSION.md` for the public repository and video URLs and list separate evidence states for Gradle build, two-Bot validation, real one-round battle, genuine scores, MCP discovery, Kiro connection, live viewer, replay creation/playback, demo recording, and clean tracked-file review.
    - Keep absent links and unexercised claims explicitly `not verified`; do not invent publication, accessibility, deadline, or video-duration evidence.
    - _Requirements: 10.6, 10.11, 11.1–11.8_

- [-] 10. Human verification checkpoint — Stage 5 fresh-checkout, video, and publication
  - From a fresh-checkout-like state, manually follow the README, verify the repository-relative Kiro launcher, inspect tracked files for secrets/generated artifacts/absolute paths, and record each exercised final claim separately.
  - Recheck the official challenge terms/deadline, record a video of at most three minutes showing connected Kiro, `list_bots`, `inspect_bot`, `run_battle`, visual proof, and genuine score components, then publish both the repository and video and verify their public accessibility.
  - Provide the actual URLs and observations to Tasks 11.1–11.2. These actions are human verification checkpoints and must not be represented as completed by automated tests.

- [-] 11. Stage 5 — persist publication and final evidence
  - [-] 11.1 Complete `SUBMISSION.md` with verified public artifacts
    - Insert the human-verified public repository and video URLs, record the observed duration and accessibility checks, and preserve `not verified` for any claim not actually exercised.
    - Ensure the submission describes only the demonstrated four-tool synchronous MVP and its live-view or replay proof.
    - _Requirements: 10.6, 10.9, 10.10, 10.11, 11.1–11.8, 12.1, 12.2_

  - [-] 11.2 Write the Stage 5 exit and final claim evidence in `STATUS.md`
    - Add exact fresh-checkout/build/direct/MCP/test/smoke/hygiene commands with every exit code, changed files, observed Kiro/visual/publication/video outcomes, remaining failures, and every unexercised claim labeled `not verified`.
    - State separately whether Gradle build, two-Bot validation, real one-round battle, genuine scores, MCP discovery, Kiro connection, live viewer, replay creation and playback, demo recording, and clean tracked-file review were exercised.
    - Mark Stage 5 complete only when README reproducibility, repository hygiene, public repository, accessible ≤3-minute video, and submission links are all verified.
    - _Requirements: 1.1, 1.4, 1.6–1.12, 10.1–10.11, 11.1–11.8_

## Notes

- Tasks marked with `*` are optional property-test implementation tasks for the one-day MVP; they remain individually traceable and schedulable. Mandatory Stage 4 unit/contract/smoke tasks still cover the acceptance-critical safety and genuine-integration checks.
- Every stage evidence task is a hard gate. It must be completed last in its stage and must remain incomplete when required evidence is absent.
- Human checkpoints are intentionally excluded from automated execution metadata. Their following evidence leaf cannot be completed until the reviewer supplies the observations.
- Test doubles are permitted only in Stage 4 unit/property/contract tests. They never satisfy direct battle, MCP client proof, real smoke, Kiro, viewer, replay-playback, video, or publication evidence.
- No task adds leaderboards, ratings, remote matchmaking/downloads/imports/uploads, containers, sandboxing claims, a custom viewer, battle controls, per-turn telemetry, intent diagnostics, browser automation, hooks, tournaments, simultaneous battles, SQLite, a feature-parity CLI, or unproven async job tools.

## Task Dependency Graph

```json
{
  "waves": [
    { "id": 0, "tasks": ["1.1"] },
    { "id": 1, "tasks": ["1.2"] },
    { "id": 2, "tasks": ["1.3"] },
    { "id": 3, "tasks": ["1.4"] },
    { "id": 4, "tasks": ["3.1", "3.2"] },
    { "id": 5, "tasks": ["3.3"] },
    { "id": 6, "tasks": ["3.4"] },
    { "id": 7, "tasks": ["3.5"] },
    { "id": 8, "tasks": ["3.6"] },
    { "id": 9, "tasks": ["4.1"] },
    { "id": 10, "tasks": ["4.2"] },
    { "id": 11, "tasks": ["4.3"] },
    { "id": 12, "tasks": ["4.4"] },
    { "id": 13, "tasks": ["4.5"] },
    { "id": 14, "tasks": ["5.1"] },
    { "id": 15, "tasks": ["7.1"] },
    { "id": 16, "tasks": ["8.1"] },
    { "id": 17, "tasks": ["8.2", "8.3", "8.4", "8.5", "8.6", "8.7", "8.8", "8.9", "8.10", "8.11", "8.12", "8.13", "8.14", "8.15", "8.16", "8.17", "8.18", "8.19"] },
    { "id": 18, "tasks": ["8.20"] },
    { "id": 19, "tasks": ["8.21"] },
    { "id": 20, "tasks": ["9.1", "9.2", "9.3"] },
    { "id": 21, "tasks": ["11.1"] },
    { "id": 22, "tasks": ["11.2"] }
  ]
}
```
