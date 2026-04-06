package com.etlions.ms_bs_etia.sede.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface SedeRepository extends JpaRepository<SedeEntity, String> {
    List<SedeEntity> findByCompanyId(String companyId);
    List<SedeEntity> findByCompanyIdAndEstado(String companyId, String estado);
    long countByCompanyId(String companyId);
    java.util.Optional<SedeEntity> findByCompanyIdAndEsDefaultTrue(String companyId);
}

