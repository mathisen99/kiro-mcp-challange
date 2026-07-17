#!/bin/sh
set -eu

ROOT=$(CDPATH= cd -- "$(dirname -- "$0")/.." && pwd)
cd "$ROOT"

fail() {
  printf '%s\n' "RELEASE_CHECK_FAILED: $*" >&2
  exit 1
}

run() {
  printf '\n>>> %s\n' "$*"
  if "$@"; then
    printf '<<< exit 0: %s\n' "$*"
  else
    code=$?
    printf '<<< exit %s: %s\n' "$code" "$*" >&2
    exit "$code"
  fi
}

printf '%s\n' 'Kiro Royale finite release verification'
printf '%s\n' 'This does not automate or verify Kiro, visual playback, publication, or video claims.'

# Required release source/configuration must already be tracked in the checkout.
for path in \
  gradlew \
  gradlew.bat \
  gradle/wrapper/gradle-wrapper.jar \
  gradle/wrapper/gradle-wrapper.properties \
  build.gradle \
  settings.gradle \
  LICENSE \
  THIRD_PARTY_NOTICES.md \
  .kiro/settings/mcp.json \
  .kiro/steering/bot-development.md \
  scripts/kiro-royale-mcp.sh \
  src/main/java/dev/kiro/royale/KiroRoyaleApplication.java \
  src/main/java/dev/kiro/royale/RegisteredBotCompiler.java \
  src/test/java/dev/kiro/royale/RealBattleSmokeTest.java
do
  git ls-files --error-unmatch "$path" >/dev/null 2>&1 || fail "required tracked file missing: $path"
done

grep -qx 'MIT License' LICENSE || fail 'LICENSE is not the selected MIT license'
grep -qx 'Copyright (c) 2026 Tommy Mathisen' LICENSE || fail 'LICENSE copyright line is missing or incorrect'

# Generated, credential, binary, recording, log, and database examples must be ignored.
for path in \
  .env \
  .env.local \
  build/release-check.jar \
  runtime/release-check.log \
  runtime/release-check.result.json \
  runtime/release-check.battle.gz \
  runtime/release-check.sqlite \
  .jqwik-database
do
  git check-ignore -q "$path" || fail "generated/sensitive example is not ignored: $path"
done

# Source, tests, workspace MCP configuration, and the one allowed Wrapper JAR must not be ignored.
for path in \
  .kiro/settings/mcp.json \
  src/main/java/dev/kiro/royale/KiroRoyaleApplication.java \
  src/test/java/dev/kiro/royale/RealBattleSmokeTest.java \
  gradle/wrapper/gradle-wrapper.jar
do
  if git check-ignore -q "$path"; then
    fail "required release file is ignored: $path"
  fi
done

# No tracked generated artifact may be present. The Wrapper bootstrap JAR is the explicit exception.
git ls-files | while IFS= read -r path; do
  case "$path" in
    .env|.env.*) fail "tracked environment file: $path" ;;
    .gradle/*|build/*|out/*|target/*) fail "tracked build output: $path" ;;
    runtime/*) fail "tracked runtime artifact: $path" ;;
    *.battle.gz|*.log|*.db|*.db-journal|*.sqlite|*.sqlite3|*.zip|*.tar|*.tar.gz|*.tgz|*.exe|*.dll|*.so|*.dylib|*.class)
      fail "tracked generated/binary artifact: $path" ;;
    *.jar) [ "$path" = 'gradle/wrapper/gradle-wrapper.jar' ] || fail "tracked JAR outside Wrapper: $path" ;;
  esac
done

# Active executable/configuration files must not hardcode a developer home directory.
if grep -R -n -E '(/home/[^/[:space:]]+|/Users/[^/[:space:]]+|[A-Za-z]:\\Users\\)' \
  .kiro/settings/mcp.json \
  build.gradle \
  settings.gradle \
  scripts/kiro-royale-mcp.sh \
  src/main/java \
  bots/kiro-bot/kiro-bot.json \
  bots/kiro-bot/kiro-bot.sh \
  bots/kiro-bot/kiro-bot.cmd \
  bots/sample-opponent/sample-opponent.json \
  bots/sample-opponent/sample-opponent.sh \
  bots/sample-opponent/sample-opponent.cmd
then
  fail 'active release configuration contains a developer-home absolute path'
fi

printf '%s\n' 'RELEASE_HYGIENE_OK: tracked/ignored/configuration checks passed'

run ./gradlew clean build
run ./gradlew clean test
run ./gradlew clean directBattle
run ./gradlew mcpProof
run ./gradlew realSmoke

printf '%s\n' 'RELEASE_VERIFICATION_OK: local automated path passed'
printf '%s\n' 'NOT VERIFIED: fresh checkout, Kiro UI, live viewer, replay playback in this run, publication, demo video, public accessibility, deadline, and video duration'
