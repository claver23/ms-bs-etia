# FLAG ESPECIFICACIONES - MANTENIMIENTO Y FUNCIONALIDAD

Este directorio es el **punto maestro de referencia** para:
- especificaciones actuales,
- nuevas especificaciones,
- ajustes de mantenimiento,
- cambios funcionales.

## Regla de uso (flag global)
En cualquier chat futuro, si se menciona:
- `FLAG_ESPECIFICACIONES_MANTENIMIENTO_FUNCIONALIDAD`
- `carpeta flag de especificaciones`

se debe revisar este directorio primero.

## Alcance de implementacion
- El **flag es global** para todo el proyecto.
- Cada especificacion define si aplica a **todo** o a **modulos concretos**.
- Si una especificacion no trae mapeo por archivo, se debe crear antes de ejecutar cambios.

## Prioridades vigentes
1. **ESPECIFICACION_01_CORRELATIVO_CONCURRENCIA.md** (PRIORIDAD ALTA)
2. **ESPECIFICACION_02_PAGINACION_REAL.md** (PRIORIDAD ALTA)
3. **ESPECIFICACION_03_STORAGE_ARCHIVOS_ESCALABLE.md**
4. **ESPECIFICACION_04_UNIFICAR_CAPA_SERVICE_PDF.md**
5. **ESPECIFICACION_05_AISLAMIENTO_MULTITENANT_SEGURIDAD.md**

## Convencion para nuevas especificaciones
- Crear archivo con formato: `ESPECIFICACION_XX_NOMBRE_CLARO.md`
- Mantener este README como indice y prioridad de ejecucion
- Si una nueva especificacion pasa a prioridad alta, moverla a las posiciones 1 o 2
- Incluir seccion obligatoria: `Mapa de archivos impactados`
