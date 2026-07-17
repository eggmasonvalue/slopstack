# Agent Guide

## Principle

Document only information that cannot be quickly recovered from code: location in
`context/MAP.md` and durable tradeoffs in `context/DECISIONS.md`.

## Guardrails

- Do not commit directly to the default branch; work on a branch and open a PR.
- Keep changes scoped to the task.
- Verify behavior before documenting claims.
- Do not add a changelog.

## Read routing

- Read `context/MAP.md` before changing module layout or data flow.
- Read `context/DECISIONS.md` before changing a recorded tradeoff.
- Read `context/CONVENTIONS.md` while writing code.
- Run `todo list` at task start and `todo claim <id>` before editing orchestrated todos.

## Write triggers

- Update MAP for module or data-flow changes.
- Update DECISIONS only for choices that pass the decision-log bar.
- Update CONVENTIONS for repeatable coding rules.
- Update README for user-facing setup or usage changes.

## Decisions

DECISIONS is a curated ADR file, not a worklog. Append only durable architecture,
public-behavior, data-shape, dependency-ownership, or expensive-migration choices
whose rationale a future agent needs. Do not record bug fixes, cleanup, dead-code
removal, renames, mechanical refactors, one-feature tactics, routine test/lint
chores, changelogs, status lists, or obvious code behavior.

CONVENTIONS contains terse imperatives. Put rationale in DECISIONS only when it
passes the decision-log bar.

Todos hold live working context. Before closing a todo, graduate rationale to
DECISIONS only when it passes that bar.

## Definition of Done

Code, checks, and durable documentation must agree. A decision-log-bar choice that
is not recorded makes the task incomplete.
