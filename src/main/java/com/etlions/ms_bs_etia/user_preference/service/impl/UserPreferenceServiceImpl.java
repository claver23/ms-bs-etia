package com.etlions.ms_bs_etia.user_preference.service.impl;

import com.etlions.ms_bs_etia.user_preference.persistence.UserPreferenceEntity;
import com.etlions.ms_bs_etia.user_preference.persistence.UserPreferenceRepository;
import com.etlions.ms_bs_etia.user_preference.service.UserPreferenceService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class UserPreferenceServiceImpl implements UserPreferenceService {

    private final UserPreferenceRepository userPreferenceRepository;

    @Override
    public List<UserPreferenceEntity> listarPorCompany(String companyId) {
        return userPreferenceRepository.findByCompanyId(companyId);
    }

    @Override
    public UserPreferenceEntity guardar(String companyId, String clave, String valor) {
        UserPreferenceEntity entity = userPreferenceRepository
                .findByCompanyIdAndClave(companyId, clave)
                .orElse(UserPreferenceEntity.builder()
                        .companyId(companyId)
                        .clave(clave)
                        .build());
        entity.setValor(valor);
        entity.setFechaActualizacion(LocalDateTime.now());
        return userPreferenceRepository.save(entity);
    }
}

