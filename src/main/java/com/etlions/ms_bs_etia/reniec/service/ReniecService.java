package com.etlions.ms_bs_etia.reniec.service;

import com.etlions.openapicodegen.dto.ReniecData;

public interface ReniecService {
    ReniecData consultarDocumento(String tipo, String numero);
}

