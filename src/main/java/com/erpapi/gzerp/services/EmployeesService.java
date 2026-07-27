package com.erpapi.gzerp.services;

import com.erpapi.gzerp.Exceptions.UserAlreadyExistException;
import com.erpapi.gzerp.dto.EmployeeRegisterDto;
import com.erpapi.gzerp.dto.EmployeeResponseDto;
import com.erpapi.gzerp.models.Employees;
import com.erpapi.gzerp.repositories.EmployeesRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@Transactional
public class EmployeesService {

    @Autowired
    private EmployeesRepo employeesRepo;

//    public ResponseEntity<?> UpdateUsers(Users userToUpdated, Long id) {
//
//    }

    public EmployeeResponseDto RegisterUserWithTenantCreated(EmployeeRegisterDto userDto) throws UserAlreadyExistException {
        List<String> conflictedFields = new ArrayList<String>();
        if (employeesRepo.existsByEmail(userDto.getEmail(), userDto.getTenantId())) {
            conflictedFields.add("User with this email already exists");}
        if (employeesRepo.existsByUserName(userDto.getUserName(), userDto.getTenantId())) {
            conflictedFields.add("User with this username already exists");}
        if (!conflictedFields.isEmpty()) {
            throw new UserAlreadyExistException(conflictedFields);}

        Employees newUser = new Employees();
        newUser.setEmail(userDto.getEmail());
        newUser.setTenantId(userDto.getTenantId());
        newUser.setUserName(userDto.getUserName());
        newUser.setPassword(userDto.getPassword());
        newUser.setName(userDto.getName());
        newUser.setAdmin(false);

        Employees savedUser = employeesRepo.save(newUser);
        return new EmployeeResponseDto(savedUser);
    }
}
