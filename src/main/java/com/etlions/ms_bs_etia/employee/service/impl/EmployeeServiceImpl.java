package com.etlions.ms_bs_etia.employee.service.impl;

import com.etlions.ms_bs_etia.app_config.exception.ResourceNotFoundException;
import com.etlions.ms_bs_etia.employee.persistence.EmployeeEntity;
import com.etlions.ms_bs_etia.employee.persistence.EmployeeRepository;
import com.etlions.ms_bs_etia.employee.service.EmployeeService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class EmployeeServiceImpl implements EmployeeService {

    private final EmployeeRepository employeeRepository;

    @Override
    public EmployeeEntity registrarEmpleado(EmployeeEntity entity) {
        entity.setEstado("ACTIVO");
        entity.setFechaCreacion(LocalDateTime.now());
        return employeeRepository.save(entity);
    }

    @Override
    public EmployeeEntity actualizarEmpleado(String employeeId, EmployeeEntity updated) {
        EmployeeEntity entity = employeeRepository.findById(employeeId)
                .orElseThrow(() -> new ResourceNotFoundException("Empleado no encontrado: " + employeeId));

        if (updated.getNombre() != null) entity.setNombre(updated.getNombre());
        if (updated.getCargo() != null) entity.setCargo(updated.getCargo());
        if (updated.getDni() != null) entity.setDni(updated.getDni());
        if (updated.getCodigo() != null) entity.setCodigo(updated.getCodigo());

        return employeeRepository.save(entity);
    }

    @Override
    public void eliminarEmpleado(String employeeId, boolean softDelete) {
        if (softDelete) {
            EmployeeEntity entity = employeeRepository.findById(employeeId)
                    .orElseThrow(() -> new ResourceNotFoundException("Empleado no encontrado: " + employeeId));
            entity.setEstado("INACTIVO");
            employeeRepository.save(entity);
        } else {
            employeeRepository.deleteById(employeeId);
        }
    }

    @Override
    public List<EmployeeEntity> listarEmpleados(String companyId) {
        return employeeRepository.findByCompanyId(companyId);
    }

    @Override
    public List<EmployeeEntity> listarEmpleadosActivos(String companyId) {
        return employeeRepository.findByCompanyIdAndEstado(companyId, "ACTIVO");
    }

    @Override
    public EmployeeEntity obtenerEmpleado(String employeeId) {
        return employeeRepository.findById(employeeId)
                .orElseThrow(() -> new ResourceNotFoundException("Empleado no encontrado: " + employeeId));
    }
}

