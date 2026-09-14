# Code Style Rules

## General

Follow standard Java and Spring conventions unless the project rules explicitly specify otherwise.

Prefer code that is:

* Clear.
* Explicit.
* Small.
* Easy to read.
* Easy to test.
* Consistent with surrounding code.

Do not optimize for brevity at the expense of readability.

Avoid comments that merely restate what the code does. Use comments when they explain why something is necessary or document non-obvious behavior.

## Java Version

The project uses Java 25.

Use modern Java features when they make the code clearer or safer, including:

* Records.
* Pattern matching.
* Switch expressions.
* Text blocks.
* `var` where the inferred type is obvious.
* Modern collection APIs where appropriate.

Do not use a newer language feature merely because it exists. Prefer the clearest implementation.

## Classes

Prefer small, focused classes with a single clear responsibility.

Avoid large classes that combine:

* HTTP handling.
* Business logic.
* Persistence.
* Mapping.
* Validation.

Split responsibilities when a class becomes difficult to understand or test.

Use one primary public type per source file.

## Dependency Injection

Use constructor injection.

Prefer Lombok's `@RequiredArgsConstructor` when it reduces boilerplate without hiding important behavior.

Example:

```java
@RequiredArgsConstructor
@Service
public class UserService {

    private final UserRepository userRepository;
}
```

Do not use field injection.

Avoid manually writing constructors when Lombok can generate them without reducing clarity.

## Lombok

Use Lombok where it reduces boilerplate without obscuring the code.

Commonly preferred annotations include:

* `@RequiredArgsConstructor`
* `@Getter`
* `@Setter` when mutable state is intentional
* `@Builder` when a builder materially improves construction
* `@Slf4j`

Avoid indiscriminate use of `@Data`, `@ToString`, and `@EqualsAndHashCode`, especially on JPA entities.

Generated `equals`, `hashCode`, and `toString` methods can cause problems with entity identity, lazy relationships, and recursive associations.

Define entity behavior explicitly when necessary.

## Records

Use records for immutable data carriers where appropriate.

Records are particularly suitable for:

* Request DTOs.
* Response DTOs.
* Small value-like objects.
* Internal immutable data structures.

Example:

```java
public record CreateUserRequest(
        @NotBlank String name,
        @Email @NotBlank String email
) {
}
```

Do not use records for JPA entities.

## Naming

Follow standard Java naming conventions.

* Classes and records: `PascalCase`
* Methods and variables: `camelCase`
* Constants: `UPPER_SNAKE_CASE`
* Packages: lowercase

Prefer descriptive names over abbreviations.

Prefer:

```java
findActiveUsers()
```

over:

```java
findActUsrs()
```

Boolean methods should normally use names such as:

```text
isActive
hasPermission
canAccess
```

## Feature Naming

Keep feature classes inside their feature package.

For example:

```text
user/
├── dto/
│   ├── UserDto.java
│   ├── CreateUserRequest.java
│   └── UserResponse.java
├── User.java
├── UserRepository.java
├── UserService.java
└── UserController.java
```

Use:

* `UserService` for application and business operations concerning users.
* `UserRepository` for persistence operations.
* `UserController` for HTTP handling.
* `UserDto` for a straightforward representation of a mapped user entity.
* `CreateUserRequest`, `UpdateUserRequest`, etc. for request bodies.
* `UserResponse` when the response has a shape that does not appropriately fit a `UserDto`.

## Methods

Methods should perform one clear operation.

Prefer early returns when they make control flow easier to follow.

Avoid deeply nested conditionals.

Prefer:

```java
if (!user.isActive()) {
    throw new UserInactiveException();
}

return user;
```

over unnecessarily nesting the main operation.

Method names should describe the operation rather than its implementation.

Prefer:

```java
userService.createUser(request);
```

over:

```java
userService.saveUserFromRequest(request);
```

when the latter exposes unnecessary implementation details.

## Collections and Streams

Use streams when they make collection transformations clearer.

Do not use streams for complex control flow simply to avoid a loop.

Prefer a straightforward loop when it is easier to understand.

Avoid unnecessarily long stream pipelines.

## Null Handling

Prefer APIs that make absence explicit.

Use `Optional` primarily for return values where absence is a meaningful result, particularly repository lookups.

Do not use `Optional` for:

* Entity fields.
* DTO fields.
* Method parameters.
* Local variables unless there is a specific reason.

Avoid returning `null` when an empty collection or `Optional` better represents the result.

## Exceptions

Use exceptions for exceptional or invalid application states.

Do not use exceptions as normal control flow.

Use specific exception types when callers need to distinguish between failure cases.

Exception handling and HTTP error mapping follow the conventions in `error-handling.md`.

## Logging

Use SLF4J for application logging, normally through Lombok's `@Slf4j`.

Use appropriate log levels:

* `DEBUG` for diagnostic information.
* `INFO` for significant application events.
* `WARN` for unexpected but recoverable situations.
* `ERROR` for failures requiring investigation.

Never log:

* Passwords.
* JWTs.
* Cookies.
* API keys.
* Authorization headers.
* Other secrets.

Avoid logging sensitive personal information unless it is explicitly required and safe.

Avoid logging the same exception repeatedly at multiple layers.

## Formatting

Follow the project's formatter and IDE configuration when present.

Do not make unrelated formatting changes while modifying a file.

Keep imports clean and remove unused imports.

Prefer consistent formatting over personal stylistic preferences.

## Annotations

Place annotations consistently and close to the declaration they affect.

Use Spring stereotype annotations appropriate to the responsibility:

* `@RestController`
* `@Service`
* `@Repository`
* `@Configuration`

Do not add annotations that have no meaningful effect.

## Comments

Code should generally explain itself through naming and structure.

Add comments when they explain:

* Why a non-obvious decision was made.
* A limitation or workaround.
* An external constraint.
* Security-sensitive behavior.
* A subtle persistence or transaction requirement.

Do not add comments that simply restate the code.

Avoid:

```java
// Get user
var user = userRepository.findById(id);
```

Prefer comments that explain non-obvious reasoning:

```java
// The external provider subject must not be exposed as our internal user ID.
var user = userRepository.findByProviderSubject(subject);
```

## Consistency

When modifying existing code, follow the established style of the surrounding code unless it conflicts with these rules.

Do not refactor unrelated code solely to impose a preferred style.

New code should follow these conventions even when older code does not.
