# Harness Backlog

Use this file when an agent discovers a missing harness capability but should
not change the operating model immediately.

## Template

```md
## Missing Harness Capability

### Title

Short name.

### Discovered While

Task or story that exposed the gap.

### Current Pain

What was hard, repeated, ambiguous, or unsafe?

### Suggested Improvement

What should be added or changed?

### Risk

Tiny, normal, or high-risk.

### Status

proposed | accepted | implemented | rejected
```

## Items

## Missing Harness Capability

### Title

Validation prerequisite checklist.

### Discovered While

Implementing `US-001-cleanup-dependency-alignment`.

### Current Pain

The story validation commands identified local environment blockers only after
implementation: Java was unavailable or `JAVA_HOME` was invalid, npm needed
permission to write its dependency cache, and the shell required
`docker-compose` instead of the `docker compose` plugin form.

### Suggested Improvement

Add a lightweight validation-prerequisites section or template that asks agents
to check Java/JAVA_HOME, Node dependencies, npm cache access, and Docker Compose
command shape before running a story's proof commands.

### Risk

Tiny.

### Status

proposed
