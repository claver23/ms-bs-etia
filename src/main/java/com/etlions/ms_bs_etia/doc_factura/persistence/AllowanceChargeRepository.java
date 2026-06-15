package com.etlions.ms_bs_etia.doc_factura.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AllowanceChargeRepository extends JpaRepository<AllowanceChargeEntity, String> {

    List<AllowanceChargeEntity> findByFacturaId(String facturaId);

    List<AllowanceChargeEntity> findByBoletaId(String boletaId);

    void deleteByFacturaId(String facturaId);

    void deleteByBoletaId(String boletaId);
}

