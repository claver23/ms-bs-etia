package com.etlions.ms_bs_etia.company.service.impl;

import com.etlions.ms_bs_etia.app_config.config.SunatProperties;
import com.etlions.ms_bs_etia.app_config.exception.ResourceNotFoundException;
import com.etlions.ms_bs_etia.company.mapper.CompanyMapper;
import com.etlions.ms_bs_etia.company.persistence.CompanyEntity;
import com.etlions.ms_bs_etia.company.persistence.CompanyRepository;
import com.etlions.ms_bs_etia.company.service.CertificadoStorageService;
import com.etlions.ms_bs_etia.company.service.CompanyService;
import com.etlions.ms_bs_etia.company.service.LogoStorageService;
import com.etlions.openapicodegen.dto.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class CompanyServiceImpl implements CompanyService {

    private final CompanyRepository companyRepository;
    private final CompanyMapper companyMapper;
    private final CertificadoStorageService certificadoStorageService;
    private final LogoStorageService logoStorageService;
    private final SunatProperties sunatProperties;

    @Override
    public CompanyResponseDto registrarEmpresa(CompanyRegisterDto request) {
        CompanyEntity entity = companyMapper.toEntity(request);
        entity.setEstado("ACTIVO");
        entity.setFechaCreacion(LocalDateTime.now());
        entity.setFechaActualizacion(LocalDateTime.now());
        CompanyEntity saved = companyRepository.save(entity);
        CompanyData data = companyMapper.toData(saved);
        return new CompanyResponseDto()
                .isSuccess(true)
                .status("OK")
                .message("Empresa registrada exitosamente")
                .timestamp(new Date())
                .data(data);
    }

    @Override
    public CompanyResponseDto actualizarEmpresa(String companyId, CompanyUpdateDto request) {
        CompanyEntity entity = companyRepository.findById(companyId)
                .orElseThrow(() -> new ResourceNotFoundException("Empresa no encontrada: " + companyId));
        companyMapper.updateEntity(request, entity);
        entity.setFechaActualizacion(LocalDateTime.now());
        CompanyEntity saved = companyRepository.save(entity);
        CompanyData data = companyMapper.toData(saved);
        return new CompanyResponseDto()
                .isSuccess(true)
                .status("OK")
                .message("Empresa actualizada exitosamente")
                .timestamp(new Date())
                .data(data);
    }

    @Override
    public SuccessResponse eliminarEmpresa(String companyId, Boolean softDelete) {
        if (Boolean.TRUE.equals(softDelete)) {
            CompanyEntity entity = companyRepository.findById(companyId)
                    .orElseThrow(() -> new ResourceNotFoundException("Empresa no encontrada: " + companyId));
            entity.setEstado("INACTIVO");
            entity.setFechaActualizacion(LocalDateTime.now());
            companyRepository.save(entity);
        } else {
            companyRepository.deleteById(companyId);
        }
        return new SuccessResponse()
                .isSuccess(true)
                .status("OK")
                .message("Empresa eliminada exitosamente")
                .timestamp(new Date());
    }

    @Override
    public CompanyListResponseDto listarEmpresas(Integer page, Integer size, String ruc, String estado) {
        List<CompanyEntity> entities = companyRepository.findAll();
        List<CompanyData> dataList = entities.stream()
                .map(companyMapper::toData)
                .toList();
        CompanyListResponseDtoAllOfData responseData = new CompanyListResponseDtoAllOfData();
        responseData.setContent(dataList);
        responseData.setPagination(new PaginationMeta()
                .currentPage(page)
                .pageSize(size)
                .totalElements((long) dataList.size())
                .totalPages(1));
        CompanyListResponseDto response = new CompanyListResponseDto();
        response.setIsSuccess(true);
        response.setStatus("OK");
        response.setMessage("Operación exitosa");
        response.setTimestamp(new Date());
        response.setData(responseData);
        return response;
    }

    @Override
    public CompanyResponseDto subirCertificado(String companyId, MultipartFile archivo) {
        CompanyEntity entity = companyRepository.findById(companyId)
                .orElseThrow(() -> new ResourceNotFoundException("Empresa no encontrada: " + companyId));

        String relativePath = certificadoStorageService.guardarCertificado(entity.getRuc(), archivo);
        entity.setCertificadoPath(relativePath);
        entity.setFechaActualizacion(LocalDateTime.now());
        CompanyEntity saved = companyRepository.save(entity);

        log.info("📜 Certificado subido para empresa {} (RUC: {}): {}", companyId, entity.getRuc(), relativePath);

        CompanyData data = companyMapper.toData(saved);
        return new CompanyResponseDto()
                .isSuccess(true)
                .status("OK")
                .message("Certificado digital subido exitosamente")
                .timestamp(new Date())
                .data(data);
    }

    @Override
    public ConexionSunatResultDto probarConexionSunat(String companyId) {
        CompanyEntity entity = companyRepository.findById(companyId)
                .orElseThrow(() -> new ResourceNotFoundException("Empresa no encontrada: " + companyId));

        boolean tieneUsuarioSol = entity.getUsuarioSol() != null && !entity.getUsuarioSol().isBlank();
        boolean tieneClaveSol = entity.getClaveSol() != null && !entity.getClaveSol().isBlank();
        boolean tieneCertificado = entity.getCertificadoPath() != null && !entity.getCertificadoPath().isBlank();

        String modo = sunatProperties.getModo();
        boolean exitoso;
        String mensaje;

        if (!tieneUsuarioSol || !tieneClaveSol) {
            exitoso = false;
            mensaje = "Credenciales SOL incompletas — configura usuario y clave SOL antes de probar la conexión.";
        } else {
            // TODO: FASE-3 — Reemplazar mock con llamada real a SUNAT web service beta
            // En modo beta, simular conexión exitosa si las credenciales están configuradas
            exitoso = true;
            if (sunatProperties.isBeta()) {
                mensaje = "✅ Conexión simulada exitosa (modo beta). Las credenciales están configuradas correctamente. "
                        + "La conexión real con SUNAT se habilitará en la Fase 3.";
            } else {
                mensaje = "✅ Conexión simulada exitosa (modo producción). "
                        + "La validación real contra el web service de SUNAT se habilitará en la Fase 3.";
            }
            if (!tieneCertificado) {
                mensaje += " ⚠️ Nota: El certificado digital (.p12/.pfx) aún no está configurado — será necesario para firmar documentos.";
            }
        }

        log.info("🔗 Prueba conexión SUNAT para empresa {} — exitoso: {}, modo: {}", companyId, exitoso, modo);

        return new ConexionSunatResultDto()
                .exitoso(exitoso)
                .mensaje(mensaje)
                .modo(modo)
                .tieneUsuarioSol(tieneUsuarioSol)
                .tieneClaveSol(tieneClaveSol)
                .tieneCertificado(tieneCertificado)
                .timestamp(new Date());
    }

    @Override
    public CompanyResponseDto subirLogo(String companyId, MultipartFile archivo) {
        CompanyEntity entity = companyRepository.findById(companyId)
                .orElseThrow(() -> new ResourceNotFoundException("Empresa no encontrada: " + companyId));

        String relativePath = logoStorageService.guardarLogo(entity.getRuc(), archivo);
        entity.setLogoPath(relativePath);
        entity.setLogoFormat(logoStorageService.getLogoFormat(entity.getRuc()));
        entity.setFechaActualizacion(LocalDateTime.now());
        CompanyEntity saved = companyRepository.save(entity);

        log.info("🖼️ Logo subido para empresa {} (RUC: {}): {}", companyId, entity.getRuc(), relativePath);

        CompanyData data = companyMapper.toData(saved);
        return new CompanyResponseDto()
                .isSuccess(true)
                .status("OK")
                .message("Logo subido exitosamente")
                .timestamp(new Date())
                .data(data);
    }

    @Override
    public Optional<byte[]> obtenerLogo(String companyId) {
        CompanyEntity entity = companyRepository.findById(companyId)
                .orElseThrow(() -> new ResourceNotFoundException("Empresa no encontrada: " + companyId));
        return logoStorageService.leerLogo(entity.getRuc());
    }

    @Override
    public String getLogoContentType(String companyId) {
        CompanyEntity entity = companyRepository.findById(companyId)
                .orElseThrow(() -> new ResourceNotFoundException("Empresa no encontrada: " + companyId));
        return logoStorageService.getContentType(entity.getRuc());
    }
}

