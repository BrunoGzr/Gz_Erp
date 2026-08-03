package com.erpapi.gzerp.services;

import com.erpapi.gzerp.exceptions.InvalidCredentialsException;
import com.erpapi.gzerp.config.SecurityUser;
import com.erpapi.gzerp.dto.LoginRequestDto;
import com.erpapi.gzerp.dto.LoginResponseDto;
import com.erpapi.gzerp.models.Employees;
import com.erpapi.gzerp.repositories.EmployeesRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class AuthService {

    @Autowired
    private EmployeesRepo employeesRepo;

    @Autowired
    private JwtService jwtService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    public LoginResponseDto authenticate(LoginRequestDto loginDto) throws InvalidCredentialsException {
        Employees employees = employeesRepo.findByEmail(loginDto.getEmail(), loginDto.getTenantId());

        if (employees == null) {
            throw new InvalidCredentialsException("Invalid email, password or tenantId");
        }

        if (!passwordEncoder.matches(loginDto.getPassword(), employees.getPassword())) {
            throw new InvalidCredentialsException("Invalid email, password or tenantId");
        }

        SecurityUser securityUser = new SecurityUser(employees);
        String token = jwtService.generateToken(securityUser);

        return new LoginResponseDto(token, employees.getEmail(), employees.getTenantId(), employees.isAdmin());
    }
}
