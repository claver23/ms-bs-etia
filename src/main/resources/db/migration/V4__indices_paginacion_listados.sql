-- Indices para acelerar listados paginados con filtros por empresa y fechas.
CREATE INDEX idx_facturas_company_fecha_numero
    ON facturas (company_id, fecha_emision, numero);

CREATE INDEX idx_facturas_company_estado_fecha
    ON facturas (company_id, estado, fecha_emision);

CREATE INDEX idx_boletas_company_fecha_numero
    ON boletas (company_id, fecha_emision, numero);

CREATE INDEX idx_boletas_company_serie_numero
    ON boletas (company_id, serie, numero);

