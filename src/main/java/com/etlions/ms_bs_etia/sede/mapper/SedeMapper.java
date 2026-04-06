package com.etlions.ms_bs_etia.sede.mapper;

import com.etlions.ms_bs_etia.sede.persistence.SedeEntity;
import com.etlions.openapicodegen.dto.SedeData;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.time.LocalDateTime;

@Mapper(componentModel = "spring")
public interface SedeMapper {

    @Mapping(target = "estado", expression = "java(entity.getEstado() != null ? SedeData.EstadoEnum.fromValue(entity.getEstado()) : SedeData.EstadoEnum.ACTIVO)")
    @Mapping(target = "fechaCreacion", expression = "java(mapLocalDateTime(entity.getFechaCreacion()))")
    @Mapping(target = "series", ignore = true)
    SedeData toData(SedeEntity entity);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "codigo", ignore = true)
    @Mapping(target = "estado", ignore = true)
    @Mapping(target = "esDefault", ignore = true)
    @Mapping(target = "fechaCreacion", ignore = true)
    SedeEntity toEntity(com.etlions.openapicodegen.dto.SedeCreateDto request);

    default java.util.Date mapLocalDateTime(LocalDateTime ldt) {
        return ldt != null ? java.sql.Timestamp.valueOf(ldt) : null;
    }
}

