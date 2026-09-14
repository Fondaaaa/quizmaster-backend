# AGENTS.md

## How to use these rules

This repository uses progressively loaded rules located in `.antigravity/rules/`.

Do not load every rule file for every task. Read only the rule files relevant to the work being performed.

`rules/project.md` is always applicable and must be loaded for every task.

Additional rules should be loaded when their subject is relevant to the task. If a task spans multiple areas, load all applicable rules before making changes.

## Rule Index

| File                      | Load when                                                                                                                            |
| ------------------------- | ------------------------------------------------------------------------------------------------------------------------------------ |
| `rules/project.md`        | Always. Project stack, architecture defaults, feature structure, DTO conventions, dependencies, and general principles.              |
| `rules/code-style.md`     | Writing or modifying Java code, naming classes or methods, using Lombok, formatting, logging, or applying Java language conventions. |
| `rules/error-handling.md` | Handling exceptions, API errors, `ProblemDetail`, validation errors, or global exception handling.                                   |
| `rules/testing.md`        | Creating or modifying tests, or when validating application behavior, API behavior, persistence, or security.                        |

Additional rule files may be added as the project grows. When a new rule file is introduced, add it to this index and specify when it should be loaded.

## Applying Rules

Rules apply to new and modified code.

When rules overlap, follow all applicable rules. More specific rules supplement `rules/project.md`; they do not replace it.

Prefer the smallest change that correctly satisfies the task.

Do not refactor unrelated code simply to apply a rule.

When existing code does not follow the current rules, do not automatically rewrite it. Apply the rules to the code being changed unless the task explicitly calls for broader cleanup.

## Before Making Changes

Before modifying the repository:

1. Read `rules/project.md`.
2. Identify the other rule files relevant to the task.
3. Read those rules before implementing the change.
4. Follow the applicable conventions throughout the implementation.
5. Add or update tests when the change affects behavior.

## Defaults

Unless a more specific rule says otherwise:

* Organize code by feature.
* Keep controllers thin.
* Keep business logic in services.
* Keep persistence logic in repositories.
* Use constructor injection.
* Prefer Lombok where it reduces boilerplate without obscuring behavior.
* Use modern Java 25 features when they improve clarity.
* Keep API contracts explicit.
* Keep secrets out of source control and logs.
* Prefer real collaborators in tests and avoid unnecessary mocking.
