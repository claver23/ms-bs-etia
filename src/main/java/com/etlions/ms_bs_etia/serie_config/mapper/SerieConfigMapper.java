package com.etlions.ms_bs_etia.serie_config.mapper;

import com.etlions.ms_bs_etia.serie_config.persistence.SerieConfigEntity;
import com.etlions.openapicodegen.dto.SerieConfigData;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.time.LocalDateTime;

@Mapper(componentModel = "spring")
public interface SerieConfigMapper {

    @Mapping(target = "tipoDocumento", expression = "java(entity.getTipoDocumento() != null ? SerieConfigData.TipoDocumentoEnum.fromValue(entity.getTipoDocumento()) : null)")
    @Mapping(target = "fechaCreacion", expression = "java(mapLocalDateTime(entity.getFechaCreacion()))")
    SerieConfigData toData(SerieConfigEntity entity);

    default java.util.Date mapLocalDateTime(LocalDateTime ldt) {
        return ldt != null ? java.sql.Timestamp.valueOf(ldt) : null;
    }
}

