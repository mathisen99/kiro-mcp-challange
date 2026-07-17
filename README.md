# Kiro Royale

Kiro Royale is a Java 21 MCP server that lets Kiro inspect two reviewed Java bots and run a **genuine Robocode Tank Royale battle** through the official Battle Runner. The synchronous `run_battle` tool returns the official ranks and score components; it does not use fixtures, random values, or hardcoded results.

The MCP integration matters because Kiro cannot directly manage the Battle Runner's JVM lifecycle, loopback server, reviewed Bot processes, recordings, and result mapping. Kiro Royale exposes that capability through four narrow stdio tools while keeping paths, commands, hosts, environments, and timeouts out of caller input.

## Demonstrated scope

The locally demonstrated path is:

1. Kiro connects to the workspace MCP server.
2. Kiro calls `list_bots` and `inspect_bot`.
3. Kiro explicitly approves synchronous `run_battle` for `kiro-bot` and `sample-opponent`.
4. Kiro Royale starts the official Tank Royale server on loopback and launches both real Bot processes.
5. The official Battle Runner completes the match and returns genuine score components.
6. Kiro Royale verifies a contained `.battle.gz` recording and cleans up owned processes.
7. With `showBattle: true`, Kiro Royale opens the trusted passive web viewer, gives it a bounded
   pre-battle connection window, and reports whether a loopback client was observed. A recording
   remains available as fallback.

The installed-Kiro and official-GUI replay flow was exercised successfully. Automatic passive-viewer launch is implemented and unit-tested, and the owner visibly observed the automatically opened Firefox viewer display a genuine `showBattle: true` battle. Replay remains the fallback visual-proof path. See [`STATUS.md`](STATUS.md) for commands, exit codes, observed scores, and evidence boundaries.

## Architecture

```text
Kiro / official MCP client
        |
        | MCP JSON-RPC over stdio
        v
Kiro Royale Java application
  +-- strict four-tool MCP adapter
  +-- static two-Bot registry and validation
  +-- one-active-battle coordinator
  +-- shared BattleService
        |
        v
Official Battle Runner 1.0.2
  +-- official server bound to IPv4 or IPv6 loopback
  +-- real kiro-bot process
  +-- real sample-opponent process
  +-- official completion results
  +-- runtime/recordings/*.battle.gz
```

Direct diagnostic mode and MCP mode use the same `BattleService` and production `OfficialBattleRunnerAdapter`. MCP mode reserves stdout for protocol traffic; diagnostics use stderr or ignored runtime output.

## Prerequisites

The exercised host used:

- OpenJDK/JDK 21 (`java` and `javac`);
- the committed Gradle Wrapper 9.6.1; no system Gradle is required;
- Linux with executable `/usr/bin/systemd-socket-activate` supporting `--inetd` and `--now`;
- a POSIX shell for the repository launch and verification scripts;
- Kiro for the IDE integration;
- the official Tank Royale GUI 1.0.2 for the demonstrated replay fallback.

The loopback-safe server startup fails closed when the exercised Linux socket-activation prerequisite is unavailable. Windows, macOS, alternate launcher locations, and other socket-activation implementations are not verified.

Pinned direct dependencies are MCP Java SDK `2.0.0`, Tank Royale Battle Runner and Bot API `1.0.2`, JUnit BOM `5.13.4`, and jqwik `1.9.3`. The Wrapper pins Gradle `9.6.1`.

## Build from a checkout

Run commands from the repository root:

```sh
java -version
./gradlew --version
./gradlew clean build
```

The first Wrapper invocation may download the pinned Gradle distribution and Maven dependencies. The public repository is:

```sh
git clone https://github.com/mathisen99/kiro-mcp-challange.git
cd kiro-mcp-challange
```

`clean build` compiles the application and both bundled Bots. Generated classes, copied dependencies, logs, results, official-server files, and recordings stay under ignored build/runtime locations.

## Direct genuine battle proof

```sh
./gradlew clean directBattle
```

This runs one round directly through the production official Battle Runner path, outside MCP, with recording enabled. A successful run reports:

- both registered Bots as valid;
- an actual `ws://127.0.0.1:<port>` endpoint;
- the official successful-completion provenance;
- exactly two ascending ranks with name, version, total score, survival score, bullet damage, ram damage, first places, and rounds played;
- a non-empty recording below `runtime/recordings/`;
- complete cleanup of the owned server and Bot processes.

Do not expect repeatable winners or scores; deterministic result repetition is not claimed.

## Kiro MCP configuration

Build the installed distribution used by the fixed launcher:

```sh
./gradlew installDist
```

The committed workspace configuration is [`.kiro/settings/mcp.json`](.kiro/settings/mcp.json):

