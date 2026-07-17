# Sample Opponent

`Sample Opponent` version `1.0` is a deterministic Java 21 Tank Royale opponent.
Its source is `src/main/java/dev/kiro/royale/bots/SampleOpponent.java`. It circles,
fires on scans, and reverses in fixed responses to wall and bot collisions; it
uses no random decisions.

The repository-owned Gradle `buildBundledBots` task compiles it under
`runtime/bots`. Bots execute local code with the current user's permissions.
