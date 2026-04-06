package com.etlions.ms_bs_etia.employee.service;

import com.etlions.openapicodegen.dto.*;

public interface EmployeeService {

    EmployeeResponseDto registrarEmpleado(EmployeeCreateDto request);

    EmployeeResponseDto actualizarEmpleado(String employeeId, EmployeeUpdateDto request);

    SuccessResponse eliminarEmpleado(String employeeId, boolean softDelete);

    EmployeeListResponseDto listarEmpleados(String companyId, String estado);

    EmployeeResponseDto obtenerEmpleado(String employeeId);
}
