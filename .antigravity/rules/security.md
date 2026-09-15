# Security Rules

## General

Security is a cross-cutting concern. Prefer secure defaults and established Spring Security mechanisms over custom security implementations.

Do not weaken security controls for convenience or to simplify tests.

Never expose secrets, credentials, tokens, or sensitive authentication data through API responses or logs.

## Authentication

The application uses JWT-based authentication delivered through an HTTP cookie.

Authentication must be handled by Spring Security rather than manually implemented in controllers or services.

JWTs must be properly validated before an authenticated identity is established.

Do not trust client-supplied identity information when the authenticated identity is already available through Spring Security.

## User Identity

The authenticated user's **database ID** is the canonical application identity.

Application code must derive the current user from the authenticated security context.

Never use a client-supplied user ID to determine the identity of the currently authenticated user.

For example, current-user operations should use the authenticated identity rather than accepting a user ID from the request.

## Authorization

Authentication and authorization are separate concerns.

* Authentication establishes who the user is.
* Authorization determines what the user is allowed to do.

Enforce authorization at the appropriate application boundary.

Do not rely solely on frontend checks or client-provided roles, permissions, or user IDs.

Users must not be able to access or modify resources belonging to another user unless the application's authorization rules explicitly permit it.

## Cookies

Authentication cookies must use secure cookie settings appropriate to the deployment environment.

Authentication cookies should not be accessible to client-side JavaScript.

Cookie configuration must consider:

* `HttpOnly`.
* `Secure`.
* Appropriate `SameSite` policy.
* Appropriate expiration and scope.

Do not expose authentication tokens through response bodies, URLs, query parameters, or client-readable storage when the cookie-based authentication design does not require it.

## Passwords

Passwords must never be stored in plaintext or reversible form.

Use Spring Security's password-hashing facilities for password storage.

Never log passwords or include them in error responses.

Password verification must be performed using the configured password encoder rather than comparing plaintext values.

## Secrets

Never commit secrets to source control.

This includes:

* JWT signing secrets or private keys.
* Database credentials.
* API keys.
* OAuth credentials.
* Encryption keys.
* Other authentication or integration secrets.

Load secrets through appropriate external configuration mechanisms.

Never include secrets in logs, exceptions, API responses, or test fixtures committed to the repository.

## JWTs

JWT implementation must use established Spring Security mechanisms.

Validate the token before trusting its claims.

At minimum, authentication must not be established from a token whose signature or validity cannot be verified.

Do not use arbitrary JWT claims as authorization decisions without validating that they are trusted and appropriate for the application.

Do not place sensitive information in JWT claims unnecessarily.

## Current-User Operations

Operations acting on the authenticated user must derive the user identity from Spring Security.

Do not accept a request parameter, path variable, or request body field as the authoritative current-user identity.

When an endpoint operates on a resource belonging to a user, authorization must verify that the authenticated user is permitted to access that resource.

## Error Handling

Authentication and authorization failures must not reveal sensitive information.

Return appropriate HTTP status codes and follow the project's Problem Details conventions.

Do not reveal whether a credential, account, token, or other security-sensitive value exists when doing so would enable user enumeration or other attacks.

Do not expose internal security exceptions or implementation details to clients.

## Logging

Never log:

* Passwords.
* JWTs.
* Authentication cookies.
* Authorization headers.
* API keys.
* Secrets.
* Sensitive credentials.

Security-related logging should contain only the information necessary for diagnosis, monitoring, or auditing.

Be careful when logging request data because request bodies and headers may contain credentials or personal information.

## Security Boundaries

Treat all client input as untrusted.

Validate and authorize data at the server boundary.

Do not assume that:

* Frontend validation is sufficient.
* Hidden fields are trustworthy.
* Request paths are authorized.
* JWT claims supplied by a client are trustworthy without validation.
* A user is authorized merely because they are authenticated.

Apply authorization checks to every operation that accesses protected resources.

## Dependencies and Configuration

Prefer Spring Security functionality over custom authentication or authorization code.

Do not add security libraries without a concrete requirement and compatibility check.

Security-sensitive configuration should have safe defaults and should not require secrets to be committed to the repository.

## Testing

Security behavior must be tested as application behavior.

Tests should cover relevant authentication and authorization boundaries, including unauthenticated access, forbidden access, authenticated access, and protection of current-user/resource ownership.

See `testing.md` for detailed testing conventions.
