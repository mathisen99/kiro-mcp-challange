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

Forbidden:

- arbitrary paths;
- arbitrary commands;
- arbitrary environment variables;
- arbitrary network hosts;
- shell fragments;
- remote repository URLs.

## Local networking

Bind the embedded server to loopback when the API permits it. Do not expose the
battle server publicly for the demo.

## Secrets

The core project should not need secrets. Keep `.env.example` non-sensitive and
do not commit `.env`.
