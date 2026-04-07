package com.etlions.ms_bs_etia.doc_factura.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CuotaCreditoRepository extends JpaRepository<CuotaCreditoEntity, String> {

    List<CuotaCreditoEntity> findByFacturaIdOrderByNumeroCuota(String facturaId);

    List<CuotaCreditoEntity> findByBoletaIdOrderByNumeroCuota(String boletaId);

    void deleteByFacturaId(String facturaId);

    void deleteByBoletaId(String boletaId);
}

