# Validation

## Proof Strategy

The story is done when backend tests prove register, login, refresh rotation,
logout, authenticated identity, and admin route rejection. Full frontend E2E is
out of scope for this backend slice.

## Test Plan

| Layer | Cases |
| --- | --- |
| Unit | JWT creation/validation, refresh token hashing, sequential rotation, concurrent single-consume rotation |
| Integration | Login cookie contract, refresh cookie rotation, logout clear cookie, request guard rejection, `/api/admin/**` rejects USER |
| E2E | Not required for this backend slice |
| Platform | Maven package and Docker Compose config |
| Performance | Not required |
| Logs/Audit | Verify no raw token/password logging by review |

## Fixtures

- Seed `USER` and `ADMIN` roles from `environment/mysql/init/ticket_init.sql`.
- Test users created during auth tests.

## Commands

```text
mvn test
mvn package
docker-compose -f environment/docker-compose-dev.yml config
```

## Acceptance Evidence

Implemented on 2026-05-23.

Commands run:

```text
mvn -pl xxxx-controller -am test
mvn test
mvn package
docker-compose -f environment/docker-compose-dev.yml config
```

Evidence:

- `mvn test` passed across the full Maven reactor.
- `mvn package` passed across the full Maven reactor.
- `docker-compose -f environment/docker-compose-dev.yml config` passed with the
  existing Docker warning: `Error loading config file: open
  C:\Users\trhoa\.docker\config.json: Access is denied.`
- Code review found one critical issue in non-atomic refresh rotation and
  important issues around cookie mutation guard, logout reachability, legacy
  order/payment userId exposure, tests, and Harness evidence.
- Review fixes applied: Redis refresh consume is atomic, refresh/logout require
  `X-Requested-With: XMLHttpRequest`, logout no longer requires an access token,
  legacy `/order/**` and `/payment/**` are restricted to `ADMIN` until E05/E06
  replace them, and controller cookie/status tests were added.
