package com.etlions.ms_bs_etia.user_preference.service;

import com.etlions.ms_bs_etia.user_preference.persistence.UserPreferenceEntity;
import java.util.List;

public interface UserPreferenceService {
    List<UserPreferenceEntity> listarPorCompany(String companyId);
    UserPreferenceEntity guardar(String companyId, String clave, String valor);
}

