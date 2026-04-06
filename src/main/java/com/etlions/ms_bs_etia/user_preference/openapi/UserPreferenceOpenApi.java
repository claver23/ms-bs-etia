package com.etlions.ms_bs_etia.user_preference.openapi;

import com.etlions.ms_bs_etia.user_preference.mapper.UserPreferenceMapper;
import com.etlions.ms_bs_etia.user_preference.persistence.UserPreferenceEntity;
import com.etlions.ms_bs_etia.user_preference.service.UserPreferenceService;
import com.etlions.openapicodegen.api.v1.UserPreferenceApi;
import com.etlions.openapicodegen.dto.UserPreferenceData;
import com.etlions.openapicodegen.dto.UserPreferenceSaveDto;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

@Controller
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class UserPreferenceOpenApi implements UserPreferenceApi {

    private final UserPreferenceService userPreferenceService;
    private final UserPreferenceMapper userPreferenceMapper;

    @Override
    public ResponseEntity<List<UserPreferenceData>> listarPreferencias(String companyId) {
        List<UserPreferenceEntity> entities = userPreferenceService.listarPorCompany(companyId);
        List<UserPreferenceData> data = entities.stream().map(userPreferenceMapper::toData).toList();
        return ResponseEntity.ok(data);
    }

    @Override
    public ResponseEntity<UserPreferenceData> guardarPreferencia(UserPreferenceSaveDto request) {
        UserPreferenceEntity saved = userPreferenceService.guardar(
                request.getCompanyId(),
                request.getClave(),
                request.getValor()
        );
        return ResponseEntity.ok(userPreferenceMapper.toData(saved));
    }
}
