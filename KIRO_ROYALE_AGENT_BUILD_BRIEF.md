# Kiro Royale — One-Day MVP Agent Build Brief

> Give this document, together with `AGENTS.md`, to Kiro, Codex, or another coding agent.
> The repository begins as a documentation scaffold. Implement the project autonomously,
> verify all external APIs before coding against them, and keep an evidence-based status log.

## 1. Mission

Build **Kiro Royale**, a custom Model Context Protocol (MCP) server that connects Kiro to
**real Robocode Tank Royale battles**.

The end-to-end experience must be:

1. Kiro inspects an editable tank bot.
2. Kiro modifies its strategy using normal repository editing tools.
3. Kiro calls the custom MCP server.
4. The MCP server launches a real Tank Royale battle through the official Battle Runner API.
5. A passive browser viewer shows the live battle, or a real replay is produced as fallback.
6. The MCP tool returns genuine rankings and score components.
7. Kiro explains the result and can improve the bot for another battle.

This must be a real integration. Hardcoded, random, or fabricated battle results do not count.

## 2. Challenge compliance

The final submission must contain:

- a custom MCP server authored in this repository;
- a meaningful project that depends on the MCP integration;
- a connection to a real external system;
- a public GitHub repository;
- the MCP server source;
- a committed `.kiro` folder;
- a README explaining the integration, setup, and project;
- no committed credentials or secrets;
- a demo video aiming for 30–60 seconds and not exceeding 3 minutes;
- an end-to-end demonstration from Kiro tool call through real battle output.

The demo video is mandatory. Reserve time for it.

## 3. One-day product decision

Implement a **single JVM application** unless a verified blocker makes that impossible.

Preferred stack:

- Java 21;
- Gradle Wrapper committed to the repository;
- official MCP Java SDK using stdio transport;
- official Robocode Tank Royale Battle Runner;
- two Java Tank Royale bots;
- lightweight JSON result files under ignored `runtime/`;
- JUnit for a few focused tests.

Do not introduce a Python orchestration layer, database, web backend, or custom viewer for the MVP.

### Why one JVM

A single process avoids:

- a Java/Python bridge protocol;
- cross-language packaging;
- additional subprocess lifecycle code;
- duplicated models and error handling;
- extra setup during the demo.

## 4. Critical implementation order

The project must preserve a working vertical slice.

### Stage 0 — verify the environment

Before writing application abstractions:

- inspect the repository;
- check `java -version`;
- verify Java 21 is usable;
- verify the current Robocode Tank Royale release and Battle Runner coordinates;
- verify the current MCP Java SDK release and stdio API;
- record resolved versions and links in `DECISIONS.md`;
- create and commit the Gradle Wrapper;
- do not assume version numbers from this document are still current.

Exit condition: dependencies resolve and a trivial Java application builds.

### Stage 1 — prove one real battle

Create two bundled bots based on current official Java examples:

- `kiro-bot`, deliberately simple and easy for an agent to edit;
- `sample-opponent`, predictable but functional.

Then write the smallest direct Battle Runner spike that:

- starts an embedded Tank Royale server;
- binds locally;
- runs one real one-round battle;
- launches both real bot processes;
- prints ordered final results;
- returns at least rank, name, version, total score, survival, bullet damage,
  ram damage, and first places;
- optionally records a `.battle.gz` replay.

Do not build MCP abstractions until this works.

Exit condition: a command produces real results for two bots without mocks.

### Stage 2 — wrap the proven battle in MCP

Implement a stdio MCP server in the same JVM application.

Important stdio rule:

- stdout is reserved for MCP JSON-RPC;
- all diagnostics and logs go to stderr or files;
- never print ordinary status text to stdout while in MCP mode.

Implement only these tools initially:

1. `get_arena_status`
2. `list_bots`
3. `inspect_bot`
4. `run_battle`

Use synchronous `run_battle` first. Limit battles to 1–5 rounds. If an actual Kiro
tool timeout is observed, replace it with asynchronous execution and add
`get_battle_status` plus `get_battle_result`. Do not pre-build job infrastructure
without evidence that it is needed.

Exit condition: an MCP client can list tools and run a real battle.

### Stage 3 — connect Kiro and the viewer

