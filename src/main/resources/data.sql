-- ═══════════════════════════════════════════════════════════════
-- ETIA — Initial Seed Data (H2 In-Memory)
-- Se ejecuta al arrancar si spring.sql.init.mode=always
-- ═══════════════════════════════════════════════════════════════

-- ══════ COMPANY ══════
INSERT INTO companies (id, ruc, razon_social, nombre_comercial, codigo_ubigeo, direccion, urbanizacion,
    departamento, provincia, distrito, usuario_sol, clave_sol, email_contacto, telefono, regimen, estado,
     emision_en_envio, logo_format, fecha_creacion, fecha_actualizacion)
VALUES ('cmp-demo-001', '20601234567', 'ETLIONS S.A.C.', 'ETLIONS', '150131',
    'Av. Javier Prado Este 1234, Piso 8', 'San Isidro Business Center',
    'LIMA', 'LIMA', 'SAN ISIDRO', 'MODDATOS', 'MODDATOS',
    'facturacion@etlions.com', '+51 999 888 777', 'general', 'ACTIVO',
    TRUE, 'png', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

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
    'Av. La Encalada 1257, Surco', 'akira.tanaka@email.jp', '+51 923 456 789', CURRENT_TIMESTAMP),
('cli-demo-006', 'cmp-demo-001', '6', '20498765432', 'DISTRIBUIDORA ANDINA S.A.',
    'Av. Industrial 2050, Ate', 'contabilidad@andina.pe', '+51 914 567 890', CURRENT_TIMESTAMP),
('cli-demo-007', 'cmp-demo-001', '6', '20301987654', 'MINERA CUSCO GOLD S.A.C.',
    'Jr. Cusco 340, San Borja', 'compras@cuscogold.pe', '+51 976 543 210', CURRENT_TIMESTAMP),
('cli-demo-008', 'cmp-demo-001', '1', '71234567', 'Luis Alberto Sánchez Ruiz',
    'Av. Arequipa 3200, Lince', 'luis.sanchez@outlook.com', '+51 934 567 891', CURRENT_TIMESTAMP),
('cli-demo-009', 'cmp-demo-001', '6', '20556677889', 'LOGÍSTICA EXPRESS PERU S.R.L.',
    'Av. Colonial 1890, Cercado', 'facturacion@logexpress.pe', '+51 956 789 012', CURRENT_TIMESTAMP),
('cli-demo-010', 'cmp-demo-001', '1', '43215678', 'Ana Cecilia Torres Vega',
    'Jr. Lampa 567, Centro de Lima', 'ana.torres@gmail.com', '+51 945 678 123', CURRENT_TIMESTAMP);

-- ══════ PRODUCTS ══════
INSERT INTO products (id, company_id, codigo_producto, codigo_producto_sunat, descripcion, unidad_medida, tipo_afectacion_igv, valor_unitario, precio_unitario, igv, estado, fecha_creacion) VALUES
('prd-demo-001', 'cmp-demo-001', 'SRV-001', '82101500', 'Servicio de Consultoría en TI', 'ZZ', '10', 2500.00, 2950.00, 450.00, 'ACTIVO', CURRENT_TIMESTAMP),
('prd-demo-002', 'cmp-demo-001', 'LIC-002', '43231500', 'Licencia Software ERP Anual', 'ZZ', '10', 1800.00, 2124.00, 324.00, 'ACTIVO', CURRENT_TIMESTAMP),
('prd-demo-003', 'cmp-demo-001', 'SPT-003', '81112200', 'Soporte Técnico Premium (mensual)', 'ZZ', '10', 350.00, 413.00, 63.00, 'ACTIVO', CURRENT_TIMESTAMP),
('prd-demo-004', 'cmp-demo-001', 'HW-004', '43211503', 'Laptop Empresarial ThinkPad T14', 'NIU', '10', 4200.00, 4956.00, 756.00, 'ACTIVO', CURRENT_TIMESTAMP),
('prd-demo-005', 'cmp-demo-001', 'HW-005', '43211900', 'Monitor 27" 4K USB-C', 'NIU', '10', 1500.00, 1770.00, 270.00, 'ACTIVO', CURRENT_TIMESTAMP),
('prd-demo-006', 'cmp-demo-001', 'SRV-006', '86132000', 'Capacitación Equipo (8 horas)', 'HUR', '10', 300.00, 354.00, 54.00, 'ACTIVO', CURRENT_TIMESTAMP),
('prd-demo-007', 'cmp-demo-001', 'SRV-007', '81112100', 'Hosting Cloud (mensual)', 'MON', '10', 450.00, 531.00, 81.00, 'ACTIVO', CURRENT_TIMESTAMP),
('prd-demo-008', 'cmp-demo-001', 'LIC-008', '43233200', 'Certificado Digital SUNAT', 'ZZ', '10', 280.00, 330.40, 50.40, 'ACTIVO', CURRENT_TIMESTAMP);

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

-- ══════ SEDES ══════
INSERT INTO sedes (id, company_id, codigo, nombre, direccion, codigo_ubigeo, departamento, provincia, distrito, telefono, email, estado, es_default, fecha_creacion) VALUES
('sede-001', 'cmp-demo-001', '001', 'Sede Principal San Isidro',
    'Av. Javier Prado Este 1234, Piso 8', '150131', 'LIMA', 'LIMA', 'SAN ISIDRO',
    '+51 999 888 777', 'sede.principal@etlions.com', 'ACTIVO', TRUE, CURRENT_TIMESTAMP),
('sede-002', 'cmp-demo-001', '002', 'Sucursal Miraflores',
    'Calle Berlín 567, Oficina 201', '150122', 'LIMA', 'LIMA', 'MIRAFLORES',
    '+51 955 444 333', 'miraflores@etlions.com', 'ACTIVO', FALSE, CURRENT_TIMESTAMP);

-- ══════ SERIES CONFIG (vinculadas a sedes) ══════
INSERT INTO series_config (id, company_id, sede_id, tipo_documento, serie, descripcion, es_default, activa, fecha_creacion) VALUES
-- Sede Principal (001)
('ser-001', 'cmp-demo-001', 'sede-001', 'FACTURA',       'F001', 'Factura — Sede Principal San Isidro',       TRUE,  TRUE, CURRENT_TIMESTAMP),
('ser-002', 'cmp-demo-001', 'sede-001', 'BOLETA',        'B001', 'Boleta — Sede Principal San Isidro',        TRUE,  TRUE, CURRENT_TIMESTAMP),
('ser-003', 'cmp-demo-001', 'sede-001', 'NOTA_CREDITO',  'FC01', 'Nota Crédito — Sede Principal San Isidro',  TRUE,  TRUE, CURRENT_TIMESTAMP),
('ser-004', 'cmp-demo-001', 'sede-001', 'NOTA_DEBITO',   'FD01', 'Nota Débito — Sede Principal San Isidro',   TRUE,  TRUE, CURRENT_TIMESTAMP),
-- Sucursal Miraflores (002)
('ser-005', 'cmp-demo-001', 'sede-002', 'FACTURA',       'F002', 'Factura — Sucursal Miraflores',       FALSE, TRUE, CURRENT_TIMESTAMP),
('ser-006', 'cmp-demo-001', 'sede-002', 'BOLETA',        'B002', 'Boleta — Sucursal Miraflores',        FALSE, TRUE, CURRENT_TIMESTAMP),
('ser-007', 'cmp-demo-001', 'sede-002', 'NOTA_CREDITO',  'FC02', 'Nota Crédito — Sucursal Miraflores',  FALSE, TRUE, CURRENT_TIMESTAMP),
('ser-008', 'cmp-demo-001', 'sede-002', 'NOTA_DEBITO',   'FD02', 'Nota Débito — Sucursal Miraflores',   FALSE, TRUE, CURRENT_TIMESTAMP);

-- ══════ FACTURAS (documentos de prueba — 5 estados, variedad de clientes/series/monedas) ══════

-- ── ACEPTADAS (6) — exitosamente procesadas por SUNAT ──
INSERT INTO facturas (id, company_id, client_id, serie, numero, fecha_emision, moneda, forma_pago, fecha_vencimiento, codigo_ubigeo, gravadas, igv, total, estado, hash_cdr, codigo_respuesta_sunat, descripcion_respuesta_sunat, leyenda, detalle_json, fecha_creacion) VALUES
('fac-demo-001', 'cmp-demo-001', 'cli-demo-001', 'F001', 1, DATE '2026-03-01', 'PEN', 'CONTADO', DATE '2026-03-01', '150131', 2500.00, 450.00, 2950.00, 'ACEPTADA',
    'a1b2c3d4e5f6a7b8c9d0', '0', 'La Factura fue aceptada',
    'SON DOS MIL NOVECIENTOS CINCUENTA Y 00/100 SOLES',
    '[{"descripcion":"Servicio de Consultoría en TI","unidadMedida":"ZZ","cantidad":1,"valorUnitario":2500.00,"precioUnitario":2950.00,"igv":450.00,"subtotal":2500.00,"tipoAfectacionIgv":"10","codigoTributo":"1000","tipoPrecio":"01","porcentajeIgv":18.00,"baseImponible":2500.00,"montoImpuesto":450.00}]',
    TIMESTAMP '2026-03-01 09:15:00'),
('fac-demo-002', 'cmp-demo-001', 'cli-demo-003', 'F001', 2, DATE '2026-03-05', 'PEN', 'CREDITO', DATE '2026-04-05', '150131', 5100.00, 918.00, 6018.00, 'ACEPTADA',
    'b2c3d4e5f6a7b8c9d0e1', '0', 'La Factura fue aceptada',
    'SON SEIS MIL DIECIOCHO Y 00/100 SOLES',
    '[{"descripcion":"Licencia Software ERP Anual","unidadMedida":"ZZ","cantidad":2,"valorUnitario":1800.00,"precioUnitario":2124.00,"igv":324.00,"subtotal":3600.00,"tipoAfectacionIgv":"10","codigoTributo":"1000","tipoPrecio":"01","porcentajeIgv":18.00,"baseImponible":3600.00,"montoImpuesto":648.00},{"descripcion":"Soporte Técnico Premium (mensual)","unidadMedida":"ZZ","cantidad":3,"valorUnitario":500.00,"precioUnitario":590.00,"igv":90.00,"subtotal":1500.00,"tipoAfectacionIgv":"10","codigoTributo":"1000","tipoPrecio":"01","porcentajeIgv":18.00,"baseImponible":1500.00,"montoImpuesto":270.00}]',
    TIMESTAMP '2026-03-05 10:30:00'),
('fac-demo-003', 'cmp-demo-001', 'cli-demo-001', 'F001', 3, DATE '2026-03-10', 'USD', 'CONTADO', DATE '2026-03-10', '150131', 1200.00, 216.00, 1416.00, 'ACEPTADA',
    'c3d4e5f6a7b8c9d0e1f2', '0', 'La Factura fue aceptada',
    'SON MIL CUATROCIENTOS DIECISEIS Y 00/100 DOLARES AMERICANOS',
    '[{"descripcion":"Consultoría Cloud AWS (2 sesiones)","unidadMedida":"ZZ","cantidad":2,"valorUnitario":600.00,"precioUnitario":708.00,"igv":108.00,"subtotal":1200.00,"tipoAfectacionIgv":"10","codigoTributo":"1000","tipoPrecio":"01","porcentajeIgv":18.00,"baseImponible":1200.00,"montoImpuesto":216.00}]',
    TIMESTAMP '2026-03-10 14:20:00'),
('fac-demo-004', 'cmp-demo-001', 'cli-demo-006', 'F002', 1, DATE '2026-03-12', 'PEN', 'CONTADO', DATE '2026-03-12', '150122', 4200.00, 756.00, 4956.00, 'ACEPTADA',
    'd4e5f6a7b8c9d0e1f2g3', '0', 'La Factura fue aceptada',
    'SON CUATRO MIL NOVECIENTOS CINCUENTA Y SEIS Y 00/100 SOLES',
    '[{"descripcion":"Laptop Empresarial ThinkPad T14","unidadMedida":"NIU","cantidad":1,"valorUnitario":4200.00,"precioUnitario":4956.00,"igv":756.00,"subtotal":4200.00,"tipoAfectacionIgv":"10","codigoTributo":"1000","tipoPrecio":"01","porcentajeIgv":18.00,"baseImponible":4200.00,"montoImpuesto":756.00}]',
    TIMESTAMP '2026-03-12 11:00:00'),
('fac-demo-005', 'cmp-demo-001', 'cli-demo-007', 'F001', 4, DATE '2026-03-15', 'PEN', 'CREDITO', DATE '2026-04-15', '150132', 7500.00, 1350.00, 8850.00, 'ACEPTADA',
    'e5f6a7b8c9d0e1f2g3h4', '0', 'La Factura fue aceptada',
    'SON OCHO MIL OCHOCIENTOS CINCUENTA Y 00/100 SOLES',
    '[{"descripcion":"Servicio de Consultoría en TI","unidadMedida":"ZZ","cantidad":3,"valorUnitario":2500.00,"precioUnitario":2950.00,"igv":450.00,"subtotal":7500.00,"tipoAfectacionIgv":"10","codigoTributo":"1000","tipoPrecio":"01","porcentajeIgv":18.00,"baseImponible":7500.00,"montoImpuesto":1350.00}]',
    TIMESTAMP '2026-03-15 16:45:00'),