```json
{
  "mcpServers": {
    "kiro-royale": {
      "command": "./scripts/kiro-royale-mcp.sh",
      "args": [],
      "disabled": false,
      "autoApprove": [
        "get_arena_status",
        "list_bots",
        "inspect_bot"
      ]
    }
  }
}
```

Open the repository as a Kiro workspace after `installDist`. The three read-only tools are auto-approved. `run_battle` intentionally requires explicit approval because it executes local code and creates runtime artifacts. The launcher accepts no arguments and starts only fixed `mcp-stdio` mode.

To prove the launcher and all four tools with the official Java MCP client harness:

```sh
./gradlew mcpProof
```

This finite proof performs initialization, discovers exactly four tools, invokes the three read-only tools, and runs one genuine recorded round through MCP.

## MCP tools and examples

All schemas reject additional properties.

### `get_arena_status`

Input:

```json
{}
```

Returns application/Java/Robocode versions, Bot root/count, active state, actual ready loopback URL or `null`, replay guidance, recording directory, and blocking prerequisites. It never invents an endpoint.

### `list_bots`

Input:

```json
{}
```

Returns exactly the two registered IDs, identities, repository-relative directories, language, validation status, and source labels.

### `inspect_bot`

Input:

```json
{
  "botId": "kiro-bot"
}
```

Returns metadata, repository-relative source files, the primary editable strategy source, reviewed build/run information, and validation issues. The other accepted ID is `sample-opponent`; filesystem paths are not accepted.

### `run_battle`

Input:

```json
{
  "botIds": ["kiro-bot", "sample-opponent"],
  "rounds": 1,
  "record": true,
  "showBattle": true
}
```

Exactly two distinct registered Bot IDs are required. `rounds` is an integer from 1 through 5; omitted `rounds` defaults to `1`. Omitted `record` defaults to `true`. Successful responses include a concise summary and structured genuine results. Failures are sanitized and contain no fabricated score or recording claim.

`showBattle` defaults to `false` so headless and automated runs do not open windows. When it is
`true`, Kiro Royale keeps the viewer on its documented default `ws://localhost:7654`, binds the
fixed listener to IPv6 loopback because the exercised Firefox host resolves `localhost` to `::1`
first, gives the Java Battle Runner the unambiguous equivalent `ws://[::1]:7654`, and opens
the fixed trusted URL `https://jandurovec.github.io/tank-royale-viewer/`, and gives the browser a
10-second pre-battle connection window. Success adds `viewerRequested: true` and reports
`viewerConnected` as the kernel-level observation state. A successful URL launch is not falsely
described as a verified connection: if Firefox connects slightly later, the battle still proceeds
and can be displayed. If the browser cannot open or port 7654 is busy, the call fails before the
battle with a sanitized error.

Firefox WebSockets can leave fixed port 7654 temporarily in `TIME_WAIT` after a visible battle.
An immediate viewer-enabled retry therefore waits up to 65 seconds for the IPv6 loopback port to
become reusable instead of failing spuriously; the battle then starts normally within the existing
finite MCP deadline.

## Visual verification

### Demonstrated official-GUI replay fallback

