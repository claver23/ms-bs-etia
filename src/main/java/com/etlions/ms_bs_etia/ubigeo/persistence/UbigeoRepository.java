package com.etlions.ms_bs_etia.ubigeo.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface UbigeoRepository extends JpaRepository<UbigeoEntity, String> {
    Optional<UbigeoEntity> findByCodigo(String codigo);
}

