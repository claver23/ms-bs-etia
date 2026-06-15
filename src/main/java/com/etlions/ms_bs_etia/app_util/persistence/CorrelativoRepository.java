package com.etlions.ms_bs_etia.app_util.persistence;

import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CorrelativoRepository extends JpaRepository<CorrelativoEntity, String> {

    Optional<CorrelativoEntity> findByCompanyIdAndSerie(String companyId, String serie);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
            select c
            from CorrelativoEntity c
            where c.companyId = :companyId and c.serie = :serie
            """)
    Optional<CorrelativoEntity> findByCompanyIdAndSerieForUpdate(@Param("companyId") String companyId,
                                                                  @Param("serie") String serie);
}
