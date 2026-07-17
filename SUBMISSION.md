# Kiro Royale submission record

This file separates locally exercised evidence from publication/video claims that require the next human checkpoint. Do not replace `not verified` with a URL or check mark until the exact artifact has been opened and reviewed.

## Human-supplied public links

| Artifact | URL | Evidence state |
|---|---|---|
| Public repository | <https://github.com/mathisen99/kiro-mcp-challange> | **verified publicly accessible** — unauthenticated GitHub API returned HTTP `200` and `private=false` on 2026-07-17 |
| Demo video | `not provided` | **not verified** — recording, upload, public accessibility, and duration have not been exercised |
| Social post | `not provided` | **not verified** — must include the repository, video, description, `#BuildWithKiro`, `#TeamKiro`, and `@kirodotdev` |
| Challenge submission | `not provided` | **not verified** — the official form has not been submitted |

## Final claim matrix

| Claim | Evidence state | Evidence boundary |
|---|---|---|
| Gradle build | **verified from isolated snapshot** | `./gradlew clean build` passed from `/tmp/kiro-royale-final.dwNmAz` during the Stage 5 fresh-checkout-like run. |
| Two-Bot validation | **verified locally** | Production registry validation reported exactly `kiro-bot` and `sample-opponent` valid before direct, MCP, and smoke battles. |
| Real one-round battle | **verified locally** | Direct mode, MCP proof, Kiro, and real smoke observed official one-round completion with two real Bot processes. |
| Genuine scores | **verified locally** | Results carried `OFFICIAL_BATTLE_RUNNER_COMPLETION` and all required official score fields. Winner/score repeatability is not claimed. |
| MCP discovery | **verified locally** | Official Java MCP client discovered exactly `get_arena_status`, `list_bots`, `inspect_bot`, and `run_battle`. |
| Kiro MCP connection | **verified manually** | Installed Kiro displayed `kiro-royale` connected and invoked all four tools in Stage 3. The isolated snapshot also passed repository-relative `mcpProof`; installed Kiro was not reopened against the snapshot. |
| Live passive viewer | **verified on Firefox/Linux through installed Kiro** | `viewerBattle` and `mcpViewerProof` passed with `showBattle=true`. Installed Kiro then invoked the same field, automatically opened Firefox, returned `viewerConnected=true`, and the owner confirmed the genuine battle was visible without manually loading a replay. |
| Replay creation | **verified locally** | Direct, MCP, Kiro, and smoke paths created contained non-empty `.battle.gz` recordings when requested. |
| Replay playback | **verified manually** | The official Tank Royale GUI 1.0.2 loaded and played the exact Stage 3 Kiro-triggered recording. |
| Demo recording | **not verified** | No final demo video has been recorded or reviewed. |
| Video duration at most 3 minutes | **not verified** | No exported video exists to time. |
| Public accessibility | **partially verified** | Repository accessibility is verified; video and social-post accessibility remain **not verified**. |
| Current challenge deadline/terms | **verified on 2026-07-17** | Official terms state the challenge period ends July 17, 2026 at 23:59 PT. Daily entries close at 23:59 PT and require a public repo, committed `.kiro`, 30-second-to-3-minute demo, qualifying social post, and entry form. |
| Clean tracked-file review | **verified for isolated snapshot** | Release hygiene and targeted tracked-file review found no tracked build/runtime artifacts or credential signatures. Active configuration contains no developer-home path. Historical evidence and hostile test fixtures intentionally contain example absolute paths. |
| Project license selected | **verified locally** | Canonical MIT text is present in `LICENSE` for copyright 2026 Tommy Mathisen. Third-party attribution is recorded separately. |

## Submission contents

