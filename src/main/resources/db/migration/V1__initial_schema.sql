-- =====================================================================
-- ETIA v1.0 — Initial Schema (19 tablas + users)
-- Compatible H2 + PostgreSQL (ANSI SQL)
-- =====================================================================

-- 1. companies
CREATE TABLE companies (
    id                    VARCHAR(255) PRIMARY KEY,
    ruc                   VARCHAR(11) UNIQUE,
    razon_social          VARCHAR(255),
    nombre_comercial      VARCHAR(255),
    codigo_ubigeo         VARCHAR(6),
    direccion             VARCHAR(255),
    urbanizacion          VARCHAR(255),
    departamento          VARCHAR(255),
    provincia             VARCHAR(255),
    distrito              VARCHAR(255),
    usuario_sol           VARCHAR(255),
    clave_sol             VARCHAR(255),
    certificado_path      VARCHAR(255),
    certificado_password  VARCHAR(255),
    email_contacto        VARCHAR(255),
    telefono              VARCHAR(255),
    regimen               VARCHAR(255),
    estado                VARCHAR(255),
    logo_path             VARCHAR(255),
    logo_format           VARCHAR(255),
    emision_en_envio      BOOLEAN DEFAULT TRUE,
    es_demo               BOOLEAN DEFAULT FALSE,
    fecha_creacion        TIMESTAMP,
    fecha_actualizacion   TIMESTAMP
);

-- 2. clients
CREATE TABLE clients (
    id                    VARCHAR(255) PRIMARY KEY,
    company_id            VARCHAR(255),
    tipo_documento        VARCHAR(255),
    numero_documento      VARCHAR(255),
    denominacion          VARCHAR(255),
    direccion             VARCHAR(255),
    departamento          VARCHAR(255),
    provincia             VARCHAR(255),
    distrito              VARCHAR(255),
    codigo_pais           VARCHAR(2),
    email                 VARCHAR(255),
    telefono              VARCHAR(255),
    es_demo               BOOLEAN DEFAULT FALSE,
    fecha_creacion        TIMESTAMP
);

-- 3. products
CREATE TABLE products (
    id                    VARCHAR(255) PRIMARY KEY,
    company_id            VARCHAR(255),
    codigo_producto       VARCHAR(255),
    codigo_producto_sunat VARCHAR(8),
    descripcion           VARCHAR(255),
    unidad_medida         VARCHAR(255),
    tipo_afectacion_igv   VARCHAR(2),
    valor_unitario        DECIMAL(12,2),
    precio_unitario       DECIMAL(12,2),
    igv                   DECIMAL(12,2),
    estado                VARCHAR(255) DEFAULT 'ACTIVO',
    es_demo               BOOLEAN DEFAULT FALSE,
    fecha_creacion        TIMESTAMP
);

-- 4. employees
CREATE TABLE employees (
    id                    VARCHAR(255) PRIMARY KEY,
    company_id            VARCHAR(255) NOT NULL,
    nombre                VARCHAR(255),
    cargo                 VARCHAR(255),
    dni                   VARCHAR(255) UNIQUE,
    codigo                VARCHAR(255) UNIQUE,
    estado                VARCHAR(255) DEFAULT 'ACTIVO',
    es_demo               BOOLEAN DEFAULT FALSE,
    fecha_creacion        TIMESTAMP
);

-- 5. ubigeos
CREATE TABLE ubigeos (
    id                    VARCHAR(255) PRIMARY KEY,
    codigo                VARCHAR(6) UNIQUE NOT NULL,
    departamento          VARCHAR(255),
    provincia             VARCHAR(255),
    distrito              VARCHAR(255)
);

-- 6. unidades_medida
CREATE TABLE unidades_medida (
    id                    VARCHAR(255) PRIMARY KEY,
    codigo                VARCHAR(5) UNIQUE NOT NULL,
    descripcion           VARCHAR(255)
);

