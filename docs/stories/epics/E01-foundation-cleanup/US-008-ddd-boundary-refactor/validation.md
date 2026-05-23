# Validation

## Proof Strategy

The story is done only when current behavior still passes and architecture
boundaries are mechanically checked.

Required proof:

1. Backend unit, repository, application, and MockMvc tests pass.
2. Backend package build passes.
3. Docker Compose config for required local MySQL and Redis remains valid.
4. Architecture check proves:
   - `xxxx-application` has no Maven dependency on `xxxx-infrastructure`.
   - application source has no `com.xxxx.ddd.infrastructure` imports.
   - infrastructure source has no controller imports.
   - controller source has no infrastructure imports.
   - infrastructure may depend on `xxxx-domain` and application contracts when
     needed.
5. Accepted API routes remain covered by controller tests.
6. Payment, auth, and Redis stock behavior retain focused regression tests.

Frontend validation is not required for this story unless backend API contracts
change. If E07 remains broken, document it as a separate blocker rather than
claiming full product validation.

## Test Plan

| Layer | Cases |
| --- | --- |
| Unit | Domain invariants for event, ticket type, order, payment, auth role rules; application services use mocked ports |
| Integration | Repository adapters save/load accepted entities; Redis stock adapter implements Lua return contract; refresh token store consumes atomically; VNPAY adapter signs and verifies callbacks |
| Controller | Auth, event, order, payment, and admin authorization routes still return accepted statuses |
| Architecture | Forbidden dependency check for Maven modules and Java imports; warnings only for existing Spring/JPA coupling in domain |
| Platform | `docker-compose -f environment/docker-compose-dev.yml config`; backend package build |
| E2E | Not required unless API behavior changes |
| Logs/Audit | Logs still identify order, payment, stock, and auth failures without leaking secrets or raw refresh tokens |

## Fixtures

- `user@example.com` / `password123` with `USER`.
- `admin@example.com` / `password123` with `USER` and `ADMIN`.
- Active event with at least one active ticket type.
- Pending order owned by the standard user.
- VNPAY callback parameter sets for valid success, valid failure, and invalid
  signature.
- Redis stock key following `TICKET_TYPE:{ticketTypeId}:STOCK`.

## Commands

Use Java 21 as in prior story evidence:

```powershell
$env:JAVA_HOME='C:\Program Files\Android\openjdk\jdk-21.0.8'
$env:PATH="$env:JAVA_HOME\bin;$env:PATH"
mvn test
mvn package
docker-compose -f environment\docker-compose-dev.yml config
```

Architecture validation command:

```powershell
mvn -pl xxxx-application,xxxx-domain test -Dtest=ArchitectureBoundaryTest
```

## Acceptance Evidence

Implemented on 2026-05-23.

- `mvn test` passed across the full backend reactor with Java 21.
- `mvn package` passed across the full backend reactor with Java 21 after
  rerunning with dependency-resolution access; the first sandboxed package run
  failed because Maven could not transfer Spring Boot dependency metadata.
- `ArchitectureBoundaryTest` passed and checks:
  - `xxxx-application/pom.xml` does not depend on `xxxx-infrastructure`.
  - application source/test code does not reference
    `com.xxxx.ddd.infrastructure`.
  - controller source does not reference infrastructure adapters.
  - infrastructure source does not reference controller classes.
- `docker-compose -f environment\docker-compose-dev.yml config` passed with
  the existing Docker config warning:
  `open C:\Users\trhoa\.docker\config.json: Access is denied`.
