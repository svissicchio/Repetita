# Agent Guidelines

## Pre-Commit Requirements
- Always run the test suite (`mvn test`) and verify zero test failures before performing any git commit.
- Never commit code changes without automated verification passing cleanly.

## Test Suite Integrity
- Do **not** modify, swallow assertions, or delete failing tests to force them to pass.
- Focus strictly on investigating and fixing the root cause logical bugs in core application code to resolve failures.

## Documentation Maintenance
- Always keep `README.md` updated to accurately reflect all architectural changes, new dependencies (Java 21, Maven, MaxiCP 0.0.3, OR-Tools), package restructurings (`edu.repetita`), command-line usage examples, and visualization features.
