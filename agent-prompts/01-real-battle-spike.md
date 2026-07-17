# Stage 0 or Stage 1 prompt: establish the real battle path

Implement only the next incomplete stage: Stage 0 or Stage 1. Never complete both
in one run, because Stage 0 evidence must be reviewed before Stage 1 begins.

If Stage 0 is incomplete, verify the host, current official dependency versions,
and current Java bot sample structure. Create the Gradle Wrapper and minimal Java
project, resolve dependencies, build a trivial application, record exact resolved
versions in `DECISIONS.md`, update `STATUS.md`, and stop.

Only when `STATUS.md` already contains reviewed successful Stage 0 evidence, add
two small Java Tank Royale bots and the smallest Battle Runner spike that runs one
real one-round battle with an embedded local server. Print genuine ordered rankings
and score components. Recording is optional until the battle works.

Do not implement MCP yet. Do not mock the Battle Runner. Do not add a database,
leaderboard, web app, or custom viewer.

For Stage 1, run the real battle and update `STATUS.md` with exact commands, exit
codes, observed bot connections, and returned scores. Do not claim success unless
the command actually completed.
