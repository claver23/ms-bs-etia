package com.etlions.ms_bs_etia.libro_electronico.mapper;

import com.etlions.ms_bs_etia.libro_electronico.persistence.LibroElectronicoEntity;
import com.etlions.openapicodegen.dto.LibroElectronicoData;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.time.LocalDateTime;

@Mapper(componentModel = "spring")
public interface LibroElectronicoMapper {

    @Mapping(target = "tipo", expression = "java(entity.getTipo() != null ? LibroElectronicoData.TipoEnum.fromValue(entity.getTipo()) : null)")
    @Mapping(target = "formato", expression = "java(entity.getFormato() != null ? LibroElectronicoData.FormatoEnum.fromValue(entity.getFormato()) : null)")
    @Mapping(target = "estado", expression = "java(entity.getEstado() != null ? LibroElectronicoData.EstadoEnum.fromValue(entity.getEstado()) : null)")
    @Mapping(target = "fechaGeneracion", expression = "java(mapLocalDateTime(entity.getFechaGeneracion()))")
    LibroElectronicoData toData(LibroElectronicoEntity entity);

    default java.util.Date mapLocalDateTime(LocalDateTime ldt) {
        return ldt != null ? java.sql.Timestamp.valueOf(ldt) : null;
    }
}