- verify the actual Gradle task or launcher used for MCP stdio;
- update `.kiro/settings/mcp.json`;
- set `disabled` to `false` only after the launcher works from the repository root;
- auto-approve only read-only tools;
- verify the server appears in Kiro;
- call every MCP tool from Kiro;
- open the hosted passive Tank Royale viewer;
- connect it to the local WebSocket server, normally `ws://localhost:7654`;
- run a battle from Kiro and confirm it appears in the viewer.

Exit condition: Kiro starts a real visible battle and receives real results.

### Stage 4 — harden only the demonstrated path

Add proportionate safeguards:

- bot IDs map only to configured directories;
- canonical bot paths remain under the repository bot root;
- rounds are bounded;
- no arbitrary executable command is accepted from MCP inputs;
- local services bind to loopback;
- process failures are returned as failures;
- child processes are cleaned up;
- generated files go under ignored `runtime/`;
- logs do not contain secrets;
- tool errors are actionable and do not expose stack traces by default.

Add only a few tests:

- path containment;
- invalid round bounds;
- unknown bot ID;
- one real integration smoke test.

Exit condition: focused tests and the real smoke test pass.

### Stage 5 — document, record, and submit

- finish the public README;
- include exact setup and verification commands;
- include architecture and MCP tool examples;
- include viewer instructions and attribution;
- include a warning that bot code is executable and untrusted;
- verify `.kiro/settings/mcp.json` works from a fresh checkout-like state;
- run a secret scan or inspect tracked files;
- record the demo video;
- publish the public repository;
- complete `SUBMISSION.md`.

Exit condition: repository and video satisfy every challenge requirement.

## 5. MVP MCP contracts

Tool responses should contain concise text and structured data where supported.
Never fabricate success when Robocode fails.

### `get_arena_status`

Return:

- application version;
- Java runtime version;
- resolved Robocode version;
- bot root;
- discovered bot count;
- whether a battle is currently executing;
- WebSocket URL;
- viewer URL or instructions;
- recording directory;
- any blocking prerequisite failure.

This tool is read-only.

### `list_bots`

Return each registered bot:

- stable bot ID;
- name;
- version;
- relative directory;
- language;
- validation status;
- source label.

Never recursively scan outside configured bot roots.

### `inspect_bot`

Input:

- registered bot ID.

Return:

- metadata;
- relative source file names;
- main editable source file;
- build/run information from the bot configuration;
- validation issues.

Do not accept arbitrary filesystem paths.

### `run_battle`

Inputs:

- exactly two registered bot IDs for the MVP;
- rounds, default 1, minimum 1, maximum 5;
- record, default true.

Behavior:

- validate both bots;
- reject unknown IDs and unsafe paths;
- start the real Battle Runner;
- run a real battle;
- return real results ordered by rank;
- include the recording path when present;
- return an explicit failure when Robocode fails.

Minimum result fields:

- rank;
- name;
- version;
- total score;
- survival score;
- bullet damage;
- ram damage;
- first places;
- rounds played.

## 6. Architecture

```text
Kiro / coding agent
        |
        | MCP over stdio
        v
Java MCP server
        |
        +---- safe bot registry
        +---- Battle Runner service
        +---- lightweight runtime result files
        |
        v
Official Robocode Tank Royale Battle Runner
        |
        +---- embedded local server
        +---- starts real bot processes
        +---- returns real scores
        +---- optionally writes .battle.gz
        |
        v
Local WebSocket server --------------> passive browser viewer
```

The viewer observes. The Battle Runner controls the match.

## 7. Initial repository structure

```text
kiro-royale/
├── .kiro/
│   ├── settings/mcp.json
│   ├── specs/kiro-royale/
│   │   ├── requirements.md
│   │   ├── design.md
│   │   └── tasks.md
│   └── steering/kiro-royale.md
├── agent-prompts/
├── bots/
│   ├── kiro-bot/
│   └── sample-opponent/
├── docs/
├── runtime/                  # ignored except .gitkeep
├── src/main/java/dev/kiro/royale/
├── src/test/java/dev/kiro/royale/
├── .env.example
├── .gitignore
├── AGENTS.md
├── DECISIONS.md
├── STATUS.md
├── SUBMISSION.md
├── README.md
└── KIRO_ROYALE_AGENT_BUILD_BRIEF.md
```

