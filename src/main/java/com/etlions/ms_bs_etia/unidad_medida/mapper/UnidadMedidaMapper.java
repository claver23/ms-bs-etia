package com.etlions.ms_bs_etia.unidad_medida.mapper;

import com.etlions.ms_bs_etia.unidad_medida.persistence.UnidadMedidaEntity;
import com.etlions.openapicodegen.dto.UnidadMedidaData;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface UnidadMedidaMapper {
    UnidadMedidaData toData(UnidadMedidaEntity entity);
}

