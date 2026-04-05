package com.etlions.ms_bs_etia.doc_notacred.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NotaCreditoRepository extends JpaRepository<NotaCreditoEntity, String> {

    List<NotaCreditoEntity> findByCompanyId(String companyId);
}

