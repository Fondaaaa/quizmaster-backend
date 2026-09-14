# Testing Rules

## General

Tests should verify **meaningful application behavior**, not implementation details.

Prefer tests that remain valid when internal implementation changes while observable behavior remains the same.

Tests should be:

* Deterministic.
* Isolated where practical.
* Readable.
* Focused on one behavior.
* Fast at the appropriate test level.

Do not write tests solely to increase coverage.

## Test Structure

Use the standard Maven test layout.

Production packages should be mirrored under `src/test/java`:

```text
src/
├── main/
│   └── java/
│       └── com/example/user/
│           └── user/
│               ├── dto/
│               ├── User.java
│               ├── UserRepository.java
│               ├── UserService.java
│               └── UserController.java
└── test/
    └── java/
        └── com/example/user/
            └── user/
                ├── UserRepositoryTest.java
                ├── UserServiceTest.java
                └── UserControllerTest.java
```

Keep tests close to the code they exercise.

## Test Levels

Choose the narrowest test level that meaningfully verifies the behavior.

### Unit Tests

Use plain JUnit tests for business and domain logic that does not require the Spring context.

Do not write unit tests for JPA entities solely to verify getters, setters, constructors, or persistence annotations. Persistence mappings and constraints are validated via repository tests, and business logic is validated via service tests.

Prefer real collaborators for internal domain logic.

Use Mockito only when a dependency represents an external boundary or a non-deterministic dependency that should be isolated.

Examples of appropriate Mockito targets include:

* External HTTP clients.
* Email providers.
* Payment providers.
* External message brokers.
* Time or randomness when determinism requires controlling it.
* Other genuinely external or non-deterministic dependencies.

Do not mock internal services merely to make a unit test easier to construct.

### Controller Tests

Use `@WebMvcTest` for controller-focused tests.

Controller tests should verify the HTTP contract, including where applicable:

* HTTP method.
* URL.
* Request validation.
* Request serialization/deserialization.
* Response status.
* Response body.
* Response content type.
* Error responses.
* Problem Details structure.
* Authentication and authorization behavior that belongs to the HTTP boundary.

Do not duplicate extensive business-logic tests in controller tests.

### Repository Tests

Use `@DataJpaTest` for repository and JPA persistence behavior.

Repository tests should use a real PostgreSQL database through **Testcontainers**.

Do not replace PostgreSQL with an in-memory database for persistence tests.

Repository tests should verify behavior that depends on:

* JPA mappings.
* Relationships.
* Constraints.
* Queries.
* Transactions where relevant.
* PostgreSQL-specific behavior.

The production database and persistence tests should use the same database technology.

### Integration Tests

Use `@SpringBootTest` when multiple application layers need to be verified together.

Use integration tests for behavior involving combinations such as:

* Security + controller + service.
* Controller + service + repository.
* Transactions across multiple repositories.
* Complete authentication flows.
* Important end-to-end application behavior.

Do not use `@SpringBootTest` when a narrower test can verify the behavior effectively.

## Mocking

Follow this principle:

> Prefer real collaborators for internal domain logic; reserve Mockito strictly for I/O boundaries and non-deterministic dependencies.

Over-mocking internal services produces brittle tests that couple assertions to implementation details rather than observable behavior.

Avoid tests such as:

```java
when(userService.findUser(id)).thenReturn(user);
```

when the service itself is part of the behavior being tested.

Prefer real internal collaborators when doing so keeps the test understandable and deterministic.

Mocks should generally verify interactions only when the interaction itself is meaningful behavior, such as ensuring an external operation is not performed after validation fails.

Do not verify incidental implementation details such as private method calls or arbitrary internal method ordering.

## Test Naming

Use the following naming convention:

```text
methodUnderTest_condition_expectedResult
```

Examples:

```java
getUser_whenUserDoesNotExist_returns404()
createUser_whenEmailAlreadyExists_returnsConflict()
updateUser_whenRequestIsInvalid_returnsValidationError()
deleteUser_whenUserIsNotAuthenticated_returns401()
```

Test names should describe observable behavior.

Avoid vague names such as:

```java
testUser()
works()
shouldWork()
testService()
```

## Arrange, Act, Assert

Prefer a clear Arrange, Act, Assert structure.

Example:

```java
@Test
void getUser_whenUserDoesNotExist_returns404() {
    // Arrange
    ...

    // Act
    ...

    // Assert
    ...
}
```

Do not add the comments when the three phases are already obvious from the test structure.

Keep setup close to the behavior being tested.

