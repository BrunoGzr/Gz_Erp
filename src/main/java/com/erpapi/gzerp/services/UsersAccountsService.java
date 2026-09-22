package com.erpapi.gzerp.services;


import com.erpapi.gzerp.dto.PartnersRegisterDto;
import com.erpapi.gzerp.enums.UserType;
import com.erpapi.gzerp.models.Roles;
import com.erpapi.gzerp.models.Tenants;
import com.erpapi.gzerp.models.UsersAccounts;
import com.erpapi.gzerp.repositories.EmployeesRepo;
import com.erpapi.gzerp.repositories.PartnersRepo;
import com.erpapi.gzerp.repositories.RolesRepo;
import com.erpapi.gzerp.repositories.UsersAccountsRepo;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class UsersAccountsService {

    private final UsersAccountsRepo usersRepo;
    private final PasswordEncoder passwordEncoder;
    private final RolesRepo rolesRepo;

    public UsersAccountsService(UsersAccountsRepo usersRepo, EmployeesRepo employeesRepo, RolesRepo rolesRepo, PasswordEncoder passwordEncoder) {
        this.usersRepo = usersRepo;
        this.passwordEncoder = passwordEncoder;
        this.rolesRepo = rolesRepo;
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
        Roles role = rolesRepo.findByNameAndIsSystemTrue("DefaultRolePartners").orElseThrow();
        newUser.setRole(role);
        newUser.setPassword(passwordEncoder.encode(dto.getPassword()));
        newUser.setTenant(tenant);
        String usernameBefore = newUser.getUsername();
        System.out.println(">>> username antes do save: [" + usernameBefore + "]");
        System.out.println(">>> tamanho antes do save: " + usernameBefore.length());
        System.out.println(">>> chars antes do save: " + java.util.Arrays.toString(usernameBefore.toCharArray()));
        newUser = usersRepo.save(newUser);
        String usernameAfter = newUser.getUsername();
        System.out.println(">>> username depois do save: [" + usernameAfter + "]");
        System.out.println(">>> tamanho depois do save: " + usernameAfter.length());
        System.out.println(">>> chars depois do save: " + java.util.Arrays.toString(usernameAfter.toCharArray()));
        return newUser;
    }
}
