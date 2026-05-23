# Design

## Domain Model

No new domain model is introduced in this story. The story prepares the codebase
for the new event/ticket type/order/payment model by removing unrelated legacy
runtime concerns.

## Application Flow

Keep only the flows that remain useful for the target product foundation:

- Spring Boot startup.
- REST request handling.
- JPA persistence.
- Redis access.
- Existing payment gateway code only if it can be adapted to VNPAY sandbox.
- Existing stock/cache code only if it can be adapted to `ticketTypeId`.

Retire flows not in the target product:

- Kafka order queue flow.
- Employee demo flow.
- API key secure demo flow.
- Resilience4j demo endpoints.
- Monitoring-only runtime flow.

## Interface Contract

The story should not publish new target APIs. It may remove or retire legacy
demo routes from the required app surface. If a route is removed, README or API
docs must not continue advertising it as required behavior.

## Data Model

No schema changes are required in this story. The next story owns the clean
schema and migrations.

## UI / Platform Impact

The React frontend remains present but is not rebuilt in this story. Docker
Compose should define MySQL and Redis as the required local services. Kafka,
Prometheus, Grafana, ELK, and exporters should not be required to start the
target app.

## Observability

Keep normal application logging. Do not keep monitoring-only dependencies or
services as required runtime. If optional dashboards or benchmark docs remain,
they must be clearly outside the required product path.

## Alternatives Considered

1. Remove every legacy artifact immediately. Rejected because optional docs and
   reusable Redis/VNPAY code may still be valuable.
2. Keep all current infrastructure. Rejected because it conflicts with the spec
   and makes the CV project harder to run.