('fac-demo-006', 'cmp-demo-001', 'cli-demo-009', 'F002', 2, DATE '2026-03-18', 'PEN', 'CONTADO', DATE '2026-03-18', '150122', 900.00, 162.00, 1062.00, 'ACEPTADA',
    'f6a7b8c9d0e1f2g3h4i5', '0', 'La Factura fue aceptada',
    'SON MIL SESENTA Y DOS Y 00/100 SOLES',
    '[{"descripcion":"Hosting Cloud (mensual)","unidadMedida":"MON","cantidad":2,"valorUnitario":450.00,"precioUnitario":531.00,"igv":81.00,"subtotal":900.00,"tipoAfectacionIgv":"10","codigoTributo":"1000","tipoPrecio":"01","porcentajeIgv":18.00,"baseImponible":900.00,"montoImpuesto":162.00}]',
    TIMESTAMP '2026-03-18 08:30:00'),

-- ── ENVIADAS (4) — en tránsito a SUNAT, pendiente de respuesta ──
('fac-demo-007', 'cmp-demo-001', 'cli-demo-001', 'F001', 5, DATE '2026-03-22', 'PEN', 'CONTADO', DATE '2026-03-22', '150131', 1500.00, 270.00, 1770.00, 'ENVIADA',
    NULL, NULL, NULL,
    'SON MIL SETECIENTOS SETENTA Y 00/100 SOLES',
    '[{"descripcion":"Monitor 27 4K USB-C","unidadMedida":"NIU","cantidad":1,"valorUnitario":1500.00,"precioUnitario":1770.00,"igv":270.00,"subtotal":1500.00,"tipoAfectacionIgv":"10","codigoTributo":"1000","tipoPrecio":"01","porcentajeIgv":18.00,"baseImponible":1500.00,"montoImpuesto":270.00}]',
    TIMESTAMP '2026-03-22 09:00:00'),
('fac-demo-008', 'cmp-demo-001', 'cli-demo-003', 'F001', 6, DATE '2026-03-25', 'PEN', 'CREDITO', DATE '2026-04-25', '150131', 3600.00, 648.00, 4248.00, 'ENVIADA',
    NULL, NULL, NULL,
    'SON CUATRO MIL DOSCIENTOS CUARENTA Y OCHO Y 00/100 SOLES',
    '[{"descripcion":"Licencia Software ERP Anual","unidadMedida":"ZZ","cantidad":2,"valorUnitario":1800.00,"precioUnitario":2124.00,"igv":324.00,"subtotal":3600.00,"tipoAfectacionIgv":"10","codigoTributo":"1000","tipoPrecio":"01","porcentajeIgv":18.00,"baseImponible":3600.00,"montoImpuesto":648.00}]',
    TIMESTAMP '2026-03-25 10:15:00'),
('fac-demo-009', 'cmp-demo-001', 'cli-demo-006', 'F002', 3, DATE '2026-03-28', 'USD', 'CONTADO', DATE '2026-03-28', '150122', 2400.00, 432.00, 2832.00, 'ENVIADA',
    NULL, NULL, NULL,
    'SON DOS MIL OCHOCIENTOS TREINTA Y DOS Y 00/100 DOLARES AMERICANOS',
    '[{"descripcion":"Capacitación Equipo (8 horas)","unidadMedida":"HUR","cantidad":8,"valorUnitario":300.00,"precioUnitario":354.00,"igv":54.00,"subtotal":2400.00,"tipoAfectacionIgv":"10","codigoTributo":"1000","tipoPrecio":"01","porcentajeIgv":18.00,"baseImponible":2400.00,"montoImpuesto":432.00}]',
    TIMESTAMP '2026-03-28 15:30:00'),
('fac-demo-010', 'cmp-demo-001', 'cli-demo-007', 'F001', 7, DATE '2026-04-01', 'PEN', 'CONTADO', DATE '2026-04-01', '150131', 280.00, 50.40, 330.40, 'ENVIADA',
    NULL, NULL, NULL,
    'SON TRESCIENTOS TREINTA Y 40/100 SOLES',
    '[{"descripcion":"Certificado Digital SUNAT","unidadMedida":"ZZ","cantidad":1,"valorUnitario":280.00,"precioUnitario":330.40,"igv":50.40,"subtotal":280.00,"tipoAfectacionIgv":"10","codigoTributo":"1000","tipoPrecio":"01","porcentajeIgv":18.00,"baseImponible":280.00,"montoImpuesto":50.40}]',
    TIMESTAMP '2026-04-01 08:00:00'),

-- ── RECHAZADAS (4) — SUNAT devolvió error, deben corregirse ──
('fac-demo-011', 'cmp-demo-001', 'cli-demo-003', 'F001', 8, DATE '2026-04-02', 'PEN', 'CONTADO', DATE '2026-04-02', '150131', 1800.00, 324.00, 2124.00, 'RECHAZADA',
    NULL, '2800', 'El tipo de documento del adquiriente no corresponde al tipo de comprobante - Factura requiere RUC (tipo 6)',
    'SON DOS MIL CIENTO VEINTICUATRO Y 00/100 SOLES',
    '[{"descripcion":"Licencia Software ERP Anual","unidadMedida":"ZZ","cantidad":1,"valorUnitario":1800.00,"precioUnitario":2124.00,"igv":324.00,"subtotal":1800.00,"tipoAfectacionIgv":"10","codigoTributo":"1000","tipoPrecio":"01","porcentajeIgv":18.00,"baseImponible":1800.00,"montoImpuesto":324.00}]',
    TIMESTAMP '2026-04-02 09:30:00'),
('fac-demo-012', 'cmp-demo-001', 'cli-demo-001', 'F001', 9, DATE '2026-04-02', 'PEN', 'CONTADO', DATE '2026-04-02', '150131', 2500.00, 450.00, 2900.00, 'RECHAZADA',
    NULL, '2022', 'El total del comprobante no coincide con la suma de valores: gravadas (2500.00) + IGV (450.00) = 2950.00, pero se declaró 2900.00',
    'SON DOS MIL NOVECIENTOS Y 00/100 SOLES',
    '[{"descripcion":"Servicio de Consultoría en TI","unidadMedida":"ZZ","cantidad":1,"valorUnitario":2500.00,"precioUnitario":2950.00,"igv":450.00,"subtotal":2500.00,"tipoAfectacionIgv":"10","codigoTributo":"1000","tipoPrecio":"01","porcentajeIgv":18.00,"baseImponible":2500.00,"montoImpuesto":450.00}]',
    TIMESTAMP '2026-04-02 11:15:00'),
('fac-demo-013', 'cmp-demo-001', 'cli-demo-006', 'F002', 4, DATE '2026-04-03', 'PEN', 'CREDITO', DATE '2026-05-03', '150122', 350.00, 63.00, 413.00, 'RECHAZADA',
    NULL, '2204', 'Serie no autorizada para el emisor: F002 no está registrada en la ficha RUC del contribuyente',
    'SON CUATROCIENTOS TRECE Y 00/100 SOLES',
    '[{"descripcion":"Soporte Técnico Premium (mensual)","unidadMedida":"ZZ","cantidad":1,"valorUnitario":350.00,"precioUnitario":413.00,"igv":63.00,"subtotal":350.00,"tipoAfectacionIgv":"10","codigoTributo":"1000","tipoPrecio":"01","porcentajeIgv":18.00,"baseImponible":350.00,"montoImpuesto":63.00}]',
    TIMESTAMP '2026-04-03 14:00:00'),
('fac-demo-014', 'cmp-demo-001', 'cli-demo-009', 'F001', 10, DATE '2026-04-03', 'PEN', 'CONTADO', DATE '2026-04-03', '150131', 8400.00, 1512.00, 9912.00, 'RECHAZADA',
    NULL, '3105', 'Fecha de emisión no puede ser mayor a la fecha actual de envío a SUNAT',
    'SON NUEVE MIL NOVECIENTOS DOCE Y 00/100 SOLES',
    '[{"descripcion":"Laptop Empresarial ThinkPad T14","unidadMedida":"NIU","cantidad":2,"valorUnitario":4200.00,"precioUnitario":4956.00,"igv":756.00,"subtotal":8400.00,"tipoAfectacionIgv":"10","codigoTributo":"1000","tipoPrecio":"01","porcentajeIgv":18.00,"baseImponible":8400.00,"montoImpuesto":1512.00}]',
    TIMESTAMP '2026-04-03 16:30:00'),

-- ── BORRADORES (5) — sin enviar, editables ──
('fac-demo-015', 'cmp-demo-001', 'cli-demo-001', 'F001', 11, DATE '2026-04-04', 'PEN', 'CONTADO', DATE '2026-04-04', '150131', 600.00, 108.00, 708.00, 'BORRADOR',
    NULL, NULL, NULL,
    'SON SETECIENTOS OCHO Y 00/100 SOLES',
    '[{"descripcion":"Capacitación Equipo (8 horas)","unidadMedida":"HUR","cantidad":2,"valorUnitario":300.00,"precioUnitario":354.00,"igv":54.00,"subtotal":600.00,"tipoAfectacionIgv":"10","codigoTributo":"1000","tipoPrecio":"01","porcentajeIgv":18.00,"baseImponible":600.00,"montoImpuesto":108.00}]',
    TIMESTAMP '2026-04-04 10:00:00'),
('fac-demo-016', 'cmp-demo-001', 'cli-demo-003', 'F001', 12, DATE '2026-04-04', 'PEN', 'CREDITO', DATE '2026-05-04', '150131', 9000.00, 1620.00, 10620.00, 'BORRADOR',
    NULL, NULL, NULL,
    'SON DIEZ MIL SEISCIENTOS VEINTE Y 00/100 SOLES',
    '[{"descripcion":"Licencia Software ERP Anual","unidadMedida":"ZZ","cantidad":5,"valorUnitario":1800.00,"precioUnitario":2124.00,"igv":324.00,"subtotal":9000.00,"tipoAfectacionIgv":"10","codigoTributo":"1000","tipoPrecio":"01","porcentajeIgv":18.00,"baseImponible":9000.00,"montoImpuesto":1620.00}]',
    TIMESTAMP '2026-04-04 11:30:00'),
('fac-demo-017', 'cmp-demo-001', 'cli-demo-007', 'F002', 5, DATE '2026-04-05', 'USD', 'CONTADO', DATE '2026-04-05', '150122', 3000.00, 540.00, 3540.00, 'BORRADOR',
    NULL, NULL, NULL,
    'SON TRES MIL QUINIENTOS CUARENTA Y 00/100 DOLARES AMERICANOS',
    '[{"descripcion":"Monitor 27 4K USB-C","unidadMedida":"NIU","cantidad":2,"valorUnitario":1500.00,"precioUnitario":1770.00,"igv":270.00,"subtotal":3000.00,"tipoAfectacionIgv":"10","codigoTributo":"1000","tipoPrecio":"01","porcentajeIgv":18.00,"baseImponible":3000.00,"montoImpuesto":540.00}]',
    TIMESTAMP '2026-04-05 09:00:00'),
('fac-demo-018', 'cmp-demo-001', 'cli-demo-009', 'F001', 13, DATE '2026-04-05', 'PEN', 'CONTADO', DATE '2026-04-05', '150131', 5530.00, 995.40, 6525.40, 'BORRADOR',
    NULL, NULL, NULL,
    'SON SEIS MIL QUINIENTOS VEINTICINCO Y 40/100 SOLES',
    '[{"descripcion":"Hosting Cloud (mensual)","unidadMedida":"MON","cantidad":2,"valorUnitario":450.00,"precioUnitario":531.00,"igv":81.00,"subtotal":900.00,"tipoAfectacionIgv":"10","codigoTributo":"1000","tipoPrecio":"01","porcentajeIgv":18.00,"baseImponible":900.00,"montoImpuesto":162.00},{"descripcion":"Soporte Técnico Premium (mensual)","unidadMedida":"ZZ","cantidad":3,"valorUnitario":350.00,"precioUnitario":413.00,"igv":63.00,"subtotal":1050.00,"tipoAfectacionIgv":"10","codigoTributo":"1000","tipoPrecio":"01","porcentajeIgv":18.00,"baseImponible":1050.00,"montoImpuesto":189.00},{"descripcion":"Certificado Digital SUNAT","unidadMedida":"ZZ","cantidad":2,"valorUnitario":280.00,"precioUnitario":330.40,"igv":50.40,"subtotal":560.00,"tipoAfectacionIgv":"10","codigoTributo":"1000","tipoPrecio":"01","porcentajeIgv":18.00,"baseImponible":560.00,"montoImpuesto":100.80},{"descripcion":"Servicio de Consultoría en TI","unidadMedida":"ZZ","cantidad":1,"valorUnitario":2500.00,"precioUnitario":2950.00,"igv":450.00,"subtotal":2500.00,"tipoAfectacionIgv":"10","codigoTributo":"1000","tipoPrecio":"01","porcentajeIgv":18.00,"baseImponible":2500.00,"montoImpuesto":450.00},{"descripcion":"Capacitación Equipo (8 horas)","unidadMedida":"HUR","cantidad":2,"valorUnitario":260.00,"precioUnitario":306.80,"igv":46.80,"subtotal":520.00,"tipoAfectacionIgv":"10","codigoTributo":"1000","tipoPrecio":"01","porcentajeIgv":18.00,"baseImponible":520.00,"montoImpuesto":93.60}]',
    TIMESTAMP '2026-04-05 14:00:00'),
