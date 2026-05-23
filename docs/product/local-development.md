# Local Development

## Required Local Services

The required local stack is:

- MySQL.
- Redis.

Suggested ports from `SPEC.MD`:

```text
Backend: http://localhost:1122
Frontend: http://localhost:5173
MySQL: localhost:3316
Redis: localhost:6319
```

## Backend Modules

The backend keeps the current multi-module shape:

```text
xxxx-start
xxxx-controller
xxxx-application
xxxx-domain
xxxx-infrastructure
xxxx.fe.com
```

Responsibilities:

- `xxxx-start`: Spring Boot bootstrap, application config, profiles, main class.
- `xxxx-controller`: REST controllers, request/response DTOs, validation,
  exception handling, web security entry points.
- `xxxx-application`: use case orchestration, transaction boundaries, auth,
  token, order, payment, and cache orchestration.
- `xxxx-domain`: domain entities, value objects, enums, services, repository
  interfaces, and business rules.
- `xxxx-infrastructure`: JPA adapters, Redis adapter, Redis Lua stock adapter,
  JWT provider, OAuth2 adapter, VNPAY gateway adapter.
- `xxxx.fe.com`: React + Vite frontend.

## Required Documentation

- `README.md`
- `docs/ONBOARDING_REPORT.md`
- `docs/API.md`
- `.env.example`

## Removed From Required Runtime

- Kafka.
- Prometheus.
- Grafana.
- ELK.
- RabbitMQ.

Benchmark artifacts may be kept only as optional documentation if a later story
accepts that scope.

