# Project Structure

## Authoritative project files

- `KIRO_ROYALE_AGENT_BUILD_BRIEF.md` — overall MVP constraints and staged execution brief.
- `.kiro/specs/kiro-royale/requirements.md` — approved behavior and acceptance criteria.
- `.kiro/specs/kiro-royale/design.md` — planned architecture and security boundaries.
- `.kiro/specs/kiro-royale/tasks.md` — strict Stage 0–5 implementation sequence.
- `STATUS.md` — evidence-backed implementation truth; update after every stage and label unexercised claims `not verified`.
- `DECISIONS.md` — accepted decisions and facts verified through execution; do not record guesses as resolved decisions.

Read these files before editing implementation code. If narrative files disagree, prefer approved requirements and the latest execution evidence over stale summaries.

## Directory responsibilities

```text
.kiro/settings/                 Workspace MCP launcher configuration
.kiro/specs/kiro-royale/       Requirements, design, and implementation tasks
.kiro/steering/                 Always-included AI project guidance
agent-prompts/                  Stage-oriented implementation prompts
docs/                           Planning, testing, security, demo, and submission guidance
src/main/java/dev/kiro/royale/  Main Java application (currently a placeholder)
src/test/java/dev/kiro/royale/  Focused Java tests (currently a placeholder)
bots/kiro-bot/                  Simple editable bundled bot
bots/sample-opponent/           Predictable bundled opponent
runtime/                        Ignored generated logs, results, and recordings
scripts/                        Transparent helpers for verified repeatable commands only
```

## Organization rules

- Keep one Java application with shared application services and separate direct-diagnostic and MCP entry adapters.
- Put internal models/services and ports inside the application core; keep filesystem, official SDK, process, and MCP concerns in boundary adapters.
- Keep each bundled bot self-contained under its fixed `bots/` directory and resolve it through a static registry. Never expose arbitrary filesystem traversal through MCP.
- Use repository-relative committed configuration. Generated artifacts belong under `runtime/`, not source or bot directories.
- Do not add stretch-feature directories or infrastructure before the real two-bot Battle Runner path and MCP flow work end to end.
- Follow the numbered tasks in order: prove the direct Battle Runner match before adding MCP, then verify Kiro/visual proof before hardening and submission work.