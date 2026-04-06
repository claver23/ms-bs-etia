package com.etlions.ms_bs_etia.serie_config.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface SerieConfigRepository extends JpaRepository<SerieConfigEntity, String> {
    List<SerieConfigEntity> findByCompanyId(String companyId);
    List<SerieConfigEntity> findByCompanyIdAndTipoDocumento(String companyId, String tipoDocumento);
    Optional<SerieConfigEntity> findByCompanyIdAndTipoDocumentoAndEsDefaultTrue(String companyId, String tipoDocumento);
}

