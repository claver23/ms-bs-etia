-- Endurece unicidad de numeracion por documento para soportar concurrencia alta.
ALTER TABLE facturas
    ADD CONSTRAINT uk_facturas_company_serie_numero UNIQUE (company_id, serie, numero);

ALTER TABLE boletas
    ADD CONSTRAINT uk_boletas_company_serie_numero UNIQUE (company_id, serie, numero);

-- Indices de soporte para busqueda y validacion de correlativos por scope.
CREATE INDEX idx_facturas_company_serie_numero ON facturas (company_id, serie, numero);
CREATE INDEX idx_boletas_company_serie_numero ON boletas (company_id, serie, numero);