## Assertions

Assert behavior that matters to the contract.

Prefer specific assertions over broad assertions.

For example, controller tests should verify the relevant response fields rather than merely asserting that the request returned a `2xx` status.

Avoid asserting irrelevant implementation details.

When an entire response is part of a stable API contract, assert the complete relevant response structure.

## API Error Testing

Controller and integration tests must verify the API's Problem Details contract for error cases.

Where applicable, verify:

* HTTP status.
* `Content-Type`.
* `type`.
* `title`.
* `status`.
* `detail`.
* `instance`.
* Application-specific extensions such as `code` or `errors`.

For validation failures, verify that the expected fields are represented in the `errors` extension.

Do not assert implementation-specific exception class names in HTTP responses.

## Security Testing

Security behavior must be explicitly tested.

At minimum, security-sensitive endpoints should cover relevant cases such as:

* Unauthenticated request returns `401`.
* Authenticated user without permission returns `403`.
* Authenticated user can perform permitted operations.
* Current-user operations use the authenticated user's database ID.
* Client-supplied user IDs cannot be used to impersonate another user.
* JWT authentication through the configured HTTP cookie works as expected.
* Invalid or expired authentication is rejected.

Security tests should verify externally observable behavior rather than internal Spring Security implementation details.

## Current User

Tests for current-user operations must establish the authenticated identity through the application's security mechanism.

Do not make the test pass by simply supplying the target user's database ID as an ordinary request parameter when production code is expected to derive the identity from authentication.

Tests should explicitly verify that the authenticated database ID is used.

## Validation

Validation tests should cover meaningful constraints and their API representation.

Test cases should include relevant:

* Missing fields.
* Invalid formats.
* Invalid values.
* Boundary values.
* Conflicting fields where applicable.

Do not create a separate test for every annotation unless the behavior is important to the API contract.

## Test Data

Prefer explicit test data when it makes the scenario easier to understand.

Use fixtures or builders when object construction becomes repetitive or obscures the test.

Do not create elaborate fixture frameworks for simple objects.

A fixture should make tests clearer, not hide important setup.

For example, a builder is appropriate when a test repeatedly needs a user with many properties:

```java
var user = UserFixture.user()
        .withEmail("alice@example.com")
        .inactive()
        .build();
```

But simple objects can be constructed directly when that is clearer.

Keep test fixtures deterministic.

Do not share mutable test state between tests.

## Testcontainers

Use Testcontainers for integration tests that require PostgreSQL.

Tests should exercise the same database technology used in production.

Do not use H2 or another in-memory database as a substitute for PostgreSQL when testing JPA or SQL behavior.

Keep container setup reusable without hiding important test configuration.

Tests must not depend on a developer's locally installed PostgreSQL instance.

## Determinism

Tests must produce the same result regardless of:

* Machine.
* Time of day.
* Local timezone.
* Execution order.
* External service availability.
* Previous test execution.

Control time, randomness, and external dependencies when they affect behavior.

Do not make tests depend on real external services.

## Test Isolation

Tests should not depend on execution order.

Each test should establish the state it requires.

Do not rely on data created by another test.

Database tests should cleanly isolate their data using the appropriate Spring test transaction behavior, container lifecycle, or explicit cleanup strategy.

## Coverage

Code coverage is a **tripwire, not the goal**.

Focus strictly on meaningful behavioral coverage.

Maintain a conservative branch-coverage floor in the approximate range of **70–80%**.

The coverage threshold exists to identify accidentally untested code, not to justify tests that provide little value.

Do not weaken or remove meaningful tests simply to satisfy the coverage threshold.

Do not add meaningless tests solely to increase the percentage.

## Test Changes

When changing behavior, update or add tests that verify the changed behavior.

Bug fixes should normally include a regression test demonstrating the original failure and the expected behavior.

Do not modify tests merely to make them pass unless the expected behavior has intentionally changed.

When refactoring without changing behavior, existing tests should continue to pass without requiring unnecessary changes to their assertions.

## Test Scope

Prefer the smallest test suite that provides confidence in the change.

For a change affecting only business logic:

* Add or update unit tests.

For a controller/API change:

* Add or update controller tests.
* Add integration coverage when the behavior crosses important application boundaries.

For persistence changes:

* Add or update repository tests using PostgreSQL/Testcontainers.

For security changes:

* Add focused security tests and integration coverage where necessary.

For multi-layer features:

* Cover the important behavior at the appropriate unit, controller, repository, and integration levels rather than testing every layer identically.
