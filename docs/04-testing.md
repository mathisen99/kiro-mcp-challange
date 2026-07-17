# Testing and verification

## Principle

Test in proportion to risk. The submission needs credible proof, not a large test
suite.

## Focused unit tests

1. A canonical bot path outside `bots/` is rejected.
2. Unknown bot IDs are rejected.
3. Rounds below 1 or above 5 are rejected.
4. Internal exceptions become sanitized tool errors.

## Real smoke test

The smoke test must use the real Battle Runner and real bundled bots.

Required assertions:

- two bots connect;
- a one-round battle completes;
- two ranked results are returned;
- names and versions match bot configuration;
- result data comes from the Battle Runner path;
- a real recording or completion event exists when configured.

## Manual verification

Record evidence for:

- viewer connection;
- Kiro MCP connection;
- MCP tools visible in Kiro;
- battle launched from Kiro;
- scores returned to Kiro.

## Avoid

- mock battle engines;
- snapshots of fabricated score JSON;
- extensive test matrices;
- browser automation;
- performance benchmarking;
- CI work before the local demo works.
