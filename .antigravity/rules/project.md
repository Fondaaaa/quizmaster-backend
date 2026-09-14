# Project Rules

## Stack

Use the versions and dependencies defined in `pom.xml`:

* Java 25
* Spring Boot 4.1.1
* Spring Web MVC
* Spring Data JPA
* Spring Security
* Spring Validation
* PostgreSQL
* Lombok
* SpringDoc OpenAPI 3.1.0
* Maven

Do not change the stack or add dependencies without a concrete reason and a compatibility check.

## General Principles

Prefer:

* Small, focused classes.
* Constructor injection.
* Explicit API contracts.
* Feature-local code.
* Clear transaction boundaries.
* Minimal coupling between features.
* Existing Spring and Java functionality over additional libraries.

Avoid:

* God services.
* Fat controllers.
* Business logic in repositories.
* Unnecessary abstractions.
* Premature generalization.
* Cross-feature coupling.
* Sensitive information in logs.

## Project Structure

Organize application code by **feature**, not by technical layer.

A typical feature should look like:

```text
user/
├── dto/
    ├── UserDto.java
├── User.Java
├── UserRepository.java
├── UserService.java
├── UserController.java
```

Only create the packages that the feature actually needs.

Keep feature-specific classes inside the feature package. Do not move them into global `controller`, `service`, `repository`, or `dto` packages unless there is a concrete project-wide reason.

Shared infrastructure should remain separate from feature code.

## DTOs

API DTOs belong to their feature:

```text
user/
└── dto/
    ├── UserDto.java
    ├── CreateUserRequest.java
    └── UserResponse.java
```

Use naming consistently:

* `UserDto` — representation of an entity or mapped domain object.
* `CreateUserRequest` / `UpdateUserRequest` — request bodies.
* `UserResponse` — response body where a `UserDto` does not appropriately represent the API response.
* `UserDto` should generally be preferred when the response is simply a representation of the mapped user data.

Do not expose JPA entities directly from HTTP endpoints.

Request and response DTOs should represent the API contract rather than mirror the persistence model unnecessarily.

## Controllers

Controllers are the HTTP boundary of the application.

They should be responsible for:

* Mapping HTTP requests to application operations.
* Request validation.
* Mapping application results to HTTP responses where necessary.
* Declaring HTTP and OpenAPI metadata.

Controllers should not contain business logic or persistence logic.

Prefer:

```text
Controller
    ↓
Service
    ↓
Repository
```

rather than allowing controllers to access repositories directly.

## Services

Services contain application and business logic.

Use services to:

* Coordinate business operations.
* Define transaction boundaries.
* Apply business rules.
* Coordinate repositories and other application services.

Services should not depend on HTTP-specific types unless there is a concrete reason.

Avoid creating services that merely delegate every method to a repository without adding meaningful application behavior.

## Repositories

Repositories are responsible for persistence access.

Keep:

* JPA queries.
* Persistence-specific operations.
* Entity retrieval and persistence.

in repositories.

Do not put business rules in repositories.

Prefer Spring Data JPA capabilities before introducing custom persistence infrastructure.

## Entities

JPA entities represent persistence state.

Do not expose entities directly as API request or response models.

Keep persistence concerns within the entity and persistence layer rather than coupling entities to HTTP or presentation concerns.

## Transactions

Define transaction boundaries at the service/application layer.

Transactions should cover a complete application operation where multiple persistence operations must succeed or fail together.

Avoid putting `@Transactional` indiscriminately on repositories or controllers.

Use read-only transactions where appropriate for operations that do not modify persistent state.

## Authentication

Authentication uses JWT through an HTTP cookie.

Authenticated users should be identified by their **database ID**.

For operations concerning the current user, never trust a user ID supplied by the client when the authenticated identity is already available from the security context.

For example, prefer:

```text
GET /users/me
```

over relying on:

```text
GET /users/{clientSuppliedId}
```

for current-user operations.

Security-specific rules belong in `security.md` once that rule file is introduced.

## HTTP APIs

HTTP endpoints must have explicit request and response contracts.

Every HTTP endpoint must be documented with SpringDoc/OpenAPI.

API error responses use the project's Problem Details conventions defined in `error-handling.md`.

API-specific conventions should be documented in a dedicated `code-style.md` when the project requires rules beyond the general project conventions.

## Dependencies

Do not add a dependency for functionality already provided by Java, Spring Boot, or an existing project dependency unless there is a concrete reason.

Before changing dependencies:

1. Check the existing `pom.xml`.
2. Confirm compatibility with Java 25 and Spring Boot 4.1.1.
3. Consider whether the requirement can be implemented with the existing stack.
4. Keep the dependency change as small as possible.

## Configuration and Secrets

Configuration should use Spring's configuration mechanisms.

Never commit:

* Passwords.
* API keys.
* JWT secrets.
* Private keys.
* Database credentials.
* Other sensitive credentials.

Never write secrets to application logs.

Environment-specific configuration should not require modifying application source code.

