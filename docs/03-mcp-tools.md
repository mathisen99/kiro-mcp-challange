# MCP tool contracts

The implementation may use the SDK's structured result support. Keep human text
brief and provide machine-readable fields.

## `get_arena_status`

No input.

Suggested output:

```json
{
  "ready": true,
  "javaVersion": "21.x",
  "robocodeVersion": "resolved-version",
  "botRoot": "bots",
  "botCount": 2,
  "battleActive": false,
  "websocketUrl": "ws://127.0.0.1:7654",
  "recordingDirectory": "runtime/recordings",
  "problems": []
}
```

## `list_bots`

No input.

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
information, and validation problems.

## `run_battle`

Input:

```json
{
  "botIds": ["kiro-bot", "sample-opponent"],
  "rounds": 1,
  "record": true
}
```

Rules:

- exactly two bot IDs for MVP;
- rounds 1–5;
- no arbitrary paths;
- one active battle at a time;
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
      "firstPlaces": 0
    }
  ],
  "recordingPath": "runtime/recordings/actual-file.battle.gz"
}
```

The numeric example values above describe shape only. Never return placeholder
values during a real call.
