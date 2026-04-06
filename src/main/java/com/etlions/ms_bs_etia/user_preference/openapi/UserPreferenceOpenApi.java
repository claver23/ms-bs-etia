package com.etlions.ms_bs_etia.user_preference.openapi;

import com.etlions.ms_bs_etia.user_preference.persistence.UserPreferenceEntity;
import com.etlions.ms_bs_etia.user_preference.service.UserPreferenceService;
import com.etlions.openapicodegen.api.v1.UserPreferenceApi;
import com.etlions.openapicodegen.dto.UserPreferenceData;
import com.etlions.openapicodegen.dto.UserPreferenceSaveRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import java.sql.Timestamp;
import java.util.List;

@Controller
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class UserPreferenceOpenApi implements UserPreferenceApi {

    private final UserPreferenceService userPreferenceService;

    @Override
    public ResponseEntity<List<UserPreferenceData>> listarPreferencias(String companyId) {
        List<UserPreferenceEntity> entities = userPreferenceService.listarPorCompany(companyId);
        List<UserPreferenceData> data = entities.stream().map(this::toData).toList();
        return ResponseEntity.ok(data);
    }

    @Override
    public ResponseEntity<UserPreferenceData> guardarPreferencia(UserPreferenceSaveRequest request) {
        UserPreferenceEntity saved = userPreferenceService.guardar(
                request.getCompanyId(),
                request.getClave(),
                request.getValor()
        );
        return ResponseEntity.ok(toData(saved));
    }

    private UserPreferenceData toData(UserPreferenceEntity e) {
        UserPreferenceData d = new UserPreferenceData();
        d.setId(e.getId());
        d.setCompanyId(e.getCompanyId());
        d.setClave(e.getClave());
        d.setValor(e.getValor());
        if (e.getFechaActualizacion() != null) {
            d.setFechaActualizacion(Timestamp.valueOf(e.getFechaActualizacion()));
        }
        return d;
    }
}

