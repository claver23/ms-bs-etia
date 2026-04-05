package com.etlions.ms_bs_etia.doc_factura.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FacturaRepository extends JpaRepository<FacturaEntity, String> {

    List<FacturaEntity> findByCompanyId(String companyId);
}

