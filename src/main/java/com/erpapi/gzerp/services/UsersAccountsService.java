package com.erpapi.gzerp.services;


import com.erpapi.gzerp.dto.EmployeeRegisterDto;
import com.erpapi.gzerp.dto.UserAccountRegisterDto;
import com.erpapi.gzerp.dto.UserAccountResponseDto;
import com.erpapi.gzerp.enums.UserType;
import com.erpapi.gzerp.models.Employees;
import com.erpapi.gzerp.models.UsersAccounts;
import com.erpapi.gzerp.repositories.EmployeesRepo;
import com.erpapi.gzerp.repositories.PartnersRepo;
import com.erpapi.gzerp.repositories.UsersAccountRepo;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class UsersAccountsService {

    private final UsersAccountRepo usersRepo;
    private final EmployeesRepo employeesRepo;
    private final PartnersRepo partnersRepo;

    public UsersAccountsService(UsersAccountRepo usersRepo, EmployeesRepo employeesRepo, PartnersRepo partnersRepo) {
        this.usersRepo = usersRepo;
        this.employeesRepo = employeesRepo;
        this.partnersRepo = partnersRepo;
    }

    public UserAccountResponseDto UserRegisterEmployee(UserAccountRegisterDto UserRegisterDto) {

        UsersAccounts savedUser = new UsersAccounts(UserRegisterDto);
        savedUser.setUserType(UserType.EMPLOYEE);
        savedUser.setTenantId(UserRegisterDto.getTenantId());
        savedUser = usersRepo.save(savedUser);

        EmployeeRegisterDto newEmployee = new EmployeeRegisterDto(UserRegisterDto);
        Employees savedEmployee = new Employees(newEmployee);
        savedEmployee.setUsersAccounts(savedUser);
        employeesRepo.save(savedEmployee);

        UserAccountResponseDto responseDto = new UserAccountResponseDto(savedUser);
        return responseDto;
    }

    public UserAccountResponseDto UserRegisterPartner(UserAccountRegisterDto userDto){
        UsersAccounts savedUser = new UsersAccounts(userDto);
        savedUser.setUserType(UserType.PARTNER);
    }
}
