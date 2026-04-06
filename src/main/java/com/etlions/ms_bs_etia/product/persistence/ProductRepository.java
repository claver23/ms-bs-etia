package com.etlions.ms_bs_etia.product.persistence;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProductRepository extends JpaRepository<ProductEntity, String> {

    List<ProductEntity> findByCompanyId(String companyId);

    List<ProductEntity> findByCompanyIdAndEstado(String companyId, String estado);

    List<ProductEntity> findByCompanyIdAndDescripcionContainingIgnoreCase(String companyId, String term);

    // Paginated queries
    Page<ProductEntity> findByCompanyId(String companyId, Pageable pageable);

    Page<ProductEntity> findByCompanyIdAndEstado(String companyId, String estado, Pageable pageable);

    Page<ProductEntity> findByCompanyIdAndDescripcionContainingIgnoreCaseOrCompanyIdAndCodigoProductoContainingIgnoreCase(
            String companyId1, String term1, String companyId2, String term2, Pageable pageable);
}
