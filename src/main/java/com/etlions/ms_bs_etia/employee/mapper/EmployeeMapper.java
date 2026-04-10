package com.etlions.ms_bs_etia.employee.mapper;

import com.etlions.ms_bs_etia.employee.persistence.EmployeeEntity;
import com.etlions.openapicodegen.dto.EmployeeCreateDto;
import com.etlions.openapicodegen.dto.EmployeeData;
import com.etlions.openapicodegen.dto.EmployeeUpdateDto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

import java.time.LocalDateTime;

@Mapper(componentModel = "spring")
public interface EmployeeMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "estado", ignore = true)
    @Mapping(target = "fechaCreacion", ignore = true)
    @Mapping(target = "esDemo", constant = "false")
    EmployeeEntity toEntity(EmployeeCreateDto request);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "companyId", ignore = true)
    @Mapping(target = "estado", ignore = true)
    @Mapping(target = "fechaCreacion", ignore = true)
    @Mapping(target = "esDemo", ignore = true)
    void updateEntity(EmployeeUpdateDto request, @MappingTarget EmployeeEntity entity);

    @Mapping(target = "estado", expression = "java(entity.getEstado() != null ? EmployeeData.EstadoEnum.fromValue(entity.getEstado()) : EmployeeData.EstadoEnum.ACTIVO)")
    @Mapping(target = "fechaCreacion", expression = "java(mapLocalDateTime(entity.getFechaCreacion()))")
    EmployeeData toData(EmployeeEntity entity);

    default java.util.Date mapLocalDateTime(LocalDateTime ldt) {
        return ldt != null ? java.sql.Timestamp.valueOf(ldt) : null;
    }
}
