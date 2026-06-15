package com.etlions.ms_bs_etia.employee.openapi;

import com.etlions.ms_bs_etia.employee.service.EmployeeService;
import com.etlions.openapicodegen.api.v1.EmployeeApi;
import com.etlions.openapicodegen.dto.*;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class EmployeeOpenApi implements EmployeeApi {

    private final EmployeeService employeeService;

    @Override
    public ResponseEntity<EmployeeResponseDto> registrarEmpleado(EmployeeCreateDto request) {
        return ResponseEntity.ok(employeeService.registrarEmpleado(request));
    }

    @Override
    public ResponseEntity<EmployeeResponseDto> actualizarEmpleado(String employeeId, EmployeeUpdateDto request) {
        return ResponseEntity.ok(employeeService.actualizarEmpleado(employeeId, request));
    }

    @Override
    public ResponseEntity<SuccessResponse> eliminarEmpleado(String employeeId, Boolean softDelete) {
        return ResponseEntity.ok(employeeService.eliminarEmpleado(employeeId, softDelete != null ? softDelete : true));
    }

    @Override
    public ResponseEntity<EmployeeListResponseDto> listarEmpleados(String companyId, String estado) {
        return ResponseEntity.ok(employeeService.listarEmpleados(companyId, estado));
    }

    @Override
    public ResponseEntity<EmployeeResponseDto> obtenerEmpleado(String employeeId) {
        return ResponseEntity.ok(employeeService.obtenerEmpleado(employeeId));
    }
}