('fac-demo-019', 'cmp-demo-001', 'cli-demo-006', 'F002', 6, DATE '2026-04-06', 'PEN', 'CREDITO', DATE '2026-05-06', '150122', 12600.00, 2268.00, 14868.00, 'BORRADOR',
    NULL, NULL, NULL,
    'SON CATORCE MIL OCHOCIENTOS SESENTA Y OCHO Y 00/100 SOLES',
    '[{"descripcion":"Laptop Empresarial ThinkPad T14","unidadMedida":"NIU","cantidad":3,"valorUnitario":4200.00,"precioUnitario":4956.00,"igv":756.00,"subtotal":12600.00,"tipoAfectacionIgv":"10","codigoTributo":"1000","tipoPrecio":"01","porcentajeIgv":18.00,"baseImponible":12600.00,"montoImpuesto":2268.00}]',
    TIMESTAMP '2026-04-06 08:00:00'),

-- ── ANULADAS (4) — documentos cancelados ──
('fac-demo-020', 'cmp-demo-001', 'cli-demo-001', 'F001', 14, DATE '2026-03-08', 'PEN', 'CONTADO', DATE '2026-03-08', '150131', 700.00, 126.00, 826.00, 'ANULADA',
    'x1y2z3a4b5c6d7e8f9g0', '0', 'La Factura fue aceptada',
    'SON OCHOCIENTOS VEINTISEIS Y 00/100 SOLES',
    '[{"descripcion":"Soporte Técnico Premium (mensual)","unidadMedida":"ZZ","cantidad":2,"valorUnitario":350.00,"precioUnitario":413.00,"igv":63.00,"subtotal":700.00,"tipoAfectacionIgv":"10","codigoTributo":"1000","tipoPrecio":"01","porcentajeIgv":18.00,"baseImponible":700.00,"montoImpuesto":126.00}]',
    TIMESTAMP '2026-03-08 10:00:00'),
('fac-demo-021', 'cmp-demo-001', 'cli-demo-003', 'F001', 15, DATE '2026-03-14', 'PEN', 'CREDITO', DATE '2026-04-14', '150131', 1800.00, 324.00, 2124.00, 'ANULADA',
    'y2z3a4b5c6d7e8f9g0h1', '0', 'La Factura fue aceptada',
    'SON DOS MIL CIENTO VEINTICUATRO Y 00/100 SOLES',
    '[{"descripcion":"Licencia Software ERP Anual","unidadMedida":"ZZ","cantidad":1,"valorUnitario":1800.00,"precioUnitario":2124.00,"igv":324.00,"subtotal":1800.00,"tipoAfectacionIgv":"10","codigoTributo":"1000","tipoPrecio":"01","porcentajeIgv":18.00,"baseImponible":1800.00,"montoImpuesto":324.00}]',
    TIMESTAMP '2026-03-14 12:00:00'),
('fac-demo-022', 'cmp-demo-001', 'cli-demo-007', 'F002', 7, DATE '2026-03-20', 'USD', 'CONTADO', DATE '2026-03-20', '150122', 600.00, 108.00, 708.00, 'ANULADA',
    'z3a4b5c6d7e8f9g0h1i2', '0', 'La Factura fue aceptada',
    'SON SETECIENTOS OCHO Y 00/100 DOLARES AMERICANOS',
    '[{"descripcion":"Capacitación Equipo (8 horas)","unidadMedida":"HUR","cantidad":2,"valorUnitario":300.00,"precioUnitario":354.00,"igv":54.00,"subtotal":600.00,"tipoAfectacionIgv":"10","codigoTributo":"1000","tipoPrecio":"01","porcentajeIgv":18.00,"baseImponible":600.00,"montoImpuesto":108.00}]',
    TIMESTAMP '2026-03-20 09:30:00'),
('fac-demo-023', 'cmp-demo-001', 'cli-demo-006', 'F001', 16, DATE '2026-03-28', 'PEN', 'CONTADO', DATE '2026-03-28', '150131', 560.00, 100.80, 660.80, 'ANULADA',
    'a4b5c6d7e8f9g0h1i2j3', '0', 'La Factura fue aceptada',
    'SON SEISCIENTOS SESENTA Y 80/100 SOLES',
    '[{"descripcion":"Certificado Digital SUNAT","unidadMedida":"ZZ","cantidad":2,"valorUnitario":280.00,"precioUnitario":330.40,"igv":50.40,"subtotal":560.00,"tipoAfectacionIgv":"10","codigoTributo":"1000","tipoPrecio":"01","porcentajeIgv":18.00,"baseImponible":560.00,"montoImpuesto":100.80}]',
    TIMESTAMP '2026-03-28 14:00:00');

-- ══════ BOLETAS (documentos de prueba) ══════
INSERT INTO boletas (id, company_id, client_id, serie, numero, fecha_emision, moneda, gravadas, igv, total, estado, leyenda, detalle_json, fecha_creacion) VALUES
('bol-demo-001', 'cmp-demo-001', 'cli-demo-002', 'B001', 1, DATE '2026-03-18', 'PEN', 350.00, 63.00, 413.00, 'ACEPTADA',
    'SON CUATROCIENTOS TRECE Y 00/100 SOLES',
    '[{"descripcion":"Soporte Técnico Premium (mensual)","unidadMedida":"ZZ","cantidad":1,"valorUnitario":350.00,"precioUnitario":413.00,"igv":63.00,"subtotal":350.00,"tipoAfectacionIgv":"10","codigoTributo":"1000","tipoPrecio":"01","porcentajeIgv":18.00,"baseImponible":350.00,"montoImpuesto":63.00}]',
    CURRENT_TIMESTAMP),
('bol-demo-002', 'cmp-demo-001', 'cli-demo-002', 'B001', 2, DATE '2026-04-02', 'PEN', 2500.00, 450.00, 2950.00, 'BORRADOR',
    'SON DOS MIL NOVECIENTOS CINCUENTA Y 00/100 SOLES',
    '[{"descripcion":"Servicio de Consultoría en TI","unidadMedida":"ZZ","cantidad":1,"valorUnitario":2500.00,"precioUnitario":2950.00,"igv":450.00,"subtotal":2500.00,"tipoAfectacionIgv":"10","codigoTributo":"1000","tipoPrecio":"01","porcentajeIgv":18.00,"baseImponible":2500.00,"montoImpuesto":450.00}]',
    CURRENT_TIMESTAMP),
('bol-demo-003', 'cmp-demo-001', 'cli-demo-008', 'B001', 3, DATE '2026-04-07', 'PEN', 180.00, 32.40, 212.40, 'VALIDADO',
    'SON DOSCIENTOS DOCE Y 40/100 SOLES',
    '[{"descripcion":"Corte de cabello premium","unidadMedida":"ZZ","cantidad":1,"valorUnitario":120.00,"precioUnitario":141.60,"igv":21.60,"subtotal":120.00,"tipoAfectacionIgv":"10","codigoTributo":"1000","tipoPrecio":"01","porcentajeIgv":18.00,"baseImponible":120.00,"montoImpuesto":21.60},{"descripcion":"Tratamiento capilar","unidadMedida":"ZZ","cantidad":1,"valorUnitario":60.00,"precioUnitario":70.80,"igv":10.80,"subtotal":60.00,"tipoAfectacionIgv":"10","codigoTributo":"1000","tipoPrecio":"01","porcentajeIgv":18.00,"baseImponible":60.00,"montoImpuesto":10.80}]',
    CURRENT_TIMESTAMP),
('bol-demo-004', 'cmp-demo-001', 'cli-demo-010', 'B001', 4, DATE '2026-04-07', 'PEN', 450.00, 81.00, 531.00, 'VALIDADO',
    'SON QUINIENTOS TREINTA Y UNO Y 00/100 SOLES',
    '[{"descripcion":"Almuerzo ejecutivo ×3","unidadMedida":"ZZ","cantidad":3,"valorUnitario":150.00,"precioUnitario":177.00,"igv":27.00,"subtotal":450.00,"tipoAfectacionIgv":"10","codigoTributo":"1000","tipoPrecio":"01","porcentajeIgv":18.00,"baseImponible":450.00,"montoImpuesto":81.00}]',
    CURRENT_TIMESTAMP),
('bol-demo-005', 'cmp-demo-001', 'cli-demo-002', 'B001', 5, DATE '2026-04-07', 'PEN', 0.00, 0.00, 75.00, 'CON_ERROR',
    'SON SETENTA Y CINCO Y 00/100 SOLES',
    '[{"descripcion":"Estacionamiento mensual","unidadMedida":"MON","cantidad":1,"valorUnitario":75.00,"precioUnitario":75.00,"igv":0,"subtotal":75.00,"tipoAfectacionIgv":"10","codigoTributo":"1000","tipoPrecio":"01","porcentajeIgv":18.00,"baseImponible":75.00,"montoImpuesto":0}]',
    CURRENT_TIMESTAMP),
('bol-demo-006', 'cmp-demo-001', 'cli-demo-008', 'B001', 6, DATE '2026-04-06', 'PEN', 800.00, 144.00, 944.00, 'ENVIADA',
    'SON NOVECIENTOS CUARENTA Y CUATRO Y 00/100 SOLES',
    '[{"descripcion":"Reparación de laptop","unidadMedida":"ZZ","cantidad":1,"valorUnitario":500.00,"precioUnitario":590.00,"igv":90.00,"subtotal":500.00,"tipoAfectacionIgv":"10","codigoTributo":"1000","tipoPrecio":"01","porcentajeIgv":18.00,"baseImponible":500.00,"montoImpuesto":90.00},{"descripcion":"Repuestos","unidadMedida":"NIU","cantidad":2,"valorUnitario":150.00,"precioUnitario":177.00,"igv":27.00,"subtotal":300.00,"tipoAfectacionIgv":"10","codigoTributo":"1000","tipoPrecio":"01","porcentajeIgv":18.00,"baseImponible":300.00,"montoImpuesto":54.00}]',
    CURRENT_TIMESTAMP),
('bol-demo-007', 'cmp-demo-001', 'cli-demo-010', 'B001', 7, DATE '2026-04-05', 'PEN', 1200.00, 216.00, 1416.00, 'ACEPTADA',
    'SON MIL CUATROCIENTOS DIECISEIS Y 00/100 SOLES',
    '[{"descripcion":"Servicio de catering evento corporativo","unidadMedida":"ZZ","cantidad":1,"valorUnitario":1200.00,"precioUnitario":1416.00,"igv":216.00,"subtotal":1200.00,"tipoAfectacionIgv":"10","codigoTributo":"1000","tipoPrecio":"01","porcentajeIgv":18.00,"baseImponible":1200.00,"montoImpuesto":216.00}]',
    CURRENT_TIMESTAMP),
('bol-demo-008', 'cmp-demo-001', 'cli-demo-002', 'B001', 8, DATE '2026-04-08', 'PEN', 320.00, 57.60, 377.60, 'VALIDADO',
    'SON TRESCIENTOS SETENTA Y SIETE Y 60/100 SOLES',
    '[{"descripcion":"Mantenimiento preventivo PC","unidadMedida":"ZZ","cantidad":2,"valorUnitario":160.00,"precioUnitario":188.80,"igv":28.80,"subtotal":320.00,"tipoAfectacionIgv":"10","codigoTributo":"1000","tipoPrecio":"01","porcentajeIgv":18.00,"baseImponible":320.00,"montoImpuesto":57.60}]',
    CURRENT_TIMESTAMP);

-- ══════ NOTAS DE CRÉDITO (documentos de prueba) ══════
INSERT INTO notas_credito (id, company_id, factura_referencia_id, motivo, monto_descuento, tipo_documento, descripcion, estado, detalle_json, fecha_creacion) VALUES
('nc-demo-001', 'cmp-demo-001', 'fac-demo-001', 'descuento', 500.00, 'factura',
    'Descuento comercial acordado post-venta 20%', 'ACEPTADA',
    '[{"descripcion":"Descuento sobre Consultoría en TI","unidadMedida":"ZZ","cantidad":1,"valorUnitario":423.73,"precioUnitario":500.00,"igv":76.27,"subtotal":423.73,"tipoAfectacionIgv":"10","codigoTributo":"1000","tipoPrecio":"01","porcentajeIgv":18.00,"baseImponible":423.73,"montoImpuesto":76.27}]',
    CURRENT_TIMESTAMP),
('nc-demo-002', 'cmp-demo-001', 'fac-demo-002', 'anulacion', 6018.00, 'factura',
    'Anulación total por error en datos del cliente', 'BORRADOR',
    '[{"descripcion":"Anulación total Factura F001-2","unidadMedida":"ZZ","cantidad":1,"valorUnitario":5100.00,"precioUnitario":6018.00,"igv":918.00,"subtotal":5100.00,"tipoAfectacionIgv":"10","codigoTributo":"1000","tipoPrecio":"01","porcentajeIgv":18.00,"baseImponible":5100.00,"montoImpuesto":918.00}]',
    CURRENT_TIMESTAMP);

-- ══════ NOTAS DE DÉBITO (documentos de prueba) ══════
INSERT INTO notas_debito (id, company_id, factura_referencia_id, motivo, monto_incremento, descripcion, estado, detalle_json, fecha_creacion) VALUES
('nd-demo-001', 'cmp-demo-001', 'fac-demo-001', 'intereses', 250.00,
    'Intereses por pago fuera de plazo (30 días)', 'ACEPTADA',
    '[{"descripcion":"Intereses moratorios sobre Factura F001-1","unidadMedida":"ZZ","cantidad":1,"valorUnitario":211.86,"precioUnitario":250.00,"igv":38.14,"subtotal":211.86,"tipoAfectacionIgv":"10","codigoTributo":"1000","tipoPrecio":"01","porcentajeIgv":18.00,"baseImponible":211.86,"montoImpuesto":38.14}]',
    CURRENT_TIMESTAMP),
