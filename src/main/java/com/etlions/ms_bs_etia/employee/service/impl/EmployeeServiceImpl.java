package com.etlions.ms_bs_etia.employee.service.impl;

import com.etlions.ms_bs_etia.app_config.exception.ResourceNotFoundException;
import com.etlions.ms_bs_etia.employee.mapper.EmployeeMapper;
import com.etlions.ms_bs_etia.employee.persistence.EmployeeEntity;
import com.etlions.ms_bs_etia.employee.persistence.EmployeeRepository;
import com.etlions.ms_bs_etia.employee.service.EmployeeService;
import com.etlions.openapicodegen.dto.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;

@Service
@RequiredArgsConstructor
public class EmployeeServiceImpl implements EmployeeService {

    private final EmployeeRepository employeeRepository;
    private final EmployeeMapper employeeMapper;

    @Override
    public EmployeeResponseDto registrarEmpleado(EmployeeCreateDto request) {
        EmployeeEntity entity = employeeMapper.toEntity(request);
        entity.setEstado("ACTIVO");
        entity.setFechaCreacion(LocalDateTime.now());
        EmployeeEntity saved = employeeRepository.save(entity);
        EmployeeData data = employeeMapper.toData(saved);
        return new EmployeeResponseDto()
                .isSuccess(true).status("OK")
                .message("Empleado registrado exitosamente")
                .timestamp(new Date()).data(data);
    }

    @Override
    public EmployeeResponseDto actualizarEmpleado(String employeeId, EmployeeUpdateDto request) {
        EmployeeEntity entity = employeeRepository.findById(employeeId)
                .orElseThrow(() -> new ResourceNotFoundException("Empleado", employeeId));
        employeeMapper.updateEntity(request, entity);
        EmployeeEntity saved = employeeRepository.save(entity);
        EmployeeData data = employeeMapper.toData(saved);
        return new EmployeeResponseDto()
                .isSuccess(true).status("OK")
                .message("Empleado actualizado exitosamente")
                .timestamp(new Date()).data(data);
    }

    @Override
    public SuccessResponse eliminarEmpleado(String employeeId, boolean softDelete) {
        if (softDelete) {
            EmployeeEntity entity = employeeRepository.findById(employeeId)
                    .orElseThrow(() -> new ResourceNotFoundException("Empleado", employeeId));
            entity.setEstado("INACTIVO");
            employeeRepository.save(entity);
        } else {
            employeeRepository.deleteById(employeeId);
        }
        return new SuccessResponse()
                .isSuccess(true).status("OK")
                .message("Empleado eliminado exitosamente")
                .timestamp(new Date());
    }

    @Override
    public EmployeeListResponseDto listarEmpleados(String companyId, String estado) {
        List<EmployeeEntity> employees;
        if ("ACTIVO".equalsIgnoreCase(estado)) {
            employees = employeeRepository.findByCompanyIdAndEstado(companyId, "ACTIVO");
        } else {
            employees = employeeRepository.findByCompanyId(companyId);
        }
        List<EmployeeData> dataList = employees.stream().map(employeeMapper::toData).toList();
        return new EmployeeListResponseDto()
                .isSuccess(true).status("OK")
                .message("Operación exitosa")
                .timestamp(new Date()).data(dataList);
    }

    @Override
    public EmployeeResponseDto obtenerEmpleado(String employeeId) {
        EmployeeEntity entity = employeeRepository.findById(employeeId)
                .orElseThrow(() -> new ResourceNotFoundException("Empleado", employeeId));
        EmployeeData data = employeeMapper.toData(entity);
        return new EmployeeResponseDto()
                .isSuccess(true).status("OK")
                .message("Operación exitosa")
                .timestamp(new Date()).data(data);
    }
}
