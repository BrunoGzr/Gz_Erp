package com.erpapi.gzerp.repositories;

import com.erpapi.gzerp.models.Employees;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EmployeesRepo extends JpaRepository<Employees, Long> {

}
