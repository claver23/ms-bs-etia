package com.etlions.ms_bs_etia.employee.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface EmployeeRepository extends JpaRepository<EmployeeEntity, String> {
    List<EmployeeEntity> findByCompanyId(String companyId);
    List<EmployeeEntity> findByCompanyIdAndEstado(String companyId, String estado);
}

