package com.erpapi.gzerp.services;


import com.erpapi.gzerp.dto.PartnersRegisterDto;
import com.erpapi.gzerp.enums.UserType;
import com.erpapi.gzerp.models.Tenants;
import com.erpapi.gzerp.models.UsersAccounts;
import com.erpapi.gzerp.repositories.EmployeesRepo;
import com.erpapi.gzerp.repositories.PartnersRepo;
import com.erpapi.gzerp.repositories.UsersAccountsRepo;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class UsersAccountsService {

    private final UsersAccountsRepo usersRepo;
    private final PasswordEncoder passwordEncoder;

    public UsersAccountsService(UsersAccountsRepo usersRepo, EmployeesRepo employeesRepo, PartnersRepo partnersRepo, PasswordEncoder passwordEncoder) {
        this.usersRepo = usersRepo;
        this.passwordEncoder = passwordEncoder;
    }

//    public UsersAccounts UserRegisterEmployee(EmployeeRegisterDto dto) {
//        UsersAccounts savedUser = new UsersAccounts();
//
//
//
//        savedUser.setUserType(UserType.EMPLOYEE);
//        savedUser.setTenantId(UserRegisterDto.getTenantId());
//        savedUser.setPassword(passwordEncoder.encode(UserRegisterDto.getPassword()));
//        savedUser = usersRepo.save(savedUser);
//        return savedUser;
//    }

    @Transactional
    public UsersAccounts userRegisterPartner(PartnersRegisterDto dto, Tenants tenant){
        UsersAccounts newUser = new UsersAccounts();
        newUser.setUserType(UserType.PARTNER);
        newUser.setEmail(dto.getEmail());
        newUser.setUsername(dto.getUsername());
        newUser.setPassword(passwordEncoder.encode(dto.getPassword()));
        newUser.setTenant(tenant);
        newUser = usersRepo.save(newUser);
        return newUser;
    }
}
