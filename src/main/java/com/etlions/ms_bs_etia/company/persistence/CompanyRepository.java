package com.etlions.ms_bs_etia.company.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CompanyRepository extends JpaRepository<CompanyEntity, String> {

    Optional<CompanyEntity> findByRuc(String ruc);

    boolean existsByRuc(String ruc);
}