('nd-demo-002', 'cmp-demo-001', 'fac-demo-003', 'gastos_administrativos', 150.00,
    'Gastos administrativos por gestión de cobranza', 'BORRADOR',
    '[{"descripcion":"Gastos administrativos cobranza","unidadMedida":"ZZ","cantidad":1,"valorUnitario":127.12,"precioUnitario":150.00,"igv":22.88,"subtotal":127.12,"tipoAfectacionIgv":"10","codigoTributo":"1000","tipoPrecio":"01","porcentajeIgv":18.00,"baseImponible":127.12,"montoImpuesto":22.88}]',
    CURRENT_TIMESTAMP);

-- ══════ CORRELATIVOS (sincronizar con facturas/boletas seed) ══════
INSERT INTO correlativos (id, company_id, serie, ultimo_numero) VALUES
('cor-001', 'cmp-demo-001', 'F001', 18),
('cor-002', 'cmp-demo-001', 'B001', 8),
('cor-003', 'cmp-demo-001', 'F002', 7),
('cor-004', 'cmp-demo-001', 'B002', 0);

-- ══════ USER PREFERENCES ══════
INSERT INTO user_preferences (id, company_id, clave, valor, fecha_actualizacion) VALUES
('pref-001', 'cmp-demo-001', 'moneda', 'PEN', CURRENT_TIMESTAMP),
('pref-002', 'cmp-demo-001', 'tema', 'dark', CURRENT_TIMESTAMP),
('pref-003', 'cmp-demo-001', 'defaultItemType', 'servicio', CURRENT_TIMESTAMP),
('pref-004', 'cmp-demo-001', 'tipoCambioFuente', 'SBS', CURRENT_TIMESTAMP),
('pref-005', 'cmp-demo-001', 'emisionEnEnvio', 'true', CURRENT_TIMESTAMP),
('pref-006', 'cmp-demo-001', 'defaultCuotas', '1', CURRENT_TIMESTAMP),
('pref-007', 'cmp-demo-001', 'cuotaDistribucion', 'FIJA_30D', CURRENT_TIMESTAMP),
('pref-008', 'cmp-demo-001', 'documentFormat', 'standard', CURRENT_TIMESTAMP);

-- ══════ TIPO DE CAMBIO (seed últimos días) ══════
INSERT INTO tipo_cambio (id, fecha, moneda_origen, moneda_destino, tasa_compra, tasa_venta, fuente, fecha_creacion) VALUES
('tc-001', '2026-04-06', 'USD', 'PEN', 3.7100, 3.7520, 'SBS', CURRENT_TIMESTAMP),
('tc-002', '2026-04-05', 'USD', 'PEN', 3.7050, 3.7480, 'SBS', CURRENT_TIMESTAMP),
('tc-003', '2026-04-04', 'USD', 'PEN', 3.7080, 3.7500, 'SBS', CURRENT_TIMESTAMP),
('tc-004', '2026-04-03', 'USD', 'PEN', 3.7120, 3.7540, 'SBS', CURRENT_TIMESTAMP),
('tc-005', '2026-04-02', 'USD', 'PEN', 3.7090, 3.7510, 'SBS', CURRENT_TIMESTAMP),
('tc-006', '2026-04-06', 'USD', 'PEN', 3.7110, 3.7530, 'SUNAT', CURRENT_TIMESTAMP),
('tc-007', '2026-04-05', 'USD', 'PEN', 3.7060, 3.7490, 'SUNAT', CURRENT_TIMESTAMP);

-- ══════ LIBROS ELECTRÓNICOS (seed) ══════
INSERT INTO libros_electronicos (id, company_id, tipo, periodo, formato, estado, archivo_url, total_registros, fecha_generacion, fecha_creacion) VALUES
('lib-001', 'cmp-demo-001', 'ventas', '2026-03', 'TXT', 'GENERADO', '/libros/cmp-demo-001/ventas-2026-03.txt', 45, TIMESTAMP '2026-04-01 08:00:00', CURRENT_TIMESTAMP),
('lib-002', 'cmp-demo-001', 'compras', '2026-03', 'TXT', 'GENERADO', '/libros/cmp-demo-001/compras-2026-03.txt', 32, TIMESTAMP '2026-04-01 08:15:00', CURRENT_TIMESTAMP),
('lib-003', 'cmp-demo-001', 'ventas', '2026-02', 'TXT', 'GENERADO', '/libros/cmp-demo-001/ventas-2026-02.txt', 38, TIMESTAMP '2026-03-01 09:00:00', CURRENT_TIMESTAMP);

-- ══════ BACKFILL UBL 2.1 — Nuevos campos FASE 2 ══════

-- Clients: dirección estructurada + país
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

-- Facturas: campos UBL (todas las demo son gravadas 100%)
UPDATE facturas SET
    tipo_operacion = '0101',
    hora_emision = TIME '09:00:00',
    exoneradas = 0, inafectas = 0, gratuitas = 0,
    total_cargos = 0, total_descuentos = 0,
    subtotal = gravadas,
    total_con_impuestos = gravadas + igv
WHERE tipo_operacion IS NULL;

-- Boletas: idem
UPDATE boletas SET
    tipo_operacion = '0101',
    hora_emision = TIME '10:00:00',
    exoneradas = 0, inafectas = 0, gratuitas = 0,
    total_cargos = 0, total_descuentos = 0,
    subtotal = gravadas,
    total_con_impuestos = gravadas + igv
WHERE tipo_operacion IS NULL;

-- ══════ CUOTAS DE CRÉDITO (facturas con forma_pago=CREDITO) ══════
-- fac-demo-002 (CREDITO, total 6018.00 PEN, vence 2026-04-05) → 2 cuotas
INSERT INTO cuotas_credito (id, factura_id, boleta_id, numero_cuota, monto, fecha_vencimiento, moneda) VALUES
('cuota-001', 'fac-demo-002', NULL, 'Cuota001', 3009.00, DATE '2026-04-05', 'PEN'),
('cuota-002', 'fac-demo-002', NULL, 'Cuota002', 3009.00, DATE '2026-05-05', 'PEN');

-- fac-demo-005 (CREDITO, total 8850.00 PEN, vence 2026-04-15) → 3 cuotas
INSERT INTO cuotas_credito (id, factura_id, boleta_id, numero_cuota, monto, fecha_vencimiento, moneda) VALUES
('cuota-003', 'fac-demo-005', NULL, 'Cuota001', 2950.00, DATE '2026-04-15', 'PEN'),
('cuota-004', 'fac-demo-005', NULL, 'Cuota002', 2950.00, DATE '2026-05-15', 'PEN'),
('cuota-005', 'fac-demo-005', NULL, 'Cuota003', 2950.00, DATE '2026-06-15', 'PEN');

-- fac-demo-008 (CREDITO, ENVIADA, total 4248.00 PEN) → 1 cuota
INSERT INTO cuotas_credito (id, factura_id, boleta_id, numero_cuota, monto, fecha_vencimiento, moneda) VALUES
('cuota-006', 'fac-demo-008', NULL, 'Cuota001', 4248.00, DATE '2026-04-25', 'PEN');

-- fac-demo-016 (CREDITO, BORRADOR, total 10620.00 PEN) → 2 cuotas
INSERT INTO cuotas_credito (id, factura_id, boleta_id, numero_cuota, monto, fecha_vencimiento, moneda) VALUES
('cuota-007', 'fac-demo-016', NULL, 'Cuota001', 5310.00, DATE '2026-05-04', 'PEN'),
('cuota-008', 'fac-demo-016', NULL, 'Cuota002', 5310.00, DATE '2026-06-04', 'PEN');

-- fac-demo-019 (CREDITO, BORRADOR, total 14868.00 PEN) → 3 cuotas
INSERT INTO cuotas_credito (id, factura_id, boleta_id, numero_cuota, monto, fecha_vencimiento, moneda) VALUES
('cuota-009', 'fac-demo-019', NULL, 'Cuota001', 4956.00, DATE '2026-05-06', 'PEN'),
('cuota-010', 'fac-demo-019', NULL, 'Cuota002', 4956.00, DATE '2026-06-06', 'PEN'),
('cuota-011', 'fac-demo-019', NULL, 'Cuota003', 4956.00, DATE '2026-07-06', 'PEN');

-- ══════ ALLOWANCE / CHARGE GLOBALES (Catálogo 53 SUNAT) ══════
-- fac-demo-001: Recargo al consumo 10% sobre base gravada
INSERT INTO allowance_charges (id, factura_id, boleta_id, charge_indicator, reason_code, reason_description, multiplier_factor, amount, base_amount, moneda) VALUES
('ac-001', 'fac-demo-001', NULL, TRUE, '46', 'Recargo al consumo', 0.1000, 250.00, 2500.00, 'PEN');

-- fac-demo-004: Descuento global 5%
INSERT INTO allowance_charges (id, factura_id, boleta_id, charge_indicator, reason_code, reason_description, multiplier_factor, amount, base_amount, moneda) VALUES
('ac-002', 'fac-demo-004', NULL, FALSE, '00', 'Descuento comercial global', 0.0500, 210.00, 4200.00, 'PEN');

-- fac-demo-005: Recargo al consumo 10%
INSERT INTO allowance_charges (id, factura_id, boleta_id, charge_indicator, reason_code, reason_description, multiplier_factor, amount, base_amount, moneda) VALUES
('ac-003', 'fac-demo-005', NULL, TRUE, '46', 'Recargo al consumo', 0.1000, 750.00, 7500.00, 'PEN');

-- Update totalCargos/totalDescuentos for affected facturas
UPDATE facturas SET total_cargos = 250.00 WHERE id = 'fac-demo-001';
UPDATE facturas SET total_descuentos = 210.00 WHERE id = 'fac-demo-004';
UPDATE facturas SET total_cargos = 750.00 WHERE id = 'fac-demo-005';

-- fac-demo-015 (BORRADOR, CONTADO): Descuento global 10% sobre base
INSERT INTO allowance_charges (id, factura_id, boleta_id, charge_indicator, reason_code, reason_description, multiplier_factor, amount, base_amount, moneda) VALUES
('ac-004', 'fac-demo-015', NULL, FALSE, '02', 'Descuento por volumen', 0.1000, 60.00, 600.00, 'PEN');
UPDATE facturas SET total_descuentos = 60.00 WHERE id = 'fac-demo-015';

-- fac-demo-016 (BORRADOR, CREDITO): Recargo al consumo 10%
INSERT INTO allowance_charges (id, factura_id, boleta_id, charge_indicator, reason_code, reason_description, multiplier_factor, amount, base_amount, moneda) VALUES
('ac-005', 'fac-demo-016', NULL, TRUE, '46', 'Recargo al consumo', 0.1000, 900.00, 9000.00, 'PEN');
UPDATE facturas SET total_cargos = 900.00 WHERE id = 'fac-demo-016';

-- fac-demo-013 (RECHAZADA, CREDITO): Descuento + cuota para testear re-emisión
INSERT INTO allowance_charges (id, factura_id, boleta_id, charge_indicator, reason_code, reason_description, multiplier_factor, amount, base_amount, moneda) VALUES
('ac-006', 'fac-demo-013', NULL, FALSE, '00', 'Descuento por pronto pago', 0.0300, 10.50, 350.00, 'PEN');
UPDATE facturas SET total_descuentos = 10.50 WHERE id = 'fac-demo-013';

INSERT INTO cuotas_credito (id, factura_id, boleta_id, numero_cuota, monto, fecha_vencimiento, moneda) VALUES
('cuota-012', 'fac-demo-013', NULL, 'Cuota001', 206.50, DATE '2026-05-03', 'PEN'),
('cuota-013', 'fac-demo-013', NULL, 'Cuota002', 206.50, DATE '2026-06-03', 'PEN');

-- fac-demo-018 (BORRADOR, CONTADO, multi-ítem): Descuento global 5% + recargo consumo 10%
INSERT INTO allowance_charges (id, factura_id, boleta_id, charge_indicator, reason_code, reason_description, multiplier_factor, amount, base_amount, moneda) VALUES
('ac-007', 'fac-demo-018', NULL, FALSE, '00', 'Descuento comercial global', 0.0500, 276.50, 5530.00, 'PEN'),
('ac-008', 'fac-demo-018', NULL, TRUE, '46', 'Recargo al consumo', 0.1000, 553.00, 5530.00, 'PEN');
UPDATE facturas SET total_descuentos = 276.50, total_cargos = 553.00 WHERE id = 'fac-demo-018';

-- fac-demo-021 (ANULADA, CREDITO): Cuota para verificar visual en estado terminal
INSERT INTO cuotas_credito (id, factura_id, boleta_id, numero_cuota, monto, fecha_vencimiento, moneda) VALUES
('cuota-014', 'fac-demo-021', NULL, 'Cuota001', 1062.00, DATE '2026-04-14', 'PEN'),
('cuota-015', 'fac-demo-021', NULL, 'Cuota002', 1062.00, DATE '2026-05-14', 'PEN');

