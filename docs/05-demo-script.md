# Demo video script

Target duration: 45–60 seconds. Maximum: 3 minutes.

## Shot list

### 0–5 seconds

Show the Kiro workspace and connected `kiro-royale` MCP server.

Voice/text:

> Kiro Royale connects Kiro to real Robocode Tank Royale battles through a custom MCP server.

### 5–15 seconds

Ask Kiro to list and inspect the bundled bots.

Show:

- `kiro-bot`;
- `sample-opponent`;
- editable strategy source.

### 15–35 seconds

Call `run_battle` for one round.

Immediately show the third-party passive viewer displaying the live battle. Open
and connect the viewer to the actual loopback WebSocket URL before recording this
shot.

If live viewing cannot be verified, show the recording from this same battle
loading and playing in the official Tank Royale GUI. Showing only the recording
file is insufficient.

### 35–50 seconds

Return to Kiro and show genuine ranked results and score components.

Voice/text:

> These scores came from the official Battle Runner, not hardcoded data.

### 50–60 seconds

Optional: show Kiro proposing or making one small strategy improvement and starting
another battle.

## Recording checklist

- [ ] readable terminal/editor scale
- [ ] no secrets or personal paths visible
- [ ] viewer already open
- [ ] actual viewer WebSocket URL verified, or replay fallback rehearsed
- [ ] battle duration tested beforehand
- [ ] notifications disabled
- [ ] result output fits on screen
- [ ] repository URL ready
- [ ] final video under 3 minutes
