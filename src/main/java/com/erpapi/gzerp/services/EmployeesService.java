package com.erpapi.gzerp.services;

import com.erpapi.gzerp.exceptions.UserAlreadyExistException;
import com.erpapi.gzerp.dto.EmployeeRegisterDto;
import com.erpapi.gzerp.dto.EmployeeResponseDto;
import com.erpapi.gzerp.models.Employees;
import com.erpapi.gzerp.repositories.EmployeesRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@Transactional
public class EmployeesService {

    @Autowired
    private EmployeesRepo employeesRepo;

    @Autowired
    private PasswordEncoder passwordEncoder;

    
    public EmployeeResponseDto RegisterUserWithTenantCreated(EmployeeRegisterDto employeeRegisterDto) throws UserAlreadyExistException {
        List<String> conflictedFields = new ArrayList<String>();
        if (employeesRepo.existsByEmail(employeeRegisterDto.getEmail(), employeeRegisterDto.getTenantId())) {
            conflictedFields.add("User with this email already exists");}
        if (employeesRepo.existsByUserName(employeeRegisterDto.getUserName(), employeeRegisterDto.getTenantId())) {
            conflictedFields.add("User with this username already exists");}
        if (!conflictedFields.isEmpty()) {
            throw new UserAlreadyExistException(conflictedFields);}

        Employees newEmployee = new Employees();
        newEmployee.setEmail(employeeRegisterDto.getEmail());
        newEmployee.setTenantId(employeeRegisterDto.getTenantId());
        newEmployee.setUserName(employeeRegisterDto.getUserName());
        newEmployee.setPassword(passwordEncoder.encode(employeeRegisterDto.getPassword()));
        newEmployee.setName(employeeRegisterDto.getName());
        newEmployee.setSalary(employeeRegisterDto.getSalary());
        newEmployee.setAdmin(false);
        Employees savedUser = employeesRepo.save(newEmployee);
        return new EmployeeResponseDto(savedUser);
    }
}
