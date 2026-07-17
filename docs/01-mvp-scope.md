# MVP scope

## Must ship

- custom Java MCP server;
- official Battle Runner integration;
- two real bundled bots;
- four core MCP tools;
- genuine scores;
- Kiro workspace configuration;
- live passive viewer or real replay fallback;
- focused tests;
- public README;
- mandatory demo video.

## Should ship when straightforward

- battle recording;
- one small result JSON file;
- a clean diagnostic Gradle task;
- a second before/after battle in the demo.

## Must not delay submission

- asynchronous job system;
- SQLite;
- leaderboards;
- ratings;
- multiplayer service;
- remote bot downloads;
- arbitrary bot import;
- custom viewer;
- containers;
- detailed telemetry;
- hooks;
- tournaments.

## Product statement

Kiro is the tank programmer and coach, not a turn-by-turn joystick. The bot code
reacts during battle; Kiro operates between battles by editing strategy, launching
matches, reading results, and improving the code.
