package com.etlions.ms_bs_etia.reniec.service;

import java.util.Map;

public interface ReniecService {
    Map<String, Object> consultarDocumento(String tipo, String numero);
}

