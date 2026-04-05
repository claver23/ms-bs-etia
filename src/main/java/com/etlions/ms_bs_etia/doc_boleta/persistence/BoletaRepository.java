package com.etlions.ms_bs_etia.doc_boleta.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BoletaRepository extends JpaRepository<BoletaEntity, String> {

    List<BoletaEntity> findByCompanyId(String companyId);
}

