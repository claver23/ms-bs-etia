package com.etlions.ms_bs_etia.tipo_cambio.mapper;

import com.etlions.ms_bs_etia.tipo_cambio.persistence.TipoCambioEntity;
import com.etlions.openapicodegen.dto.TipoCambioData;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Mapper(componentModel = "spring")
public interface TipoCambioMapper {

    @Mapping(target = "fecha", expression = "java(mapLocalDate(entity.getFecha()))")
    @Mapping(target = "tasaCompra", expression = "java(mapBigDecimalToDouble(entity.getTasaCompra()))")
    @Mapping(target = "tasaVenta", expression = "java(mapBigDecimalToDouble(entity.getTasaVenta()))")
    @Mapping(target = "fechaCreacion", expression = "java(mapLocalDateTime(entity.getFechaCreacion()))")
    TipoCambioData toData(TipoCambioEntity entity);

    default java.util.Date mapLocalDate(LocalDate ld) {
        return ld != null ? java.sql.Date.valueOf(ld) : null;
    }

    default Double mapBigDecimalToDouble(BigDecimal bd) {
        return bd != null ? bd.doubleValue() : null;
    }

    default java.util.Date mapLocalDateTime(LocalDateTime ldt) {
        return ldt != null ? java.sql.Timestamp.valueOf(ldt) : null;
    }
}

