package com.etlions.ms_bs_etia.sede.service;

import com.etlions.ms_bs_etia.sede.persistence.SedeEntity;
import com.etlions.ms_bs_etia.serie_config.persistence.SerieConfigEntity;
import java.util.List;

public interface SedeService {
    List<SedeEntity> listarPorCompany(String companyId);
    List<SedeEntity> listarPorCompanyYEstado(String companyId, String estado);
    SedeEntity obtenerSede(String sedeId);
    SedeEntity crearSede(SedeEntity sede, boolean generarSeries);
    SedeEntity actualizarSede(String sedeId, SedeEntity updated);
    void eliminarSede(String sedeId);
    List<SerieConfigEntity> generarSeriesParaSede(String sedeId);
}

