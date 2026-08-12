# Lakehouse Control Plane

Backend Java 17 + Spring Boot para gobernar ejecuciones Azure Databricks: catálogo, parámetros seguros, costo, aprobación, Jobs API 2.2, calidad, incidentes y auditoría.

## Capacidades demostrables

- `run-now` con `idempotency_token` igual al UUID interno.
- Reintento con backoff ante HTTP 429 y polling de estado.
- Allowlist de parámetros para evitar inyección en jobs/notebooks.
- Full refresh con costo 2.5× y aprobación obligatoria sobre el umbral.
- Reglas de calidad que bloquean la promoción y abren incidente.
- Cancelación protegida por rol y registro de cada acción.
- Dashboard, health/Prometheus, PostgreSQL/Flyway, Docker y CI.
- Simulador local autónomo; Bicep para Azure Container Apps con Managed Identity.

## Demo local sin costo Azure

```bash
docker compose up -d --wait
./gradlew bootRun
```

Ejecución normal con throttling simulado:

```bash
curl -X POST http://localhost:8098/api/v1/executions -H 'Content-Type: application/json' \
  -d '{"pipelineKey":"sales-bronze-gold","requestedBy":"ana@example.com","parameters":{"source_date":"2026-08-11","simulate_rate_limit":"true"}}'
```

Consulta `GET /api/v1/executions/{id}` tras unos segundos. Para calidad fallida agrega `"fail_quality":"true"`.

Un full refresh queda `APPROVAL_REQUIRED` con costo `100.00`:

```bash
curl -X POST http://localhost:8098/api/v1/executions/{id}/approve \
  -H 'X-Role: DATA_APPROVER' -H 'X-Actor: supervisor@example.com'
```

La integración Azure usa `DATABRICKS_HOST` y credenciales OAuth/identidad administrada externas. No se versionan tokens personales.

Consulta [docs/ARCHITECTURE.md](docs/ARCHITECTURE.md) y [docs/RUNBOOK.md](docs/RUNBOOK.md).
