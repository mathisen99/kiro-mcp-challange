# Kiro Bot

`Kiro Bot` version `1.0` is the deliberately simple editable Java 21 Tank Royale bot.
Its primary strategy source is
`src/main/java/dev/kiro/royale/bots/KiroBot.java`. The repository-owned Gradle
`buildBundledBots` task compiles it under `runtime/bots`; the fixed launch scripts
contain no caller-provided command data.

The strategy continuously sweeps or locks its independent radar, predicts a moving target's
near-future position, fires only when the gun is aligned, strafes across the opponent's firing
line, and reverses after hits or wall contact. The small firepower, aim, and movement methods are
intentionally easy for Kiro to tune. Bots execute local code with the current user's permissions.
