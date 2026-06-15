package com.etlions.ms_bs_etia.app_util.openapi;

import com.etlions.ms_bs_etia.app_util.service.OnboardingService;
import com.etlions.ms_bs_etia.auth.persistence.UserRepository;
import com.etlions.ms_bs_etia.auth.service.JwtService;
import com.etlions.openapicodegen.api.v1.OnboardingApi;
import com.etlions.openapicodegen.dto.OnboardingRequestDto;
import com.etlions.openapicodegen.dto.OnboardingResponseDto;
import com.etlions.openapicodegen.dto.OnboardingResponseDtoAllOfData;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import jakarta.servlet.http.HttpServletRequest;
import java.util.Date;
import java.util.List;

@Controller
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class OnboardingOpenApi implements OnboardingApi {

    private final OnboardingService onboardingService;
    private final JwtService jwtService;
    private final UserRepository userRepository;
    private final HttpServletRequest httpRequest;

    @Override
    public ResponseEntity<OnboardingResponseDto> registrarOnboarding(OnboardingRequestDto request) {
        // Extract userId from JWT if present
        String userId = null;
        String authHeader = httpRequest.getHeader("Authorization");
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);
            if (jwtService.validateToken(token)) {
                userId = jwtService.extractUserId(token);
            }
        }

        OnboardingService.OnboardingResult result = onboardingService.registrarEmpresa(
                userId,
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

        // Generate a fresh token with the new companyId
        String newToken = null;
        if (userId != null) {
            var userOpt = userRepository.findById(userId);
            if (userOpt.isPresent()) {
                newToken = jwtService.generateToken(userOpt.get());
            }
        }

        OnboardingResponseDto response = new OnboardingResponseDto();
        response.setIsSuccess(true);
        response.setStatus("OK");
        response.setMessage(result.message());
        response.setTimestamp(new Date());
        response.setData(data);

        // Add new token in a custom header so frontend can update it
        var builder = ResponseEntity.ok();
        if (newToken != null) {
            builder.header("X-New-Token", newToken);
        }
        return builder.body(response);
    }
}