-- ══════ XML CONTENT — Backfill para facturas ACEPTADAS/ENVIADAS ══════
-- Facturas con estado ACEPTADA o ENVIADA llevan XML UBL 2.1 generado
UPDATE facturas SET xml_content =
'<?xml version="1.0" encoding="UTF-8"?><Invoice xmlns="urn:oasis:names:specification:ubl:schema:xsd:Invoice-2" xmlns:cac="urn:oasis:names:specification:ubl:schema:xsd:CommonAggregateComponents-2" xmlns:cbc="urn:oasis:names:specification:ubl:schema:xsd:CommonBasicComponents-2"><cbc:UBLVersionID>2.1</cbc:UBLVersionID><cbc:CustomizationID>2.0</cbc:CustomizationID><cbc:ID>F001-00000001</cbc:ID><cbc:IssueDate>2026-03-01</cbc:IssueDate><cbc:IssueTime>09:00:00</cbc:IssueTime><cbc:InvoiceTypeCode listID="0101">01</cbc:InvoiceTypeCode><cbc:DocumentCurrencyCode>PEN</cbc:DocumentCurrencyCode><cac:AccountingSupplierParty><cac:Party><cac:PartyIdentification><cbc:ID schemeID="6">20601234567</cbc:ID></cac:PartyIdentification><cac:PartyLegalEntity><cbc:RegistrationName>ETLIONS S.A.C.</cbc:RegistrationName></cac:PartyLegalEntity></cac:Party></cac:AccountingSupplierParty><cac:AccountingCustomerParty><cac:Party><cac:PartyIdentification><cbc:ID schemeID="6">20505050501</cbc:ID></cac:PartyIdentification><cac:PartyLegalEntity><cbc:RegistrationName>INVERSIONES TECH PERU S.A.C.</cbc:RegistrationName></cac:PartyLegalEntity></cac:Party></cac:AccountingCustomerParty><cac:PaymentTerms><cbc:ID>FormaPago</cbc:ID><cbc:PaymentMeansID>Contado</cbc:PaymentMeansID></cac:PaymentTerms><cac:TaxTotal><cbc:TaxAmount currencyID="PEN">450.00</cbc:TaxAmount><cac:TaxSubtotal><cbc:TaxableAmount currencyID="PEN">2500.00</cbc:TaxableAmount><cbc:TaxAmount currencyID="PEN">450.00</cbc:TaxAmount><cac:TaxCategory><cbc:ID>S</cbc:ID><cbc:Percent>18.00</cbc:Percent><cac:TaxScheme><cbc:ID>1000</cbc:ID><cbc:Name>IGV</cbc:Name></cac:TaxScheme></cac:TaxCategory></cac:TaxSubtotal></cac:TaxTotal><cac:LegalMonetaryTotal><cbc:LineExtensionAmount currencyID="PEN">2500.00</cbc:LineExtensionAmount><cbc:TaxInclusiveAmount currencyID="PEN">2950.00</cbc:TaxInclusiveAmount><cbc:PayableAmount currencyID="PEN">2950.00</cbc:PayableAmount></cac:LegalMonetaryTotal><cac:InvoiceLine><cbc:ID>1</cbc:ID><cbc:InvoicedQuantity unitCode="ZZ">1</cbc:InvoicedQuantity><cbc:LineExtensionAmount currencyID="PEN">2500.00</cbc:LineExtensionAmount><cac:Item><cbc:Description>Servicio de Consultoria en TI</cbc:Description></cac:Item><cac:Price><cbc:PriceAmount currencyID="PEN">2500.00</cbc:PriceAmount></cac:Price></cac:InvoiceLine></Invoice>',
xml_path = '20601234567/01/F001/00000001/20601234567-01-F001-00000001.xml'
WHERE id = 'fac-demo-001';

UPDATE facturas SET xml_content =
'<?xml version="1.0" encoding="UTF-8"?><Invoice xmlns="urn:oasis:names:specification:ubl:schema:xsd:Invoice-2" xmlns:cac="urn:oasis:names:specification:ubl:schema:xsd:CommonAggregateComponents-2" xmlns:cbc="urn:oasis:names:specification:ubl:schema:xsd:CommonBasicComponents-2"><cbc:UBLVersionID>2.1</cbc:UBLVersionID><cbc:CustomizationID>2.0</cbc:CustomizationID><cbc:ID>F001-00000002</cbc:ID><cbc:IssueDate>2026-03-05</cbc:IssueDate><cbc:IssueTime>10:30:00</cbc:IssueTime><cbc:InvoiceTypeCode listID="0101">01</cbc:InvoiceTypeCode><cbc:DocumentCurrencyCode>PEN</cbc:DocumentCurrencyCode><cac:AccountingSupplierParty><cac:Party><cac:PartyIdentification><cbc:ID schemeID="6">20601234567</cbc:ID></cac:PartyIdentification><cac:PartyLegalEntity><cbc:RegistrationName>ETLIONS S.A.C.</cbc:RegistrationName></cac:PartyLegalEntity></cac:Party></cac:AccountingSupplierParty><cac:AccountingCustomerParty><cac:Party><cac:PartyIdentification><cbc:ID schemeID="6">20612345678</cbc:ID></cac:PartyIdentification><cac:PartyLegalEntity><cbc:RegistrationName>GLOBAL IMPORTS E.I.R.L.</cbc:RegistrationName></cac:PartyLegalEntity></cac:Party></cac:AccountingCustomerParty><cac:PaymentTerms><cbc:ID>FormaPago</cbc:ID><cbc:PaymentMeansID>Credito</cbc:PaymentMeansID><cbc:Amount currencyID="PEN">6018.00</cbc:Amount></cac:PaymentTerms><cac:LegalMonetaryTotal><cbc:LineExtensionAmount currencyID="PEN">5100.00</cbc:LineExtensionAmount><cbc:TaxInclusiveAmount currencyID="PEN">6018.00</cbc:TaxInclusiveAmount><cbc:PayableAmount currencyID="PEN">6018.00</cbc:PayableAmount></cac:LegalMonetaryTotal></Invoice>',
xml_path = '20601234567/01/F001/00000002/20601234567-01-F001-00000002.xml'
WHERE id = 'fac-demo-002';

-- ENVIADAS también tienen XML generado (pendiente de respuesta SUNAT)
UPDATE facturas SET xml_content =
'<?xml version="1.0" encoding="UTF-8"?><Invoice xmlns="urn:oasis:names:specification:ubl:schema:xsd:Invoice-2" xmlns:cac="urn:oasis:names:specification:ubl:schema:xsd:CommonAggregateComponents-2" xmlns:cbc="urn:oasis:names:specification:ubl:schema:xsd:CommonBasicComponents-2"><cbc:UBLVersionID>2.1</cbc:UBLVersionID><cbc:CustomizationID>2.0</cbc:CustomizationID><cbc:ID>F001-00000005</cbc:ID><cbc:IssueDate>2026-03-22</cbc:IssueDate><cbc:IssueTime>09:00:00</cbc:IssueTime><cbc:InvoiceTypeCode listID="0101">01</cbc:InvoiceTypeCode><cbc:DocumentCurrencyCode>PEN</cbc:DocumentCurrencyCode><cac:AccountingSupplierParty><cac:Party><cac:PartyIdentification><cbc:ID schemeID="6">20601234567</cbc:ID></cac:PartyIdentification><cac:PartyLegalEntity><cbc:RegistrationName>ETLIONS S.A.C.</cbc:RegistrationName></cac:PartyLegalEntity></cac:Party></cac:AccountingSupplierParty><cac:AccountingCustomerParty><cac:Party><cac:PartyIdentification><cbc:ID schemeID="6">20505050501</cbc:ID></cac:PartyIdentification><cac:PartyLegalEntity><cbc:RegistrationName>INVERSIONES TECH PERU S.A.C.</cbc:RegistrationName></cac:PartyLegalEntity></cac:Party></cac:AccountingCustomerParty><cac:PaymentTerms><cbc:ID>FormaPago</cbc:ID><cbc:PaymentMeansID>Contado</cbc:PaymentMeansID></cac:PaymentTerms><cac:TaxTotal><cbc:TaxAmount currencyID="PEN">270.00</cbc:TaxAmount><cac:TaxSubtotal><cbc:TaxableAmount currencyID="PEN">1500.00</cbc:TaxableAmount><cbc:TaxAmount currencyID="PEN">270.00</cbc:TaxAmount><cac:TaxCategory><cbc:ID>S</cbc:ID><cbc:Percent>18.00</cbc:Percent><cac:TaxScheme><cbc:ID>1000</cbc:ID><cbc:Name>IGV</cbc:Name></cac:TaxScheme></cac:TaxCategory></cac:TaxSubtotal></cac:TaxTotal><cac:LegalMonetaryTotal><cbc:LineExtensionAmount currencyID="PEN">1500.00</cbc:LineExtensionAmount><cbc:TaxInclusiveAmount currencyID="PEN">1770.00</cbc:TaxInclusiveAmount><cbc:PayableAmount currencyID="PEN">1770.00</cbc:PayableAmount></cac:LegalMonetaryTotal><cac:InvoiceLine><cbc:ID>1</cbc:ID><cbc:InvoicedQuantity unitCode="NIU">1</cbc:InvoicedQuantity><cbc:LineExtensionAmount currencyID="PEN">1500.00</cbc:LineExtensionAmount><cac:Item><cbc:Description>Monitor 27 4K USB-C</cbc:Description></cac:Item><cac:Price><cbc:PriceAmount currencyID="PEN">1500.00</cbc:PriceAmount></cac:Price></cac:InvoiceLine></Invoice>',
xml_path = '20601234567/01/F001/00000005/20601234567-01-F001-00000005.xml'
WHERE id = 'fac-demo-007';

-- fac-demo-003 (ACEPTADA, USD, Contado)
UPDATE facturas SET xml_content =
'<?xml version="1.0" encoding="UTF-8"?><Invoice xmlns="urn:oasis:names:specification:ubl:schema:xsd:Invoice-2" xmlns:cac="urn:oasis:names:specification:ubl:schema:xsd:CommonAggregateComponents-2" xmlns:cbc="urn:oasis:names:specification:ubl:schema:xsd:CommonBasicComponents-2"><cbc:UBLVersionID>2.1</cbc:UBLVersionID><cbc:CustomizationID>2.0</cbc:CustomizationID><cbc:ID>F001-00000003</cbc:ID><cbc:IssueDate>2026-03-10</cbc:IssueDate><cbc:IssueTime>14:20:00</cbc:IssueTime><cbc:InvoiceTypeCode listID="0101">01</cbc:InvoiceTypeCode><cbc:DocumentCurrencyCode>USD</cbc:DocumentCurrencyCode><cbc:Note languageLocaleID="1000">SON MIL CUATROCIENTOS DIECISEIS Y 00/100 DOLARES AMERICANOS</cbc:Note><cac:AccountingSupplierParty><cac:Party><cac:PartyIdentification><cbc:ID schemeID="6">20601234567</cbc:ID></cac:PartyIdentification><cac:PartyLegalEntity><cbc:RegistrationName>ETLIONS S.A.C.</cbc:RegistrationName><cac:RegistrationAddress><cbc:ID schemeName="Ubigeos">150131</cbc:ID><cbc:AddressTypeCode>0001</cbc:AddressTypeCode><cbc:CityName>LIMA</cbc:CityName><cbc:CountrySubentity>LIMA</cbc:CountrySubentity><cbc:District>SAN ISIDRO</cbc:District><cac:AddressLine><cbc:Line>Av. Javier Prado Este 1234, Piso 8</cbc:Line></cac:AddressLine><cac:Country><cbc:IdentificationCode>PE</cbc:IdentificationCode></cac:Country></cac:RegistrationAddress></cac:PartyLegalEntity></cac:Party></cac:AccountingSupplierParty><cac:AccountingCustomerParty><cac:Party><cac:PartyIdentification><cbc:ID schemeID="6">20505050501</cbc:ID></cac:PartyIdentification><cac:PartyLegalEntity><cbc:RegistrationName>INVERSIONES TECH PERU S.A.C.</cbc:RegistrationName></cac:PartyLegalEntity></cac:Party></cac:AccountingCustomerParty><cac:PaymentTerms><cbc:ID>FormaPago</cbc:ID><cbc:PaymentMeansID>Contado</cbc:PaymentMeansID></cac:PaymentTerms><cac:TaxTotal><cbc:TaxAmount currencyID="USD">216.00</cbc:TaxAmount><cac:TaxSubtotal><cbc:TaxableAmount currencyID="USD">1200.00</cbc:TaxableAmount><cbc:TaxAmount currencyID="USD">216.00</cbc:TaxAmount><cac:TaxCategory><cbc:ID>S</cbc:ID><cbc:Percent>18.00</cbc:Percent><cac:TaxScheme><cbc:ID>1000</cbc:ID><cbc:Name>IGV</cbc:Name></cac:TaxScheme></cac:TaxCategory></cac:TaxSubtotal></cac:TaxTotal><cac:LegalMonetaryTotal><cbc:LineExtensionAmount currencyID="USD">1200.00</cbc:LineExtensionAmount><cbc:TaxInclusiveAmount currencyID="USD">1416.00</cbc:TaxInclusiveAmount><cbc:PayableAmount currencyID="USD">1416.00</cbc:PayableAmount></cac:LegalMonetaryTotal><cac:InvoiceLine><cbc:ID>1</cbc:ID><cbc:InvoicedQuantity unitCode="ZZ">2</cbc:InvoicedQuantity><cbc:LineExtensionAmount currencyID="USD">1200.00</cbc:LineExtensionAmount><cac:Item><cbc:Description>Consultoria Cloud AWS (2 sesiones)</cbc:Description></cac:Item><cac:Price><cbc:PriceAmount currencyID="USD">600.00</cbc:PriceAmount></cac:Price></cac:InvoiceLine></Invoice>',
xml_path = '20601234567/01/F001/00000003/20601234567-01-F001-00000003.xml'
WHERE id = 'fac-demo-003';

