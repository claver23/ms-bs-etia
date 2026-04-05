package com.etlions.ms_bs_etia.client.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ClientRepository extends JpaRepository<ClientEntity, String> {

    List<ClientEntity> findByCompanyId(String companyId);
}

