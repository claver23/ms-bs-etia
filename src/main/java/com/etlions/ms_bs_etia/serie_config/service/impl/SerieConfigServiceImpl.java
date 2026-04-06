package com.etlions.ms_bs_etia.serie_config.service.impl;

import com.etlions.ms_bs_etia.serie_config.persistence.SerieConfigEntity;
import com.etlions.ms_bs_etia.serie_config.persistence.SerieConfigRepository;
import com.etlions.ms_bs_etia.serie_config.service.SerieConfigService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class SerieConfigServiceImpl implements SerieConfigService {

    private final SerieConfigRepository serieConfigRepository;

    @Override
    public List<SerieConfigEntity> listarPorCompany(String companyId) {
        return serieConfigRepository.findByCompanyId(companyId);
    }

    @Override
    public List<SerieConfigEntity> listarPorTipo(String companyId, String tipoDocumento) {
        return serieConfigRepository.findByCompanyIdAndTipoDocumento(companyId, tipoDocumento);
    }

    @Override
    public List<SerieConfigEntity> listarPorSede(String sedeId) {
        return serieConfigRepository.findBySedeId(sedeId);
    }
}