1. Run a recorded battle from Kiro, `mcpProof`, or `directBattle`.
2. Note the returned repository-relative `.battle.gz` path under `runtime/recordings/`.
3. Obtain the official Tank Royale GUI matching version `1.0.2` from the [official release](https://github.com/robocode-dev/tank-royale/releases/tag/v1.0.2) and verify its published checksum.
4. In the GUI choose **Battle → Replay from File**.
5. Select the exact recording returned by that battle and confirm both Bots visibly play.

Stage 3 used this flow successfully with the exact Kiro-triggered recording. File existence alone is not replay proof.

### Automatic passive hosted viewer

Call `run_battle` with `showBattle: true` from a graphical Linux desktop. Kiro Royale opens the
third-party [Tank Royale Viewer](https://github.com/jandurovec/tank-royale-viewer), waits briefly
for a client on the fixed loopback endpoint, reports whether one was observed, and then starts the
battle. The viewer is passive and cannot control the battle. If you previously changed its saved
Server URL, open its gear menu once and reset it to `ws://localhost:7654`.
After the official result arrives, Kiro Royale keeps the local viewer connection open for five
seconds before cleanup so the victory screen and scores remain readable. Headless battles do not
incur this display hold.

The same production path can be checked outside Kiro with:

```sh
./gradlew viewerBattle
```

To verify the entire official MCP stdio path with automatic viewing enabled:

```sh
./gradlew mcpViewerProof
```

The automatic launch contract is covered by focused tests. On 2026-07-17 the owner confirmed that
the automatically opened Firefox viewer visibly displayed the genuine battle. Use the demonstrated
official-GUI replay fallback if the live path is unavailable.

## Tests and release preflight

Focused example/contract suite:

```sh
./gradlew clean test
```

Genuine one-round integration smoke test:

```sh
./gradlew realSmoke
```

Complete finite local release preflight:

```sh
./scripts/verify-release.sh
```

The script transparently runs tracked-file/ignore/config hygiene checks followed by the evidenced clean build, focused tests, direct battle, MCP proof, and real smoke commands. It does **not** automate Kiro, the GUI/viewer, publication, challenge submission, or video claims. It creates ignored runtime artifacts and runs three genuine one-round battles.

A truly fresh-checkout-like run and final human tracked-file review remain part of the next checkpoint and are not claimed by this script.

## Security

> **Bots execute local code with the current user's permissions.** Kiro Royale does not provide a sandbox. Only the two reviewed bundled Bot IDs are registered, but you must still review Bot changes before running them.

The MCP boundary does not accept arbitrary paths, commands, shell fragments, environment overrides, hosts, URLs, repositories, ports, or timeout values. Production process launches use application-owned argument lists, the Tank Royale server binds to loopback, one battle may run at a time, output is bounded/redacted, and generated data stays under ignored `runtime/`.

Do not replace bundled Bots with downloaded or unreviewed executables. Do not expose the local server beyond loopback.

## Troubleshooting

- **`distribution is missing; run ./gradlew installDist first`** — run the evidenced `./gradlew installDist` command from the repository root, then reconnect the Kiro MCP server.
- **Socket activation prerequisite unavailable** — verify the exercised Linux prerequisite exists at `/usr/bin/systemd-socket-activate`. The application intentionally does not fall back to the official embedded wildcard-binding mode.
- **Bot validation reports missing compiled classes/runtime** — run `./gradlew clean build` from the repository root.
- **Kiro does not reconnect after a build** — use Kiro's MCP reconnect control after `./gradlew installDist`; do not add arguments or an absolute path to `mcp.json`.
- **Battle already active** — wait for the synchronous call and cleanup to finish; requests are not queued.
- **`VIEWER_UNAVAILABLE`** — ensure `/usr/bin/xdg-open` and a graphical X11/Wayland session are available, close anything using local port 7654, and reset the viewer's saved Server URL to `ws://localhost:7654`. Viewer-enabled battles bind `[::1]:7654` to match this host's IPv6-first `localhost`; headless battles retain dynamic IPv4 loopback ports. Keep recording enabled and use the same-battle official-GUI replay flow when running headlessly.
- **A repeat viewer battle pauses on “Connecting”** — allow the same MCP call to continue. The
  application waits up to 65 seconds for the previous Firefox WebSocket's fixed port to leave
  `TIME_WAIT`; do not repeatedly cancel and retry the request.
- **Viewer works from a terminal but Kiro returns `VIEWER_UNAVAILABLE`** — reconnect the MCP server
  after updating this repository. Kiro supplies MCP children a reduced environment; the fixed
  launcher restores only validated desktop-session variables from its direct Kiro parent so
  `/usr/bin/xdg-open` can reach the same graphical session.
- **Recording not found after `clean`** — runtime recordings are generated and ignored; a later clean or manual runtime cleanup may remove prior evidence. Run a new recorded battle.
- **SLF4J no-provider warning** — the exercised SDK emits this warning on stderr. It is non-fatal and does not contaminate MCP stdout.

## Known limitations

- Exactly four synchronous MCP tools, two fixed bundled Bots, 1–5 rounds, and one active battle.
- Linux socket activation is the only verified safe server-start path.
- Automatic passive viewing is verified on the exercised Firefox/Linux desktop; other browsers, headless hosts, Windows, and macOS are not verified. Official-GUI replay remains the fallback.
- Genuine production timeout/abort/startup/recording-failure/forced-kill branches and live-battle JVM shutdown races are not integration-verified.
- Optional jqwik Properties 1–16 were not implemented; focused examples and contract tests cover the mandatory Stage 4 gate.
- No remote Bot import, shell execution, custom viewer, async jobs, tournament, leaderboard, database, telemetry, browser automation, or sandboxing claim.
- The public repository, fresh-checkout-like release flow, tracked-file review, and current challenge deadline/terms are verified. Demo recording, video/social-post accessibility, final duration, and form submission remain **not verified**.

## License and attribution

Kiro Royale is available under the [MIT License](LICENSE), copyright 2026 Tommy Mathisen.

Third-party dependencies and tools retain their own licenses and trademarks. See [`THIRD_PARTY_NOTICES.md`](THIRD_PARTY_NOTICES.md) for pinned versions, source links, and attribution. Robocode Tank Royale and the Model Context Protocol projects are not affiliated with or endorsing this repository.