-- fac-demo-004 (ACEPTADA, PEN, Contado, Sede Miraflores F002)
UPDATE facturas SET xml_content =
'<?xml version="1.0" encoding="UTF-8"?><Invoice xmlns="urn:oasis:names:specification:ubl:schema:xsd:Invoice-2" xmlns:cac="urn:oasis:names:specification:ubl:schema:xsd:CommonAggregateComponents-2" xmlns:cbc="urn:oasis:names:specification:ubl:schema:xsd:CommonBasicComponents-2"><cbc:UBLVersionID>2.1</cbc:UBLVersionID><cbc:CustomizationID>2.0</cbc:CustomizationID><cbc:ID>F002-00000001</cbc:ID><cbc:IssueDate>2026-03-12</cbc:IssueDate><cbc:IssueTime>11:00:00</cbc:IssueTime><cbc:InvoiceTypeCode listID="0101">01</cbc:InvoiceTypeCode><cbc:DocumentCurrencyCode>PEN</cbc:DocumentCurrencyCode><cbc:Note languageLocaleID="1000">SON CUATRO MIL NOVECIENTOS CINCUENTA Y SEIS Y 00/100 SOLES</cbc:Note><cac:AccountingSupplierParty><cac:Party><cac:PartyIdentification><cbc:ID schemeID="6">20601234567</cbc:ID></cac:PartyIdentification><cac:PartyLegalEntity><cbc:RegistrationName>ETLIONS S.A.C.</cbc:RegistrationName><cac:RegistrationAddress><cbc:ID schemeName="Ubigeos">150122</cbc:ID><cbc:AddressTypeCode>0002</cbc:AddressTypeCode><cbc:CityName>LIMA</cbc:CityName><cbc:CountrySubentity>LIMA</cbc:CountrySubentity><cbc:District>MIRAFLORES</cbc:District><cac:AddressLine><cbc:Line>Calle Berlin 567, Oficina 201</cbc:Line></cac:AddressLine><cac:Country><cbc:IdentificationCode>PE</cbc:IdentificationCode></cac:Country></cac:RegistrationAddress></cac:PartyLegalEntity></cac:Party></cac:AccountingSupplierParty><cac:AccountingCustomerParty><cac:Party><cac:PartyIdentification><cbc:ID schemeID="6">20498765432</cbc:ID></cac:PartyIdentification><cac:PartyLegalEntity><cbc:RegistrationName>DISTRIBUIDORA ANDINA S.A.</cbc:RegistrationName></cac:PartyLegalEntity></cac:Party></cac:AccountingCustomerParty><cac:PaymentTerms><cbc:ID>FormaPago</cbc:ID><cbc:PaymentMeansID>Contado</cbc:PaymentMeansID></cac:PaymentTerms><cac:AllowanceCharge><cbc:ChargeIndicator>false</cbc:ChargeIndicator><cbc:AllowanceChargeReasonCode>00</cbc:AllowanceChargeReasonCode><cbc:MultiplierFactorNumeric>0.0500</cbc:MultiplierFactorNumeric><cbc:Amount currencyID="PEN">210.00</cbc:Amount><cbc:BaseAmount currencyID="PEN">4200.00</cbc:BaseAmount></cac:AllowanceCharge><cac:TaxTotal><cbc:TaxAmount currencyID="PEN">756.00</cbc:TaxAmount><cac:TaxSubtotal><cbc:TaxableAmount currencyID="PEN">4200.00</cbc:TaxableAmount><cbc:TaxAmount currencyID="PEN">756.00</cbc:TaxAmount><cac:TaxCategory><cbc:ID>S</cbc:ID><cbc:Percent>18.00</cbc:Percent><cac:TaxScheme><cbc:ID>1000</cbc:ID><cbc:Name>IGV</cbc:Name></cac:TaxScheme></cac:TaxCategory></cac:TaxSubtotal></cac:TaxTotal><cac:LegalMonetaryTotal><cbc:LineExtensionAmount currencyID="PEN">4200.00</cbc:LineExtensionAmount><cbc:TaxInclusiveAmount currencyID="PEN">4956.00</cbc:TaxInclusiveAmount><cbc:AllowanceTotalAmount currencyID="PEN">210.00</cbc:AllowanceTotalAmount><cbc:PayableAmount currencyID="PEN">4956.00</cbc:PayableAmount></cac:LegalMonetaryTotal><cac:InvoiceLine><cbc:ID>1</cbc:ID><cbc:InvoicedQuantity unitCode="NIU">1</cbc:InvoicedQuantity><cbc:LineExtensionAmount currencyID="PEN">4200.00</cbc:LineExtensionAmount><cac:Item><cbc:Description>Laptop Empresarial ThinkPad T14</cbc:Description></cac:Item><cac:Price><cbc:PriceAmount currencyID="PEN">4200.00</cbc:PriceAmount></cac:Price></cac:InvoiceLine></Invoice>',
xml_path = '20601234567/01/F002/00000001/20601234567-01-F002-00000001.xml'
WHERE id = 'fac-demo-004';

-- fac-demo-005 (ACEPTADA, PEN, Crédito, 3 cuotas + recargo consumo)
UPDATE facturas SET xml_content =
'<?xml version="1.0" encoding="UTF-8"?><Invoice xmlns="urn:oasis:names:specification:ubl:schema:xsd:Invoice-2" xmlns:cac="urn:oasis:names:specification:ubl:schema:xsd:CommonAggregateComponents-2" xmlns:cbc="urn:oasis:names:specification:ubl:schema:xsd:CommonBasicComponents-2"><cbc:UBLVersionID>2.1</cbc:UBLVersionID><cbc:CustomizationID>2.0</cbc:CustomizationID><cbc:ID>F001-00000004</cbc:ID><cbc:IssueDate>2026-03-15</cbc:IssueDate><cbc:IssueTime>16:45:00</cbc:IssueTime><cbc:DueDate>2026-04-15</cbc:DueDate><cbc:InvoiceTypeCode listID="0101">01</cbc:InvoiceTypeCode><cbc:DocumentCurrencyCode>PEN</cbc:DocumentCurrencyCode><cbc:Note languageLocaleID="1000">SON OCHO MIL OCHOCIENTOS CINCUENTA Y 00/100 SOLES</cbc:Note><cac:AccountingSupplierParty><cac:Party><cac:PartyIdentification><cbc:ID schemeID="6">20601234567</cbc:ID></cac:PartyIdentification><cac:PartyLegalEntity><cbc:RegistrationName>ETLIONS S.A.C.</cbc:RegistrationName></cac:PartyLegalEntity></cac:Party></cac:AccountingSupplierParty><cac:AccountingCustomerParty><cac:Party><cac:PartyIdentification><cbc:ID schemeID="6">20301987654</cbc:ID></cac:PartyIdentification><cac:PartyLegalEntity><cbc:RegistrationName>MINERA CUSCO GOLD S.A.C.</cbc:RegistrationName></cac:PartyLegalEntity></cac:Party></cac:AccountingCustomerParty><cac:PaymentTerms><cbc:ID>FormaPago</cbc:ID><cbc:PaymentMeansID>Credito</cbc:PaymentMeansID><cbc:Amount currencyID="PEN">8850.00</cbc:Amount></cac:PaymentTerms><cac:PaymentTerms><cbc:ID>FormaPago</cbc:ID><cbc:PaymentMeansID>Cuota001</cbc:PaymentMeansID><cbc:Amount currencyID="PEN">2950.00</cbc:Amount><cbc:PaymentDueDate>2026-04-15</cbc:PaymentDueDate></cac:PaymentTerms><cac:PaymentTerms><cbc:ID>FormaPago</cbc:ID><cbc:PaymentMeansID>Cuota002</cbc:PaymentMeansID><cbc:Amount currencyID="PEN">2950.00</cbc:Amount><cbc:PaymentDueDate>2026-05-15</cbc:PaymentDueDate></cac:PaymentTerms><cac:PaymentTerms><cbc:ID>FormaPago</cbc:ID><cbc:PaymentMeansID>Cuota003</cbc:PaymentMeansID><cbc:Amount currencyID="PEN">2950.00</cbc:Amount><cbc:PaymentDueDate>2026-06-15</cbc:PaymentDueDate></cac:PaymentTerms><cac:AllowanceCharge><cbc:ChargeIndicator>true</cbc:ChargeIndicator><cbc:AllowanceChargeReasonCode>46</cbc:AllowanceChargeReasonCode><cbc:MultiplierFactorNumeric>0.1000</cbc:MultiplierFactorNumeric><cbc:Amount currencyID="PEN">750.00</cbc:Amount><cbc:BaseAmount currencyID="PEN">7500.00</cbc:BaseAmount></cac:AllowanceCharge><cac:TaxTotal><cbc:TaxAmount currencyID="PEN">1350.00</cbc:TaxAmount></cac:TaxTotal><cac:LegalMonetaryTotal><cbc:LineExtensionAmount currencyID="PEN">7500.00</cbc:LineExtensionAmount><cbc:TaxInclusiveAmount currencyID="PEN">8850.00</cbc:TaxInclusiveAmount><cbc:ChargeTotalAmount currencyID="PEN">750.00</cbc:ChargeTotalAmount><cbc:PayableAmount currencyID="PEN">8850.00</cbc:PayableAmount></cac:LegalMonetaryTotal><cac:InvoiceLine><cbc:ID>1</cbc:ID><cbc:InvoicedQuantity unitCode="ZZ">3</cbc:InvoicedQuantity><cbc:LineExtensionAmount currencyID="PEN">7500.00</cbc:LineExtensionAmount><cac:Item><cbc:Description>Servicio de Consultoria en TI</cbc:Description></cac:Item><cac:Price><cbc:PriceAmount currencyID="PEN">2500.00</cbc:PriceAmount></cac:Price></cac:InvoiceLine></Invoice>',
xml_path = '20601234567/01/F001/00000004/20601234567-01-F001-00000004.xml'
WHERE id = 'fac-demo-005';

-- fac-demo-006 (ACEPTADA, PEN, Contado, Sede Miraflores F002)
UPDATE facturas SET xml_content =
'<?xml version="1.0" encoding="UTF-8"?><Invoice xmlns="urn:oasis:names:specification:ubl:schema:xsd:Invoice-2" xmlns:cac="urn:oasis:names:specification:ubl:schema:xsd:CommonAggregateComponents-2" xmlns:cbc="urn:oasis:names:specification:ubl:schema:xsd:CommonBasicComponents-2"><cbc:UBLVersionID>2.1</cbc:UBLVersionID><cbc:CustomizationID>2.0</cbc:CustomizationID><cbc:ID>F002-00000002</cbc:ID><cbc:IssueDate>2026-03-18</cbc:IssueDate><cbc:IssueTime>08:30:00</cbc:IssueTime><cbc:InvoiceTypeCode listID="0101">01</cbc:InvoiceTypeCode><cbc:DocumentCurrencyCode>PEN</cbc:DocumentCurrencyCode><cac:AccountingSupplierParty><cac:Party><cac:PartyIdentification><cbc:ID schemeID="6">20601234567</cbc:ID></cac:PartyIdentification><cac:PartyLegalEntity><cbc:RegistrationName>ETLIONS S.A.C.</cbc:RegistrationName></cac:PartyLegalEntity></cac:Party></cac:AccountingSupplierParty><cac:AccountingCustomerParty><cac:Party><cac:PartyIdentification><cbc:ID schemeID="6">20556677889</cbc:ID></cac:PartyIdentification><cac:PartyLegalEntity><cbc:RegistrationName>LOGISTICA EXPRESS PERU S.R.L.</cbc:RegistrationName></cac:PartyLegalEntity></cac:Party></cac:AccountingCustomerParty><cac:PaymentTerms><cbc:ID>FormaPago</cbc:ID><cbc:PaymentMeansID>Contado</cbc:PaymentMeansID></cac:PaymentTerms><cac:TaxTotal><cbc:TaxAmount currencyID="PEN">162.00</cbc:TaxAmount></cac:TaxTotal><cac:LegalMonetaryTotal><cbc:LineExtensionAmount currencyID="PEN">900.00</cbc:LineExtensionAmount><cbc:TaxInclusiveAmount currencyID="PEN">1062.00</cbc:TaxInclusiveAmount><cbc:PayableAmount currencyID="PEN">1062.00</cbc:PayableAmount></cac:LegalMonetaryTotal><cac:InvoiceLine><cbc:ID>1</cbc:ID><cbc:InvoicedQuantity unitCode="MON">2</cbc:InvoicedQuantity><cbc:LineExtensionAmount currencyID="PEN">900.00</cbc:LineExtensionAmount><cac:Item><cbc:Description>Hosting Cloud (mensual)</cbc:Description></cac:Item><cac:Price><cbc:PriceAmount currencyID="PEN">450.00</cbc:PriceAmount></cac:Price></cac:InvoiceLine></Invoice>',
xml_path = '20601234567/01/F002/00000002/20601234567-01-F002-00000002.xml'
WHERE id = 'fac-demo-006';

