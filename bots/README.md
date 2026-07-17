# Bundled bots

This directory will contain two reviewed Java Tank Royale bots.

## Rules for implementation

- Base configuration and launcher structure on current official Java samples.
- Do not invent a bot manifest format from memory.
- Keep every bot self-contained under its directory.
- Use repository-relative dependencies and commands.
- Make `kiro-bot` strategy easy to understand and edit.
- Make `sample-opponent` simple and deterministic enough for a demo.
- Do not commit compiled output.

Bot code is executable. Do not automatically download and run third-party bots.
