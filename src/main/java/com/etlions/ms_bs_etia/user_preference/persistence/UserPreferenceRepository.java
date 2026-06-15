package com.etlions.ms_bs_etia.user_preference.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface UserPreferenceRepository extends JpaRepository<UserPreferenceEntity, String> {
    List<UserPreferenceEntity> findByCompanyId(String companyId);
    Optional<UserPreferenceEntity> findByCompanyIdAndClave(String companyId, String clave);
}

