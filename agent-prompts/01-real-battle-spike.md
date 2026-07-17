# Stage 0–1 prompt: prove the real battle

Implement only Stage 0 and Stage 1.

First verify the host, current official dependency versions, and current Java bot
sample structure. Create the Gradle Wrapper and minimal Java project. Record exact
resolved versions in `DECISIONS.md`.

Then add two small Java Tank Royale bots and the smallest Battle Runner spike that
runs one real one-round battle with an embedded local server. Print genuine ordered
rankings and score components. Recording is optional until the battle works.

Do not implement MCP yet. Do not mock the Battle Runner. Do not add a database,
leaderboard, web app, or custom viewer.

Run the real battle. Update `STATUS.md` with exact commands, exit codes, observed
bot connections, and returned scores. Do not claim success unless the command
actually completed.