-- 7. sedes
CREATE TABLE sedes (
    id                    VARCHAR(255) PRIMARY KEY,
    company_id            VARCHAR(255) NOT NULL,
    codigo                VARCHAR(3) NOT NULL,
    nombre                VARCHAR(255) NOT NULL,
    direccion             VARCHAR(255),
    codigo_ubigeo         VARCHAR(6),
    departamento          VARCHAR(255),
    provincia             VARCHAR(255),
    distrito              VARCHAR(255),
    telefono              VARCHAR(255),
    email                 VARCHAR(255),
    estado                VARCHAR(255) DEFAULT 'ACTIVO',
    es_default            BOOLEAN DEFAULT FALSE,
    fecha_creacion        TIMESTAMP,
    UNIQUE (company_id, codigo)
);

-- 8. series_config
CREATE TABLE series_config (
    id                    VARCHAR(255) PRIMARY KEY,
    company_id            VARCHAR(255) NOT NULL,
    sede_id               VARCHAR(255),
    tipo_documento        VARCHAR(255) NOT NULL,
    serie                 VARCHAR(4) NOT NULL,
    descripcion           VARCHAR(255),
    es_default            BOOLEAN DEFAULT FALSE,
    activa                BOOLEAN DEFAULT TRUE,
    fecha_creacion        TIMESTAMP,
    UNIQUE (company_id, tipo_documento, serie)
);

-- 9. correlativos
CREATE TABLE correlativos (
    id                    VARCHAR(255) PRIMARY KEY,
    company_id            VARCHAR(255) NOT NULL,
    serie                 VARCHAR(4) NOT NULL,
    ultimo_numero         INTEGER NOT NULL DEFAULT 0,
    UNIQUE (company_id, serie)
);

-- 10. facturas
CREATE TABLE facturas (
    id                           VARCHAR(255) PRIMARY KEY,
    company_id                   VARCHAR(255),
    client_id                    VARCHAR(255),
    serie                        VARCHAR(255),
    numero                       INTEGER,
    fecha_emision                DATE,
    moneda                       VARCHAR(255),
    forma_pago                   VARCHAR(255),
    tipo_cambio                  DECIMAL(10,4),
    fecha_vencimiento            DATE,
    codigo_ubigeo                VARCHAR(6),
    tipo_operacion               VARCHAR(4),
    hora_emision                 TIME,
    gravadas                     DECIMAL(12,2),
    exoneradas                   DECIMAL(12,2),
    inafectas                    DECIMAL(12,2),
    gratuitas                    DECIMAL(12,2),
    igv                          DECIMAL(12,2),
    total                        DECIMAL(12,2),
    total_cargos                 DECIMAL(12,2),
    total_descuentos             DECIMAL(12,2),
    subtotal                     DECIMAL(12,2),
    total_con_impuestos          DECIMAL(12,2),
    estado                       VARCHAR(255),
    hash_cdr                     VARCHAR(255),
    codigo_respuesta_sunat       VARCHAR(255),
    descripcion_respuesta_sunat  VARCHAR(255),
    leyenda                      VARCHAR(255),
    detalle_json                 TEXT,
    xml_content                  TEXT,
    xml_path                     VARCHAR(255),
    errores_validacion           TEXT,
    fecha_creacion               TIMESTAMP
);

-- 11. boletas
CREATE TABLE boletas (
    id                           VARCHAR(255) PRIMARY KEY,
    company_id                   VARCHAR(255),
    client_id                    VARCHAR(255),
    serie                        VARCHAR(255),
    numero                       INTEGER,
    fecha_emision                DATE,
    moneda                       VARCHAR(255),
    forma_pago                   VARCHAR(255),
    tipo_cambio                  DECIMAL(10,4),
    fecha_vencimiento            DATE,
    codigo_ubigeo                VARCHAR(6),
    tipo_operacion               VARCHAR(4),
    hora_emision                 TIME,
    gravadas                     DECIMAL(12,2),
    exoneradas                   DECIMAL(12,2),
    inafectas                    DECIMAL(12,2),
    gratuitas                    DECIMAL(12,2),
    igv                          DECIMAL(12,2),
    total                        DECIMAL(12,2),
    total_cargos                 DECIMAL(12,2),
    total_descuentos             DECIMAL(12,2),
    subtotal                     DECIMAL(12,2),
    total_con_impuestos          DECIMAL(12,2),
    estado                       VARCHAR(255),
    hash_cdr                     VARCHAR(255),
    codigo_respuesta_sunat       VARCHAR(255),
    descripcion_respuesta_sunat  VARCHAR(255),
    leyenda                      VARCHAR(255),
    detalle_json                 TEXT,
    xml_content                  TEXT,
    xml_path                     VARCHAR(255),
    errores_validacion           TEXT,
    resumen_diario_id            VARCHAR(255),
    fecha_creacion               TIMESTAMP
);

