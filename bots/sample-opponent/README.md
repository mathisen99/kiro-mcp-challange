# Sample Opponent

`Sample Opponent` version `1.0` is a deterministic Java 21 Tank Royale opponent.
Its source is `src/main/java/dev/kiro/royale/bots/SampleOpponent.java`. It keeps an independent
radar sweep, aims directly at scanned coordinates, fires only when aligned, orbits the target,
and reverses on a fixed schedule or collision response. It uses no random decisions and remains
intentionally simpler than Kiro Bot's predictive aim.

The repository-owned Gradle `buildBundledBots` task compiles it under
`runtime/bots`. Bots execute local code with the current user's permissions.
