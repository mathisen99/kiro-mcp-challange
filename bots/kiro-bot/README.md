# Kiro Bot

`Kiro Bot` version `1.0` is the deliberately simple editable Java 21 Tank Royale bot.
Its primary strategy source is
`src/main/java/dev/kiro/royale/bots/KiroBot.java`. The repository-owned Gradle
`buildBundledBots` task compiles it under `runtime/bots`; the fixed launch scripts
contain no caller-provided command data.

The strategy follows a predictable patrol arc, scans with the gun, fires on scans,
and evades perpendicular to incoming bullets. Bots execute local code with the
current user's permissions.
