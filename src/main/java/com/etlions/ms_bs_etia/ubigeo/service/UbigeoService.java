package com.etlions.ms_bs_etia.ubigeo.service;

import com.etlions.ms_bs_etia.ubigeo.persistence.UbigeoEntity;
import java.util.List;
import java.util.Optional;

public interface UbigeoService {
    List<UbigeoEntity> listarTodos();
    Optional<UbigeoEntity> buscarPorCodigo(String codigo);
}

