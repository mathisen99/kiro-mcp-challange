# Kiro Royale

Kiro Royale is a planned custom MCP server that lets Kiro launch and analyze **real
Robocode Tank Royale battles**.

> **Current repository state:** implementation scaffold only. The documentation,
> approved requirements, stage prompts, and safety constraints are ready; Kiro still
> needs to create and approve the design and task plan. The Java application, bots,
> Gradle Wrapper, and verified MCP launcher have not been implemented.

## The demo goal

Kiro should be able to:

1. discover two bundled bots;
2. inspect the editable `kiro-bot`;
3. launch a real battle through a custom MCP tool;
4. show the battle in a passive browser viewer;
5. receive genuine Robocode rankings and score components;
6. modify the bot and fight again.

## Start here

Read in this order:

1. [`AGENTS.md`](AGENTS.md)
2. [`KIRO_ROYALE_AGENT_BUILD_BRIEF.md`](KIRO_ROYALE_AGENT_BUILD_BRIEF.md)
3. [`.kiro/specs/kiro-royale/requirements.md`](.kiro/specs/kiro-royale/requirements.md)
4. [`STATUS.md`](STATUS.md)
5. [`DECISIONS.md`](DECISIONS.md)

Next, continue the requirements-first workflow in Kiro to create and review
`design.md`, followed by `tasks.md`. Use the stage prompts only after those files
exist and agree with the approved requirements.

Then assign one stage at a time to a coding agent. The first technical milestone is
a direct, real Battle Runner match—before MCP abstractions.

## Planned stack

- Java 21
- Gradle Wrapper
- official MCP Java SDK over stdio
- official Robocode Tank Royale Battle Runner
- two Java Tank Royale bots
- passive browser viewer
- JUnit for focused tests

The implementation agent must verify and pin current dependency versions before
creating build files.

## Repository map

- `.kiro/` — disabled workspace MCP starter configuration and Kiro specification
- `agent-prompts/` — copy-paste prompts for each implementation stage
- `bots/` — placeholders for the two bundled bots
- `docs/` — architecture, scope, risks, testing, demo, and submission guidance
- `src/` — Java source placeholders
- `runtime/` — ignored generated output
- `STATUS.md` — evidence log and current state
- `DECISIONS.md` — version and architecture decisions
- `SUBMISSION.md` — final submission record

## Before publishing

- choose and add a project license;
- verify every setup command from a clean checkout;
- set the MCP entry to `disabled: false`;
- ensure runtime output is untracked;
- record the mandatory demo video;
- complete the submission checklist.

## Suggested first implementation prompt

After Kiro creates and approves `design.md` and `tasks.md`, use
[`agent-prompts/00-orchestrator.md`](agent-prompts/00-orchestrator.md). It must
complete only the next incomplete stage and stop for evidence review.
