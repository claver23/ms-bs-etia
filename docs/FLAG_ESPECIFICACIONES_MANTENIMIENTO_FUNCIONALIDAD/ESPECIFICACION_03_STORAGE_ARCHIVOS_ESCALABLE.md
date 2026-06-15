# ESPECIFICACION 03 - STORAGE DE ARCHIVOS ESCALABLE

## Objetivo
Preparar almacenamiento de XML/PDF para despliegue horizontal.

## Lineamientos
- Desacoplar del filesystem local de instancia.
- Definir interfaz de storage (local vs objeto remoto).
- Soportar proveedor tipo S3/MinIO en perfiles productivos.

## Criterios de aceptacion
- Archivos accesibles desde cualquier instancia.
- Rutas/identificadores persistidos y trazables.
- Estrategia de migracion desde almacenamiento actual documentada.

