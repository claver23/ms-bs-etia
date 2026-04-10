-- ==================================================================
-- ETIA -- Initial Seed Data (H2 In-Memory)
-- Se ejecuta al arrancar si spring.sql.init.mode=always
-- Solo datos de referencia -- sin documentos demo
-- ==================================================================

-- ====== COMPANY ======
INSERT INTO companies (id, ruc, razon_social, nombre_comercial, codigo_ubigeo, direccion, urbanizacion,
    departamento, provincia, distrito, usuario_sol, clave_sol, email_contacto, telefono, regimen, estado,
     emision_en_envio, logo_format, es_demo, fecha_creacion, fecha_actualizacion)
VALUES ('cmp-demo-001', '20601234567', 'ETLIONS S.A.C.', 'ETLIONS', '150131',
    'Av. Javier Prado Este 1234, Piso 8', 'San Isidro Business Center',
    'LIMA', 'LIMA', 'SAN ISIDRO', 'MODDATOS', 'MODDATOS',
    'facturacion@etlions.com', '+51 999 888 777', 'general', 'ACTIVO',
    TRUE, 'png', TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

-- ====== CLIENTS ======
INSERT INTO clients (id, company_id, tipo_documento, numero_documento, denominacion, direccion, email, telefono, es_demo, fecha_creacion) VALUES
('cli-demo-001', 'cmp-demo-001', '6', '20505050501', 'INVERSIONES TECH PERU S.A.C.'
    , 'Calle Los Pinos 456, Miraflores', 'compras@techperu.com', '+51 998 776 554', TRUE, CURRENT_TIMESTAMP),
('cli-demo-002', 'cmp-demo-001', '1', '45678912', 'Maria Garcia Perez'
    , 'Jr. Huallaga 234, Lima', 'maria.garcia@gmail.com', '+51 987 654 321', TRUE, CURRENT_TIMESTAMP),
('cli-demo-003', 'cmp-demo-001', '6', '20612345678', 'GLOBAL IMPORTS E.I.R.L.'
    , 'Av. Argentina 1580, Callao', 'ventas@globalimports.pe', '+51 945 123 456', TRUE, CURRENT_TIMESTAMP),
('cli-demo-004', 'cmp-demo-001', '4', '001234567', 'Jean Pierre Dupont'
    , 'Calle Berlin 980, Miraflores', 'jean.dupont@mail.com', '+51 912 345 678', TRUE, CURRENT_TIMESTAMP),
('cli-demo-005', 'cmp-demo-001', '7', 'JP4567890', 'Akira Tanaka'
    , 'Av. La Encalada 1257, Surco', 'akira.tanaka@email.jp', '+51 923 456 789', TRUE, CURRENT_TIMESTAMP),
('cli-demo-006', 'cmp-demo-001', '6', '20498765432', 'DISTRIBUIDORA ANDINA S.A.'
    , 'Av. Industrial 2050, Ate', 'contabilidad@andina.pe', '+51 914 567 890', TRUE, CURRENT_TIMESTAMP),
('cli-demo-007', 'cmp-demo-001', '6', '20301987654', 'MINERA CUSCO GOLD S.A.C.'
    , 'Jr. Cusco 340, San Borja', 'compras@cuscogold.pe', '+51 976 543 210', TRUE, CURRENT_TIMESTAMP),
('cli-demo-008', 'cmp-demo-001', '1', '71234567', 'Luis Alberto Sanchez Ruiz'
    , 'Av. Arequipa 3200, Lince', 'luis.sanchez@outlook.com', '+51 934 567 891', TRUE, CURRENT_TIMESTAMP),
('cli-demo-009', 'cmp-demo-001', '6', '20556677889', 'LOGISTICA EXPRESS PERU S.R.L.'
    , 'Av. Colonial 1890, Cercado', 'facturacion@logexpress.pe', '+51 956 789 012', TRUE, CURRENT_TIMESTAMP),
('cli-demo-010', 'cmp-demo-001', '1', '43215678', 'Ana Cecilia Torres Vega'
    , 'Jr. Lampa 567, Centro de Lima', 'ana.torres@gmail.com', '+51 945 678 123', TRUE, CURRENT_TIMESTAMP);

-- ====== PRODUCTS ======
INSERT INTO products (id, company_id, codigo_producto, codigo_producto_sunat, descripcion, unidad_medida, tipo_afectacion_igv, valor_unitario, precio_unitario, igv, estado, es_demo, fecha_creacion) VALUES
('prd-demo-001', 'cmp-demo-001', 'SRV-001', '82101500', 'Servicio de Consultoria en TI', 'ZZ', '10', 2500.00, 2950.00, 450.00, 'ACTIVO', TRUE, CURRENT_TIMESTAMP),
('prd-demo-002', 'cmp-demo-001', 'LIC-002', '43231500', 'Licencia Software ERP Anual', 'ZZ', '10', 1800.00, 2124.00, 324.00, 'ACTIVO', TRUE, CURRENT_TIMESTAMP),
('prd-demo-003', 'cmp-demo-001', 'SPT-003', '81112200', 'Soporte Tecnico Premium (mensual)', 'ZZ', '10', 350.00, 413.00, 63.00, 'ACTIVO', TRUE, CURRENT_TIMESTAMP),
('prd-demo-004', 'cmp-demo-001', 'HW-004', '43211503', 'Laptop Empresarial ThinkPad T14', 'NIU', '10', 4200.00, 4956.00, 756.00, 'ACTIVO', TRUE, CURRENT_TIMESTAMP),
('prd-demo-005', 'cmp-demo-001', 'HW-005', '43211900', 'Monitor 27" 4K USB-C', 'NIU', '10', 1500.00, 1770.00, 270.00, 'ACTIVO', TRUE, CURRENT_TIMESTAMP),
('prd-demo-006', 'cmp-demo-001', 'SRV-006', '86132000', 'Capacitacion Equipo (8 horas)', 'HUR', '10', 300.00, 354.00, 54.00, 'ACTIVO', TRUE, CURRENT_TIMESTAMP),
('prd-demo-007', 'cmp-demo-001', 'SRV-007', '81112100', 'Hosting Cloud (mensual)', 'MON', '10', 450.00, 531.00, 81.00, 'ACTIVO', TRUE, CURRENT_TIMESTAMP),
('prd-demo-008', 'cmp-demo-001', 'LIC-008', '43233200', 'Certificado Digital SUNAT', 'ZZ', '10', 280.00, 330.40, 50.40, 'ACTIVO', TRUE, CURRENT_TIMESTAMP);

-- ====== EMPLOYEES ======
INSERT INTO employees (id, company_id, nombre, cargo, dni, codigo, estado, es_demo, fecha_creacion) VALUES
('emp-demo-001', 'cmp-demo-001', 'Carlos Mendoza Rivera', 'Contador Principal', '45678901', 'EMP-001', 'ACTIVO', TRUE, CURRENT_TIMESTAMP),
('emp-demo-002', 'cmp-demo-001', 'Maria Lopez Gutierrez', 'Asistente Contable', '32165498', 'EMP-002', 'ACTIVO', TRUE, CURRENT_TIMESTAMP),
('emp-demo-003', 'cmp-demo-001', 'Jorge Ramirez Torres', 'Cajero', '78945612', 'EMP-003', 'ACTIVO', TRUE, CURRENT_TIMESTAMP);

-- ====== UBIGEOS (catalogo SUNAT) ======
INSERT INTO ubigeos (id, codigo, departamento, provincia, distrito) VALUES
('ubg-001', '150101', 'LIMA', 'LIMA', 'LIMA'),
('ubg-002', '150131', 'LIMA', 'LIMA', 'SAN ISIDRO'),
('ubg-003', '150122', 'LIMA', 'LIMA', 'MIRAFLORES'),
('ubg-004', '150132', 'LIMA', 'LIMA', 'SAN BORJA'),
('ubg-005', '150140', 'LIMA', 'LIMA', 'SANTIAGO DE SURCO'),
('ubg-006', '040101', 'AREQUIPA', 'AREQUIPA', 'AREQUIPA'),
('ubg-007', '130101', 'LA LIBERTAD', 'TRUJILLO', 'TRUJILLO');

-- ====== UNIDADES DE MEDIDA (catalogo SUNAT) ======
INSERT INTO unidades_medida (id, codigo, descripcion) VALUES
('um-001', 'NIU', 'Unidad (und)'),
('um-002', 'ZZ', 'Servicio'),
('um-003', 'HUR', 'Hora'),
('um-004', 'KGM', 'Kilogramo'),
('um-005', 'TNE', 'Tonelada'),
('um-006', 'LTR', 'Litro'),
('um-007', 'MTR', 'Metro'),
('um-008', 'MTK', 'Metro cuadrado'),
('um-009', 'GLL', 'Galon'),
('um-010', 'DAY', 'Dia'),
('um-011', 'MON', 'Mes'),
('um-012', 'BX', 'Caja'),
('um-013', 'DZN', 'Docena'),
('um-014', 'PK', 'Paquete');

-- ====== SEDES ======
INSERT INTO sedes (id, company_id, codigo, nombre, direccion, codigo_ubigeo, departamento, provincia, distrito, telefono, email, estado, es_default, fecha_creacion) VALUES
('sede-001', 'cmp-demo-001', '001', 'Sede Principal San Isidro'
    , 'Av. Javier Prado Este 1234, Piso 8', '150131', 'LIMA', 'LIMA', 'SAN ISIDRO'
    , '+51 999 888 777', 'sede.principal@etlions.com', 'ACTIVO', TRUE, CURRENT_TIMESTAMP),
('sede-002', 'cmp-demo-001', '002', 'Sucursal Miraflores'
    , 'Calle Berlin 567, Oficina 201', '150122', 'LIMA', 'LIMA', 'MIRAFLORES'
    , '+51 955 444 333', 'miraflores@etlions.com', 'ACTIVO', FALSE, CURRENT_TIMESTAMP);

-- ====== SERIES CONFIG (vinculadas a sedes) ======
INSERT INTO series_config (id, company_id, sede_id, tipo_documento, serie, descripcion, es_default, activa, fecha_creacion) VALUES
('ser-001', 'cmp-demo-001', 'sede-001', 'FACTURA',       'F001', 'Factura - Sede Principal San Isidro',       TRUE,  TRUE, CURRENT_TIMESTAMP),
('ser-002', 'cmp-demo-001', 'sede-001', 'BOLETA',        'B001', 'Boleta - Sede Principal San Isidro',        TRUE,  TRUE, CURRENT_TIMESTAMP),
('ser-003', 'cmp-demo-001', 'sede-001', 'NOTA_CREDITO',  'FC01', 'Nota Credito - Sede Principal San Isidro',  TRUE,  TRUE, CURRENT_TIMESTAMP),
('ser-004', 'cmp-demo-001', 'sede-001', 'NOTA_DEBITO',   'FD01', 'Nota Debito - Sede Principal San Isidro',   TRUE,  TRUE, CURRENT_TIMESTAMP),
('ser-005', 'cmp-demo-001', 'sede-002', 'FACTURA',       'F002', 'Factura - Sucursal Miraflores',       FALSE, TRUE, CURRENT_TIMESTAMP),
('ser-006', 'cmp-demo-001', 'sede-002', 'BOLETA',        'B002', 'Boleta - Sucursal Miraflores',        FALSE, TRUE, CURRENT_TIMESTAMP),
('ser-007', 'cmp-demo-001', 'sede-002', 'NOTA_CREDITO',  'FC02', 'Nota Credito - Sucursal Miraflores',  FALSE, TRUE, CURRENT_TIMESTAMP),
('ser-008', 'cmp-demo-001', 'sede-002', 'NOTA_DEBITO',   'FD02', 'Nota Debito - Sucursal Miraflores',   FALSE, TRUE, CURRENT_TIMESTAMP);

-- ====== CORRELATIVOS (todos en 0 -- sin documentos precargados) ======
INSERT INTO correlativos (id, company_id, serie, ultimo_numero) VALUES
('cor-001', 'cmp-demo-001', 'F001', 0),
('cor-002', 'cmp-demo-001', 'B001', 0),
('cor-003', 'cmp-demo-001', 'F002', 0),
('cor-004', 'cmp-demo-001', 'B002', 0),
('cor-005', 'cmp-demo-001', 'FC01', 0),
('cor-006', 'cmp-demo-001', 'FD01', 0),
('cor-007', 'cmp-demo-001', 'FC02', 0),
('cor-008', 'cmp-demo-001', 'FD02', 0);

-- ====== USER PREFERENCES ======
INSERT INTO user_preferences (id, company_id, clave, valor, fecha_actualizacion) VALUES
('pref-001', 'cmp-demo-001', 'moneda', 'PEN', CURRENT_TIMESTAMP),
('pref-002', 'cmp-demo-001', 'tema', 'dark', CURRENT_TIMESTAMP),
('pref-003', 'cmp-demo-001', 'defaultItemType', 'servicio', CURRENT_TIMESTAMP),
('pref-004', 'cmp-demo-001', 'tipoCambioFuente', 'SBS', CURRENT_TIMESTAMP),
('pref-005', 'cmp-demo-001', 'emisionEnEnvio', 'true', CURRENT_TIMESTAMP),
('pref-006', 'cmp-demo-001', 'defaultCuotas', '1', CURRENT_TIMESTAMP),
('pref-007', 'cmp-demo-001', 'cuotaDistribucion', 'FIJA_30D', CURRENT_TIMESTAMP),
('pref-008', 'cmp-demo-001', 'documentFormat', 'standard', CURRENT_TIMESTAMP);

-- ====== TIPO DE CAMBIO (seed ultimos dias) ======
INSERT INTO tipo_cambio (id, fecha, moneda_origen, moneda_destino, tasa_compra, tasa_venta, fuente, fecha_creacion) VALUES
('tc-001', '2026-04-06', 'USD', 'PEN', 3.7100, 3.7520, 'SBS', CURRENT_TIMESTAMP),
('tc-002', '2026-04-05', 'USD', 'PEN', 3.7050, 3.7480, 'SBS', CURRENT_TIMESTAMP),
('tc-003', '2026-04-04', 'USD', 'PEN', 3.7080, 3.7500, 'SBS', CURRENT_TIMESTAMP),
('tc-004', '2026-04-03', 'USD', 'PEN', 3.7120, 3.7540, 'SBS', CURRENT_TIMESTAMP),
('tc-005', '2026-04-02', 'USD', 'PEN', 3.7090, 3.7510, 'SBS', CURRENT_TIMESTAMP),
('tc-006', '2026-04-06', 'USD', 'PEN', 3.7110, 3.7530, 'SUNAT', CURRENT_TIMESTAMP),
('tc-007', '2026-04-05', 'USD', 'PEN', 3.7060, 3.7490, 'SUNAT', CURRENT_TIMESTAMP);

-- ====== LIBROS ELECTRONICOS (seed) ======
INSERT INTO libros_electronicos (id, company_id, tipo, periodo, formato, estado, archivo_url, total_registros, fecha_generacion, fecha_creacion) VALUES
('lib-001', 'cmp-demo-001', 'ventas', '2026-03', 'TXT', 'GENERADO', '/libros/cmp-demo-001/ventas-2026-03.txt', 45, TIMESTAMP '2026-04-01 08:00:00', CURRENT_TIMESTAMP),
('lib-002', 'cmp-demo-001', 'compras', '2026-03', 'TXT', 'GENERADO', '/libros/cmp-demo-001/compras-2026-03.txt', 32, TIMESTAMP '2026-04-01 08:15:00', CURRENT_TIMESTAMP),
('lib-003', 'cmp-demo-001', 'ventas', '2026-02', 'TXT', 'GENERADO', '/libros/cmp-demo-001/ventas-2026-02.txt', 38, TIMESTAMP '2026-03-01 09:00:00', CURRENT_TIMESTAMP);

-- ====== BACKFILL: Clients direccion estructurada + pais ======
UPDATE clients SET departamento = 'LIMA', provincia = 'LIMA', distrito = 'MIRAFLORES', codigo_pais = 'PE' WHERE id = 'cli-demo-001';
UPDATE clients SET departamento = 'LIMA', provincia = 'LIMA', distrito = 'LIMA',       codigo_pais = 'PE' WHERE id = 'cli-demo-002';
UPDATE clients SET departamento = 'CALLAO', provincia = 'CALLAO', distrito = 'CALLAO', codigo_pais = 'PE' WHERE id = 'cli-demo-003';
UPDATE clients SET departamento = 'LIMA', provincia = 'LIMA', distrito = 'MIRAFLORES', codigo_pais = 'FR' WHERE id = 'cli-demo-004';
UPDATE clients SET departamento = 'LIMA', provincia = 'LIMA', distrito = 'SANTIAGO DE SURCO', codigo_pais = 'JP' WHERE id = 'cli-demo-005';
UPDATE clients SET departamento = 'LIMA', provincia = 'LIMA', distrito = 'ATE',        codigo_pais = 'PE' WHERE id = 'cli-demo-006';
UPDATE clients SET departamento = 'LIMA', provincia = 'LIMA', distrito = 'SAN BORJA',  codigo_pais = 'PE' WHERE id = 'cli-demo-007';
UPDATE clients SET departamento = 'LIMA', provincia = 'LIMA', distrito = 'LINCE',      codigo_pais = 'PE' WHERE id = 'cli-demo-008';
UPDATE clients SET departamento = 'LIMA', provincia = 'LIMA', distrito = 'CERCADO',    codigo_pais = 'PE' WHERE id = 'cli-demo-009';
UPDATE clients SET departamento = 'LIMA', provincia = 'LIMA', distrito = 'CERCADO DE LIMA', codigo_pais = 'PE' WHERE id = 'cli-demo-010';

-- ====== USERS (Sprint 3 — Seguridad) ======
-- Password: Etia2025! (BCrypt hash)
INSERT INTO users (id, email, password_hash, nombre, company_id, rol, activo, fecha_creacion)
VALUES ('usr-admin-001', 'admin@etlions.com',
        '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy',
        'Admin ETLIONS', 'cmp-demo-001', 'ADMIN', TRUE, CURRENT_TIMESTAMP);

