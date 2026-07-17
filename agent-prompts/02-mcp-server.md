# Stage 2 prompt: expose the proven battle through MCP

The direct real battle must already work and be recorded in `STATUS.md`.

Wrap that exact battle path in a custom Java MCP server using the current official
MCP Java SDK and stdio transport.

Implement only:

- `get_arena_status`;
- `list_bots`;
- `inspect_bot`;
- synchronous `run_battle`.

Use registered bot IDs, canonical path containment, exactly two bots, and 1–5
rounds. Return genuine Battle Runner results. Keep stdout exclusively for MCP
protocol messages and send diagnostics to stderr.

Verify tool discovery and invoke `run_battle` through an MCP client. Update
`STATUS.md` with evidence. Do not build async jobs unless a real timeout is observed.
