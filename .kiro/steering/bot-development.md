# LLM-coded Bot workflow

When the user asks to create, code, evolve, improve, or invent a Kiro Bot strategy and then battle,
the selected Kiro model must produce a real source edit before calling `run_battle`.

1. Call `inspect_bot` for `kiro-bot` and use its `primaryEditableSource`.
2. Read the current source and design a concrete strategy change. Do not claim a new strategy when
   no meaningful edit was made.
3. Edit only the registered Kiro Bot source unless the user explicitly asks to change the fixed
   opponent. Preserve its package, main class, Bot identity, pinned API compatibility, and safe
   repository-relative structure.
4. Call `run_battle`. It compiles the current fixed registered sources automatically before it
   launches anything. Do not send source, paths, commands, hosts, or environment values as MCP input.
5. If the tool returns `BOT_COMPILE_FAILED`, use the bounded line/column diagnostics to repair the
   source and retry. Never describe a failed compile as a battle.
6. After genuine completion, explain the strategy edit, report the returned source SHA-256 for
   `kiro-bot`, and evaluate only the genuine official scores. Winners and scores are not deterministic.

If the user explicitly asks only to run or rerun the current Bot, do not silently rewrite it.
The checked-in strategy is an editable scaffold; the meaningful LLM-generated edit is made with
normal Kiro workspace tools before the MCP battle call.
