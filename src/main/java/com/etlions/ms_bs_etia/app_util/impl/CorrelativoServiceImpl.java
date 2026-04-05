package com.etlions.ms_bs_etia.app_util.impl;

import com.etlions.ms_bs_etia.app_util.CorrelativoService;
import com.etlions.ms_bs_etia.app_util.persistence.CorrelativoEntity;
import com.etlions.ms_bs_etia.app_util.persistence.CorrelativoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CorrelativoServiceImpl implements CorrelativoService {

    private final CorrelativoRepository correlativoRepository;

    @Override
    @Transactional
    public int siguienteNumero(String companyId, String serie) {
        CorrelativoEntity correlativo = correlativoRepository
                .findByCompanyIdAndSerie(companyId, serie)
                .orElseGet(() -> correlativoRepository.save(
                        CorrelativoEntity.builder()
                                .companyId(companyId)
                                .serie(serie)
                                .ultimoNumero(0)
                                .build()));
        int siguiente = correlativo.getUltimoNumero() + 1;
        correlativo.setUltimoNumero(siguiente);
        correlativoRepository.save(correlativo);
        return siguiente;
    }
}

