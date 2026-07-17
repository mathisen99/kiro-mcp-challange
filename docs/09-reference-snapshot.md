# External reference snapshot

Snapshot date: 2026-07-17.

This is a starting point, not a substitute for Stage 0 verification.

## Kiro

- Workspace MCP configuration: `.kiro/settings/mcp.json`
- Supported local server fields include `command`, `args`, `env`, `disabled`, and
  IDE `autoApprove`.
- Save/reload behavior and exact launcher working directory must be tested in the
  installed Kiro version.

Official documentation:

- https://kiro.dev/docs/mcp/configuration/
- https://kiro.dev/docs/mcp/
- https://kiro.dev/docs/hooks/types/

## Robocode Tank Royale

- Upstream repository `VERSION` reported `1.0.2` on the snapshot date.
- The upstream GitHub repository showed release `1.0.2` as latest.
- Battle Runner remains JVM-only.
- Battle Runner supports embedded servers, synchronous and asynchronous battles,
  real events, real results, and `.battle.gz` recording.

Official references:

- https://robocode.dev/
- https://robocode.dev/api/battle-runner.html
- https://github.com/robocode-dev/tank-royale
- https://raw.githubusercontent.com/robocode-dev/tank-royale/main/VERSION

## MCP Java SDK

- Upstream latest release reported `v2.0.0`.
- The core SDK includes stdio server transport.
- Verify the exact Gradle dependency form against the current official quickstart.

Official references:

- https://java.sdk.modelcontextprotocol.io/latest/quickstart/
- https://java.sdk.modelcontextprotocol.io/latest/server/
- https://github.com/modelcontextprotocol/java-sdk/releases/latest

## Passive viewer

- Hosted static viewer is available from the viewer repository's GitHub Pages link.
- It is a passive observer and does not control battles.
- Default connection is `ws://localhost:7654`.
- No local Node.js installation is needed when using the hosted build.

Reference:

- https://github.com/jandurovec/tank-royale-viewer

## Verification rule

Do not copy version numbers into build files until Gradle resolves the exact
Battle Runner, Bot API, and MCP SDK artifacts successfully.
