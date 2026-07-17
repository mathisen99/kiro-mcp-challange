# Stage 3 prompt: connect Kiro and the viewer

The MCP server must already launch a real battle through an MCP client.

Create and verify the repository-root launcher used by Kiro. Update
`.kiro/settings/mcp.json` so it matches the real command, then set `disabled` to
`false`.

Verify in the installed Kiro version that:

- the server connects;
- the four tools appear;
- read-only auto-approval is appropriate;
- Kiro can call each tool;
- Kiro can launch a real one-round battle.

Open the hosted passive Tank Royale viewer and connect it to the local WebSocket
server before the battle begins. Prove that the Kiro-triggered battle is visible.
If live viewing is unavailable, incompatible, blocked, or too late to observe the
battle, load and play the recording from that same battle in the official Tank
Royale GUI. File existence alone is not replay proof.

Update `STATUS.md` with exact evidence and any viewer limitation. Record the live
viewer as `not verified` when replay is used. Do not add hooks.
