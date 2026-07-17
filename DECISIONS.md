# Decision log

Record decisions only after verification.

## ADR-001 — One JVM application

**Status:** accepted for the initial MVP.

**Decision:** implement the MCP server and Battle Runner orchestration in one Java
application.

**Reason:** reduces cross-language packaging, bridge protocols, and process-lifecycle
risk during a one-day challenge.

**Revisit when:** a proven incompatibility prevents the MCP Java SDK and Battle Runner
from coexisting.

## ADR-002 — Synchronous battle first

**Status:** accepted for the first vertical slice.

**Decision:** implement synchronous `run_battle` first with 1–5 rounds.

**Reason:** it avoids job storage, polling, and detached worker infrastructure.

**Revisit when:** an actual Kiro or MCP timeout is observed.

## ADR-003 — Passive hosted viewer

**Status:** provisional until Stage 3 compatibility is exercised.

**Decision:** attempt to use the existing third-party passive web viewer rather than
building or vendoring one. Use official-GUI replay playback if live compatibility
cannot be demonstrated.

**Reason:** the viewer is presentation infrastructure, not the custom integration.

**Verification required:** connect the viewer to the actual loopback WebSocket URL
before a Battle Runner match and observe the Kiro-triggered battle. Repository
availability alone does not prove compatibility.

## Version resolution record

Fill this in during Stage 0:

| Component | Resolved version | Verification command/source | Date |
|---|---:|---|---|
| Java | TODO | `java -version` | TODO |
| Gradle Wrapper | TODO | `./gradlew --version` | TODO |
| MCP Java SDK | TODO | dependency resolution | TODO |
| Tank Royale | TODO | upstream `VERSION` and dependency resolution | TODO |
| Battle Runner artifact | TODO | Gradle dependency resolution | TODO |
| Java Bot API | TODO | Gradle dependency resolution | TODO |

## Launcher decision

Final command used by `.kiro/settings/mcp.json`:

```text
TODO
```

Verification evidence:

```text
TODO
```
