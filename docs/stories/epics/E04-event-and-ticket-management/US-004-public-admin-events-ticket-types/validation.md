# Validation

## Planned Proof

- `mvn -pl xxxx-controller -am test`
- `mvn test`
- `mvn package`
- `docker-compose -f environment\docker-compose-dev.yml config`

## Evidence

- pass: `mvn -pl xxxx-controller -am test`
- pass: `mvn test`
- pass: `mvn package`
- pass: `docker-compose -f environment\docker-compose-dev.yml config`

Docker Compose emitted the known local warning:

```text
Error loading config file: open C:\Users\trhoa\.docker\config.json: Access is denied.
```

## Notes

- Application proof covers active public filtering, inactive event rejection, active ticket type filtering, event/ticket type validation, soft delete, and Redis cache key invalidation.
- Controller proof covers public unauthenticated access, inactive/missing public detail `404`, admin `401`/`403`, admin create success, and missing event handling for ticket type creation.
