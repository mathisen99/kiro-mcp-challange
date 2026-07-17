# Third-party notices

Kiro Royale integrates with, builds against, or documents the following third-party projects. Their names and trademarks remain the property of their respective owners. This repository does not claim affiliation or endorsement.

| Component | Pinned/used version | Role | License/source |
|---|---:|---|---|
| Model Context Protocol Java SDK | `2.0.0` | stdio MCP server and finite client proof | [MIT License and source](https://github.com/modelcontextprotocol/java-sdk/tree/v2.0.0) |
| Robocode Tank Royale Battle Runner and Java Bot API | `1.0.2` | official server, battle orchestration, Bot API, results, and recordings | [Apache License 2.0 and source](https://github.com/robocode-dev/tank-royale/tree/v1.0.2) |
| Robocode Tank Royale Java sample Bots | `1.0.2` reference | configuration/launcher/API patterns used when authoring the two repository Bots; the local strategies and identities were changed | [Apache License 2.0 source](https://github.com/robocode-dev/tank-royale/tree/v1.0.2/sample-bots/java) |
| Gradle Wrapper / Gradle | `9.6.1` | reproducible build bootstrap | [Apache License 2.0 source](https://github.com/gradle/gradle/tree/v9.6.1) |
| JUnit 5 | BOM `5.13.4` | example and contract tests | [Eclipse Public License 2.0 source](https://github.com/junit-team/junit5/tree/r5.13.4) |
| jqwik | `1.9.3` | pinned property-testing dependency; optional property tasks were not implemented | [Eclipse Public License 2.0 source](https://github.com/jqwik-team/jqwik/tree/1.9.3) |
| Tank Royale GUI | `1.0.2` used manually, not bundled | demonstrated official replay playback | [official Apache-2.0 release](https://github.com/robocode-dev/tank-royale/releases/tag/v1.0.2) |
| Tank Royale Viewer | not bundled; compatibility not verified | documented third-party passive live-view option only | [upstream project and its license](https://github.com/jandurovec/tank-royale-viewer) |

Downloaded Gradle distributions, Maven artifacts, the Tank Royale GUI, official server extraction, compiled Bots, and recordings are generated/downloaded locally and excluded from the tracked repository. The Gradle Wrapper bootstrap JAR is the deliberate tracked binary exception.

Kiro Royale's original project code is provided under the repository's [MIT License](LICENSE). Third-party components and adapted material remain subject to their respective licenses above.

Content was rephrased for compliance with licensing restrictions. Consult each linked upstream license and release for authoritative terms before redistribution.
