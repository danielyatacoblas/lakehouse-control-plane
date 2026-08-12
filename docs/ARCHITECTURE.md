# Arquitectura

```text
API Java → catálogo/costo/aprobación → PostgreSQL + auditoría
                    │
                    └→ Databricks Jobs API 2.2 (idempotency_token)
                                      │
                       polling con retry 429/backoff
                                      │
                         calidad → éxito o incidente
```

PostgreSQL guarda control, costo estimado y auditoría; los datos analíticos permanecen en el lakehouse. El perfil `local-fake` implementa el contrato HTTP de Jobs API sin consumir Azure. En producción se desactiva ese perfil y `DATABRICKS_HOST` apunta al workspace.

La API local usa encabezados de rol para hacer visible el límite de autorización. En Azure se reemplazan por claims verificados de Entra ID. La infraestructura Bicep asigna identidad administrada a Container Apps; los secretos no se escriben en el repositorio.
