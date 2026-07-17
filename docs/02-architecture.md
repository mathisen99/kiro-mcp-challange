# Architecture

## Context

Robocode Tank Royale uses WebSocket-based components. The official Battle Runner
lets a JVM application start and control battles programmatically.

## MVP architecture

```text
Kiro
  |
  | stdio MCP
  v
Kiro Royale Java process
  |
  +-- input validation
  +-- safe bot registry
  +-- Battle Runner lifecycle
  +-- result mapping
  |
  v
Managed local Tank Royale server process
  |
  +-- kiro-bot process
  +-- sample-opponent process
  +-- passive viewer observer
```

## Boundaries

### Authored here

- MCP server;
- tool contracts;
- bot registry;
- battle orchestration;
- result mapping;
- bundled bots;
- verification and documentation.

### External

- MCP Java SDK;
- Robocode Tank Royale;
- Battle Runner;
- Tank Royale Viewer.

The selected Tank Royale Viewer is a third-party passive observer. It is not part
of the custom MCP server and must not control battle execution. The actual
loopback WebSocket URL comes from the managed server configuration; do not assume
port 7654 in implementation code.

## Failure model

Failures are first-class results:

- missing Java or dependency;
- invalid bot config;
- bot failed to start;
- connection timeout;
- server port unavailable;
- battle aborted;
- battle wall-clock timeout;
- viewer unavailable;
- recording failure.

Do not convert these into successful fake results.

## Logging

MCP stdio is protocol-sensitive. In MCP mode:

- stdout: JSON-RPC only;
- stderr: diagnostic logs;
- `runtime/logs/`: optional detailed logs.

## Runtime output

Suggested ignored layout:

```text
runtime/
├── logs/
├── recordings/
└── results/
```

The repository `.gitignore` must keep generated contents under these directories
untracked while preserving `runtime/.gitkeep`.
