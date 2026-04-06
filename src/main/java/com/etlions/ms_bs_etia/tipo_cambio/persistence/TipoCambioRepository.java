package com.etlions.ms_bs_etia.tipo_cambio.persistence;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.Optional;

public interface TipoCambioRepository extends JpaRepository<TipoCambioEntity, String> {

    Optional<TipoCambioEntity> findFirstByFechaAndMonedaOrigenOrderByFechaCreacionDesc(
            LocalDate fecha, String monedaOrigen);

    Optional<TipoCambioEntity> findFirstByFechaAndMonedaOrigenAndFuenteOrderByFechaCreacionDesc(
            LocalDate fecha, String monedaOrigen, String fuente);

    Optional<TipoCambioEntity> findFirstByMonedaOrigenOrderByFechaDesc(String monedaOrigen);

    Page<TipoCambioEntity> findByFuente(String fuente, Pageable pageable);
}

