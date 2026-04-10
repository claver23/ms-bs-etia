package com.etlions.ms_bs_etia.app_util.service;

import com.etlions.ms_bs_etia.app_util.persistence.CorrelativoEntity;
import com.etlions.ms_bs_etia.app_util.persistence.CorrelativoRepository;
import com.etlions.ms_bs_etia.auth.persistence.UserEntity;
import com.etlions.ms_bs_etia.auth.persistence.UserRepository;
import com.etlions.ms_bs_etia.company.persistence.CompanyEntity;
import com.etlions.ms_bs_etia.company.persistence.CompanyRepository;
import com.etlions.ms_bs_etia.sede.persistence.SedeEntity;
import com.etlions.ms_bs_etia.sede.persistence.SedeRepository;
import com.etlions.ms_bs_etia.serie_config.persistence.SerieConfigEntity;
import com.etlions.ms_bs_etia.serie_config.persistence.SerieConfigRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Servicio de onboarding: registra empresa, sede principal, series y correlativos iniciales.
 * Flujo: register → crear company → sede → 4 series → 4 correlativos
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class OnboardingService {

    private final CompanyRepository companyRepository;
    private final SedeRepository sedeRepository;
    private final SerieConfigRepository serieConfigRepository;
    private final CorrelativoRepository correlativoRepository;
    private final UserRepository userRepository;

    /**
     * Resultado del onboarding.
     */
    public record OnboardingResult(String companyId, String sedeId, String message) {}

    /**
     * Registra una nueva empresa con su configuración inicial (sin userId — legacy/OpenAPI).
     */
    @Transactional
    public OnboardingResult registrarEmpresa(String ruc, String razonSocial, String direccion,
                                              String departamento, String provincia,
                                              String distrito, String codigoUbigeo) {
        return registrarEmpresa(null, ruc, razonSocial, direccion, departamento, provincia, distrito, codigoUbigeo);
    }

    /**
     * Registra una nueva empresa y vincula al usuario autenticado.
     */
    @Transactional
    public OnboardingResult registrarEmpresa(String userId, String ruc, String razonSocial, String direccion,
                                              String departamento, String provincia,
                                              String distrito, String codigoUbigeo) {
        // ── Validaciones ──
        if (ruc == null || ruc.isBlank()) {
            throw new IllegalArgumentException("El RUC es obligatorio");
        }
        if (!ruc.matches("\\d{11}")) {
            throw new IllegalArgumentException("El RUC debe tener exactamente 11 dígitos numéricos");
        }
        if (!ruc.startsWith("10") && !ruc.startsWith("20")) {
            throw new IllegalArgumentException("El RUC debe iniciar con 10 (persona natural) o 20 (persona jurídica)");
        }
        if (razonSocial == null || razonSocial.isBlank()) {
            throw new IllegalArgumentException("La razón social es obligatoria");
        }
        if (companyRepository.existsByRuc(ruc)) {
            throw new IllegalArgumentException("Ya existe una empresa registrada con RUC: " + ruc);
        }

        log.info("🚀 Onboarding: Registrando empresa RUC={}", ruc);

        // 1. Crear CompanyEntity
        CompanyEntity company = CompanyEntity.builder()
                .ruc(ruc)
                .razonSocial(razonSocial)
                .direccion(direccion)
                .departamento(departamento)
                .provincia(provincia)
                .distrito(distrito)
                .codigoUbigeo(codigoUbigeo)
                .estado("ACTIVO")
                .usuarioSol("MODDATOS")
                .claveSol("MODDATOS")
                .emisionEnEnvio(true)
                .esDemo(false)
                .fechaCreacion(LocalDateTime.now())
                .fechaActualizacion(LocalDateTime.now())
                .build();
        company = companyRepository.save(company);
        log.info("✅ Company creada: id={}", company.getId());

        // 2. Crear SedeEntity "Sede Principal"
        SedeEntity sede = SedeEntity.builder()
                .companyId(company.getId())
                .codigo("001")
                .nombre("Sede Principal")
                .direccion(direccion)
                .codigoUbigeo(codigoUbigeo)
                .departamento(departamento)
                .provincia(provincia)
                .distrito(distrito)
                .esDefault(true)
                .estado("ACTIVO")
                .fechaCreacion(LocalDateTime.now())
                .build();
        sede = sedeRepository.save(sede);
        log.info("✅ Sede creada: id={}", sede.getId());

        // 3. Crear 4 SerieConfigEntity
        List<SerieData> series = List.of(
                new SerieData("F001", "FACTURA", "Serie Facturas"),
                new SerieData("B001", "BOLETA", "Serie Boletas"),
                new SerieData("FC01", "NOTA_CREDITO", "Serie Notas de Crédito"),
                new SerieData("FD01", "NOTA_DEBITO", "Serie Notas de Débito")
        );

        for (SerieData s : series) {
            SerieConfigEntity serieConfig = SerieConfigEntity.builder()
                    .companyId(company.getId())
                    .sedeId(sede.getId())
                    .tipoDocumento(s.tipo)
                    .serie(s.serie)
                    .descripcion(s.descripcion)
                    .esDefault(true)
                    .activa(true)
                    .fechaCreacion(LocalDateTime.now())
                    .build();
            serieConfigRepository.save(serieConfig);

            // 4. Crear CorrelativoEntity (ultimoNumero=0)
            CorrelativoEntity correlativo = CorrelativoEntity.builder()
                    .companyId(company.getId())
                    .serie(s.serie)
                    .ultimoNumero(0)
                    .build();
            correlativoRepository.save(correlativo);
        }
        log.info("✅ 4 series + 4 correlativos creados para company={}", company.getId());

        // 5. Vincular usuario a empresa (si se proporcionó userId)
        if (userId != null && !userId.isBlank()) {
            final String companyId = company.getId();
            userRepository.findById(userId).ifPresent(user -> {
                user.setCompanyId(companyId);
                userRepository.save(user);
                log.info("✅ Usuario {} vinculado a company={}", userId, companyId);
            });
        }

        return new OnboardingResult(company.getId(), sede.getId(),
                "Empresa registrada exitosamente. Series: F001, B001, FC01, FD01");
    }

    private record SerieData(String serie, String tipo, String descripcion) {}
}

