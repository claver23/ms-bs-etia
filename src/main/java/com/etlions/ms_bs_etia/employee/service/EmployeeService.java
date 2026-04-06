package com.etlions.ms_bs_etia.employee.service;

import com.etlions.ms_bs_etia.employee.persistence.EmployeeEntity;

import java.util.List;

public interface EmployeeService {

    EmployeeEntity registrarEmpleado(EmployeeEntity entity);

    EmployeeEntity actualizarEmpleado(String employeeId, EmployeeEntity entity);

    void eliminarEmpleado(String employeeId, boolean softDelete);

    List<EmployeeEntity> listarEmpleados(String companyId);

    List<EmployeeEntity> listarEmpleadosActivos(String companyId);

    EmployeeEntity obtenerEmpleado(String employeeId);
}

