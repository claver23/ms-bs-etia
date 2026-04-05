package com.etlions.ms_bs_etia.client.mapper;

import com.etlions.ms_bs_etia.client.persistence.ClientEntity;
import com.etlions.openapicodegen.dto.ClientData;
import com.etlions.openapicodegen.dto.ClientRegisterRequest;
import com.etlions.openapicodegen.dto.ClientUpdateRequest;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface ClientMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "fechaCreacion", ignore = true)
    @Mapping(target = "tipoDocumento", expression = "java(request.getTipoDocumento() != null ? request.getTipoDocumento().getValue() : null)")
    ClientEntity toEntity(ClientRegisterRequest request);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "companyId", ignore = true)
    @Mapping(target = "fechaCreacion", ignore = true)
    @Mapping(target = "tipoDocumento", ignore = true)
    @Mapping(target = "numeroDocumento", ignore = true)
    void updateEntity(ClientUpdateRequest request, @MappingTarget ClientEntity entity);

    @Mapping(target = "tipoDocumento", expression = "java(entity.getTipoDocumento() != null ? ClientData.TipoDocumentoEnum.fromValue(entity.getTipoDocumento()) : null)")
    ClientData toData(ClientEntity entity);
}


