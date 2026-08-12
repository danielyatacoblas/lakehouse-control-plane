# Runbook

- `401/403`: comprobar host del workspace, asignación del service principal y permisos `CAN USE/CAN MANAGE`.
- `429`: revisar frecuencia de solicitudes; el cliente reintenta tres veces con backoff. No crear bucles ilimitados.
- Ejecución detenida: consultar run id, cancelar desde la API y conservar el audit trail.
- Falla de calidad: revisar `quality_incident`; no promover Gold hasta corregir origen/regla y crear una nueva ejecución.
- Presupuesto: las cargas `full_refresh` requieren aprobación al superar el umbral. Eliminar el resource group de demo al finalizar.
