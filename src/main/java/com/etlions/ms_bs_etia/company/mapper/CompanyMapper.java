package com.etlions.ms_bs_etia.company.mapper;

import com.etlions.ms_bs_etia.company.persistence.CompanyEntity;
import com.etlions.openapicodegen.dto.CompanyData;
import com.etlions.openapicodegen.dto.CompanyRegisterDto;
import com.etlions.openapicodegen.dto.CompanyUpdateDto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface CompanyMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "estado", ignore = true)
    @Mapping(target = "fechaCreacion", ignore = true)
    @Mapping(target = "fechaActualizacion", ignore = true)
    @Mapping(target = "logoPath", ignore = true)
    @Mapping(target = "logoFormat", ignore = true)
    @Mapping(target = "regimen", expression = "java(request.getRegimen() != null ? request.getRegimen().getValue() : null)")
    CompanyEntity toEntity(CompanyRegisterDto request);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "ruc", ignore = true)
    @Mapping(target = "estado", ignore = true)
    @Mapping(target = "fechaCreacion", ignore = true)
    @Mapping(target = "fechaActualizacion", ignore = true)
    @Mapping(target = "certificadoPath", ignore = true)
    @Mapping(target = "logoPath", ignore = true)
    @Mapping(target = "logoFormat", ignore = true)
    @Mapping(target = "razonSocial", ignore = true)
    @Mapping(target = "codigoUbigeo", ignore = true)
    @Mapping(target = "urbanizacion", ignore = true)
    @Mapping(target = "departamento", ignore = true)
    @Mapping(target = "provincia", ignore = true)
    @Mapping(target = "distrito", ignore = true)
    @Mapping(target = "regimen", ignore = true)
    void updateEntity(CompanyUpdateDto request, @MappingTarget CompanyEntity entity);

    @Mapping(target = "regimen", expression = "java(entity.getRegimen() != null ? CompanyData.RegimenEnum.fromValue(entity.getRegimen()) : null)")
    @Mapping(target = "estado", expression = "java(entity.getEstado() != null ? CompanyData.EstadoEnum.fromValue(entity.getEstado()) : null)")
    @Mapping(target = "tieneUsuarioSol", expression = "java(entity.getUsuarioSol() != null && !entity.getUsuarioSol().isBlank())")
    @Mapping(target = "tieneClaveSol", expression = "java(entity.getClaveSol() != null && !entity.getClaveSol().isBlank())")
    @Mapping(target = "tieneCertificado", expression = "java(entity.getCertificadoPath() != null && !entity.getCertificadoPath().isBlank())")
    @Mapping(target = "tieneLogo", expression = "java(entity.getLogoPath() != null && !entity.getLogoPath().isBlank())")
    CompanyData toData(CompanyEntity entity);
}