-- fac-demo-008 (ENVIADA, PEN, Crédito)
UPDATE facturas SET xml_content =
'<?xml version="1.0" encoding="UTF-8"?><Invoice xmlns="urn:oasis:names:specification:ubl:schema:xsd:Invoice-2" xmlns:cac="urn:oasis:names:specification:ubl:schema:xsd:CommonAggregateComponents-2" xmlns:cbc="urn:oasis:names:specification:ubl:schema:xsd:CommonBasicComponents-2"><cbc:UBLVersionID>2.1</cbc:UBLVersionID><cbc:CustomizationID>2.0</cbc:CustomizationID><cbc:ID>F001-00000006</cbc:ID><cbc:IssueDate>2026-03-25</cbc:IssueDate><cbc:IssueTime>10:15:00</cbc:IssueTime><cbc:DueDate>2026-04-25</cbc:DueDate><cbc:InvoiceTypeCode listID="0101">01</cbc:InvoiceTypeCode><cbc:DocumentCurrencyCode>PEN</cbc:DocumentCurrencyCode><cac:AccountingSupplierParty><cac:Party><cac:PartyIdentification><cbc:ID schemeID="6">20601234567</cbc:ID></cac:PartyIdentification><cac:PartyLegalEntity><cbc:RegistrationName>ETLIONS S.A.C.</cbc:RegistrationName></cac:PartyLegalEntity></cac:Party></cac:AccountingSupplierParty><cac:AccountingCustomerParty><cac:Party><cac:PartyIdentification><cbc:ID schemeID="6">20612345678</cbc:ID></cac:PartyIdentification><cac:PartyLegalEntity><cbc:RegistrationName>GLOBAL IMPORTS E.I.R.L.</cbc:RegistrationName></cac:PartyLegalEntity></cac:Party></cac:AccountingCustomerParty><cac:PaymentTerms><cbc:ID>FormaPago</cbc:ID><cbc:PaymentMeansID>Credito</cbc:PaymentMeansID><cbc:Amount currencyID="PEN">4248.00</cbc:Amount></cac:PaymentTerms><cac:PaymentTerms><cbc:ID>FormaPago</cbc:ID><cbc:PaymentMeansID>Cuota001</cbc:PaymentMeansID><cbc:Amount currencyID="PEN">4248.00</cbc:Amount><cbc:PaymentDueDate>2026-04-25</cbc:PaymentDueDate></cac:PaymentTerms><cac:TaxTotal><cbc:TaxAmount currencyID="PEN">648.00</cbc:TaxAmount></cac:TaxTotal><cac:LegalMonetaryTotal><cbc:LineExtensionAmount currencyID="PEN">3600.00</cbc:LineExtensionAmount><cbc:TaxInclusiveAmount currencyID="PEN">4248.00</cbc:TaxInclusiveAmount><cbc:PayableAmount currencyID="PEN">4248.00</cbc:PayableAmount></cac:LegalMonetaryTotal><cac:InvoiceLine><cbc:ID>1</cbc:ID><cbc:InvoicedQuantity unitCode="ZZ">2</cbc:InvoicedQuantity><cbc:LineExtensionAmount currencyID="PEN">3600.00</cbc:LineExtensionAmount><cac:Item><cbc:Description>Licencia Software ERP Anual</cbc:Description></cac:Item><cac:Price><cbc:PriceAmount currencyID="PEN">1800.00</cbc:PriceAmount></cac:Price></cac:InvoiceLine></Invoice>',
xml_path = '20601234567/01/F001/00000006/20601234567-01-F001-00000006.xml'
WHERE id = 'fac-demo-008';

-- fac-demo-009 (ENVIADA, USD, Contado, Sede Miraflores)
UPDATE facturas SET xml_content =
'<?xml version="1.0" encoding="UTF-8"?><Invoice xmlns="urn:oasis:names:specification:ubl:schema:xsd:Invoice-2" xmlns:cac="urn:oasis:names:specification:ubl:schema:xsd:CommonAggregateComponents-2" xmlns:cbc="urn:oasis:names:specification:ubl:schema:xsd:CommonBasicComponents-2"><cbc:UBLVersionID>2.1</cbc:UBLVersionID><cbc:CustomizationID>2.0</cbc:CustomizationID><cbc:ID>F002-00000003</cbc:ID><cbc:IssueDate>2026-03-28</cbc:IssueDate><cbc:IssueTime>15:30:00</cbc:IssueTime><cbc:InvoiceTypeCode listID="0101">01</cbc:InvoiceTypeCode><cbc:DocumentCurrencyCode>USD</cbc:DocumentCurrencyCode><cac:AccountingSupplierParty><cac:Party><cac:PartyIdentification><cbc:ID schemeID="6">20601234567</cbc:ID></cac:PartyIdentification><cac:PartyLegalEntity><cbc:RegistrationName>ETLIONS S.A.C.</cbc:RegistrationName></cac:PartyLegalEntity></cac:Party></cac:AccountingSupplierParty><cac:AccountingCustomerParty><cac:Party><cac:PartyIdentification><cbc:ID schemeID="6">20498765432</cbc:ID></cac:PartyIdentification><cac:PartyLegalEntity><cbc:RegistrationName>DISTRIBUIDORA ANDINA S.A.</cbc:RegistrationName></cac:PartyLegalEntity></cac:Party></cac:AccountingCustomerParty><cac:PaymentTerms><cbc:ID>FormaPago</cbc:ID><cbc:PaymentMeansID>Contado</cbc:PaymentMeansID></cac:PaymentTerms><cac:TaxTotal><cbc:TaxAmount currencyID="USD">432.00</cbc:TaxAmount></cac:TaxTotal><cac:LegalMonetaryTotal><cbc:LineExtensionAmount currencyID="USD">2400.00</cbc:LineExtensionAmount><cbc:TaxInclusiveAmount currencyID="USD">2832.00</cbc:TaxInclusiveAmount><cbc:PayableAmount currencyID="USD">2832.00</cbc:PayableAmount></cac:LegalMonetaryTotal><cac:InvoiceLine><cbc:ID>1</cbc:ID><cbc:InvoicedQuantity unitCode="HUR">8</cbc:InvoicedQuantity><cbc:LineExtensionAmount currencyID="USD">2400.00</cbc:LineExtensionAmount><cac:Item><cbc:Description>Capacitacion Equipo (8 horas)</cbc:Description></cac:Item><cac:Price><cbc:PriceAmount currencyID="USD">300.00</cbc:PriceAmount></cac:Price></cac:InvoiceLine></Invoice>',
xml_path = '20601234567/01/F002/00000003/20601234567-01-F002-00000003.xml'
WHERE id = 'fac-demo-009';

-- fac-demo-010 (ENVIADA, PEN, Contado)
UPDATE facturas SET xml_content =
'<?xml version="1.0" encoding="UTF-8"?><Invoice xmlns="urn:oasis:names:specification:ubl:schema:xsd:Invoice-2" xmlns:cac="urn:oasis:names:specification:ubl:schema:xsd:CommonAggregateComponents-2" xmlns:cbc="urn:oasis:names:specification:ubl:schema:xsd:CommonBasicComponents-2"><cbc:UBLVersionID>2.1</cbc:UBLVersionID><cbc:CustomizationID>2.0</cbc:CustomizationID><cbc:ID>F001-00000007</cbc:ID><cbc:IssueDate>2026-04-01</cbc:IssueDate><cbc:IssueTime>08:00:00</cbc:IssueTime><cbc:InvoiceTypeCode listID="0101">01</cbc:InvoiceTypeCode><cbc:DocumentCurrencyCode>PEN</cbc:DocumentCurrencyCode><cac:AccountingSupplierParty><cac:Party><cac:PartyIdentification><cbc:ID schemeID="6">20601234567</cbc:ID></cac:PartyIdentification><cac:PartyLegalEntity><cbc:RegistrationName>ETLIONS S.A.C.</cbc:RegistrationName></cac:PartyLegalEntity></cac:Party></cac:AccountingSupplierParty><cac:AccountingCustomerParty><cac:Party><cac:PartyIdentification><cbc:ID schemeID="6">20301987654</cbc:ID></cac:PartyIdentification><cac:PartyLegalEntity><cbc:RegistrationName>MINERA CUSCO GOLD S.A.C.</cbc:RegistrationName></cac:PartyLegalEntity></cac:Party></cac:AccountingCustomerParty><cac:PaymentTerms><cbc:ID>FormaPago</cbc:ID><cbc:PaymentMeansID>Contado</cbc:PaymentMeansID></cac:PaymentTerms><cac:TaxTotal><cbc:TaxAmount currencyID="PEN">50.40</cbc:TaxAmount></cac:TaxTotal><cac:LegalMonetaryTotal><cbc:LineExtensionAmount currencyID="PEN">280.00</cbc:LineExtensionAmount><cbc:TaxInclusiveAmount currencyID="PEN">330.40</cbc:TaxInclusiveAmount><cbc:PayableAmount currencyID="PEN">330.40</cbc:PayableAmount></cac:LegalMonetaryTotal><cac:InvoiceLine><cbc:ID>1</cbc:ID><cbc:InvoicedQuantity unitCode="ZZ">1</cbc:InvoicedQuantity><cbc:LineExtensionAmount currencyID="PEN">280.00</cbc:LineExtensionAmount><cac:Item><cbc:Description>Certificado Digital SUNAT</cbc:Description></cac:Item><cac:Price><cbc:PriceAmount currencyID="PEN">280.00</cbc:PriceAmount></cac:Price></cac:InvoiceLine></Invoice>',
xml_path = '20601234567/01/F001/00000007/20601234567-01-F001-00000007.xml'
WHERE id = 'fac-demo-010';

-- ══════ REQ-8: DOCUMENTOS COMPLETOS (crédito + cuotas + descuentos + multi-item) ══════

-- fac-demo-025: BORRADOR, CRÉDITO, 4 ítems, cargo 10% + descuento 5%, 3 cuotas
-- Gravadas: 5000+1800+1050+560 = 8410 | IGV: 900+324+189+100.80 = 1513.80
-- Cargo: 841.00 (10% de 8410) | Descuento: 420.50 (5% de 8410)
-- Total: 8410 + 1513.80 + 841.00 - 420.50 = 10344.30
INSERT INTO facturas (id, company_id, client_id, serie, numero, fecha_emision, moneda, forma_pago, fecha_vencimiento, codigo_ubigeo, tipo_operacion, hora_emision,
    gravadas, exoneradas, inafectas, gratuitas, igv, total, total_cargos, total_descuentos, subtotal, total_con_impuestos,
    estado, leyenda, detalle_json, fecha_creacion) VALUES
('fac-demo-025', 'cmp-demo-001', 'cli-demo-003', 'F001', 17, DATE '2026-04-07', 'PEN', 'CREDITO', DATE '2026-06-07', '150131', '0101', TIME '11:30:00',
    8410.00, 0, 0, 0, 1513.80, 10344.30, 841.00, 420.50, 8410.00, 9923.80,
    'BORRADOR',
    'SON DIEZ MIL TRESCIENTOS CUARENTA Y CUATRO Y 30/100 SOLES',
    '[{"descripcion":"Servicio de Consultoría en TI","unidadMedida":"ZZ","cantidad":2,"valorUnitario":2500.00,"precioUnitario":2950.00,"igv":900.00,"subtotal":5000.00,"tipoAfectacionIgv":"10","codigoTributo":"1000","tipoPrecio":"01","porcentajeIgv":18.00,"baseImponible":5000.00,"montoImpuesto":900.00},{"descripcion":"Licencia Software ERP Anual","unidadMedida":"ZZ","cantidad":1,"valorUnitario":1800.00,"precioUnitario":2124.00,"igv":324.00,"subtotal":1800.00,"tipoAfectacionIgv":"10","codigoTributo":"1000","tipoPrecio":"01","porcentajeIgv":18.00,"baseImponible":1800.00,"montoImpuesto":324.00},{"descripcion":"Soporte Técnico Premium (mensual)","unidadMedida":"ZZ","cantidad":3,"valorUnitario":350.00,"precioUnitario":413.00,"igv":63.00,"subtotal":1050.00,"tipoAfectacionIgv":"10","codigoTributo":"1000","tipoPrecio":"01","porcentajeIgv":18.00,"baseImponible":1050.00,"montoImpuesto":189.00},{"descripcion":"Certificado Digital SUNAT","unidadMedida":"ZZ","cantidad":2,"valorUnitario":280.00,"precioUnitario":330.40,"igv":50.40,"subtotal":560.00,"tipoAfectacionIgv":"10","codigoTributo":"1000","tipoPrecio":"01","porcentajeIgv":18.00,"baseImponible":560.00,"montoImpuesto":100.80}]',
    TIMESTAMP '2026-04-07 11:30:00');

-- Cargos/Descuentos para fac-demo-025
INSERT INTO allowance_charges (id, factura_id, boleta_id, charge_indicator, reason_code, reason_description, multiplier_factor, amount, base_amount, moneda) VALUES
('ac-025-1', 'fac-demo-025', NULL, TRUE, '46', 'Recargo al consumo', 0.1000, 841.00, 8410.00, 'PEN'),
('ac-025-2', 'fac-demo-025', NULL, FALSE, '00', 'Descuento comercial global', 0.0500, 420.50, 8410.00, 'PEN');

