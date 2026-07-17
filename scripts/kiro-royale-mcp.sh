#!/bin/sh
set -eu

if [ "$#" -ne 0 ]; then
  printf '%s\n' 'Kiro Royale MCP launcher accepts no arguments' >&2
  exit 64
fi

# Kiro intentionally gives MCP servers a reduced environment. Restore only the
# desktop-session values needed by the fixed, opt-in live-view URL opener from
# Kiro's direct parent process. Never copy the parent's full environment.
PARENT_ENV="/proc/$PPID/environ"
if [ -r "$PARENT_ENV" ]; then
  parent_value() {
    tr '\000' '\n' < "$PARENT_ENV" | sed -n "s/^$1=//p" | sed -n '1p'
  }

  value=$(parent_value DISPLAY || true)
  case "$value" in :[0-9]*) export DISPLAY="$value" ;; esac

  value=$(parent_value WAYLAND_DISPLAY || true)
  case "$value" in wayland-[0-9]*) export WAYLAND_DISPLAY="$value" ;; esac

  runtime_dir="/run/user/$(id -u)"
  value=$(parent_value XDG_RUNTIME_DIR || true)
  if [ "$value" = "$runtime_dir" ]; then export XDG_RUNTIME_DIR="$value"; fi

  value=$(parent_value DBUS_SESSION_BUS_ADDRESS || true)
  if [ "$value" = "unix:path=$runtime_dir/bus" ]; then export DBUS_SESSION_BUS_ADDRESS="$value"; fi

  value=$(parent_value XDG_SESSION_TYPE || true)
  case "$value" in wayland|x11) export XDG_SESSION_TYPE="$value" ;; esac
fi

ROOT=$(CDPATH= cd -- "$(dirname -- "$0")/.." && pwd)
APP="$ROOT/build/install/kiro-royale/bin/kiro-royale"

if [ ! -x "$APP" ]; then
  printf '%s\n' 'Kiro Royale distribution is missing; run ./gradlew installDist first' >&2
  exit 1
fi

exec "$APP" mcp-stdio
