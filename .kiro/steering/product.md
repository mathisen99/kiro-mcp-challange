# Product

Kiro Royale is a one-day MVP that connects Kiro to genuine Robocode Tank Royale battles through a custom Model Context Protocol (MCP) server. It lets Kiro inspect two bundled Java bots, edit the simple `kiro-bot`, launch an official Battle Runner match, and receive genuine ranked score components. Battles should be visually verifiable through a passive live viewer or an official-GUI replay.

## MVP boundaries

- Expose exactly four initial MCP tools: `get_arena_status`, `list_bots`, `inspect_bot`, and synchronous `run_battle`.
- Run exactly two distinct registered bots for 1–5 rounds; use stable bot IDs rather than caller-provided paths or commands.
- Execute production battles only through the official Battle Runner with real bot processes. Never substitute fixtures, randomness, mocks, or hardcoded scores.
- Return ranks and score components only from official successful-completion data.
- Keep the MVP focused: no leaderboard, remote bot import, custom viewer, tournaments, database, concurrent battles, or asynchronous jobs without documented timeout evidence.

## Delivery rule

Work through Stage 0 → Stage 5 in order. A direct genuine battle must be proven before MCP is implemented. Advance only after `STATUS.md` records the prior stage's exact commands, exit codes, changed files, observations, failures, and explicitly unverified claims.