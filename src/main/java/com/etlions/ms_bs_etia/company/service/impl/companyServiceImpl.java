package com.etlions.ms_bs_etia.company.service.impl;

import com.etlions.ms_bs_etia.app_config.exception.ResourceNotFoundException;
import com.etlions.ms_bs_etia.company.mapper.CompanyMapper;
import com.etlions.ms_bs_etia.company.persistence.CompanyEntity;
import com.etlions.ms_bs_etia.company.persistence.CompanyRepository;
import com.etlions.ms_bs_etia.company.service.CompanyService;
import com.etlions.openapicodegen.dto.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CompanyServiceImpl implements CompanyService {

    private final CompanyRepository companyRepository;
    private final CompanyMapper companyMapper;

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
}

