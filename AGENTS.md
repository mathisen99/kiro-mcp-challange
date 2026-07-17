# Instructions for coding agents

## Objective

Implement Kiro Royale as a one-day MVP. The only valid proof is a custom MCP server
that launches a **real** Robocode Tank Royale battle and returns genuine results.

## Required reading

Before editing code, read:

1. `KIRO_ROYALE_AGENT_BUILD_BRIEF.md`
2. `.kiro/specs/kiro-royale/requirements.md`
3. `.kiro/specs/kiro-royale/design.md`
4. `.kiro/specs/kiro-royale/tasks.md`
5. `STATUS.md`
6. `DECISIONS.md`

## Operating rules

- Inspect the current repository before changing it.
- Work through the numbered stages in order.
- Prove one direct Battle Runner match before implementing MCP.
- Verify current official documentation and dependency versions.
- Pin resolved versions only after dependency resolution succeeds.
- Keep one JVM application unless a documented blocker requires another design.
- Never replace real battle execution with fake, random, or hardcoded data.
- Mocks are allowed only for focused unit tests.
- Reserve stdout exclusively for MCP protocol traffic in MCP mode.
- Send logs to stderr or ignored log files.
- Use repository-relative paths.
- Keep generated files under ignored `runtime/`.
- Never accept arbitrary filesystem paths or executable commands from MCP inputs.
- Bind network services to loopback by default.
- Do not commit secrets, downloaded binaries, build output, recordings, or local databases.
- Avoid scope expansion until the end-to-end demo works.
- Do not implement leaderboard, remote bot import, custom viewer, or tournament features for the MVP.

## Required evidence

After each stage, update `STATUS.md` with:

- exact commands executed;
- exit codes;
- files changed;
- observed output;
- remaining failures;
- any claim that is still unverified.

Do not mark a task complete based only on code inspection.

## Final verification standard

The final report must state separately whether each item was actually exercised:

- Gradle build;
- two bot validation;
- real one-round battle;
- real scores;
- MCP tool discovery;
- Kiro MCP connection;
- live viewer;
- replay creation;
- demo recording;
- clean tracked-file review.

Use “not verified” rather than guessing.