-- 12. notas_credito
CREATE TABLE notas_credito (
    id                           VARCHAR(255) PRIMARY KEY,
    company_id                   VARCHAR(255),
    client_id                    VARCHAR(255),
    documento_referencia_id      VARCHAR(255),
    serie_doc_ref                VARCHAR(255),
    numero_doc_ref               INTEGER,
    tipo_documento_referencia    VARCHAR(255),
    motivo                       VARCHAR(255),
    codigo_motivo_sunat          VARCHAR(2),
    monto_descuento              DECIMAL(12,2),
    descripcion                  VARCHAR(255),
    serie                        VARCHAR(255),
    numero                       INTEGER,
    fecha_emision                DATE,
    moneda                       VARCHAR(255),
    forma_pago                   VARCHAR(255),
    tipo_cambio                  DECIMAL(10,4),
    fecha_vencimiento            DATE,
    codigo_ubigeo                VARCHAR(6),
    tipo_operacion               VARCHAR(4),
    hora_emision                 TIME,
    gravadas                     DECIMAL(12,2),
    exoneradas                   DECIMAL(12,2),
    inafectas                    DECIMAL(12,2),
    gratuitas                    DECIMAL(12,2),
    igv                          DECIMAL(12,2),
    total                        DECIMAL(12,2),
    total_cargos                 DECIMAL(12,2),
    total_descuentos             DECIMAL(12,2),
    subtotal                     DECIMAL(12,2),
    total_con_impuestos          DECIMAL(12,2),
    estado                       VARCHAR(255),
    hash_cdr                     VARCHAR(255),
    codigo_respuesta_sunat       VARCHAR(255),
    descripcion_respuesta_sunat  VARCHAR(255),
    leyenda                      VARCHAR(255),
    detalle_json                 TEXT,
    xml_content                  TEXT,
    xml_path                     VARCHAR(255),
    errores_validacion           TEXT,
    fecha_creacion               TIMESTAMP
);

-- 13. notas_debito
CREATE TABLE notas_debito (
    id                           VARCHAR(255) PRIMARY KEY,
    company_id                   VARCHAR(255),
    client_id                    VARCHAR(255),
    documento_referencia_id      VARCHAR(255),
    serie_doc_ref                VARCHAR(255),
    numero_doc_ref               INTEGER,
    tipo_documento_referencia    VARCHAR(255),
    motivo                       VARCHAR(255),
    codigo_motivo_sunat          VARCHAR(2),
    monto_incremento             DECIMAL(12,2),
    descripcion                  VARCHAR(255),
    serie                        VARCHAR(255),
    numero                       INTEGER,
    fecha_emision                DATE,
    moneda                       VARCHAR(255),
    forma_pago                   VARCHAR(255),
    tipo_cambio                  DECIMAL(10,4),
    fecha_vencimiento            DATE,
    codigo_ubigeo                VARCHAR(6),
    tipo_operacion               VARCHAR(4),
    hora_emision                 TIME,
    gravadas                     DECIMAL(12,2),
    exoneradas                   DECIMAL(12,2),
    inafectas                    DECIMAL(12,2),
    gratuitas                    DECIMAL(12,2),
    igv                          DECIMAL(12,2),
    total                        DECIMAL(12,2),
    total_cargos                 DECIMAL(12,2),
    total_descuentos             DECIMAL(12,2),
    subtotal                     DECIMAL(12,2),
    total_con_impuestos          DECIMAL(12,2),
    estado                       VARCHAR(255),
    hash_cdr                     VARCHAR(255),
    codigo_respuesta_sunat       VARCHAR(255),
    descripcion_respuesta_sunat  VARCHAR(255),
    leyenda                      VARCHAR(255),
    detalle_json                 TEXT,
    xml_content                  TEXT,
    xml_path                     VARCHAR(255),
    errores_validacion           TEXT,
    fecha_creacion               TIMESTAMP
);

