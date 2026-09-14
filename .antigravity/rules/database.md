# Database Rules

## General

The project uses PostgreSQL with Spring Data JPA.

Persistence code should prioritize:

* Correctness.
* Predictable queries.
* Clear transaction boundaries.
* Minimal unnecessary database access.

Keep persistence concerns out of controllers and API DTOs.

## Entities

JPA entities belong to their feature:

```text
user/User.java
```

Entities:

* Represent persistent domain state.
* May contain domain behavior and invariants.
* Must not depend on HTTP, controllers, or API DTOs.
* Must not contain repository calls or external-service orchestration.
* Must not be exposed directly through HTTP APIs.

Map entities to API DTOs at the service/application boundary.

Use database-generated IDs for persistence identity unless there is a concrete reason otherwise.

Do not blindly use Lombok-generated `equals`, `hashCode`, or `toString` on JPA entities. Avoid relationships in these methods unless explicitly justified.

## Relationships

Define relationships intentionally. Do not add them merely because tables are related.

* Prefer unidirectional relationships unless bidirectional navigation is actually needed.
* Do not use `EAGER` loading as a default.
* Prefer `LAZY` associations and explicitly fetch related data when a use case requires it.
* Avoid unnecessary entity graphs and large object graphs.
* Avoid `@ManyToMany` when the relationship has meaningful state; use an explicit join entity instead.

## Fetching

Use explicit fetching strategies for each use case.

When related data is required, prefer:

* Fetch joins.
* Entity graphs.
* Purpose-specific repository queries.
* Projections when only part of the data is needed.

Do not solve N+1 queries by changing relationships to `EAGER`.

Be especially careful about lazy relationships during DTO mapping and collection processing.

Controllers and JSON serialization must not determine which database queries execute.

## Cascade and Orphan Removal

Do not use `CascadeType.ALL` by default.

Use cascade only when the parent intentionally owns the child's lifecycle.

Use `orphanRemoval = true` only when removing a child from the parent means the child should be deleted.

## Entity Mutability

Keep entity state changes intentional.

Prefer domain methods for meaningful state transitions rather than unrestricted setters when appropriate.

Entities must provide the constructor required by JPA. Keep it as restricted as the mapping allows and use a separate constructor/factory for valid domain initialization.

## Repositories

Repositories own persistence operations:

* Queries.
* Persistence-specific fetching.
* Persistence-specific projections.
* Database lookups.

Do not put business workflows or external-service calls in repositories.

Prefer repository methods that express meaningful persistence operations.

## Transactions

Define transaction boundaries at the service/application layer.

Use `@Transactional` for write units of work and `@Transactional(readOnly = true)` for appropriate read operations.

Keep transactions as small as practical. Avoid slow external calls inside transactions unless required for consistency.

Do not rely on Open Session in View to hide poor query design.

## Queries and Performance

Avoid:

* N+1 queries.
* Unbounded queries for potentially large datasets.
* Loading large entity graphs unnecessarily.
* Repeated queries inside loops.
* Loading full entities when a suitable projection is sufficient.

Use pagination for potentially large collections.

Do not optimize speculatively. Inspect the actual query/access pattern before introducing complexity.

## Database Constraints

Enforce important invariants at the database level where appropriate:

* `NOT NULL`.
* `UNIQUE`.
* Foreign keys.
* Indexes.
* Useful check constraints.

Application validation does not replace database constraints.

## Schema Changes

Follow the project's database migration strategy for schema changes.

Entity changes that require schema changes must include the corresponding migration.

## Testing

Repository and persistence behavior must be tested against PostgreSQL using Testcontainers.

Do not use H2 as a substitute for PostgreSQL persistence tests.

See `testing.md` for testing conventions.
