#!/bin/sh
set -eu

if [ "$#" -ne 0 ]; then
  printf '%s\n' 'Kiro Royale MCP launcher accepts no arguments' >&2
  exit 64
fi

ROOT=$(CDPATH= cd -- "$(dirname -- "$0")/.." && pwd)
APP="$ROOT/build/install/kiro-royale/bin/kiro-royale"

if [ ! -x "$APP" ]; then
  printf '%s\n' 'Kiro Royale distribution is missing; run ./gradlew installDist first' >&2
  exit 1
fi

exec "$APP" mcp-stdio