-- 14. cuotas_credito
CREATE TABLE cuotas_credito (
    id                    VARCHAR(255) PRIMARY KEY,
    factura_id            VARCHAR(255),
    boleta_id             VARCHAR(255),
    numero_cuota          VARCHAR(20) NOT NULL,
    monto                 DECIMAL(12,2) NOT NULL,
    fecha_vencimiento     DATE NOT NULL,
    moneda                VARCHAR(3)
);

-- 15. allowance_charges
CREATE TABLE allowance_charges (
    id                    VARCHAR(255) PRIMARY KEY,
    factura_id            VARCHAR(255),
    boleta_id             VARCHAR(255),
    charge_indicator      BOOLEAN NOT NULL,
    reason_code           VARCHAR(4),
    reason_description    VARCHAR(250),
    multiplier_factor     DECIMAL(8,4),
    amount                DECIMAL(12,2) NOT NULL,
    base_amount           DECIMAL(12,2),
    moneda                VARCHAR(3)
);

-- 16. user_preferences
CREATE TABLE user_preferences (
    id                    VARCHAR(255) PRIMARY KEY,
    company_id            VARCHAR(255) NOT NULL,
    clave                 VARCHAR(255) NOT NULL,
    valor                 VARCHAR(500),
    fecha_actualizacion   TIMESTAMP,
    UNIQUE (company_id, clave)
);

-- 17. tipo_cambio
CREATE TABLE tipo_cambio (
    id                    VARCHAR(255) PRIMARY KEY,
    fecha                 DATE,
    moneda_origen         VARCHAR(3),
    moneda_destino        VARCHAR(3),
    tasa_compra           DECIMAL(10,4),
    tasa_venta            DECIMAL(10,4),
    fuente                VARCHAR(10),
    fecha_creacion        TIMESTAMP
);

-- 18. libros_electronicos
CREATE TABLE libros_electronicos (
    id                    VARCHAR(255) PRIMARY KEY,
    company_id            VARCHAR(255),
    tipo                  VARCHAR(255),
    periodo               VARCHAR(255),
    formato               VARCHAR(255),
    estado                VARCHAR(255),
    archivo_url           VARCHAR(255),
    total_registros       INTEGER,
    fecha_generacion      TIMESTAMP,
    fecha_creacion        TIMESTAMP
);

-- 19. resumenes_diarios
CREATE TABLE resumenes_diarios (
    id                    VARCHAR(255) PRIMARY KEY,
    company_id            VARCHAR(255) NOT NULL,
    identificador         VARCHAR(255) UNIQUE NOT NULL,
    fecha_generacion      DATE NOT NULL,
    fecha_referencia      DATE NOT NULL,
    estado                VARCHAR(255) NOT NULL,
    ticket_sunat          VARCHAR(255),
    codigo_respuesta      VARCHAR(255),
    descripcion_respuesta VARCHAR(255),
    cantidad_boletas      INTEGER,
    xml_content           TEXT,
    fecha_creacion        TIMESTAMP
);

-- 20. users (Sprint 3 — Seguridad)
CREATE TABLE users (
    id                    VARCHAR(255) PRIMARY KEY,
    email                 VARCHAR(255) UNIQUE NOT NULL,
    password_hash         VARCHAR(255) NOT NULL,
    nombre                VARCHAR(255),
    company_id            VARCHAR(255),
    rol                   VARCHAR(50) DEFAULT 'USER',
    activo                BOOLEAN DEFAULT TRUE,
    fecha_creacion        TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

