-- ═══════════════════════════════════════════════════════════════
-- ETIA — Initial Seed Data (H2 In-Memory)
-- Se ejecuta al arrancar si spring.sql.init.mode=always
-- ═══════════════════════════════════════════════════════════════

-- ══════ COMPANY ══════
INSERT INTO companies (id, ruc, razon_social, nombre_comercial, codigo_ubigeo, direccion, urbanizacion,
    departamento, provincia, distrito, usuario_sol, clave_sol, email_contacto, telefono, regimen, estado,
    fecha_creacion, fecha_actualizacion)
VALUES ('cmp-demo-001', '20601234567', 'ETLIONS S.A.C.', 'ETLIONS', '150131',
    'Av. Javier Prado Este 1234, Piso 8', 'San Isidro Business Center',
    'LIMA', 'LIMA', 'SAN ISIDRO', 'MODDATOS', 'MODDATOS',
    'facturacion@etlions.com', '+51 999 888 777', 'general', 'ACTIVO',
    CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

-- ══════ CLIENT ══════
INSERT INTO clients (id, company_id, tipo_documento, numero_documento, denominacion, direccion, email, telefono, fecha_creacion) VALUES
('cli-demo-001', 'cmp-demo-001', '6', '20505050501', 'INVERSIONES TECH PERU S.A.C.',
    'Calle Los Pinos 456, Miraflores', 'compras@techperu.com', '+51 998 776 554', CURRENT_TIMESTAMP),
('cli-demo-002', 'cmp-demo-001', '1', '45678912', 'María García Pérez',
    'Jr. Huallaga 234, Lima', 'maria.garcia@gmail.com', '+51 987 654 321', CURRENT_TIMESTAMP),
('cli-demo-003', 'cmp-demo-001', '6', '20612345678', 'GLOBAL IMPORTS E.I.R.L.',
    'Av. Argentina 1580, Callao', 'ventas@globalimports.pe', '+51 945 123 456', CURRENT_TIMESTAMP),
('cli-demo-004', 'cmp-demo-001', '4', '001234567', 'Jean Pierre Dupont',
    'Calle Berlín 980, Miraflores', 'jean.dupont@mail.com', '+51 912 345 678', CURRENT_TIMESTAMP),
('cli-demo-005', 'cmp-demo-001', '7', 'JP4567890', 'Akira Tanaka',
    'Av. La Encalada 1257, Surco', 'akira.tanaka@email.jp', '+51 923 456 789', CURRENT_TIMESTAMP);

-- ══════ PRODUCTS ══════
INSERT INTO products (id, company_id, codigo_producto, descripcion, unidad_medida, valor_unitario, precio_unitario, igv, estado, fecha_creacion) VALUES
('prd-demo-001', 'cmp-demo-001', 'SRV-001', 'Servicio de Consultoría en TI', 'ZZ', 2500.00, 2950.00, 450.00, 'ACTIVO', CURRENT_TIMESTAMP),
('prd-demo-002', 'cmp-demo-001', 'LIC-002', 'Licencia Software ERP Anual', 'ZZ', 1800.00, 2124.00, 324.00, 'ACTIVO', CURRENT_TIMESTAMP),
('prd-demo-003', 'cmp-demo-001', 'SPT-003', 'Soporte Técnico Premium (mensual)', 'ZZ', 350.00, 413.00, 63.00, 'ACTIVO', CURRENT_TIMESTAMP);

-- ══════ EMPLOYEES ══════
INSERT INTO employees (id, company_id, nombre, cargo, dni, codigo, estado, fecha_creacion) VALUES
('emp-demo-001', 'cmp-demo-001', 'Carlos Mendoza Rivera', 'Contador Principal', '45678901', 'EMP-001', 'ACTIVO', CURRENT_TIMESTAMP),
('emp-demo-002', 'cmp-demo-001', 'María López Gutiérrez', 'Asistente Contable', '32165498', 'EMP-002', 'ACTIVO', CURRENT_TIMESTAMP),
('emp-demo-003', 'cmp-demo-001', 'Jorge Ramírez Torres', 'Cajero', '78945612', 'EMP-003', 'ACTIVO', CURRENT_TIMESTAMP);

-- ══════ UBIGEOS (catálogo SUNAT) ══════
INSERT INTO ubigeos (id, codigo, departamento, provincia, distrito) VALUES
('ubg-001', '150101', 'LIMA', 'LIMA', 'LIMA'),
('ubg-002', '150131', 'LIMA', 'LIMA', 'SAN ISIDRO'),
('ubg-003', '150122', 'LIMA', 'LIMA', 'MIRAFLORES'),
('ubg-004', '150132', 'LIMA', 'LIMA', 'SAN BORJA'),
('ubg-005', '150140', 'LIMA', 'LIMA', 'SANTIAGO DE SURCO'),
('ubg-006', '040101', 'AREQUIPA', 'AREQUIPA', 'AREQUIPA'),
('ubg-007', '130101', 'LA LIBERTAD', 'TRUJILLO', 'TRUJILLO');

-- ══════ UNIDADES DE MEDIDA (catálogo SUNAT) ══════
INSERT INTO unidades_medida (id, codigo, descripcion) VALUES
('um-001', 'NIU', 'Unidad (und)'),
('um-002', 'ZZ', 'Servicio'),
('um-003', 'HUR', 'Hora'),
('um-004', 'KGM', 'Kilogramo'),
('um-005', 'TNE', 'Tonelada'),
('um-006', 'LTR', 'Litro'),
('um-007', 'MTR', 'Metro'),
('um-008', 'MTK', 'Metro cuadrado'),
('um-009', 'GLL', 'Galón'),
('um-010', 'DAY', 'Día'),
('um-011', 'MON', 'Mes'),
('um-012', 'BX', 'Caja'),
('um-013', 'DZN', 'Docena'),
('um-014', 'PK', 'Paquete');

-- ══════ SERIES CONFIG ══════
INSERT INTO series_config (id, company_id, tipo_documento, serie, descripcion, es_default, activa, fecha_creacion) VALUES
('ser-001', 'cmp-demo-001', 'FACTURA',       'F001', 'Serie principal Factura',        TRUE,  TRUE, CURRENT_TIMESTAMP),
('ser-002', 'cmp-demo-001', 'BOLETA',        'B001', 'Serie principal Boleta',         TRUE,  TRUE, CURRENT_TIMESTAMP),
('ser-003', 'cmp-demo-001', 'NOTA_CREDITO',  'FC01', 'Serie principal Nota Crédito',   TRUE,  TRUE, CURRENT_TIMESTAMP),
('ser-004', 'cmp-demo-001', 'NOTA_DEBITO',   'FD01', 'Serie principal Nota Débito',    TRUE,  TRUE, CURRENT_TIMESTAMP);

-- ══════ FACTURAS (documentos de prueba) ══════
INSERT INTO facturas (id, company_id, client_id, serie, numero, fecha_emision, moneda, gravadas, igv, total, estado, leyenda, detalle_json, fecha_creacion) VALUES
('fac-demo-001', 'cmp-demo-001', 'cli-demo-001', 'F001', 1, DATE '2026-03-15', 'PEN', 2500.00, 450.00, 2950.00, 'ACEPTADA',
    'SON DOS MIL NOVECIENTOS CINCUENTA Y 00/100 SOLES',
    '[{"descripcion":"Servicio de Consultoría en TI","unidadMedida":"ZZ","cantidad":1,"valorUnitario":2500.00,"precioUnitario":2950.00,"igv":450.00,"subtotal":2500.00}]',
    CURRENT_TIMESTAMP),
('fac-demo-002', 'cmp-demo-001', 'cli-demo-003', 'F001', 2, DATE '2026-03-20', 'PEN', 5100.00, 918.00, 6018.00, 'BORRADOR',
    'SON SEIS MIL DIECIOCHO Y 00/100 SOLES',
    '[{"descripcion":"Licencia Software ERP Anual","unidadMedida":"ZZ","cantidad":2,"valorUnitario":1800.00,"precioUnitario":2124.00,"igv":324.00,"subtotal":1800.00},{"descripcion":"Soporte Técnico Premium (mensual)","unidadMedida":"ZZ","cantidad":3,"valorUnitario":500.00,"precioUnitario":590.00,"igv":90.00,"subtotal":500.00}]',
    CURRENT_TIMESTAMP),
('fac-demo-003', 'cmp-demo-001', 'cli-demo-001', 'F001', 3, DATE '2026-04-01', 'USD', 1200.00, 216.00, 1416.00, 'ACEPTADA',
    'SON MIL CUATROCIENTOS DIECISEIS Y 00/100 DOLARES AMERICANOS',
    '[{"descripcion":"Consultoría Cloud AWS (2 sesiones)","unidadMedida":"ZZ","cantidad":2,"valorUnitario":600.00,"precioUnitario":708.00,"igv":108.00,"subtotal":600.00}]',
    CURRENT_TIMESTAMP);

-- ══════ BOLETAS (documentos de prueba) ══════
INSERT INTO boletas (id, company_id, client_id, serie, numero, fecha_emision, moneda, gravadas, igv, total, estado, leyenda, detalle_json, fecha_creacion) VALUES
('bol-demo-001', 'cmp-demo-001', 'cli-demo-002', 'B001', 1, DATE '2026-03-18', 'PEN', 350.00, 63.00, 413.00, 'ACEPTADA',
    'SON CUATROCIENTOS TRECE Y 00/100 SOLES',
    '[{"descripcion":"Soporte Técnico Premium (mensual)","unidadMedida":"ZZ","cantidad":1,"valorUnitario":350.00,"precioUnitario":413.00,"igv":63.00,"subtotal":350.00}]',
    CURRENT_TIMESTAMP),
('bol-demo-002', 'cmp-demo-001', 'cli-demo-002', 'B001', 2, DATE '2026-04-02', 'PEN', 2500.00, 450.00, 2950.00, 'BORRADOR',
    'SON DOS MIL NOVECIENTOS CINCUENTA Y 00/100 SOLES',
    '[{"descripcion":"Servicio de Consultoría en TI","unidadMedida":"ZZ","cantidad":1,"valorUnitario":2500.00,"precioUnitario":2950.00,"igv":450.00,"subtotal":2500.00}]',
    CURRENT_TIMESTAMP);

-- ══════ NOTAS DE CRÉDITO (documentos de prueba) ══════
INSERT INTO notas_credito (id, company_id, factura_referencia_id, motivo, monto_descuento, tipo_documento, descripcion, estado, detalle_json, fecha_creacion) VALUES
('nc-demo-001', 'cmp-demo-001', 'fac-demo-001', 'descuento', 500.00, 'factura',
    'Descuento comercial acordado post-venta 20%', 'ACEPTADA',
    '[{"descripcion":"Descuento sobre Consultoría en TI","unidadMedida":"ZZ","cantidad":1,"valorUnitario":423.73,"precioUnitario":500.00,"igv":76.27,"subtotal":423.73}]',
    CURRENT_TIMESTAMP),
('nc-demo-002', 'cmp-demo-001', 'fac-demo-002', 'anulacion', 6018.00, 'factura',
    'Anulación total por error en datos del cliente', 'BORRADOR',
    '[{"descripcion":"Anulación total Factura F001-2","unidadMedida":"ZZ","cantidad":1,"valorUnitario":5100.00,"precioUnitario":6018.00,"igv":918.00,"subtotal":5100.00}]',
    CURRENT_TIMESTAMP);

-- ══════ NOTAS DE DÉBITO (documentos de prueba) ══════
INSERT INTO notas_debito (id, company_id, factura_referencia_id, motivo, monto_incremento, descripcion, estado, detalle_json, fecha_creacion) VALUES
('nd-demo-001', 'cmp-demo-001', 'fac-demo-001', 'intereses', 250.00,
    'Intereses por pago fuera de plazo (30 días)', 'ACEPTADA',
    '[{"descripcion":"Intereses moratorios sobre Factura F001-1","unidadMedida":"ZZ","cantidad":1,"valorUnitario":211.86,"precioUnitario":250.00,"igv":38.14,"subtotal":211.86}]',
    CURRENT_TIMESTAMP),
('nd-demo-002', 'cmp-demo-001', 'fac-demo-003', 'gastos_administrativos', 150.00,
    'Gastos administrativos por gestión de cobranza', 'BORRADOR',
    '[{"descripcion":"Gastos administrativos cobranza","unidadMedida":"ZZ","cantidad":1,"valorUnitario":127.12,"precioUnitario":150.00,"igv":22.88,"subtotal":127.12}]',
    CURRENT_TIMESTAMP);

-- ══════ CORRELATIVOS (sincronizar con facturas/boletas seed) ══════
INSERT INTO correlativos (id, company_id, serie, ultimo_numero) VALUES
('cor-001', 'cmp-demo-001', 'F001', 3),
('cor-002', 'cmp-demo-001', 'B001', 2);

-- ══════ USER PREFERENCES ══════
INSERT INTO user_preferences (id, company_id, clave, valor, fecha_actualizacion) VALUES
('pref-001', 'cmp-demo-001', 'moneda', 'PEN', CURRENT_TIMESTAMP),
('pref-002', 'cmp-demo-001', 'tema', 'dark', CURRENT_TIMESTAMP),
('pref-003', 'cmp-demo-001', 'defaultItemType', 'servicio', CURRENT_TIMESTAMP);

