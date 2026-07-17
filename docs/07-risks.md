# Risk register

| Risk | Impact | Mitigation | Cut decision |
|---|---|---|---|
| Battle Runner artifact/API mismatch | Blocks core | Verify dependency resolution and run official example first | Stop all higher-level work |
| Bot configuration mismatch | Bots do not connect | Copy current official Java sample structure | Do not invent config |
| stdout logging corrupts MCP | Kiro cannot connect | Route all MCP-mode logs to stderr | Treat as release blocker |
| Battle exceeds tool timeout | Tool fails | Start with one round; add async only after observing timeout | Cut persistence first |
| Viewer URL or port mismatch | Viewer cannot connect | Return and verify the actual loopback WebSocket URL before battle | Use replay fallback |
| Viewer unavailable or battle too fast to observe | Visual demo blocked | Keep real headless battle and recording working | Play the same battle recording in the official GUI |
| Bot output or battle hangs | MCP call exhausts resources | Bound captured output and enforce finite connection and wall-clock timeouts | Return a sanitized failure |
| Scope expansion | Missed submission | Enforce stage exits and cut list | No leaderboard/hooks |
| Generated files committed | Public repo hygiene failure | Ignore `runtime/`, inspect tracked files | Block publication |
| Secret or personal path shown | Security/privacy issue | Use env examples and repo-relative paths | Re-record demo |
| Video left too late | Invalid submission | Reserve Stage 5 time before stretch work | Drop all stretch features |
