package com.etlions.ms_bs_etia.unidad_medida.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface UnidadMedidaRepository extends JpaRepository<UnidadMedidaEntity, String> {
    Optional<UnidadMedidaEntity> findByCodigo(String codigo);
}

