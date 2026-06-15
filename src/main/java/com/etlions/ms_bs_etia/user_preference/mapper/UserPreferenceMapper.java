package com.etlions.ms_bs_etia.user_preference.mapper;

import com.etlions.ms_bs_etia.user_preference.persistence.UserPreferenceEntity;
import com.etlions.openapicodegen.dto.UserPreferenceData;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.time.LocalDateTime;

@Mapper(componentModel = "spring")
public interface UserPreferenceMapper {

    @Mapping(target = "fechaActualizacion", expression = "java(mapLocalDateTime(entity.getFechaActualizacion()))")
    UserPreferenceData toData(UserPreferenceEntity entity);

    default java.util.Date mapLocalDateTime(LocalDateTime ldt) {
        return ldt != null ? java.sql.Timestamp.valueOf(ldt) : null;
    }
}