The implementation agent may adjust package-level layout, but must keep generated
state out of source directories.

## 8. Scope cuts

Do not implement these before the mandatory end-to-end demo works:

- SQLite;
- leaderboard or ratings;
- remote matchmaking;
- importing or executing arbitrary remote bots;
- a public upload service;
- containers or sandboxing;
- a custom viewer;
- pause/resume/step controls;
- detailed per-turn telemetry;
- intent diagnostics;
- automatic browser opening;
- background hooks;
- tournament brackets;
- multiple simultaneous battles;
- a separate CLI with complete feature parity.

A small human-facing diagnostic CLI or Gradle task is acceptable only when it
directly helps verification.

## 9. Optional stretch features, in order

After all acceptance criteria are proven:

1. asynchronous battle ID, status, and result tools;
2. compact round/death telemetry;
3. before/after result comparison;
4. `.battle.gz` replay handling;
5. disabled-by-default `PostTaskExec` background battle hook.

The hook must never block or fail the Kiro task that triggered it.

## 10. Security requirements

- Resolve and normalize bot paths.
- Require bot paths to remain under configured roots.
- Use Java process APIs with argument lists.
- Never expose a general shell or command-execution MCP tool.
- Bound rounds, participants, output, and timeouts.
- Bind local services to `127.0.0.1`.
- Do not clone or execute remote repositories automatically.
- Treat all imported bot programs as untrusted executable code.
- Keep credentials and machine-specific paths out of Git.
- Return sanitized errors to MCP clients.
- Clean up child processes on failure and shutdown.

## 11. Testing strategy

Testing is intentionally narrow.

### Unit tests

Test only code with meaningful local risk:

- path containment;
- registry lookup;
- round bounds;
- sanitized failures.

### Real smoke test

Provide one opt-in or normal integration test that:

1. builds the project;
2. validates both bundled bots;
3. runs a real one-round battle;
4. receives two ranked real results;
5. verifies the result was not produced by a fixture;
6. verifies a recording or genuine completion event when recording is enabled.

Mocks may be used in unit tests, but not as proof of project completion.

## 12. Demo script

Target 45–60 seconds:

1. Show Kiro with the MCP server connected.
2. Call `list_bots`.
3. Ask Kiro to inspect `kiro-bot`.
4. Call `run_battle` for one round.
5. Show the live viewer during the battle.
6. Show the real returned rankings and score components.
7. Optionally show one small strategy edit and a second battle.

Do not spend the demo on installation, code scrolling, or long test output.

## 13. MVP acceptance criteria

The MVP is complete only when:

- [ ] this repository contains a custom MCP server;
- [ ] the MCP server uses stdio and connects from Kiro;
- [ ] two bundled real Tank Royale bots are valid;
- [ ] `run_battle` launches the official Battle Runner;
- [ ] battle data is real, not hardcoded;
- [ ] final rankings and score components are returned;
- [ ] failures remain failures;
- [ ] the battle is visible live or a real replay is demonstrated;
- [ ] `.kiro/settings/mcp.json` is committed and functional;
- [ ] generated files and secrets are excluded from Git;
- [ ] the real smoke test passes;
- [ ] README setup is reproducible;
- [ ] a public GitHub repository exists;
- [ ] the demo video shows the complete integration;
- [ ] the demo video is no longer than 3 minutes.

## 14. Cut rule when time is running out

Cut in this order:

1. all stretch features;
2. replay polish;
3. result persistence;
4. extra unit tests;
5. fancy README presentation.

Never cut:

- the custom MCP server;
- the real Battle Runner call;
- two real bots;
- structured real results;
- Kiro connection;
- committed `.kiro` configuration;
- demo video;
- public repository.

## 15. Evidence requirements for the coding agent

At the end of every stage, update `STATUS.md` with:

- exact commands run;
- their exit status;
- what was manually observed;
- files changed;
- unresolved blockers;
- claims that are not yet verified.

Do not claim that live viewing, Kiro integration, recording, or background execution
works until each has actually been exercised.

## 16. Final instruction

Begin with the smallest real proof: two bots, one official Battle Runner match, and
parsed genuine results. Then expose that exact working path through MCP. Keep the
repository runnable, avoid platform-building, and reserve enough time to record and
submit the required video.
