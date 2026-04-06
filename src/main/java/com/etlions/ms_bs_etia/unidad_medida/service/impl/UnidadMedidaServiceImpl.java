package com.etlions.ms_bs_etia.unidad_medida.service.impl;

import com.etlions.ms_bs_etia.unidad_medida.persistence.UnidadMedidaEntity;
import com.etlions.ms_bs_etia.unidad_medida.persistence.UnidadMedidaRepository;
import com.etlions.ms_bs_etia.unidad_medida.service.UnidadMedidaService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UnidadMedidaServiceImpl implements UnidadMedidaService {

    private final UnidadMedidaRepository unidadMedidaRepository;

    @Override
    public List<UnidadMedidaEntity> listarTodas() {
        return unidadMedidaRepository.findAll();
    }
}

