package com.etlions.ms_bs_etia.serie_config.service;

import com.etlions.ms_bs_etia.serie_config.persistence.SerieConfigEntity;
import java.util.List;

public interface SerieConfigService {
    List<SerieConfigEntity> listarPorCompany(String companyId);
    List<SerieConfigEntity> listarPorTipo(String companyId, String tipoDocumento);
}

