package com.etlions.ms_bs_etia.app_util.openapi;

import com.etlions.ms_bs_etia.app_util.service.OnboardingService;
import com.etlions.openapicodegen.api.v1.OnboardingApi;
import com.etlions.openapicodegen.dto.OnboardingRequestDto;
import com.etlions.openapicodegen.dto.OnboardingResponseDto;
import com.etlions.openapicodegen.dto.OnboardingResponseDtoAllOfData;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.Date;
import java.util.List;

@Controller
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class OnboardingOpenApi implements OnboardingApi {

    private final OnboardingService onboardingService;

    @Override
    public ResponseEntity<OnboardingResponseDto> registrarOnboarding(OnboardingRequestDto request) {
        OnboardingService.OnboardingResult result = onboardingService.registrarEmpresa(
                request.getRuc(),
                request.getRazonSocial(),
                request.getDireccion(),
                request.getDepartamento(),
                request.getProvincia(),
                request.getDistrito(),
                request.getCodigoUbigeo()
        );

        OnboardingResponseDtoAllOfData data = new OnboardingResponseDtoAllOfData()
                .companyId(result.companyId())
                .sedeId(result.sedeId())
                .seriesCreadas(List.of("F001", "B001", "FC01", "FD01"));

        OnboardingResponseDto response = new OnboardingResponseDto();
        response.setIsSuccess(true);
        response.setStatus("OK");
        response.setMessage(result.message());
        response.setTimestamp(new Date());
        response.setData(data);

        return ResponseEntity.ok(response);
    }
}


