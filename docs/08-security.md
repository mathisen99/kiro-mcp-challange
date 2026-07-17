# Security notes

## Bot code is executable

Tank Royale bots are programs. Running a bot executes code with the user's local
permissions.

For the MVP:

- run only the two bundled reviewed bots;
- do not automatically clone or execute remote repositories;
- do not advertise arbitrary bot imports as safe;
- document this warning in the final README.

## MCP input boundary

The MCP server should expose intent-level operations, not operating-system access.

Allowed:

- select a registered bot ID;
- select 1–5 rounds;
- request recording.

The `run_battle` schema accepts only `botIds`, `rounds`, and `record`, requires two
distinct registered bot IDs, and rejects additional properties.

Forbidden:

- arbitrary paths;
- arbitrary commands;
- arbitrary environment variables;
- arbitrary network hosts;
- shell fragments;
- remote repository URLs.

## Local networking

Bind the managed server to loopback. If the resolved API cannot guarantee a
loopback-only service, record the blocker and do not expose the server or claim the
security requirement is satisfied.

Use finite Bot connection and battle wall-clock timeouts that MCP callers cannot
override. Bound captured child-process output and always clean up processes after
success, failure, timeout, or shutdown.

## Secrets

The core project does not need secrets or an environment file. Do not commit
`.env`, credentials, tokens, or private keys.
