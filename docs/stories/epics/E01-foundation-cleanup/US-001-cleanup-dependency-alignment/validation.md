# Validation

## Proof Strategy

Proof should show that the required app foundation builds and runs without
Kafka, monitoring, or demo-only runtime services, while retaining MySQL, Redis,
JPA, Spring Boot modules, and React.

## Test Plan

| Layer | Cases |
| --- | --- |
| Unit | Existing unit tests still pass if present |
| Integration | Spring context starts without Kafka/monitoring required config; JPA and Redis beans are available |
| E2E | Not required for this cleanup story |
| Platform | Docker Compose starts MySQL and Redis as the only required services |
| Performance | Not required |
| Logs/Audit | Startup logs show no missing Kafka/monitoring provider failures |

## Fixtures

- Local MySQL service.
- Local Redis service.
- Existing Spring Boot application config.

## Commands

Commands used for this implementation pass:

```text
mvn test
mvn package
npm --prefix xxxx.fe.com ci
npm --prefix xxxx.fe.com run build
docker compose -f environment/docker-compose-dev.yml config
docker-compose -f environment/docker-compose-dev.yml config
```

## Acceptance Evidence

- Backend environment: commands were run with temporary
  `JAVA_HOME=C:\Program Files\Android\openjdk\jdk-21.0.8` and `PATH` prepended
  in this shell. The host should set this globally or provide another Java 21
  JDK before running backend validation without the temporary override.
- `mvn test`: passed after allowing Maven network access to resolve
  dependencies. The full reactor succeeded for `xxxx.com`, `xxxx-domain`,
  `xxxx-infrastructure`, `xxxx-application`, `xxxx-controller`, and
  `xxxx-start`.
- `mvn package`: passed after allowing Maven network access to resolve
  packaging plugins. The full reactor succeeded and `xxxx-start` was
  repackaged as a Spring Boot jar.
- `npm --prefix xxxx.fe.com ci`: passed after allowing npm to write its dependency cache outside the workspace.
- `npm --prefix xxxx.fe.com run build`: passed. Vite produced the production frontend build.
- `docker compose -f environment/docker-compose-dev.yml config`: blocked in this shell because `docker` did not accept the Compose plugin `-f` flag and could not read `C:\Users\trhoa\.docker\config.json`.
- `docker-compose -f environment/docker-compose-dev.yml config`: passed and showed only MySQL and Redis services. Docker still warned that it could not read `C:\Users\trhoa\.docker\config.json`.
- Source/config scan: passed with no remaining Kafka, MQ order, queue table,
  demo controller, Resilience4j, Prometheus, Grafana, or Logstash references in
  checked source/config paths.
- Code review: completed by reviewer agent `Turing`. No critical issues were found. Two important issues were fixed: `howtostart.md` now uses `mvn` instead of missing `./mvnw`, and Spring Security/OAuth2 dependencies are scoped to `xxxx-controller` instead of the parent POM.

US-001 is implemented. No E2E proof was required for this cleanup story.
