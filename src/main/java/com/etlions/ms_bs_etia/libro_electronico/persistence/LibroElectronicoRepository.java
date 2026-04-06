package com.etlions.ms_bs_etia.libro_electronico.persistence;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LibroElectronicoRepository extends JpaRepository<LibroElectronicoEntity, String> {

    Page<LibroElectronicoEntity> findByCompanyId(String companyId, Pageable pageable);

    Page<LibroElectronicoEntity> findByCompanyIdAndTipo(String companyId, String tipo, Pageable pageable);

    Page<LibroElectronicoEntity> findByCompanyIdAndPeriodo(String companyId, String periodo, Pageable pageable);

    Page<LibroElectronicoEntity> findByCompanyIdAndTipoAndPeriodo(String companyId, String tipo, String periodo, Pageable pageable);
}

