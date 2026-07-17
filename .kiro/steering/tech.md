# Technology

## Verified stack

- Java 21 in one JVM application.
- Gradle with a version-controlled Gradle Wrapper.
- Official Robocode Tank Royale Battle Runner and Java Bot API.
- Official MCP Java SDK using stdio transport.
- JUnit Platform for focused tests; optional jqwik property tasks remain deferred.
- Two bundled Java bots: `kiro-bot` and `sample-opponent`.

Pinned versions and verified API decisions are recorded in `DECISIONS.md`. Production uses MCP Java SDK `2.0.0`, Tank Royale `1.0.2`, Java 21, and Gradle Wrapper `9.6.1`.

## Architecture and coding conventions

- Keep direct diagnostic and MCP modes as outer adapters over the same composition root and battle service.
- Dependency direction is adapters → application services/models → ports; application code must not depend on MCP SDK or concrete Battle Runner types.
- Isolate external APIs in thin adapters such as the official runner and MCP stdio adapters.
- Allow only one active battle; use finite, caller-immutable timeouts and deterministic cleanup of application-owned child processes.
- Bind services to loopback. MCP input must never accept arbitrary paths, commands, environment variables, hosts, URLs, or repositories.
- Use argument-list process APIs and bound child-process output. In MCP mode, reserve stdout exclusively for JSON-RPC; send logs to stderr or ignored `runtime/` files.
- Use mocks only in focused tests, never as evidence of genuine battle completion.
- Keep generated logs, results, and recordings under ignored `runtime/`; do not commit secrets, downloads, binaries, build output, recordings, or databases.

## Commands

Run commands from the repository root; exact exercised results are recorded in `STATUS.md`:

```sh
java -version
./gradlew --version
./gradlew dependencies --configuration runtimeClasspath
./gradlew clean build
./gradlew clean test
```

The implemented verification tasks are `stage0Probe`, `directBattle`, `viewerBattle`, `mcpProof`,
`mcpViewerProof`, and `realSmoke`. Run them from the repository root and record exact commands and
exit codes in `STATUS.md`. `run_battle` recompiles only fixed registered Bot sources with the local
JDK before genuine execution and returns their SHA-256 hashes.
