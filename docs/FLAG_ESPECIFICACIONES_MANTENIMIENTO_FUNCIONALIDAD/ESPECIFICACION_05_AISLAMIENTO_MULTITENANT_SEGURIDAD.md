# ESPECIFICACION 05 - AISLAMIENTO MULTITENANT Y SEGURIDAD

## Objetivo
Garantizar aislamiento de datos por empresa en todas las operaciones.

## Lineamientos
- Validar coherencia entre `companyId` del token y solicitudes.
- Restringir accesos cruzados entre empresas.
- Definir excepciones controladas para rol administrador global.

## Criterios de aceptacion
- No existe lectura/escritura cruzada entre tenants.
- Auditoria de accesos por empresa disponible.
- Pruebas de seguridad negativas y positivas documentadas.

