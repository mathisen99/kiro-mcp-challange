# Scripts

Scripts in this directory expose only finite, reviewed repository operations. They do not accept caller-provided commands, paths, hosts, environments, or remote repositories.

- `kiro-royale-mcp.sh` — repository-relative zero-argument launcher for the installed Kiro Royale distribution in fixed `mcp-stdio` mode.
- `verify-release.sh` — transparent finite local preflight. It checks tracked/ignored/configuration hygiene, then runs the evidenced clean build, focused tests, direct genuine battle, official MCP client proof, and real smoke commands.

`verify-release.sh` does not automate or establish Kiro UI connection, viewer/GUI playback, publication, challenge submission, public accessibility, or video claims. Review its commands directly before running it; generated battle artifacts remain under ignored `runtime/`.