| Item | State |
|---|---|
| Custom stdio MCP server source | present and locally exercised |
| Meaningful official Robocode integration | locally exercised |
| `.kiro/settings/mcp.json` | present, enabled, repository-relative; isolated-snapshot MCP proof passed |
| Kiro spec/steering files | present |
| Public setup README | prepared; isolated-snapshot release flow passed |
| Gradle Wrapper, source, tests, pinned versions | present |
| Generated runtime/build output excluded | verified by isolated-snapshot release hygiene and tracked-file review |
| Selected project license | MIT License (`LICENSE`) |
| Public repository | publicly accessible at the URL above |
| Accessible demo video at most 3 minutes | **not verified** |

## Reproducible local verification

Run from the repository root:

```sh
./scripts/verify-release.sh
```

The finite script checks active release configuration/tracked-file patterns and runs these evidence-backed commands:

```sh
./gradlew clean build
./gradlew clean test
./gradlew clean directBattle
./gradlew mcpProof
./gradlew realSmoke
```

It covers a clean local build, both bundled Bots through production/test validation, direct genuine battle proof, official MCP client proof, focused tests, and the genuine smoke test. It deliberately does not automate or claim Kiro UI behavior, viewer/GUI playback, publication, challenge submission, or video recording.

## Remaining human checkpoint

1. Record a 30-second-to-3-minute demo showing connected Kiro, `list_bots`, `inspect_bot`, `run_battle`, the demonstrated replay proof, and genuine score fields.
2. Upload the video and verify it is publicly accessible.
3. Publish the required X or LinkedIn post with the repository/video links, short description, `#BuildWithKiro`, `#TeamKiro`, and `@kirodotdev`; verify the post publicly.
4. Submit the official challenge form before the applicable 23:59 PT daily deadline and record the final URLs above.
5. Record the observed video duration and final submission outcome in `STATUS.md`.

## Submission copy

### Short project description

Kiro Royale is a custom Java MCP server that lets Kiro inspect editable Tank Royale Bots and launch a genuine one-round Robocode battle through the official Battle Runner. It returns real rankings and score components, binds the local service to loopback, cleans up owned processes, and produces an official replay for visual proof.

### How Kiro was used

Kiro drove the repository through a requirements-first spec workflow and acted as the real MCP client for the finished integration. The project began by defining strict stages in `.kiro/specs/kiro-royale`: dependency verification, a direct official Battle Runner proof, MCP wrapping, installed-Kiro verification, focused hardening, and release evidence. Kiro then connected to the committed repository-relative MCP configuration and discovered exactly four tools: `get_arena_status`, `list_bots`, `inspect_bot`, and `run_battle`. From Kiro, I listed the two reviewed bundled Bots, inspected the editable `kiro-bot` source, and explicitly approved a synchronous one-round battle against `sample-opponent`. The MCP result exposed official completion provenance, both rankings, all required score components, the actual loopback WebSocket URL, recording path, and cleanup state. When the client initially displayed only a short text summary, the adapter and regression coverage were improved so genuine structured fields were also visible in Kiro’s text result. The exact Kiro-triggered `.battle.gz` recording was then loaded and played in the official Tank Royale GUI, proving the visual result without claiming unverified live-viewer compatibility.

### Social post template

Built Kiro Royale: a custom Java MCP server that lets Kiro inspect editable Robocode Tank Royale Bots and launch a genuine official battle with real scores and replay proof. Source: https://github.com/mathisen99/kiro-mcp-challange — Demo: `<VIDEO_URL>` #BuildWithKiro #TeamKiro @kirodotdev

## Demonstrated limitations

The submission is the synchronous four-tool MVP only. It supports exactly two fixed reviewed Bots, 1–5 rounds, one active battle, loopback-only local execution, automatic opt-in viewing through the trusted hosted passive viewer, and official-GUI replay as a fallback. It does not claim async jobs, remote Bot import, arbitrary execution, sandboxing, a custom viewer, tournaments, leaderboards, persistence, or deterministic winners/scores.

## Attribution and license status

Kiro Royale is licensed under the [MIT License](LICENSE). See [`THIRD_PARTY_NOTICES.md`](THIRD_PARTY_NOTICES.md) for dependencies, tools, and adapted-example attribution.
