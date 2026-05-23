# Exec Plan

## Goal

Refactor the backend module boundaries so the codebase matches the DDD
dependency direction documented in `docs/ARCHITECTURE.md` while preserving
accepted auth, event, order, stock, and payment behavior.

## Scope

In scope:

- Remove the Maven dependency from `xxxx-application` to
  `xxxx-infrastructure`.
- Replace application imports of infrastructure classes with contracts owned
  by `xxxx-application` or `xxxx-domain`.
- Move Redis, VNPAY, refresh-token, JWT, and cache adapter details behind those
  contracts where they cross the application boundary.
- Add an architecture validation check for forbidden dependencies.
- Keep existing backend tests passing.
- Update story evidence and `docs/TEST_MATRIX.md`.

Out of scope:

- Frontend rebuild or design polish.
- New user-visible features.
- Large schema redesign.
- Replacing MySQL, Redis, Spring Boot, or React.
- Changing accepted API routes without a separate confirmation.
- Removing legacy routes unless tests and docs prove they are unused or the
  human approves the removal.

## Risk Classification

Risk flags:

- Auth.
- Authorization.
- Data model.
- External systems.
- Public contracts.
- Existing behavior.
- Weak proof.
- Multi-domain.

Hard gates:

- Auth.
- Authorization.
- Data migration or data loss if persistence mapping changes unexpectedly.
- External provider behavior for VNPAY.
- Removing or weakening validation requirements.

Lane: high-risk.

## Work Phases

1. Baseline discovery.
   - Run `mvn test`, `mvn package`, frontend build/lint if E07 is involved,
     and Docker Compose config.
   - Capture current forbidden imports and Maven dependency graph.
   - List application classes that import infrastructure.

2. Contract design.
   - Define application ports for VNPAY, stock cache, refresh token storage,
     token issuing/parsing, event cache invalidation, and distributed locking
     only where needed by active flows.
   - Keep contract names product-oriented and avoid provider-specific names in
     application code.
   - Put repository-style contracts in `xxxx-domain`; put gateway/cache/token
     contracts in `xxxx-application` when they serve use case orchestration.

3. Boundary inversion.
   - Update application services to depend on contracts.
   - Move infrastructure implementations behind those contracts.
   - Remove `xxxx-infrastructure` from `xxxx-application/pom.xml`.
   - Keep Spring wiring in outer modules.

4. Domain cleanup.
   - Keep domain repository interfaces as the boundary used by application.
   - Do not require splitting JPA entities from domain entities in this story.
   - If domain service implementations are mostly persistence orchestration,
     move that orchestration into application services only when it is needed
     to remove the application-to-infrastructure dependency.

5. Legacy surface isolation.
   - Identify `/ticket/**`, `/order/**`, and `/payment/**` dependencies.
   - Either leave them isolated as compatibility routes or create a separate
     story for removal.

6. Architecture check.
   - Add a runnable forbidden-dependency check.
   - Include it in validation expectations.

7. Verification and docs.
   - Run validation commands.
   - Update `docs/TEST_MATRIX.md`, story validation evidence, and any affected
     product docs.
   - Record a decision if the persistence/domain split is deferred or if route
     removal is approved.

## Stop Conditions

Pause for human confirmation if:

- The implementation requires changing accepted API behavior.
- A database migration is needed.
- Legacy route removal is needed to complete the dependency inversion.
- The cleanup would require a full persistence rewrite or JPA/domain split.
- Existing auth, authorization, payment, or stock validation must be weakened.
- Frontend E07 failures block backend architecture proof.
