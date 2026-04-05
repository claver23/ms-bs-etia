package com.etlions.ms_bs_etia.app_util.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CorrelativoRepository extends JpaRepository<CorrelativoEntity, String> {

    Optional<CorrelativoEntity> findByCompanyIdAndSerie(String companyId, String serie);
}

