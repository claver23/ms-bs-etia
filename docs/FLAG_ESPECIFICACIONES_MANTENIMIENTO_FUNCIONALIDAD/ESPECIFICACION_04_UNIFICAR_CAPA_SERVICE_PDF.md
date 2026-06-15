# ESPECIFICACION 04 - UNIFICAR DESCARGA PDF EN CAPA SERVICE

## Objetivo
Concentrar logica de descarga/generacion PDF en services de dominio.

## Lineamientos
- Controllers deben delegar al service, sin acceso directo a repositorios.
- Reutilizar reglas de negocio en un unico punto.
- Mantener consistencia entre endpoints OpenAPI y controllers auxiliares.

## Criterios de aceptacion
- Sin logica de negocio en controller.
- Trazabilidad uniforme de errores y validaciones.
- Menor duplicidad de parseo y armado de datos.

