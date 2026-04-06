package com.etlions.ms_bs_etia.employee.openapi;

import com.etlions.ms_bs_etia.employee.persistence.EmployeeEntity;
import com.etlions.ms_bs_etia.employee.service.EmployeeService;
import com.etlions.openapicodegen.api.v1.EmployeeApi;
import com.etlions.openapicodegen.dto.*;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.Date;
import java.util.List;

@Controller
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class EmployeeOpenApi implements EmployeeApi {

    private final EmployeeService employeeService;

    @Override
    public ResponseEntity<ActualizarEmpleado200Response> registrarEmpleado(EmployeeCreateRequest request) {
        EmployeeEntity entity = EmployeeEntity.builder()
                .companyId(request.getCompanyId())
                .nombre(request.getNombre())
                .cargo(request.getCargo())
                .dni(request.getDni())
                .codigo(request.getCodigo())
                .build();
        EmployeeEntity saved = employeeService.registrarEmpleado(entity);
        return ResponseEntity.ok(new ActualizarEmpleado200Response()
                .isSuccess(true).status("OK")
                .message("Empleado registrado exitosamente")
                .timestamp(new Date()).data(toData(saved)));
    }

    @Override
    public ResponseEntity<ActualizarEmpleado200Response> actualizarEmpleado(String employeeId,
                                                                             EmployeeUpdateRequest request) {
        EmployeeEntity updated = EmployeeEntity.builder()
                .nombre(request.getNombre())
                .cargo(request.getCargo())
                .dni(request.getDni())
                .codigo(request.getCodigo())
                .build();
        EmployeeEntity saved = employeeService.actualizarEmpleado(employeeId, updated);
        return ResponseEntity.ok(new ActualizarEmpleado200Response()
                .isSuccess(true).status("OK")
                .message("Empleado actualizado exitosamente")
                .timestamp(new Date()).data(toData(saved)));
    }

    @Override
    public ResponseEntity<SuccessResponse> eliminarEmpleado(String employeeId, Boolean softDelete) {
        employeeService.eliminarEmpleado(employeeId, softDelete != null ? softDelete : true);
        return ResponseEntity.ok(new SuccessResponse()
                .isSuccess(true).status("OK")
                .message("Empleado eliminado exitosamente")
                .timestamp(new Date()));
    }

    @Override
    public ResponseEntity<ListarEmpleados200Response> listarEmpleados(String companyId, String estado) {
        List<EmployeeEntity> employees;
        if ("ACTIVO".equalsIgnoreCase(estado)) {
            employees = employeeService.listarEmpleadosActivos(companyId);
        } else {
            employees = employeeService.listarEmpleados(companyId);
        }
        List<EmployeeData> dataList = employees.stream().map(this::toData).toList();
        return ResponseEntity.ok(new ListarEmpleados200Response()
                .isSuccess(true).status("OK")
                .message("Operación exitosa")
                .timestamp(new Date()).data(dataList));
    }

    @Override
    public ResponseEntity<ActualizarEmpleado200Response> obtenerEmpleado(String employeeId) {
        EmployeeEntity entity = employeeService.obtenerEmpleado(employeeId);
        return ResponseEntity.ok(new ActualizarEmpleado200Response()
                .isSuccess(true).status("OK")
                .message("Operación exitosa")
                .timestamp(new Date()).data(toData(entity)));
    }

    private EmployeeData toData(EmployeeEntity e) {
        EmployeeData d = new EmployeeData();
        d.setId(e.getId());
        d.setCompanyId(e.getCompanyId());
        d.setNombre(e.getNombre());
        d.setCargo(e.getCargo());
        d.setDni(e.getDni());
        d.setCodigo(e.getCodigo());
        if (e.getEstado() != null) {
            d.setEstado(EmployeeData.EstadoEnum.fromValue(e.getEstado()));
        }
        if (e.getFechaCreacion() != null) {
            d.setFechaCreacion(java.sql.Timestamp.valueOf(e.getFechaCreacion()));
        }
        return d;
    }
}

