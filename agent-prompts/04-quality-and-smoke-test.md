# Stage 4 prompt: focused quality

The complete Kiro-to-real-battle path must already work.

Add only focused tests for:

- bot path containment;
- unknown bot IDs;
- invalid round bounds;
- sanitized failures.

Add one real integration smoke test that runs one round with both bundled bots and
asserts two ranked genuine results. Verify process cleanup and ignored runtime
output.

Run the tests and update `STATUS.md`. Avoid broad test matrices, CI polish,
databases, ratings, and unrelated refactoring.
