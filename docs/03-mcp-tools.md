# MCP tool contracts

Keep human text brief and provide machine-readable JSON-compatible fields using
the resolved SDK's supported tool-result representation.

## `get_arena_status`

No input. Reject unexpected input properties.

Suggested output:

```json
{
  "ready": true,
  "applicationVersion": "actual application version",
  "javaVersion": "21.x",
  "robocodeVersion": "resolved-version",
  "botRoot": "bots",
  "botCount": 2,
  "battleActive": false,
  "websocketUrl": null,
  "viewerInstructions": "actual instructions for the current server state",
  "recordingDirectory": "runtime/recordings",
  "blockingPrerequisites": []
}
```

`websocketUrl` is the actual loopback URL when the managed server is available;
otherwise it is `null`. Never report a guessed or stale port.

## `list_bots`

No input. Reject unexpected input properties.

Return only registered roots. Suggested fields:

```json
{
  "bots": [
    {
      "id": "kiro-bot",
      "name": "resolved from bot config",
      "version": "resolved from bot config",
      "language": "java",
      "path": "bots/kiro-bot",
      "valid": true,
      "source": "bundled"
    }
  ]
}
```

## `inspect_bot`

Input:

```json
{"botId": "kiro-bot"}
```

Return metadata, relevant relative files, editable entry source, run/build
information, and validation problems. Reject additional properties and unknown
Bot IDs.

## `run_battle`

Input:

```json
{
  "botIds": ["kiro-bot", "sample-opponent"],
  "rounds": 1,
  "record": true
}
```

`rounds` defaults to 1 and `record` defaults to `true`. If `record` is `false`, do
not require or claim a recording.

Rules:

- exactly two distinct registered bot IDs for MVP;
- rounds 1–5;
- reject non-integer rounds and duplicate bot IDs;
- accept only `botIds`, `rounds`, and `record`; reject additional properties;
- validate both bots before starting;
- no arbitrary paths, commands, environment overrides, hosts, or URLs;
- one active battle at a time;
- finite server-connect and battle wall-clock timeouts;
- real Battle Runner only.

Suggested output:

```json
{
  "success": true,
  "roundsPlayed": 1,
  "results": [
    {
      "rank": 1,
      "name": "actual name",
      "version": "actual version",
      "totalScore": 0,
      "survival": 0,
      "bulletDamage": 0,
      "ramDamage": 0,
      "firstPlaces": 0,
      "roundsPlayed": 1
    }
  ],
  "recordingPath": "runtime/recordings/actual-file.battle.gz"
}
```

The numeric example values above describe shape only. Every production rank and
score component must come from Battle Runner completion data. Never return these
example values or any other placeholder values during a real call.