-- Cuotas para fac-demo-025 (3 cuotas × 3448.10 = 10344.30)
INSERT INTO cuotas_credito (id, factura_id, boleta_id, numero_cuota, monto, fecha_vencimiento, moneda) VALUES
('cuota-025-1', 'fac-demo-025', NULL, 'Cuota001', 3448.10, DATE '2026-05-07', 'PEN'),
('cuota-025-2', 'fac-demo-025', NULL, 'Cuota002', 3448.10, DATE '2026-06-07', 'PEN'),
('cuota-025-3', 'fac-demo-025', NULL, 'Cuota003', 3448.10, DATE '2026-07-07', 'PEN');

-- fac-demo-026: ACEPTADA, CRÉDITO, 3 ítems, descuento 3%, 2 cuotas
-- Gravadas: 8400+4500+2700 = 15600 | IGV: 1512+810+486 = 2808
-- Descuento: 468.00 (3% de 15600)
-- Total: 15600 + 2808 - 468 = 17940.00
INSERT INTO facturas (id, company_id, client_id, serie, numero, fecha_emision, moneda, forma_pago, fecha_vencimiento, codigo_ubigeo, tipo_operacion, hora_emision,
    gravadas, exoneradas, inafectas, gratuitas, igv, total, total_cargos, total_descuentos, subtotal, total_con_impuestos,
    estado, hash_cdr, codigo_respuesta_sunat, descripcion_respuesta_sunat,
    leyenda, detalle_json, fecha_creacion) VALUES
('fac-demo-026', 'cmp-demo-001', 'cli-demo-007', 'F001', 18, DATE '2026-04-01', 'PEN', 'CREDITO', DATE '2026-05-31', '150131', '0101', TIME '14:00:00',
    15600.00, 0, 0, 0, 2808.00, 17940.00, 0, 468.00, 15600.00, 18408.00,
    'ACEPTADA', 'g7h8i9j0k1l2m3n4o5p6', '0', 'La Factura fue aceptada',
    'SON DIECISIETE MIL NOVECIENTOS CUARENTA Y 00/100 SOLES',
    '[{"descripcion":"Laptop Empresarial ThinkPad T14","unidadMedida":"NIU","cantidad":2,"valorUnitario":4200.00,"precioUnitario":4956.00,"igv":756.00,"subtotal":8400.00,"tipoAfectacionIgv":"10","codigoTributo":"1000","tipoPrecio":"01","porcentajeIgv":18.00,"baseImponible":8400.00,"montoImpuesto":1512.00},{"descripcion":"Monitor 27 4K USB-C","unidadMedida":"NIU","cantidad":3,"valorUnitario":1500.00,"precioUnitario":1770.00,"igv":270.00,"subtotal":4500.00,"tipoAfectacionIgv":"10","codigoTributo":"1000","tipoPrecio":"01","porcentajeIgv":18.00,"baseImponible":4500.00,"montoImpuesto":810.00},{"descripcion":"Hosting Cloud (mensual)","unidadMedida":"MON","cantidad":6,"valorUnitario":450.00,"precioUnitario":531.00,"igv":81.00,"subtotal":2700.00,"tipoAfectacionIgv":"10","codigoTributo":"1000","tipoPrecio":"01","porcentajeIgv":18.00,"baseImponible":2700.00,"montoImpuesto":486.00}]',
    TIMESTAMP '2026-04-01 14:00:00');

-- Descuento para fac-demo-026
INSERT INTO allowance_charges (id, factura_id, boleta_id, charge_indicator, reason_code, reason_description, multiplier_factor, amount, base_amount, moneda) VALUES
('ac-026-1', 'fac-demo-026', NULL, FALSE, '02', 'Descuento por volumen', 0.0300, 468.00, 15600.00, 'PEN');

-- Cuotas para fac-demo-026 (2 cuotas × 8970.00 = 17940.00)
INSERT INTO cuotas_credito (id, factura_id, boleta_id, numero_cuota, monto, fecha_vencimiento, moneda) VALUES
('cuota-026-1', 'fac-demo-026', NULL, 'Cuota001', 8970.00, DATE '2026-05-01', 'PEN'),
('cuota-026-2', 'fac-demo-026', NULL, 'Cuota002', 8970.00, DATE '2026-05-31', 'PEN');

-- XML para fac-demo-026 (ACEPTADA con crédito + cuotas + descuento)
UPDATE facturas SET xml_content =
'<?xml version="1.0" encoding="UTF-8"?><Invoice xmlns="urn:oasis:names:specification:ubl:schema:xsd:Invoice-2" xmlns:cac="urn:oasis:names:specification:ubl:schema:xsd:CommonAggregateComponents-2" xmlns:cbc="urn:oasis:names:specification:ubl:schema:xsd:CommonBasicComponents-2"><cbc:UBLVersionID>2.1</cbc:UBLVersionID><cbc:CustomizationID>2.0</cbc:CustomizationID><cbc:ID>F001-00000018</cbc:ID><cbc:IssueDate>2026-04-01</cbc:IssueDate><cbc:IssueTime>14:00:00</cbc:IssueTime><cbc:DueDate>2026-05-31</cbc:DueDate><cbc:InvoiceTypeCode listID="0101">01</cbc:InvoiceTypeCode><cbc:DocumentCurrencyCode>PEN</cbc:DocumentCurrencyCode><cbc:Note languageLocaleID="1000">SON DIECISIETE MIL NOVECIENTOS CUARENTA Y 00/100 SOLES</cbc:Note><cac:AccountingSupplierParty><cac:Party><cac:PartyIdentification><cbc:ID schemeID="6">20601234567</cbc:ID></cac:PartyIdentification><cac:PartyLegalEntity><cbc:RegistrationName>ETLIONS S.A.C.</cbc:RegistrationName><cac:RegistrationAddress><cbc:ID schemeName="Ubigeos">150131</cbc:ID><cbc:AddressTypeCode>0001</cbc:AddressTypeCode><cbc:CityName>LIMA</cbc:CityName><cbc:CountrySubentity>LIMA</cbc:CountrySubentity><cbc:District>SAN ISIDRO</cbc:District><cac:AddressLine><cbc:Line>Av. Javier Prado Este 1234, Piso 8</cbc:Line></cac:AddressLine><cac:Country><cbc:IdentificationCode>PE</cbc:IdentificationCode></cac:Country></cac:RegistrationAddress></cac:PartyLegalEntity></cac:Party></cac:AccountingSupplierParty><cac:AccountingCustomerParty><cac:Party><cac:PartyIdentification><cbc:ID schemeID="6">20301987654</cbc:ID></cac:PartyIdentification><cac:PartyLegalEntity><cbc:RegistrationName>MINERA CUSCO GOLD S.A.C.</cbc:RegistrationName></cac:PartyLegalEntity></cac:Party></cac:AccountingCustomerParty><cac:PaymentTerms><cbc:ID>FormaPago</cbc:ID><cbc:PaymentMeansID>Credito</cbc:PaymentMeansID><cbc:Amount currencyID="PEN">17940.00</cbc:Amount></cac:PaymentTerms><cac:PaymentTerms><cbc:ID>FormaPago</cbc:ID><cbc:PaymentMeansID>Cuota001</cbc:PaymentMeansID><cbc:Amount currencyID="PEN">8970.00</cbc:Amount><cbc:PaymentDueDate>2026-05-01</cbc:PaymentDueDate></cac:PaymentTerms><cac:PaymentTerms><cbc:ID>FormaPago</cbc:ID><cbc:PaymentMeansID>Cuota002</cbc:PaymentMeansID><cbc:Amount currencyID="PEN">8970.00</cbc:Amount><cbc:PaymentDueDate>2026-05-31</cbc:PaymentDueDate></cac:PaymentTerms><cac:AllowanceCharge><cbc:ChargeIndicator>false</cbc:ChargeIndicator><cbc:AllowanceChargeReasonCode>02</cbc:AllowanceChargeReasonCode><cbc:MultiplierFactorNumeric>0.0300</cbc:MultiplierFactorNumeric><cbc:Amount currencyID="PEN">468.00</cbc:Amount><cbc:BaseAmount currencyID="PEN">15600.00</cbc:BaseAmount></cac:AllowanceCharge><cac:TaxTotal><cbc:TaxAmount currencyID="PEN">2808.00</cbc:TaxAmount><cac:TaxSubtotal><cbc:TaxableAmount currencyID="PEN">15600.00</cbc:TaxableAmount><cbc:TaxAmount currencyID="PEN">2808.00</cbc:TaxAmount><cac:TaxCategory><cbc:ID>S</cbc:ID><cbc:Percent>18.00</cbc:Percent><cac:TaxScheme><cbc:ID>1000</cbc:ID><cbc:Name>IGV</cbc:Name></cac:TaxScheme></cac:TaxCategory></cac:TaxSubtotal></cac:TaxTotal><cac:LegalMonetaryTotal><cbc:LineExtensionAmount currencyID="PEN">15600.00</cbc:LineExtensionAmount><cbc:TaxInclusiveAmount currencyID="PEN">18408.00</cbc:TaxInclusiveAmount><cbc:AllowanceTotalAmount currencyID="PEN">468.00</cbc:AllowanceTotalAmount><cbc:PayableAmount currencyID="PEN">17940.00</cbc:PayableAmount></cac:LegalMonetaryTotal><cac:InvoiceLine><cbc:ID>1</cbc:ID><cbc:InvoicedQuantity unitCode="NIU">2</cbc:InvoicedQuantity><cbc:LineExtensionAmount currencyID="PEN">8400.00</cbc:LineExtensionAmount><cac:Item><cbc:Description>Laptop Empresarial ThinkPad T14</cbc:Description></cac:Item><cac:Price><cbc:PriceAmount currencyID="PEN">4200.00</cbc:PriceAmount></cac:Price></cac:InvoiceLine><cac:InvoiceLine><cbc:ID>2</cbc:ID><cbc:InvoicedQuantity unitCode="NIU">3</cbc:InvoicedQuantity><cbc:LineExtensionAmount currencyID="PEN">4500.00</cbc:LineExtensionAmount><cac:Item><cbc:Description>Monitor 27 4K USB-C</cbc:Description></cac:Item><cac:Price><cbc:PriceAmount currencyID="PEN">1500.00</cbc:PriceAmount></cac:Price></cac:InvoiceLine><cac:InvoiceLine><cbc:ID>3</cbc:ID><cbc:InvoicedQuantity unitCode="MON">6</cbc:InvoicedQuantity><cbc:LineExtensionAmount currencyID="PEN">2700.00</cbc:LineExtensionAmount><cac:Item><cbc:Description>Hosting Cloud (mensual)</cbc:Description></cac:Item><cac:Price><cbc:PriceAmount currencyID="PEN">450.00</cbc:PriceAmount></cac:Price></cac:InvoiceLine></Invoice>',
xml_path = '20601234567/01/F001/00000018/20601234567-01-F001-00000018.xml'
WHERE id = 'fac-demo-026';

-- Todas las ACEPTADAS y ENVIADAS restantes: generar xml_path mínimo
UPDATE facturas SET xml_path = '20601234567/01/' || serie || '/' || LPAD(CAST(numero AS VARCHAR), 8, '0') || '/20601234567-01-' || serie || '-' || LPAD(CAST(numero AS VARCHAR), 8, '0') || '.xml'
WHERE estado IN ('ACEPTADA', 'ENVIADA') AND xml_path IS NULL;

-- ══════ BOLETAS: Enriquecimiento REQ-A ══════

-- bol-demo-005 CON_ERROR: errores de validación
UPDATE boletas SET errores_validacion = '["El IGV calculado (0.00) no coincide con el total esperado","La base gravada debe ser mayor a 0 si tipoAfectacionIgv=10"]'
WHERE id = 'bol-demo-005';

-- Boletas VALIDADAS: generar forma_pago y xml_path
UPDATE boletas SET forma_pago = 'CONTADO', fecha_vencimiento = fecha_emision WHERE forma_pago IS NULL;

-- Boletas ACEPTADAS/VALIDADAS/ENVIADAS: generar xml_path
UPDATE boletas SET xml_path = '20601234567/03/' || serie || '/' || LPAD(CAST(numero AS VARCHAR), 8, '0') || '/20601234567-03-' || serie || '-' || LPAD(CAST(numero AS VARCHAR), 8, '0') || '.xml'
WHERE estado IN ('ACEPTADA', 'VALIDADO', 'ENVIADA') AND xml_path IS NULL;

-- ══════ RESÚMENES DIARIOS (seed) ══════
INSERT INTO resumenes_diarios (id, company_id, identificador, fecha_generacion, fecha_referencia, estado, ticket_sunat, codigo_respuesta, descripcion_respuesta, cantidad_boletas, fecha_creacion) VALUES
('rd-demo-001', 'cmp-demo-001', 'RC-20260405-001', DATE '2026-04-05', DATE '2026-04-05', 'ACEPTADO',
    'mock-ticket-1712300000001', '0', 'El Resumen diario RC-20260405-001 ha sido aceptado',
    2, TIMESTAMP '2026-04-05 18:00:00'),
('rd-demo-002', 'cmp-demo-001', 'RC-20260406-001', DATE '2026-04-06', DATE '2026-04-06', 'ENVIADO',
    'mock-ticket-1712400000001', NULL, NULL,
    1, TIMESTAMP '2026-04-06 19:00:00');

-- Asociar boletas a resúmenes diarios
UPDATE boletas SET resumen_diario_id = 'rd-demo-001' WHERE id IN ('bol-demo-001', 'bol-demo-007');
UPDATE boletas SET resumen_diario_id = 'rd-demo-002' WHERE id = 'bol-demo-006';

