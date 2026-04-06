package com.etlions.ms_bs_etia.employee.mapper;

import com.etlions.ms_bs_etia.employee.persistence.EmployeeEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.Map;

@Mapper(componentModel = "spring")
public interface EmployeeMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "estado", ignore = true)
    @Mapping(target = "fechaCreacion", ignore = true)
    EmployeeEntity toEntity(String companyId,
                            String nombre,
                            String cargo,
                            String dni,
                            String codigo);

    default Map<String, Object> toMap(EmployeeEntity entity) {
        return Map.of(
                "id", entity.getId(),
                "companyId", entity.getCompanyId(),
                "nombre", entity.getNombre() != null ? entity.getNombre() : "",
                "cargo", entity.getCargo() != null ? entity.getCargo() : "",
                "dni", entity.getDni() != null ? entity.getDni() : "",
                "codigo", entity.getCodigo() != null ? entity.getCodigo() : "",
                "estado", entity.getEstado() != null ? entity.getEstado() : "ACTIVO",
                "fechaCreacion", entity.getFechaCreacion() != null ? entity.getFechaCreacion().toString() : ""
        );
    }
}

