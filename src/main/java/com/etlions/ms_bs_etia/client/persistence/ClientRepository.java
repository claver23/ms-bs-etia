package com.etlions.ms_bs_etia.client.persistence;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ClientRepository extends JpaRepository<ClientEntity, String> {

    List<ClientEntity> findByCompanyId(String companyId);

    @Query("""
            select c
            from ClientEntity c
            where c.companyId = :companyId
              and (:tipoDocumento is null or c.tipoDocumento = :tipoDocumento)
              and (:numeroDocumento is null or c.numeroDocumento = :numeroDocumento)
              and (:denominacion is null or lower(c.denominacion) like lower(concat('%', :denominacion, '%')))
            """)
    Page<ClientEntity> buscarConFiltros(@Param("companyId") String companyId,
                                        @Param("tipoDocumento") String tipoDocumento,
                                        @Param("numeroDocumento") String numeroDocumento,
                                        @Param("denominacion") String denominacion,
                                        Pageable pageable);
}

