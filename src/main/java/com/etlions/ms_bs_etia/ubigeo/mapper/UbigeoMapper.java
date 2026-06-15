package com.etlions.ms_bs_etia.ubigeo.mapper;

import com.etlions.ms_bs_etia.ubigeo.persistence.UbigeoEntity;
import com.etlions.openapicodegen.dto.UbigeoData;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface UbigeoMapper {
    UbigeoData toData(UbigeoEntity entity);
}

