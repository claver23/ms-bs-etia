-- OLA A: indices para listados paginados en client, nota_credito, nota_debito y resumen_diario.

CREATE INDEX idx_clients_company_tipo_numero
    ON clients (company_id, tipo_documento, numero_documento);

CREATE INDEX idx_clients_company_denominacion
    ON clients (company_id, denominacion);

CREATE INDEX idx_notas_credito_company_fecha_serie_estado
    ON notas_credito (company_id, fecha_emision, serie, estado);

CREATE INDEX idx_notas_credito_company_docref
    ON notas_credito (company_id, documento_referencia_id);

CREATE INDEX idx_notas_debito_company_fecha_serie_estado
    ON notas_debito (company_id, fecha_emision, serie, estado);

CREATE INDEX idx_notas_debito_company_docref
    ON notas_debito (company_id, documento_referencia_id);

CREATE INDEX idx_resumenes_diarios_company_fecha
    ON resumenes_diarios (company_id, fecha_generacion);

