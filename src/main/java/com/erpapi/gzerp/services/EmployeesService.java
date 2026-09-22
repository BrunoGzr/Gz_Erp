package com.erpapi.gzerp.services;

import com.erpapi.gzerp.exceptions.EmployeeAlreadyExistException;
import com.erpapi.gzerp.dto.EmployeeRegisterDto;
import com.erpapi.gzerp.dto.EmployeeResponseDto;
import com.erpapi.gzerp.models.Employees;
import com.erpapi.gzerp.repositories.EmployeesRepo;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@Transactional
public class EmployeesService {


    private EmployeesRepo employeesRepo;
    private AuthService authService;
    private PasswordEncoder passwordEncoder;

    public EmployeesService(EmployeesRepo employeesRepo, AuthService authService, PasswordEncoder passwordEncoder) {
        this.employeesRepo = employeesRepo;
        this.authService = authService;
        this.passwordEncoder = passwordEncoder;
    }

//    public EmployeeResponseDto RegisterEmployeeWithTenantCreated(EmployeeRegisterDto employeeRegisterDto) throws EmployeeAlreadyExistException {
//        List<String> conflictedFields = new ArrayList<String>();
//        if (employeesRepo.existsByEmail(employeeRegisterDto.getEmail(), employeeRegisterDto.getTenantId())) {
//            conflictedFields.add("User with this email already exists");}
//        if (employeesRepo.existsByUserName(employeeRegisterDto.getUserName(), employeeRegisterDto.getTenantId())) {
//            conflictedFields.add("User with this username already exists");}
//        if (!conflictedFields.isEmpty()) {
//            throw new EmployeeAlreadyExistException(conflictedFields);}
//
//        Employees newEmployee = new Employees();
//        authService.cpfValid(employeeRegisterDto.getCpf());
//        newEmployee.setCpf(authService.cpfFormater(employeeRegisterDto.getCpf()));
//        newEmployee.setTenantId(employeeRegisterDto.getTenantId());
//        newEmployee.setFullName(employeeRegisterDto.getFullName());
//        newEmployee.setSalary(employeeRegisterDto.getSalary());
//        Employees savedUser = employeesRepo.save(newEmployee);
//        return new EmployeeResponseDto(savedUser);
//    }
    
    
}
