# Kiro Royale

![Kiro Royale: an AI-authored tank entering a digital battle arena](assets/kiro-royale-hero.webp)

**Kiro programs a Robocode bot, compiles its strategy, and immediately tests it in a genuine
Tank Royale battle.**

Kiro Royale is a custom Java MCP server that turns an LLM-authored source edit into a real match:

> **program → compile → battle → watch → improve**

Kiro edits the registered `kiro-bot` with its normal workspace tools. The MCP server then compiles
that exact source, runs it against the fixed sample opponent through the official Robocode Tank
Royale Battle Runner, opens a live viewer when requested, and returns genuine scores plus the
SHA-256 hash of the source used. No winner or score is fabricated.

## Video demo

[Watch Kiro Royale program a bot and run a real Tank Royale battle on YouTube](https://www.youtube.com/watch?v=lcrK_C6V3F0).

## Demonstrated scope

The complete flow has been exercised through installed Kiro:

1. Kiro inspects `kiro-bot` and its primary editable Java source.
2. The current Kiro model writes a meaningful strategy change.
3. `run_battle` compiles both fixed registered sources with the local JDK.
4. The official Battle Runner launches two real Bot processes on loopback.
5. Firefox opens automatically and displays the live battle when `showBattle: true`.
6. The victory screen remains visible for five seconds.
7. MCP returns official rankings, score components, recording path, cleanup state, and source
   hashes so Kiro can evaluate the strategy and improve it again.

The demonstrated Kiro-authored Bot dealt real bullet damage in an official match, and its returned
source hash matched the edited file independently. Detailed commands, results, and evidence
boundaries are recorded in [STATUS.md](STATUS.md).

## Quick start

### Requirements

- Linux with `/usr/bin/systemd-socket-activate`
- JDK 21 (`java` and `javac`)
- a POSIX shell
- Kiro
- Firefox or another desktop browser for live viewing

Only Linux with Firefox has been verified. The committed Gradle Wrapper downloads the pinned build
and runtime dependencies on first use.

```sh
git clone https://github.com/mathisen99/kiro-mcp-challange.git
cd kiro-mcp-challange
./gradlew clean build installDist
```

Open the repository as a Kiro workspace. The committed
[`.kiro/settings/mcp.json`](.kiro/settings/mcp.json) starts the repository-relative launcher:

```json
{
  "mcpServers": {
    "kiro-royale": {
      "command": "./scripts/kiro-royale-mcp.sh",
      "args": [],
      "disabled": false,
      "autoApprove": ["get_arena_status", "list_bots", "inspect_bot"]
    }
  }
}
```

The three read-only tools are auto-approved. `run_battle` requires approval because it compiles and
executes local Bot code.

### Ask Kiro to program and battle

Use a prompt that explicitly asks for a source change—not merely a rerun:

> Inspect `kiro-bot`, design and implement a meaningful new strategy with the current Kiro model,
> and keep `sample-opponent` unchanged. Then run one recorded round with `showBattle: true`. Repair
> any `BOT_COMPILE_FAILED` diagnostics and explain the edit, source hash, and genuine scores.

The included [Bot-development steering](.kiro/steering/bot-development.md) guides Kiro through
inspect → edit → compile → battle → evaluate. A plain “run a battle” request intentionally runs the
current source without silently rewriting it.

## MCP tools

Kiro Royale exposes exactly four synchronous stdio tools. Every schema rejects extra properties.

| Tool | Purpose |
|---|---|
| `get_arena_status` | Report readiness, versions, active state, recording location, and blockers. |
| `list_bots` | List the two fixed registered Bots. |
| `inspect_bot` | Return safe metadata and the repository-relative primary editable source. |
| `run_battle` | Compile the current registered sources and run a genuine official battle. |

Example battle input:

```json
{
  "botIds": ["kiro-bot", "sample-opponent"],
  "rounds": 1,
  "record": true,
  "showBattle": true
}
```

`rounds` accepts `1`–`5` and defaults to `1`. `record` defaults to `true`; `showBattle` defaults to
`false`. A successful result includes:

- official completion provenance and actual loopback WebSocket URL;
- both ranks and official total, survival, bullet, ram, and first-place scores;
- rounds played, recording path, and process-cleanup state;
- `viewerRequested` / `viewerConnected` states;
- SHA-256 hashes for the exact Bot sources compiled for that match.

Compilation is restricted to the two application-registered source files. Callers cannot provide
source text, paths, commands, compiler flags, environment values, hosts, ports, URLs, or timeouts.
A compiler error returns bounded `BOT_COMPILE_FAILED` line/column diagnostics and starts no battle.

## How it works

```text
Kiro edits KiroBot.java
        │
        ▼
MCP over stdio ──► fixed-source Java 21 compiler ──► SHA-256 provenance
        │
        ▼
official Battle Runner ──► two real Bot processes on loopback
        │
        ├──► genuine scores returned to Kiro
        ├──► runtime/recordings/*.battle.gz
        └──► optional live browser viewer
```

The application uses one shared `BattleService` for direct diagnostics and MCP. MCP stdout is
reserved for JSON-RPC. Generated classes, dependencies, logs, recordings, and server files remain
under ignored build/runtime directories.

The verified stack is Java 21, Gradle 9.6.1, MCP Java SDK 2.0.0, and Tank Royale 1.0.2.

## Live viewer and replay

With `showBattle: true`, Kiro Royale opens the trusted hosted
[Tank Royale Viewer](https://github.com/jandurovec/tank-royale-viewer), waits briefly for its
loopback connection at `ws://localhost:7654`, starts the battle, and keeps the final result visible
for five seconds. If the viewer has a saved custom endpoint, reset it to `ws://localhost:7654`.

Recorded battles are stored beneath ignored `runtime/recordings/`. They can be opened through
**Battle → Replay from File** in the matching official Tank Royale GUI. Replay is the fallback when
a desktop viewer is unavailable.

## Verification

```sh
./gradlew clean test       # 24 focused and contract tests
./gradlew directBattle     # genuine official battle without MCP
./gradlew mcpProof         # all four tools plus a genuine MCP battle
./gradlew mcpViewerProof   # genuine MCP battle with live viewer
./gradlew realSmoke        # production integration smoke test
./scripts/verify-release.sh
```

The release script performs repository hygiene checks, clean build/test, and three genuine
headless battles. It does not automate Kiro, visual observation, publication, or video claims.

## Security and limits

> **Bot source executes locally with your user permissions. Review Kiro's edit before approving a
> battle. Kiro Royale is not a sandbox.**

- Exactly two fixed local Bots, four MCP tools, one active battle, and 1–5 rounds.
- Network services bind to loopback; MCP callers cannot choose network or process settings.
- No remote Bot import, arbitrary shell execution, custom viewer, async jobs, tournament,
  leaderboard, database, telemetry, or deterministic-result claim.
- Linux socket activation and Firefox are the verified path; Windows, macOS, other browsers, and
  headless live viewing are not verified.
- The Kiro Bot is intentionally editable, not claimed to be competitively sophisticated.

If the viewer is unavailable, confirm that `/usr/bin/xdg-open` can reach your desktop session and
that local port `7654` is free. After rebuilding, reconnect the MCP server in Kiro so it uses the
latest installed distribution. See [STATUS.md](STATUS.md) for deeper troubleshooting and exact
evidence.

## License

Kiro Royale is released under the [MIT License](LICENSE), copyright 2026 Tommy Mathisen.
Third-party projects retain their own licenses and trademarks; see
[THIRD_PARTY_NOTICES.md](THIRD_PARTY_NOTICES.md).
