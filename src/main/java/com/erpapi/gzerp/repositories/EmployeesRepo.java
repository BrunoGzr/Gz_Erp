package com.erpapi.gzerp.repositories;

import com.erpapi.gzerp.models.Employees;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EmployeesRepo extends JpaRepository<Employees, Long> {

    boolean existsByEmail(String email, Long tenantId);
    Employees findByEmail(String email, Long tenantId);

    boolean existsByUserName(String userName, Long tenantId);
    Employees findByUserName(String userName, Long tenantId);
}
