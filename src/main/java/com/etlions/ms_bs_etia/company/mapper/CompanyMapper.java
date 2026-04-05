package com.etlions.ms_bs_etia.company.mapper;

import com.etlions.ms_bs_etia.company.persistence.CompanyEntity;
import com.etlions.openapicodegen.dto.CompanyData;
import com.etlions.openapicodegen.dto.CompanyRegisterRequest;
import com.etlions.openapicodegen.dto.CompanyUpdateRequest;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface CompanyMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "estado", ignore = true)
    @Mapping(target = "fechaCreacion", ignore = true)
    @Mapping(target = "fechaActualizacion", ignore = true)
    @Mapping(target = "regimen", expression = "java(request.getRegimen() != null ? request.getRegimen().getValue() : null)")
    CompanyEntity toEntity(CompanyRegisterRequest request);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "ruc", ignore = true)
    @Mapping(target = "estado", ignore = true)
    @Mapping(target = "fechaCreacion", ignore = true)
    @Mapping(target = "fechaActualizacion", ignore = true)
    @Mapping(target = "usuarioSol", ignore = true)
    @Mapping(target = "claveSol", ignore = true)
    @Mapping(target = "certificadoPath", ignore = true)
    @Mapping(target = "certificadoPassword", ignore = true)
    void updateEntity(CompanyUpdateRequest request, @MappingTarget CompanyEntity entity);

    @Mapping(target = "regimen", expression = "java(entity.getRegimen() != null ? CompanyData.RegimenEnum.fromValue(entity.getRegimen()) : null)")
    @Mapping(target = "estado", expression = "java(entity.getEstado() != null ? CompanyData.EstadoEnum.fromValue(entity.getEstado()) : null)")
    CompanyData toData(CompanyEntity entity);
}

