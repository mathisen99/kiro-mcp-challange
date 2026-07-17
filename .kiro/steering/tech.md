# Technology

## Current state

This repository is an implementation scaffold. Requirements, design, and tasks are present, but there is currently no Gradle build, Wrapper, Java implementation, resolved dependency set, or verified build/test/run command. Treat versions and external API signatures as unresolved until Stage 0 proves them.

## Planned stack

- Java 21 in one JVM application.
- Gradle with a version-controlled Gradle Wrapper.
- Official Robocode Tank Royale Battle Runner and Java Bot API.
- Official MCP Java SDK using stdio transport.
- JUnit Platform; jqwik is planned for focused property tests.
- Two bundled Java bots: `kiro-bot` and `sample-opponent`.

Resolve current official artifacts and APIs through Gradle before implementation, then pin only versions that resolve successfully and record them in `DECISIONS.md`. Do not copy candidate versions from planning documents as verified facts.

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

Commands documented in `.kiro/specs/kiro-royale/tasks.md` are planned until their results appear in `STATUS.md`:

```sh
java -version
./gradlew --version
./gradlew dependencies --configuration runtimeClasspath
./gradlew clean build
./gradlew clean test
```

Stage-specific dependency probes, direct-battle runs, MCP client proofs, and real smoke-test task names must be added only after implementation and verification. Run commands from the repository root and record exact commands and exit codes in `STATUS.md`.