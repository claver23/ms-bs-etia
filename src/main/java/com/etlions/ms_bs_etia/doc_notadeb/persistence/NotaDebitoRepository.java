package com.etlions.ms_bs_etia.doc_notadeb.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NotaDebitoRepository extends JpaRepository<NotaDebitoEntity, String> {

    List<NotaDebitoEntity> findByCompanyId(String companyId);
}

