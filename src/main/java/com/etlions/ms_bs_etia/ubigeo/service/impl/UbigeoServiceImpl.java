package com.etlions.ms_bs_etia.ubigeo.service.impl;

import com.etlions.ms_bs_etia.ubigeo.persistence.UbigeoEntity;
import com.etlions.ms_bs_etia.ubigeo.persistence.UbigeoRepository;
import com.etlions.ms_bs_etia.ubigeo.service.UbigeoService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UbigeoServiceImpl implements UbigeoService {

    private final UbigeoRepository ubigeoRepository;

    @Override
    public List<UbigeoEntity> listarTodos() {
        return ubigeoRepository.findAll();
    }

    @Override
    public Optional<UbigeoEntity> buscarPorCodigo(String codigo) {
        return ubigeoRepository.findByCodigo(codigo);
    }
}

